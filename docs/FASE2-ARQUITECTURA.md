# Motor nativo de fase 2

Filament y gltfio **1.77.3**, OpenGL ES, dependencias exactas. Sus POM publicados
declaran kotlin-stdlib 2.4.0: se actualiza también el compilador Kotlin incorporado
en AGP y el plugin Compose a 2.4.0. No se ignoran errores de metadata.

- `AvatarRepository`: lectura y preparación en Dispatchers.IO, un candidato cada vez.
- `GlbContainer`: validación de cabecera, longitud y chunks; solo GLB autocontenido.
- `WebpPreparer`: BitmapFactory Android decodifica WebP; PNG conserva dimensiones y
  canales. Se anexan bufferViews y se resuelve EXT_texture_webp a source estándar.
  La geometría Draco y los materiales se conservan. Esta copia transitoria necesita
  memoria adicional; no se guardan versiones reducidas ni se sobrescribe el original.
  El codificador emite barras literales en MIME: cgltf no resolvía el proveedor
  cuando JSONObject producía `image\/png`. La prueba GPU comprueba también color,
  pues una silueta sin texturas no equivale a una carga visual correcta.
- `HoloGenerator`: dos toros triangulados representados por sus aristas y núcleo
  icosaédrico subdividido. Materiales metálicos PBR. Sin clip o giro automático.
- `FilamentRenderer`: Engine, carga nativa, recursos y destrucción en un único
  HandlerThread. TextureView transparente, recorte circular Compose y cámara fija.
  No se usa el controlador orbital de ModelViewer, pues rotaría la cámara.
- `AvatarLoader`: carga asíncrona de recursos y cola de renderables, timeout de
  120 s, aceptación después de cargar. Durante la sustitución coexisten el avatar
  anterior y un candidato; es necesario para conservar el anterior ante errores.
  No se cargan todos los presets juntos. La memoria pico debe probarse en Samsung.
- `LoadedAvatar`: padres independientes de orientación/escala y centrado; la raíz
  y el esqueleto glTF conservan sus transformaciones originales. Centrado antes
  de escala: T(-cx, -cy-.02, -cz), escala 2.8/dimensión máxima. Holo conserva su
  tamaño procedural y solo recibe escala del usuario.
- `AnimationController`: primer clip, tiempo acumulado solo al reproducir y en
  primer plano. La pausa no bloquea el padre que recibe sensores/arrastre.
- `TransformController`: cuaterniones, filtro .15 y respuesta .08 convertidos
  desde 60 Hz a tiempo real; velocidad máxima 3 rad/s. El dedo tiene prioridad;
  al soltar/cancelar conserva la pose visible y recalibra el sensor.
- `OrientationController`: TYPE_GAME_ROTATION_VECTOR con giroscopio; alternativa
  TYPE_ROTATION_VECTOR, también condicionada a giroscopio. La fusión Android
  sustituye la integración manual con deriva. Ajuste por rotación de pantalla.
  Sin sensores compatibles permanece disponible el arrastre y se informa.
- `RenderLifecycle`: Activity RESUMED, pantalla interactiva y ajustes cerrados.
  Desregistra sensores y detiene frames en pausa, pantalla apagada y selector.
  No se solicita mantener la pantalla encendida ni modo ambiente continuo.

Zoom: .3–3, toque inmediato .1, repetición .03 cada 50 ms tras 250 ms. Cancelar
o soltar cancela el trabajo. DataStore persiste escala y solo avatares aceptados.
Pinch no se añade sin validación física. No se modifica el conteo de pasos.

El plano de sombra usa `shadowMultiplier`, material compilado por matc 1.77.3
(OpenGL móvil), tangentes para la normal horizontal y transparencia .25.
Se desactiva el dithering porque producía puntos sobre el fondo transparente.
Todavía hay diferencias de iluminación con Three.js; requieren calibración visual.

`RenderMetrics` escribe tiempos de carga, PSS del proceso, heap nativo y tasa de
frames enviados a Logcat. **PSS no es memoria GPU** y la tasa enviada no acredita
presentación en pantalla ni estabilidad a 30 FPS. GPU real requiere herramientas
del dispositivo. El coste de frame CPU y la prueba offscreen se reportan aparte.

La pantalla intermedia hace accesibles modelos, zoom, pausa y arrastre. El menú,
tipografías, silueta inferior, anillo y precisión visual final se completan en
fase 3. No debe usarse esta pantalla como prueba de fidelidad final.

## Fuentes primarias comprobadas

- [Filament 1.77.3](https://github.com/google/filament/releases/tag/v1.77.3).
- [Metadatos Maven de gltfio](https://repo.maven.apache.org/maven2/com/google/android/filament/gltfio-android/maven-metadata.xml).
- [Fuentes Java publicadas de gltfio 1.77.3](https://repo.maven.apache.org/maven2/com/google/android/filament/gltfio-android/1.77.3/gltfio-android-1.77.3-sources.jar).
- [Configuración Android WebP/Draco](https://github.com/google/filament/blob/v1.77.3/android/gltfio-android/CMakeLists.txt).
- [Materiales Filament y shadowMultiplier](https://google.github.io/filament/main/materials.html).
- [Sensores de movimiento Android](https://developer.android.com/develop/sensors-and-location/sensors/sensors_motion).
