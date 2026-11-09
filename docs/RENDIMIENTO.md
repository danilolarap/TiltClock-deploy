# Optimización para Wear OS

Los cuatro modelos estáticos usaban entre 1,98 y 3,13 millones de triángulos.
Se generaron variantes para el reloj con Meshoptimizer, conservando materiales,
coordenadas UV y texturas. La simplificación pondera también normales y UV para
conservar los reflejos. El umbral de error es 0,005 (0,001 para la moneda); se
intenta alcanzar 45.000 triángulos, o 200.000 para la moneda, sin forzar ese umbral.

| Modelo | Antes | Ahora |
|---|---:|---:|
| Mapache | 3.122.652 | 142.524 |
| Moneda | 1.975.857 | 273.643 |
| Pájaro | 3.116.404 | 101.837 |
| Riot | 3.133.220 | 45.170 |

Las texturas pasan de 1024 a 512 píxeles por lado (75 % menos texeles).
Las variantes usan PNG y buffers glTF normales: el reloj ya no descomprime Draco
ni convierte WebP a PNG al cargar estos modelos. Los originales de escritorio
siguen en el proyecto TiltClock. Las variantes reemplazan únicamente los cuatro
GLB del paquete Android; Fox conserva su animación y el átomo sigue siendo procedural.
La reducción de detalle es deliberada y puede apreciarse con mucho zoom.

El motor utiliza sombras de 256 × 256 en vez de 1024 × 1024. Se evitan matrices
de transformación idénticas, la actualización del esqueleto al pausar, y bucles
de renderizado duplicados cuando coinciden la creación de la superficie y la
reanudación. El sensor reutiliza su buffer. La hora se consulta cada segundo para
detectar cambios del sistema/zona, pero solo emite cuando cambia el minuto o la
zona, evitando recomponer toda la pantalla cada segundo. Se mantiene el objetivo
de 30 FPS y la suspensión del renderizado con ajustes abiertos o app en segundo plano.

## Reproducir los modelos

```powershell
npm.cmd ci --prefix tools/model-pipeline --cache .npm-cache
node tools/model-pipeline/optimize.mjs C:/Users/Danilo/Documents/TiltClock/assets/models
if ($LASTEXITCODE -ne 0) { throw 'No copiar: falló la validación de modelos' }
Copy-Item build/watch-models/*.glb app/src/main/assets/models
powershell -ExecutionPolicy Bypass -File tools/build-phase2.ps1 -Offline -LogPath docs/build-performance.log
```

La herramienta escribe primero en `build/watch-models` y valida la reducción,
dimensiones de texturas y lectura de los GLB antes de copiarlos al paquete.
Las cifras verificables están en `watch-model-optimization.json`. Se conservan
las licencias y atribuciones de los modelos de origen.

La reducción de geometría, texturas y trabajo de CPU no equivale a una medida de
FPS del Samsung. La velocidad sostenida, temperatura y autonomía requieren una
prueba en el reloj físico. Las capturas y la prueba GPU del emulador se guardan
en `performance-gpu`; no acreditan rendimiento físico.

Validación final: 35 pruebas unitarias aprobadas, Lint con 0 errores y 13 avisos,
y los seis modelos cargados y renderizados por Filament en el emulador. También
pasó la comprobación de conservar el avatar ante una carga inválida. La comparación
visual está en `performance-comparison.png`; la moneda conserva más geometría para
evitar reflejos facetados. Los GLB importados por el usuario no se simplifican con
esta herramienta de empaquetado y su coste depende del archivo importado.
