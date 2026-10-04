# Rokid Product Recognizer — MVP V1

Primer prototipo para **Rokid Glasses con pantalla**. La aplicación analiza imágenes de la cámara y muestra en el HUD la categoría reconocida y la confianza estimada.

## Objetivo de V1

- Capturar frames desde la cámara con CameraX.
- Ejecutar reconocimiento local con ML Kit Image Labeling.
- Mostrar solamente texto sobre el HUD, sin preview de cámara.
- Filtrar detecciones inestables para reducir parpadeos.
- Funcionar sin Google Play Services durante el reconocimiento.

> V1 reconoce categorías generales. El reconocimiento de productos/SKU concretos se implementará en V2 con un modelo personalizado.

## APK automático en GitHub

El repositorio incluye `.github/workflows/build-apk.yml`.

Cada vez que se haga `push` a `main`, GitHub Actions compilará automáticamente el APK de depuración.

Para descargarlo:

1. Abre la pestaña **Actions** del repositorio.
2. Entra al último workflow **Build Rokid APK** que haya terminado correctamente.
3. Baja hasta **Artifacts**.
4. Descarga **RokidProductRecognizer-V1-APK**.
5. Descomprime el archivo descargado; dentro estará `RokidProductRecognizer-V1-debug.apk`.
6. Pasa ese APK a las Rokid e instálalo mediante el método de sideload/Toolbox disponible en tu versión de las gafas.

También puedes pulsar **Run workflow** desde la pestaña Actions para compilarlo manualmente.

## Compilación local opcional

Requisitos:

- JDK 17
- Android SDK 35

Configura `JAVA_HOME` y `ANDROID_HOME` y ejecuta `./gradlew :app:testDebugUnitTest :app:lintDebug :app:assembleDebug`.
En Windows usa `gradlew.bat`. No se requiere Android Studio. Instala también Build Tools 34.0.0 (predeterminado de AGP 8.7) o permite que Gradle lo instale con las licencias aceptadas.

El APK local quedará normalmente en:

`app/build/outputs/apk/debug/app-debug.apk`

## Stack

- Java 17
- Android Gradle Plugin 8.7.3
- Android SDK 35
- CameraX 1.4.2
- ML Kit Image Labeling 17.0.9
- minSdk 29
- targetSdk 32

## Alcance y compatibilidad de V1

- ML Kit clasifica la imagen completa; puede seleccionar una etiqueta de la escena en vez del producto. No garantiza todas las categorías deseadas ni identifica SKU.
- Modelo incluido `com.google.mlkit:image-labeling:17.0.9`: disponible desde el primer inicio, sin descarga del modelo. Prueba el primer inicio en modo avión.
- Confianza mínima 65%, un análisis cada 450 ms como máximo, tres coincidencias consecutivas de categoría y caducidad del resultado a los 2.5 segundos sin confirmación.
- Sin `Preview` ni imágenes en pantalla: texto blanco sobre negro. En pantalla óptica, el negro debe emitir poca o ninguna luz; el comportamiento final depende del hardware y firmware.
- Requiere Android API 29 o superior y cámara expuesta mediante Camera2. Se prefiere cámara trasera; si no existe, se usa la primera cámara accesible.
- No se presupone la versión de YodaOS ni compatibilidad universal entre modelos Rokid. Si la cámara requiere SDK propietario, primero registra modelo, firmware y error del HUD; la arquitectura V1 no se cambia sin explicarlo.
- CameraX 1.6.2 requiere compileSdk 36 y AGP 8.9.1 según sus metadatos AAR. Se fija 1.4.2 para conservar SDK 35 y AGP 8.7.3.
- El APK debug es instalable por sideload. Los builds en runners efímeros pueden tener firmas debug distintas: una actualización podría requerir desinstalar la anterior.
- Si se rechaza el permiso, toca el texto inferior para abrir los ajustes de la app, habilita cámara y regresa. Si las gafas no permiten tocarlo, abre los ajustes mediante su herramienta de control.

## Prueba física pendiente

1. Instala el APK y concede cámara.
2. Comprueba que solo aparece texto y puedes ver el entorno.
3. Apunta a botella, alimento y caja con buena iluminación; espera tres detecciones.
4. Retira el objeto: el resultado antiguo debe desaparecer tras unos 2.5 segundos sin confirmación.
5. Cierra y vuelve a abrir la aplicación y repite el primer inicio sin Internet.

## Próxima etapa: V2 (fuera de este MVP)

Sustituir el clasificador genérico por un modelo entrenado para productos específicos, por ejemplo:

- Producto / familia
- Variante
- SKU
- Número de parte
- Confianza

Después podremos añadir datos de proceso, instrucciones, OK/NOK y trazabilidad.
