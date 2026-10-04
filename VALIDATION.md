# Estado de validación — 3 de octubre de 2026

## Revisado y corregido

Todos los archivos del proyecto original fueron inspeccionados. Se conserva Java 17, compileSdk 35, minSdk 29, targetSdk 32, CameraX ImageAnalysis sin Preview y ML Kit con modelo incluido. No se implementó V2.

- Habilitado AndroidX y añadido Gradle Wrapper 8.9 con checksum de distribución.
- Eliminado `android:keepScreenOn` del Manifest: la Activity ya aplica FLAG_KEEP_SCREEN_ON.
- CameraX fijado a 1.4.2: el AAR de 1.6.2 exige compileSdk 36 y AGP 8.9.1.
- Eliminado String.isBlank() para evitar depender de esa API de Java en Android 29.
- Traducciones por coincidencia exacta; `Candle` no se confunde con `Can`.
- Filtro con tres coincidencias consecutivas, resultado caducable y limpieza al pausar.
- Frames cerrados al finalizar la inferencia o ante errores; cierre del clasificador coordinado con tareas en curso.
- Fondo negro e interfaz inmersiva, sin preview de cámara.
- Workflow para main/manual/PR, Java 17, SDK 35, Wrapper, pruebas, lint y artifact RokidProductRecognizer-V1-APK.

## Resultado real de las comprobaciones

- Resolución de dependencias, checkDebugAarMetadata, recursos y procesamiento del Manifest: completados localmente.
- javac generó las clases Android, pero la tarea terminó con AccessDeniedException al resolver/cerrar JAR del SDK/caché en el sandbox Windows. Esto NO equivale a una compilación Gradle exitosa.
- Las tres pruebas JUnit del filtro y traductor se ejecutaron por separado: OK (3 tests). Su compilación también reportó el problema del sandbox al cerrar los JAR; las clases generadas pudieron ejecutarse.
- assembleDebug y lintDebug no completados; APK NO validado ni publicado.
- GitHub: repositorio vacío confirmado. Lectura disponible; publicación rechazada con HTTP 403 Resource not accessible by integration tanto en Contents API como Git Blobs API.
- Commit guardado localmente. Push y GitHub Actions pendientes de habilitar escritura de contenido/workflows en la conexión o disponer de Git autenticado.

## Paso pendiente

Con acceso habilitado, publicar main, ejecutar y revisar Build Rokid APK, corregir cualquier fallo real de CI y proporcionar el enlace del artifact del run exitoso. No existe todavía un enlace de descarga de APK.

La compatibilidad de cámara Camera2 y pantalla óptica requiere prueba física en el modelo y firmware exactos de Rokid. No se cambió a un SDK propietario ni se presupone compatibilidad universal.
