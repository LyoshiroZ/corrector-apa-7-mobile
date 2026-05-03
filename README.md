# APU-APA — App Android (WebView)

App Android nativa que envuelve la web de **APU-APA** en un WebView,
igual que hacen Supremacy 1914, Twitter Lite, etc.

## Cómo funciona

- La app abre `https://apa-formatter--yoshiror88.replit.app/` en un WebView.
- El usuario ve **exactamente** la web (idéntica al 100%).
- Cuando actualizas la web, la app se actualiza sola (no hay que recompilar).
- Subida de archivos Word: usa el explorador nativo de Android.
- Descargas: usa el `DownloadManager` de Android (notificación + carpeta Descargas).
- Pull-to-refresh para recargar.
- Pantalla de error si no hay internet.

## Cómo publicar

1. **Crea un nuevo repositorio en GitHub** llamado por ejemplo
   `apuapa-android-webview` (o reemplaza el contenido de tu repo actual).
2. **Sube el contenido de la carpeta `mobile-webview/`** a la raíz del repo.
3. GitHub Actions compilará el APK automáticamente.
4. Descarga el APK desde Actions → último workflow → Artifacts.

## Compilación local (opcional)

```bash
cd mobile-webview
gradle wrapper --gradle-version 8.5
./gradlew assembleDebug
# APK queda en: app/build/outputs/apk/debug/app-debug.apk
```

## Cambiar la URL

Edita `app/src/main/java/com/apuapa/webview/MainActivity.kt`,
línea con `APP_URL`.
