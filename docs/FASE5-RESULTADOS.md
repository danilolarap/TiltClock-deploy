# Fase 5: validación disponible y entrega

Esta entrega termina la ronda de verificación ejecutable en este entorno. La
aceptación física Samsung permanece abierta. No se declara compatibilidad física
probada, rendimiento estable ni igualdad visual de un píxel.

Resultado final ejecutado: 35 pruebas aprobadas, 0 fallos; Lint 0 errores y
18 avisos. Seis presets cargados y renderizados nativamente. Importación y
restauración aprobadas sin conexión. Firma v2 debug verificada; el manifiesto
release no incluye actividades, instrumentaciones ni proveedor de pruebas.
La pose de los fixtures se fija desactivando sensores sólo en el host visual debug.
Los hashes exactos de ambos APK figuran en phase5-verification.json.

Se revisaron integridad de recursos, importación, ciclo de vida y empaquetado.
Se corrigió la cuota de almacenamiento para que se aplique durante toda la copia,
no sólo al iniciar. Se añadieron pruebas de cuota y política de actividad. El icono
nativo evita depender de un recurso predeterminado del sistema.

Ejecutar build-phase5.ps1, test-phase5.ps1 y verify-phase5.ps1 en tools. El informe
phase5-verification.json conserva número de pruebas, Lint, hashes y separación de
componentes debug/release. phase5-matrix.json distingue aprobado, no ejecutado y
objetivo no cumplido. Los logs de cada motor conservan tiempos y memoria del proceso;
no son memoria GPU ni frames realmente presentados por un Samsung.

Las capturas de fase 5 usan dos diámetros lógicos aproximados de 298 y 229 dp,
temas claro/oscuro, español/inglés y fontScale 1,3. Los datos fijos de comparación
existen sólo en debug. La herramienta phase5-overlay.html superpone capturas al
mismo diámetro útil, excluyendo el borde web. Persisten diferencias de cápsulas,
tipografía, botón inferior, anillo, blur y luz del 3D; el objetivo de 1 px no se cumple.

La prueba de seis modelos ejercita JNI/gltfio y píxeles renderizados. La prueba de
importación comprueba copia exacta, rechazo de inválidos/externos, aceptación GPU y
restauración tras reiniciar con Wi-Fi/datos del emulador desactivados. El proveedor
real del emulador no ofrece pasos diarios: muestra no disponible, sin cero ficticio.

Para instalar el APK debug: FASE5-INSTALACION.md. El release es unsigned y sólo se
genera para comprobar el empaquetado. No se publica una versión de tienda ni una
esfera del sistema. Para relojes sin documentos la transferencia ADB es sólo debug;
la transferencia de usuario release sigue siendo una limitación documentada.

No se ejecutaron sensores físicos, igualdad con Samsung Health, permisos de salud
en un Watch real, bisel, autonomía, ambiente Samsung ni memoria/FPS físicos. Se
entrega la matriz FASE5-SAMSUNG.md para registrar esas verificaciones. Los tests de
fecha/zona/caducidad y los de ciclo de vida son unitarios, no sustituyen esas pruebas.

El proyecto original conserva sus archivos auditados; no hay selector de formato,
Apple Watch ni Xiaomi en el producto. Los recursos y sus licencias están inventariados
en FASE5-LICENCIAS.md. La fase queda entregada con estas limitaciones de aceptación.
