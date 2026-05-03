[app]
title = APU-APA
package.name = apuapa
package.domain = com.apuapa

source.dir = .
source.include_exts = py,png,jpg,jpeg,kv,atlas,ttf,json
source.include_patterns = assets/*
source.exclude_dirs = tests, bin, .buildozer, __pycache__, .github

version = 1.0.13

# Icono de la app (mostrado en el cajón de aplicaciones)
icon.filename = %(source.dir)s/assets/logo.png

# Pantalla de carga (mientras Python arranca)
presplash.filename = %(source.dir)s/assets/presplash.png
android.presplash_color = #FAF6EC

# Dependencias Python (recipes de python-for-android).
# lxml 5.2.2 es compatible con Python 3.11 (la <=4.9 da error 'incomplete struct _frame').
requirements = python3,kivy==2.3.0,kivymd==1.2.0,pillow,requests,python-docx,lxml==5.2.2,typing_extensions,plyer,android,pyjnius

orientation = portrait
fullscreen = 0

android.permissions = INTERNET, READ_EXTERNAL_STORAGE, WRITE_EXTERNAL_STORAGE

android.api = 33
android.minapi = 21
android.ndk = 25b
android.ndk_api = 21
# Fijamos build-tools 34 para evitar el problema de licencia de build-tools 37
android.build_tools_version = 34.0.0
android.archs = arm64-v8a

# Forzamos python-for-android desde la rama master de GitHub.
# La master tiene los parches para Python 3.11 + NDK 25 (setgrent, _lzma, grp module)
# que NO estan en la version 2024.1.21 que viene en pip.
p4a.fork = kivy
p4a.branch = master

android.allow_backup = True
android.release_artifact = apk

log_level = 2
warn_on_root = 1

[buildozer]
log_level = 2
warn_on_root = 1
