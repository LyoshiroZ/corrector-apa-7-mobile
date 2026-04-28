[app]
title = Corrector APA 7
package.name = correctorapa7
package.domain = com.correctorapa

source.dir = .
source.include_exts = py,png,jpg,kv,atlas,ttf,json
source.include_patterns = assets/*
source.exclude_dirs = tests, bin, .buildozer, __pycache__

version = 1.0.0

# Dependencias Python (recipes de python-for-android).
# python-docx requiere lxml; google-genai NO se incluye porque la app móvil
# llama a Gemini directamente por REST con `requests`.
requirements = python3,kivy==2.3.0,kivymd==1.2.0,pillow,materialyoucolor,exceptiongroup,asyncgui,asynckivy,requests,certifi,charset-normalizer,idna,urllib3,python-docx,lxml,plyer,android,pyjnius

orientation = portrait
fullscreen = 0

# Permisos Android
android.permissions = INTERNET, READ_EXTERNAL_STORAGE, WRITE_EXTERNAL_STORAGE

# Targets de Android: API 33 (Play Store), mínimo 21 (Android 5.0)
android.api = 33
android.minapi = 21
android.ndk = 25b
android.archs = arm64-v8a, armeabi-v7a

# Para subir a Google Play se necesita un AAB firmado.
# Comenta la siguiente línea si solo quieres un APK de prueba.
android.release_artifact = aab

android.allow_backup = True

# Icono y splash (deja los predeterminados o coloca tus PNG en assets/)
# icon.filename = %(source.dir)s/assets/icon.png
# presplash.filename = %(source.dir)s/assets/presplash.png

# Logging
log_level = 2
warn_on_root = 1

[buildozer]
log_level = 2
warn_on_root = 1
