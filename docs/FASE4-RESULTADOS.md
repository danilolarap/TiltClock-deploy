# Fase 4: resultados y pendientes

Implementados Health Services STEPS_DAILY con capacidades, permisos, caducidad,
reemplazo de totales, reinicio diario y cambios de zona; anillo y texto consumen
la misma lectura. Batería y reloj siguen siendo datos del sistema. No hay contador
simulado, pasos editables ni integración supuesta con Samsung Health.

Importación GLB 2 autocontenida: selector real, copia privada, validación, persistencia
tras aceptación nativa y mantenimiento del avatar anterior si falla. El botón cambia
a Cambiar .GLB / Change .GLB. Sin proveedor muestra una explicación y admite la
transferencia ADB documentada para APK debug. La distribución release sin proveedor
requiere definir una transferencia de usuario; no se añade una app móvil auxiliar.

Las pruebas instrumentadas verifican bytes exactos de Fox importado desde content URI,
acceso durable desde una nueva instancia del almacén, rechazo de un archivo inválido
y de imágenes externas, y conservación de preferencias tras errores. Sólo el
ContentProvider de pruebas genera esos documentos; no se incluye en release.

El proveedor de Health Services del emulador no entrega pasos diarios: la aplicación
informa Unavailable, nunca cero. Esto comprueba el camino sin proveedor; no demuestra
mediciones reales de Samsung ni permite comparar su conteo con Samsung Health.

Resultado final: 31 pruebas aprobadas, 0 fallos; Lint 0 errores y 19 avisos.
La prueba ADB verificó SHA-256, aceptación GPU del GLB importado y restauración del
mismo identificador tras forzar cierre y abrir de nuevo. Se repitió con la red del
emulador desactivada. El selector de documentos abrió y se canceló correctamente.
Su captura no se publica porque la vista Recientes incluye archivos ajenos a TiltClock.

SHA-256 del APK final:
`7cf61f35d0c7dcee1cb40594714fb140bc620311a146cd2bc7e65545b3a17b4a`.

Herramientas reproducibles: `build-phase4.ps1`, `test-phase4.mjs`,
`verify-phase4.ps1` y `transfer-avatar.mjs` en tools. Informes de ejecución:
`phase4-device.json`, `phase4-instrumentation.log`, `phase4-verification.json`
y `build-phase4.log`. El APK está en app/build/outputs/apk/debug/app-debug.apk.

Entorno: Android API 37 de teléfono, host circular exclusivo de debug, 800 × 800 px,
430 dpi. No hay reloj físico ni imagen Wear OS instalada. Las capturas phase4
documentan importación en ese entorno, no fidelidad ni rendimiento Samsung.

Pendientes físicos: datos diarios con permiso concedido, denegación y revocación,
midnight real, zona, ahorro energético, ambiente y regreso, compatibilidad del
proveedor de documentos de cada reloj y medición de memoria/FPS. Los cambios de día,
zona, caducidad, orden de lotes y duplicados se cubren además con pruebas unitarias.

La pantalla conserva únicamente geometría circular. No se añaden Apple Watch,
Xiaomi ni selección de formato. El original TiltClock permanece de sólo lectura.
