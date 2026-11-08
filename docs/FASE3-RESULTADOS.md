# Fase 3: implementación y verificación

La interfaz principal integra las cápsulas de fecha/pasos y hora, anillo de
progreso, batería, lápiz, controles, botón inferior curvo y brillo. Se empaquetan
Inter y Space Grotesk con OFL. El renderizado continúa siendo 3D nativo real.

Los ajustes circulares permiten elegir seis avatares locales, tema e idioma.
Atrás vuelve del submenú a ajustes y después al reloj. Las selecciones se guardan
al realizarlas. La manipulación y sensores se suspenden mientras están abiertos.
Las filas se desplazan hasta el centro; anchura calculada en ambos extremos.

## Evidencias reproducibles

- `tools/build-phase3.ps1 -Offline`: compila APK, pruebas y Lint.
- `tools/verify-phase3.ps1`: 22 pruebas, integridad del original, hashes de fuentes/APK y Lint.
- `tools/capture-phase3.ps1`: capturas claro/oscuro, español/inglés, dos diámetros lógicos y letra ampliada.
- `tools/compare-phase3.ps1`: diferencia RGB global normalizada contra la web.
- `tools/test-phase3-navigation.ps1`: secuencia de navegación y persistencia en el host de depuración.

Entorno: emulador Android API 37 de teléfono con configuración circular forzada
exclusivamente en la Activity debug; pantalla 800 × 800 px, densidades 430 y
560 dpi (aproximadamente 298 y 229 dp), fontScale 1 y 1,3. No es un Samsung ni
un emulador Wear OS; bisel y sensores físicos requieren comprobación real.

Los fixtures 08:27 AM, LUN 24 OCT, 1259 pasos y 85% sólo existen en el host debug.
Las capturas de preferencias usan lecturas reales del emulador. La producción
sigue mostrando pasos desconocidos hasta integrar Health Services en fase 4.

Resultado final: 22 pruebas aprobadas, 0 fallos; Lint 0 errores y 14 avisos.
APK verificado con firma v2 y un firmante de depuración. SHA-256:
`fd2f00c242f9f8e9eb51367fce080cc42048f79b9e63c1a19f7ebae6a9be0d84`.
El error RGB absoluto medio normalizado es 15,79/255 en claro y 20,09/255 en oscuro.
Esta medida global incluye el 3D y no equivale a desplazamiento geométrico.
Con letra ampliada la fecha puede ocupar dos líneas dentro de su cápsula.

## Límites y diferencias pendientes

La comparación global incluye diferencias de iluminación, tipografía, sombras y
espaciado. No demuestra precisión geométrica de un píxel. Quedan diferencias de
blur (aproximación mediante alfa), contraste del anillo, iluminación del átomo,
botón inferior e iconos; la validación final visual y física corresponde a fase 5.
No se declara pixel perfect ni probado en Samsung.

Los controles siempre visibles sustituyen el hover; sus zonas de 48 dp requieren
más separación vertical. En los extremos de ajustes las filas se estrechan y deben
desplazarse al centro para poder leerlas y accionarlas completas.

La importación de documentos y los pasos diarios siguen en fase 4. El botón
inferior abre por ahora los ajustes con presets locales. No se ha creado una
esfera seleccionable del sistema. No aparece selector Apple Watch, Xiaomi ni formato.

El APK y los hashes finales figuran en `phase3-verification.json`; Lint sin errores,
con avisos registrados en `build-phase3.log` y el informe generado por Gradle.
Las capturas y la comparación cuantitativa se conservan en `captures/phase3-*`
y `phase3-comparison.json`. El original conserva sus 20 archivos auditados intactos.
