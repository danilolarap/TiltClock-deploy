# Auditoría de la referencia — fase 1

Documento histórico de fase 1. El estado posterior (renderizador, Fox y capturas
desbloqueadas) está en [resultados de fase 2](FASE2-RESULTADOS.md).

Fecha de trabajo: 8 de noviembre de 2026, America/Bogota.

## Archivos y protección

Se leyeron el HTML, CSS, los seis módulos JavaScript, README y LICENSE del original.
Solo se copiaron los GLB locales y la licencia. No se instaló nada ni se escribió en TiltClock.
`reference-sha256.json` registra todos los archivos fuera de `.git`, incluida la configuración local;
`node tools/audit-reference.mjs --verify` verifica que siguen idénticos.

`deviceSelector.js` se consultó como contexto; no existe su equivalente Android.
El podómetro web mezcla un valor inicial 1.259, acelerómetro, edición manual y persistencia:
ninguna de esas fuentes se migró. El HTML contiene batería 85% sin fuente Android.
La lista de extensiones real prevalece sobre las afirmaciones generales del README.

## Modelos originales copiados

| Modelo | Bytes | Vértices declarados | Triángulos declarados | Clips |
|---|---:|---:|---:|---:|
| Pájaro | 8.693.308 | 1.630.806 | 3.116.404 | 0 |
| Mapache | 9.323.260 | 1.693.658 | 3.122.652 | 0 |
| Moneda | 5.472.008 | 1.323.346 | 1.975.857 | 0 |
| Riot | 7.848.296 | 1.637.304 | 3.133.220 | 0 |

Los cuatro son GLB 2.0, generador glTF-Transform v4.4.2. Todos requieren
`KHR_draco_mesh_compression` y `EXT_texture_webp`; tienen tres imágenes WebP embebidas,
un material, una malla y ningún recurso externo. El detalle y los SHA-256 están en
`models-inventory.json`. Los conteos proceden de accesores; esta comprobación estructural
no descomprime Draco, no sustituye al validador Khronos y no prueba carga GPU.

El tamaño comprimido de 5–9 MB no mide la memoria en ejecución. Millones de triángulos
suponen un riesgo para memoria y rendimiento; hace falta medir cada modelo en el reloj.
Filament/gltfio es candidato, no una elección validada todavía. No se generaron derivados optimizados.

`holo` no es un GLB: requiere geometría procedural. No tiene giro autónomo en el código.
Fox no existe localmente y **no está incluido** en el APK. Su descarga original apunta a
`main` en un CDN, sin revisión fija; se pospone hasta revisar licencia y fijar procedencia.

## Referencia visual y medidas

Viewport previsto: **390 × 844 CSS px, DPR 1**, menor que `sm` de Tailwind; hora 30 CSS px.
El marco tiene 340 px, borde 4 px y padding 16 px por lado. Bajo el box-sizing de Tailwind,
se espera pantalla exterior de 300 px y área interior de 298 px. **Es un cálculo del código,
no una medición DOM realizada**. No convertir 340 px en 340 dp.

Falta medir `getBoundingClientRect`, clientWidth y lienzo real con Tailwind cargado,
capturar el círculo sin marco y establecer el diámetro útil definitivo. El harness en
`tools/capture-reference.mjs` comprueba Three.js, canvas, tipografías y tamaño antes de
aceptar capturas. Abre la referencia por `file:` con perfil Chrome separado en este workspace.
Fixtures solo en esa herramienta: 08:27 AM, LUN, 24 OCT, 1.259 pasos, 85%, holo, escala 1.

Intentos ejecutados: Chrome headless por CLI y por CDP pipe, también con `--disable-gpu`.
El proceso GPU terminó repetidamente con código -1073741790 y CDP agotó el tiempo en
Page.enable. Registro: `captures/browser.log`; resultado: `captures/reference-report.json`.
No se obtuvieron capturas comparables ni superposiciones. No hay afirmación pixel perfect.
Las peticiones HTTPS de Node/PowerShell también fallaron; no se pudieron descargar fuentes.

Las fuentes de la pantalla diagnóstica son del sistema y no constituyen una sustitución aprobada
para el diseño final. Inter/Space Grotesk y sus licencias quedan pendientes antes de fase 3.

## Decisiones técnicas verificadas

- AGP 9.4 admite API 37 y exige Gradle 9.6; Build Tools 36.0.0 y JDK mínimo 17.
  Se fijó AGP 9.4.1 disponible localmente, Gradle 9.6.0 con SHA-256 del wrapper,
  Kotlin Compose 2.2.10 y SDK 37. JBR instalado: Java 25.
- Dependencias fijadas en `app/build.gradle.kts`; algunas versiones disponibles en caché
  son anteriores a las últimas estables. No se afirma que todas sean las últimas.
- Sin dependencia Filament ni componentes Compose Wear específicos aún; se integran al
  disponer de sus artefactos y validar su compatibilidad. No se usó WebView.
- Activity con requisitos `watch` y `screen.round`; validación de modo reloj y pantalla circular.
  No hay servicio WatchFace. La Activity no aparece automáticamente como esfera principal.
- La documentación WFF exige ese formato para las esferas desde enero de 2026.
  La experiencia GLB interactiva permanece en la aplicación nativa.
- Health Services aporta datos procesados por el reloj. `STEPS_DAILY` es un total diario
  y las entregas pasivas pueden agruparse. Aún no se integró ni se comparó con Samsung Health.
- Meta propia TiltClock: 10.000 pasos. No se atribuye a Samsung Health.
- `CircularGeometry.rowWidth` usa el extremo vertical más alejado de cada fila y margen
  radial; sus pruebas de dos diámetros verifican que ambas esquinas caben. Falta probar la lista real.

## Fuentes consultadas

- [AGP 9.4 y compatibilidad](https://developer.android.com/build/releases/agp-9-4-0-release-notes)
- [Activity](https://developer.android.com/jetpack/androidx/releases/activity)
- [Compose UI](https://developer.android.com/jetpack/androidx/releases/compose-ui)
- [DataStore](https://developer.android.com/jetpack/androidx/releases/datastore)
- [Compose Wear](https://developer.android.com/jetpack/androidx/releases/wear-compose)
- [Watch Face Format](https://developer.android.com/training/wearables/wff)
- [gltfio y proveedores de texturas](https://google.github.io/filament/dup/gltfio.html)
- [Health Services: capacidades y lotes](https://developer.android.com/health-and-fitness/health-services/compatibility)
- [Health Services: permisos](https://developer.android.com/health-and-fitness/health-services/permissions)
