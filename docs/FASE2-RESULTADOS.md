# Resultado de fase 2

Implementado el renderizador nativo y su integración interactiva. Compilación,
20 pruebas unitarias, Lint y prueba GPU de los seis avatares ejecutados con éxito.
**Filament es una elección provisional para Samsung hasta medirlo en el reloj.**
No se ha probado un Galaxy Watch físico ni un emulador Wear OS.

## Evidencias ejecutadas

| Comprobación | Resultado y evidencia |
|---|---|
| APK debug 0.2.0-phase2, código 2 | BUILD SUCCESSFUL; `build-phase2.log` |
| Pruebas JVM | 20 aprobadas, sin errores, fallos ni omitidas |
| Android Lint | 0 errores; 9 avisos, principalmente versiones fijadas y configuración heredada; informe en app/build/reports |
| Firma | V2 válida, un firmante debug; `apk-phase2-signature.log` |
| Draco, texturas y materiales | Los cuatro GLB originales renderizan con color; WebP y PNG decodificados tienen los mismos píxeles en Android |
| Presets | Holo procedural, Fox animado, Pájaro, Mapache, Moneda y Riot; `render-probe/report.json` y seis PNG |
| Carga inválida | El avatar anterior permanece; afirmación comprobada por Instrumentation |
| Animación y rotación | Primer clip de Fox; arrastre con clip pausado visible en `captures/android-fox-paused-drag.png` |
| Táctil integrado | Capturas antes/después de arrastre y zoom sostenido; selección de Fox y recuperación tras detener/reabrir proceso |
| Persistencia | Avatar Fox y escala ampliada recuperados; `captures/android-restored.png`. Pausa y orientación manual no se persisten |
| Ciclo de vida | Pausa/regreso y apertura de selector detienen el bucle; registros en `render-metrics.log`; falta verificación de sensores físicos |
| Referencia | Capturas claro/oscuro con fuentes cargadas y Mapache; `captures/reference-report.json` |
| Protección original | 20 archivos sin cambios, .git excluido; `reference-phase2-verification.log` |
| Modelos y licencias en APK | Cuatro hashes originales y Fox verificados, licencia original y licencias añadidas presentes; `delivery-phase2.json` |
| Exclusiones de producto | Sin opciones Apple Watch, Xiaomi ni cambio de formato; tampoco WebView, pasos ficticios o edición manual |

Entorno GPU: Android Emulator API 37, x86_64, teléfono, OpenGL ES 3.1 traducido a
AMD Radeon(TM) Graphics. Primero se intentó SwiftShader; el emulador terminó al
renderizar. Con GPU del equipo y Vulkan desactivado se completó la prueba.
Esto no acredita compatibilidad o rendimiento en Samsung.

## Carga y memoria observadas

Prueba offscreen de 400 × 400, escala 1, pose identidad, primer clip disponible.
Los tiempos incluyen preparación y comprobaciones adicionales del harness de
texturas; no son un benchmark aislado de carga en producción. Se ejecutó una
secuencia, no una distribución estadística.

| Avatar | Carga total del harness (ms) | PSS tras render (KiB) | Clips disponibles |
|---|---:|---:|---|
| Holo | 308 | 126.109 | No |
| Fox | 100 | 117.746 | Sí; se reproduce el primero |
| Pájaro | 3.988 | 241.657 | No |
| Mapache | 3.167 | 260.965 | No |
| Moneda | 1.547 | 222.449 | No |
| Riot | 2.373 | 252.745 | No |

En la muestra de aceptación de Mapache se registraron **387.353 KiB de PSS**;
no es una medición del máximo absoluto. PSS incluye el proceso, no solo el modelo,
y **no equivale a memoria GPU**. El registro diferencia heap nativo y PSS.
El reemplazo conserva anterior+candidato hasta aceptar: la memoria transitoria
y los millones de triángulos siguen siendo riesgos para el reloj.

La pantalla interactiva del emulador envió aproximadamente **28,6–29,0 FPS** tras
la carga; durante arranque hubo muestras inferiores y frames omitidos. No se
declaran 30 FPS estables ni frames efectivamente presentados. Los 30 frames
offscreen por modelo verifican funcionamiento y tienen tiempos registrados;
no se usan para atribuir una tasa de pantalla real.

## Comparación visual y pendientes

Referencia móvil: viewport **390 × 844 CSS px, DPR 1**. DOM medido: marco 340 px,
pantalla exterior 300 px, borde 1 px, **clientWidth/clientHeight y canvas 298 px**.
Hora efectiva 30 CSS px. Las capturas de la referencia excluyen el marco exterior.

Se inspeccionaron imágenes de ambos motores. Se corrigieron dos fallos visibles:
texturas negras por MIME escapado `image\/png` y ruido de transparencia por
dithering. El plano recibió tangentes que requiere el material de sombra.
Se ajustó la luz cian usando unidades de Filament, sin equipararlas a Three.js.

Persisten diferencias de brillo, reflejos, sombra y rasterización de las aristas
del átomo. Sus líneas pueden mostrar solapamientos/aliasing al ampliar.
La pantalla intermedia usa fuentes del sistema y un selector de presets básico;
no es el diseño final. Las capturas de interacción usan **800 × 800 px, densidad
320, host debug circular**, datos reales del emulador y escala alterada por la prueba.
No son comparaciones 2D con fixtures ni una prueba de tamaños de Samsung.

No se hizo superposición ni se midió desviación de 1 píxel; corresponde a fase 3
junto con fuentes locales, cápsulas, anillo, botón curvo, menú completo y ES/EN
aplicados desde preferencias. Los textos actuales siguen el idioma del sistema.
Los pasos siguen no disponibles hasta Health Services en fase 4; la batería y
hora visibles proceden de Android. No se atribuyen a Samsung Health.

Pendientes físicos: sensores y ejes en muñeca, transición dedo/inclinación real,
memoria GPU, temperatura/consumo, dos tamaños circulares, 30 FPS, modo avión y
comportamiento de ambiente. Ver `FASE2-PRUEBAS-SAMSUNG.md`.

## Entrega

APK: `app/build/outputs/apk/debug/app-debug.apk`, **81.914.213 bytes**.
SHA-256: `4c9e7e25d60cecb94b50cf0ecbff00efa4896cd8d5c57a9cb047e1190d7077cb`.
SDK mínimo 30, target/compile 37. Sigue siendo una Activity de aplicación.
No implementa esfera WFF ni promete Tizen. Los entrypoints de verificación
solo existen en `src/debug`, sin accesos visibles en el launcher del producto.

Las instrucciones de compilación/instalación están en README. Fox tiene revisión
fija, hash y atribuciones en `reference/fox-source.json`; los originales se
empaquetan intactos. No se generan archivos en la carpeta web de referencia.
