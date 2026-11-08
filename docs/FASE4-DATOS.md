# Procedencia y semántica de las mediciones

Pasos: AndroidX Health Services Client 1.1.0, PassiveMonitoringClient y
DataType.STEPS_DAILY. Se comprueba supportedDataTypesPassiveMonitoring antes
de registrar. Permiso ACTIVITY_RECOGNITION; no se solicitan métricas adicionales.
No se usa Health Connect, contador desde el arranque ni acelerómetro propio.

Cada intervalo diario es un total: sustituye al anterior, jamás se suma. Se ordenan
las muestras por fin del intervalo. El tiempo relativo al arranque se convierte
con Instant.now − SystemClock.elapsedRealtime. No hay caché persistente de pasos.
Las lecturas de otro día, negativas, futuras o con más de 15 minutos se excluyen.
La comprobación de vigencia se repite cada 10 segundos. Cambios de día, hora y zona
invalidan la lectura; tras cambios del reloj sólo se admiten muestras nuevas.
Se rechaza el total del día anterior cuyo intervalo termina exactamente a medianoche.

El permiso se solicita al tocar el componente existente. Tras denegarlo, volver a
tocarlo permite abrir los permisos de la aplicación. El registro se cancela al
dejar de estar en primer plano interactivo; regresar inicia una consulta nueva.
Si no llegan datos durante 60 segundos se muestra no disponible; se sigue escuchando.
Los callbacks admiten contenedores con varias muestras sin duplicar los totales.

Meta: 10.000 es el objetivo propio de TiltClock. No se ha encontrado ni integrado
una API pública del reloj para leer su objetivo Samsung. Tampoco se garantiza que
STEPS_DAILY sea idéntico al total de Samsung Health, que requiere validación física.

Batería: ACTION_BATTERY_CHANGED, con nivel/escala reales y entrega sticky al
reanudarse la colección. Hora y fecha: sistema y zona horaria actual cada segundo.
No se introducen mediciones ficticias en producción.

Fuentes oficiales consultadas:
- https://developer.android.com/jetpack/androidx/releases/health
- https://developer.android.com/health-and-fitness/health-services/monitor-background
- https://developer.android.com/health-and-fitness/health-services/permissions
- https://android.googlesource.com/platform/frameworks/support/+/HEAD/health/health-services-client/src/main/java/androidx/health/services/client/data/DataType.kt

Samsung físico pendiente: capacidades reales, permisos concedidos/denegados,
conteo diario frente al proveedor, cambio de día/zona y actualizaciones agrupadas.
