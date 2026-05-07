package com.apuapa.webview

import android.app.AlertDialog
import android.content.ContentValues
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.json.JSONArray
import java.io.File
import java.io.FileInputStream
import java.io.InputStream

data class Plantilla(
    val nombre: String,
    val descripcion: String,
    val archivo: String,
)

class PlantillasActivity : AppCompatActivity() {

    private val plantillas = mutableListOf<Plantilla>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_plantillas)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Plantillas APA 7 (offline)"
        toolbar.setNavigationOnClickListener { finish() }

        cargarPlantillas()

        val rv = findViewById<RecyclerView>(R.id.recycler)
        rv.layoutManager = LinearLayoutManager(this)
        rv.adapter = PlantillasAdapter(plantillas) { p -> descargar(p) }
    }

    private fun cargarPlantillas() {
        try {
            val text = assets.open("plantillas/indice.json")
                .bufferedReader().use { it.readText() }
            val arr = JSONArray(text)
            for (i in 0 until arr.length()) {
                val o = arr.getJSONObject(i)
                plantillas.add(
                    Plantilla(
                        nombre = o.getString("nombre"),
                        descripcion = o.getString("descripcion"),
                        archivo = o.getString("archivo"),
                    )
                )
            }
        } catch (e: Exception) {
            Toast.makeText(
                this, "Error cargando plantillas: ${e.message}",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun descargar(p: Plantilla) {
        try {
            val bytes = assets.open("plantillas/${p.archivo}")
                .use { it.readBytes() }
            val ruta = guardar(p.archivo, bytes)
            AlertDialog.Builder(this)
                .setTitle("Plantilla descargada")
                .setMessage(
                    "'${p.nombre}' se guardó en:\n\n$ruta\n\n" +
                    "Ábrela desde tu app de Archivos → Descargas → APU-APA"
                )
                .setPositiveButton("Aceptar", null)
                .show()
        } catch (e: Exception) {
            Toast.makeText(
                this, "Error al descargar: ${e.message}",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun guardar(nombre: String, bytes: ByteArray): String {
        // Android 10+ — MediaStore (carpeta pública sin permisos)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val resolver = contentResolver
            val cv = ContentValues().apply {
                put(MediaStore.Downloads.DISPLAY_NAME, nombre)
                put(
                    MediaStore.Downloads.MIME_TYPE,
                    "application/vnd.openxmlformats-officedocument." +
                            "wordprocessingml.document"
                )
                put(
                    MediaStore.Downloads.RELATIVE_PATH,
                    Environment.DIRECTORY_DOWNLOADS + "/APU-APA"
                )
            }
            val uri = resolver.insert(
                MediaStore.Downloads.EXTERNAL_CONTENT_URI, cv
            ) ?: throw Exception("MediaStore rechazó el archivo")
            resolver.openOutputStream(uri)?.use { it.write(bytes) }
            return "Descargas/APU-APA/$nombre"
        }
        // Android < 10 — fallback a almacenamiento privado
        val dir = File(
            getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS),
            "APU-APA"
        )
        if (!dir.exists()) dir.mkdirs()
        val out = File(dir, nombre)
        out.writeBytes(bytes)
        return out.absolutePath
    }
}

class PlantillasAdapter(
    private val items: List<Plantilla>,
    private val onClick: (Plantilla) -> Unit,
) : RecyclerView.Adapter<PlantillasAdapter.VH>() {

    class VH(v: View) : RecyclerView.ViewHolder(v) {
        val nombre: TextView = v.findViewById(R.id.txt_nombre)
        val desc: TextView = v.findViewById(R.id.txt_descripcion)
        val btn: ImageButton = v.findViewById(R.id.btn_descargar)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_plantilla, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val p = items[position]
        holder.nombre.text = p.nombre
        holder.desc.text = p.descripcion
        holder.itemView.setOnClickListener { onClick(p) }
        holder.btn.setOnClickListener { onClick(p) }
    }

    override fun getItemCount() = items.size
}
