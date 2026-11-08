# Resultado de fase 1

Este informe conserva la evidencia de fase 1. Para el APK actual y las pruebas
del renderizador consultar [fase 2](FASE2-RESULTADOS.md).

La base Android está implementada y compilada. **La fase no cierra todavía su referencia
visual: las capturas válidas están bloqueadas por el entorno.** La experiencia 3D y la
interfaz final pertenecen a las fases siguientes. No se ha ejecutado la app en un reloj.

## Verificaciones ejecutadas

| Comprobación | Resultado | Evidencia |
|---|---|---|
| Compilar Kotlin y generar APK debug | Aprobado, BUILD SUCCESSFUL | `build-phase1.log` |
| Pruebas unitarias | 5 aprobadas, 0 fallos/errores/omitidas | `../app/build/reports/tests/testDebugUnitTest/index.html` |
| Firma del APK | V2 válida, un firmante debug | `apk-signature.log` |
| Metadatos de APK | com.tiltclock.wear; min 30, target/compile 37; watch + screen.round | `apk-badging.txt` |
| Modelos empaquetados | Cuatro; SHA-256 idénticos al original | `delivery-verification.json` |
| Licencia original en APK | Presente en assets/licenses/TiltClock-MIT.txt | `delivery-verification.json` |
| Integridad del original | 20 archivos idénticos; .git excluido | `reference-verification.log` |
| Otros formatos y placeholders en Kotlin/XML de producción | Ausentes en la búsqueda ejecutada | `delivery-verification.json` |
| Android Lint | Bloqueado: falta lint-gradle:32.4.1 en caché offline | `lint-attempt.log` |
| Capturas de referencia | Bloqueadas: Chrome GPU falla; CDP Page.enable agota espera | `captures/browser.log`, `captures/reference-report.json` |
| Descarga de fuentes/dependencias nuevas | Bloqueada: red del terminal restringida | Ver auditoría |
| Emulador Wear | No instalado; solo AVD Medium_Phone_API_37.0 | Consulta emulator -list-avds |
| ADB/dispositivo conectado | Enumeración no completada: adb intenta crear \\.android y recibe Permission denied | Intento adb devices -l |
| Samsung físico | No probado | Requiere dispositivo y conexión ADB funcional |

Las cinco pruebas cubren medianoche/mediodía, cambio de zona, domingo y cambio de día,
distinción entre dato desconocido y cero, límites del progreso, y esquinas de filas
circulares con dos diámetros (192 y 228 unidades). La prueba geométrica **no equivale**
a probar dos relojes, fuente ampliada o lista interactiva.

## APK de esta fase

Ruta: `app/build/outputs/apk/debug/app-debug.apk`, relativa a la raíz del proyecto.

- Versión: `0.1.0-phase1` (código 1).
- Tamaño: 56.945.804 bytes.
- SHA-256: `80615921d0e8f96c3d439a51ddc34f38f9281f6fa8c2f7d47d3ff475bbacecdd`.
- Incluye los cuatro modelos originales, aunque aún no hay renderizador que los abra.
- Fox no está empaquetado. Ninguno de los cuatro modelos locales contiene clips.

El informe JSON se regenera con `tools/verify-delivery.ps1` después de compilar.
La clave incluida en el entorno de trabajo es de depuración, ignorada por Git; no es una clave de publicación.

## Datos de producción de la base

- Fecha/hora: `ZonedDateTime.now()` del sistema del dispositivo; formato 12 horas, sin segundos visibles.
- Batería: broadcast protegido y sticky `ACTION_BATTERY_CHANGED`, cociente real nivel/escala.
  Lectura inválida produce estado no disponible; el repositorio no usa 85% ni otro fallback.
- Pasos: estado no disponible con integración pendiente; **no hay conteo implementado aún**.
- Progreso: función preparada para usar el mismo total válido; desconocido devuelve null.
- Meta: 10.000, objetivo propio de TiltClock; no leído de Samsung Health.
- Persistencia: esquema DataStore y operaciones de avatar, escala, tema, idioma y ruta importada.
  Sus controles y su recuperación funcional en dispositivo siguen pendientes.

La integración Android de hora y batería está compilada; falta verificar su actualización
y ciclo de vida sobre Wear OS real. Los repositorios se suscriben mientras la Activity está activa.
No hay renderizado 3D continuo ni sensores registrados todavía.

## Pendiente de probar o implementar

Carga GLB en GPU; WebP/Draco en Filament; materiales, encuadre y sombras; átomo procedural;
inclinación y arrastre; transición entre controles; zoom sostenido; clips; importación;
Health Services, permisos y frescura diaria; fuentes locales; tema e idioma interactivos;
menú circular, bisel y atrás; fuente ampliada; sin conexión en dispositivo; recursos GPU;
memoria, tiempos de carga y FPS. No hay medición de 30 FPS ni prueba en Samsung.

No hay capturas Android, superposición ni error geométrico medido. No se afirma pixel perfect.
La pantalla actual es diagnóstica y usa tipografía del sistema; será reemplazada por el diseño
original con Inter y Space Grotesk al implementar fase 3. No se aprobó una aproximación visual final.

Confirmación: **no aparece ninguna opción Apple Watch, Xiaomi ni cambio de formato** en la
implementación Android. No hay selector oculto, preferencia de formato ni recursos de esos diseños.
La app tampoco implementa una esfera de sistema, ni simula serlo.
