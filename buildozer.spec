[app]
title = Corrector APA 7
package.name = correctorapa7
package.domain = com.correctorapa

source.dir = .
source.include_exts = py,png,jpg,kv,atlas,ttf,json
source.include_patterns = assets/*
source.exclude_dirs = tests, bin, .buildozer, __pycache__, .github

version = 1.0.0

# Dependencias Python (recipes de python-for-android).
# android y pyjnius son necesarios para que plyer funcione en Android.
# python-docx requiere lxml; google-genai NO se incluye porque la app
# llama a Gemini directamente por REST con requests.
requirements = python3,kivy==2.3.0,kivymd==1.2.0,pillow,requests,python-docx,lxml,plyer,android,pyjnius

orientation = portrait
fullscreen = 0

android.permissions = INTERNET, READ_EXTERNAL_STORAGE, WRITE_EXTERNAL_STORAGE

android.api = 33
android.minapi = 21
android.ndk = 26b
android.ndk_api = 21
android.archs = arm64-v8a

android.allow_backup = True

# Nota: android.release_artifact solo aplica a builds de RELEASE.
# El workflow de CI usa "buildozer android debug" que siempre produce un APK.
# Cuando quieras subir a Play Store, ejecuta "buildozer android release" localmente.
android.release_artifact = aab

log_level = 2
warn_on_root = 1

[buildozer]
log_level = 2
warn_on_root = 1
