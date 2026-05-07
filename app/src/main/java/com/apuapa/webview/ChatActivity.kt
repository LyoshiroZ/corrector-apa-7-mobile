package com.apuapa.webview

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.Executors

data class Mensaje(val rol: String, val texto: String) // "user" | "model"

class ChatActivity : AppCompatActivity() {

    companion object {
        // Endpoint Gemini en el servidor (la API key NO está aquí)
        private const val CHAT_URL =
            "https://apa-formatter--yoshiror88.replit.app/api/chat"
        private const val PREFS = "apuapa_chat"
        private const val KEY_HISTORY = "history_json"
        private const val MAX_HISTORY = 20
    }

    private val mensajes = mutableListOf<Mensaje>()
    private lateinit var adapter: ChatAdapter
    private lateinit var rv: RecyclerView
    private lateinit var input: EditText
    private lateinit var btnSend: ImageButton
    private lateinit var progress: ProgressBar
    private val ejecutor = Executors.newSingleThreadExecutor()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.title = "Chat IA · APU-APA"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { finish() }

        rv = findViewById(R.id.recycler_chat)
        input = findViewById(R.id.input_message)
        btnSend = findViewById(R.id.btn_send)
        progress = findViewById(R.id.progress_chat)

        adapter = ChatAdapter(mensajes)
        val lm = LinearLayoutManager(this)
        lm.stackFromEnd = true
        rv.layoutManager = lm
        rv.adapter = adapter

        cargarHistorial()
        if (mensajes.isEmpty()) {
            mensajes.add(Mensaje("model",
                "¡Hola! Soy APU-APA, tu asistente académico. " +
                "Pregúntame sobre normas APA 7, ortografía, " +
                "redacción de tesis, ensayos o cualquier duda " +
                "académica. ¿En qué te puedo ayudar?"))
            adapter.notifyDataSetChanged()
        }
        rv.scrollToPosition(mensajes.size - 1)

        btnSend.isEnabled = false
        input.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, a: Int,
                                           b: Int, c: Int) {}
            override fun onTextChanged(s: CharSequence?, a: Int,
                                       b: Int, c: Int) {
                btnSend.isEnabled = !s.isNullOrBlank()
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        btnSend.setOnClickListener { enviar() }

        findViewById<View>(R.id.btn_clear).setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Borrar conversación")
                .setMessage("¿Quieres empezar una nueva conversación? " +
                            "Se borrará el historial actual.")
                .setPositiveButton("Sí, borrar") { _, _ ->
                    mensajes.clear()
                    mensajes.add(Mensaje("model",
                        "Conversación nueva. ¿En qué te ayudo?"))
                    adapter.notifyDataSetChanged()
                    guardarHistorial()
                }
                .setNegativeButton("Cancelar", null)
                .show()
        }
    }

    private fun enviar() {
        val texto = input.text.toString().trim()
        if (texto.isEmpty()) return
        if (texto.length > 4000) {
            Toast.makeText(this,
                "Mensaje muy largo (máx. 4000 caracteres)",
                Toast.LENGTH_SHORT).show()
            return
        }

        mensajes.add(Mensaje("user", texto))
        adapter.notifyItemInserted(mensajes.size - 1)
        rv.scrollToPosition(mensajes.size - 1)
        input.setText("")
        progress.visibility = View.VISIBLE
        btnSend.isEnabled = false

        // Historial sin el mensaje recién añadido
        val historial = mensajes.dropLast(1).takeLast(12)

        ejecutor.submit {
            val (ok, respuesta) = llamarApi(texto, historial)
            runOnUiThread {
                progress.visibility = View.GONE
                btnSend.isEnabled = !input.text.isNullOrBlank()
                mensajes.add(Mensaje(
                    if (ok) "model" else "error",
                    respuesta))
                adapter.notifyItemInserted(mensajes.size - 1)
                rv.scrollToPosition(mensajes.size - 1)
                guardarHistorial()
            }
        }
    }

    private fun llamarApi(
        mensaje: String, historial: List<Mensaje>
    ): Pair<Boolean, String> {
        return try {
            val url = URL(CHAT_URL)
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "POST"
            conn.connectTimeout = 15_000
            conn.readTimeout = 60_000
            conn.doOutput = true
            conn.setRequestProperty("Content-Type",
                "application/json; charset=utf-8")
            conn.setRequestProperty("Accept", "application/json")

            val histArr = JSONArray()
            for (m in historial) {
                if (m.rol != "user" && m.rol != "model") continue
                histArr.put(JSONObject()
                    .put("role", m.rol)
                    .put("text", m.texto))
            }
            val body = JSONObject()
                .put("message", mensaje)
                .put("history", histArr)
                .toString()

            OutputStreamWriter(conn.outputStream, Charsets.UTF_8)
                .use { it.write(body) }

            val code = conn.responseCode
            val stream = if (code in 200..299) conn.inputStream
                         else conn.errorStream
            val txt = BufferedReader(
                InputStreamReader(stream, Charsets.UTF_8)
            ).use { it.readText() }

            val json = try { JSONObject(txt) } catch (_: Exception) {
                JSONObject()
            }
            if (code in 200..299) {
                Pair(true, json.optString("reply",
                    "(respuesta vacía)"))
            } else {
                Pair(false, "⚠ " + json.optString("error",
                    "Error del servidor (HTTP $code)."))
            }
        } catch (e: Exception) {
            Pair(false,
                "⚠ Sin conexión o el servidor no responde. " +
                "Revisa tu internet e inténtalo otra vez.\n\n" +
                "(${e.javaClass.simpleName})")
        }
    }

    private fun guardarHistorial() {
        try {
            val arr = JSONArray()
            for (m in mensajes.takeLast(MAX_HISTORY)) {
                arr.put(JSONObject()
                    .put("role", m.rol)
                    .put("text", m.texto))
            }
            getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                .edit()
                .putString(KEY_HISTORY, arr.toString())
                .apply()
        } catch (_: Exception) {}
    }

    private fun cargarHistorial() {
        try {
            val raw = getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                .getString(KEY_HISTORY, null) ?: return
            val arr = JSONArray(raw)
            for (i in 0 until arr.length()) {
                val o = arr.getJSONObject(i)
                mensajes.add(Mensaje(
                    rol = o.getString("role"),
                    texto = o.getString("text")))
            }
        } catch (_: Exception) {}
    }

    override fun onDestroy() {
        ejecutor.shutdownNow()
        super.onDestroy()
    }
}

class ChatAdapter(private val items: List<Mensaje>) :
    RecyclerView.Adapter<ChatAdapter.VH>() {

    companion object {
        const val TIPO_USER = 1
        const val TIPO_MODEL = 2
        const val TIPO_ERROR = 3
    }

    override fun getItemViewType(position: Int): Int = when (items[position].rol) {
        "user" -> TIPO_USER
        "error" -> TIPO_ERROR
        else -> TIPO_MODEL
    }

    class VH(v: View) : RecyclerView.ViewHolder(v) {
        val txt: TextView = v.findViewById(R.id.txt_mensaje)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val layout = when (viewType) {
            TIPO_USER -> R.layout.item_msg_user
            TIPO_ERROR -> R.layout.item_msg_error
            else -> R.layout.item_msg_model
        }
        val v = LayoutInflater.from(parent.context)
            .inflate(layout, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.txt.text = items[position].texto
    }

    override fun getItemCount() = items.size
}
