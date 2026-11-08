# TiltClock 3D: cinco fases

Proyecto independiente: `C:\Users\Danilo\Documents\TiltClock-deploy`.
Referencia de solo lectura: `C:\Users\Danilo\Documents\TiltClock`.

## 1. Auditoría y base Android — historial

- Inventariar HTML, CSS, JavaScript, licencia y GLB sin escribir en el original.
- Registrar SHA-256 del original y comprobar las copias de los cuatro modelos.
- Inspeccionar cabecera, extensiones, accesores, materiales, imágenes y animaciones GLB.
- Crear proyecto Gradle Kotlin DSL, Activity Wear, ViewModel, StateFlow y DataStore.
- Crear contratos de datos sin placeholders; conectar hora y batería del sistema.
- Probar formato horario, fecha, progreso y geometría circular.
- Compilar y firmar APK de depuración.
- Intentar capturas web y documentar bloqueos del entorno.

Salida: base Android ejecutable y evidencias técnicas. La pantalla diagnóstica identifica
la fase; **no es todavía la interfaz TiltClock terminada**. Se reemplaza en fase 3.
Las capturas web se desbloquearon en fase 2: ver `captures/reference-report.json`.

## 2. Renderizador y manipulación 3D

Implementación y límites actuales: [arquitectura](FASE2-ARQUITECTURA.md),
[resultados](FASE2-RESULTADOS.md) y [verificación física pendiente](FASE2-PRUEBAS-SAMSUNG.md).
Las tareas de validación física de esta lista permanecen abiertas hasta probar el reloj.

- Obtener versiones verificadas de Filament/gltfio y ejecutar una prueba con cada GLB original.
- Resolver `KHR_draco_mesh_compression` y `EXT_texture_webp` requeridas, sin perder materiales.
  La documentación de TextureProvider describe PNG/JPEG y KTX2: verificar WebP en Android;
  si hace falta, decodificar a un formato GPU compatible preservando los archivos originales.
- Medir carga, memoria CPU/GPU y tiempo de frame antes de consolidar el motor.
- Generar átomo con geometría real; cámara, luces y sombras comparadas con la referencia.
- Centrado antes de escala, desplazamiento Y -0,02 y escala base 2,8/dimensión máxima.
- Control por cuaterniones: arrastre con prioridad, sensor relativo recalibrado al soltar,
  filtros dependientes del tiempo y sin deriva por integrar velocidad indefinidamente.
- Zoom, pulsación sostenida, reproducción del primer clip cuando exista, liberación GPU
  y conservación del avatar anterior si falla una sustitución.
- Obtener Fox desde una revisión fija, verificar hash, licencia y atribución antes de empaquetarlo.

Criterio: GLB real controlable por dedo y sensor; ningún prerenderizado como sustitución.
Si el rendimiento falla, presentar medidas y opciones; no reducir los originales silenciosamente.

## 3. Pantalla fiel y ajustes circulares

Implementación y evidencias: [alcance](FASE3-ALCANCE.md), [geometría](FASE3-GEOMETRIA.md)
y [resultados](FASE3-RESULTADOS.md). La comparación registra diferencias pendientes;
la validación física y el objetivo de un píxel permanecen abiertos.

- Completar capturas claro/oscuro con fuentes y recursos cargados, viewport móvil explícito.
- Empaquetar Inter y Space Grotesk con sus licencias; fijar versiones de Compose Wear disponibles.
  La base actual usa Compose Foundation: **los componentes específicos de Compose Wear aún no están integrados**.
- Centralizar dimensiones a partir del diámetro útil medido; capas, cápsulas, anillo,
  lápiz, controles derechos, botón curvo, cristal, brillo y halos.
- Menú circular de una columna: Avatar, Tema, Idioma y Cerrar. Submenús y atrás.
- Filas con anchura calculada por ambos extremos verticales, desplazamiento al centro,
  bisel/entrada rotatoria, accesibilidad y animación de apertura/cierre.
- Aplicar y persistir idioma, tema, escala y avatar sin botón Guardar.
- Eliminar la pantalla diagnóstica de fase 1; no añadir pantallas de bienvenida.

Criterio: capturas comparables; errores geométricos, tipográficos y 3D medidos por separado.

## 4. Pasos reales, importación y ciclo de vida

- Health Services: consultar capacidades y `STEPS_DAILY`, permiso ACTIVITY_RECOGNITION
  según API y proveedor; estados de carga, permiso y no disponible.
- Tratar los totales como totales, no incrementos; validar fecha, zona, reinicio diario,
  antigüedad y actualizaciones agrupadas. No prometer coincidencia con Samsung Health.
- Meta propia 10.000 salvo una integración compatible que proporcione meta del dispositivo.
- Verificar batería en reloj real y recuperación al regresar de segundo plano.
- Consultar proveedor de documentos; GLB autocontenido válido, persistencia privada,
  errores sin perder el avatar anterior. Sin proveedor: explicar la limitación en el componente
  y validar una transferencia concreta por ADB para desarrollo; decidir solución de usuario.
- Detener sensores/renderizado al perder actividad y en ambiente; no mantener pantalla encendida.
- Todos los presets empaquetados; sin CDN en funcionamiento normal.

Criterio: ninguna medición ficticia ni edición manual; importación válida e inválida y uso sin red.

## 5. Validación y entrega

- Dos tamaños circulares, temas, idiomas, fuente ampliada y navegación atrás.
- Sensores físicos, arrastre, transición entre ambos, zoom, animaciones y persistencia.
- Permisos denegados, cambio de día/zona, segundo plano, importación y sin conexión.
- Capturas Android y web con fixtures únicamente en pruebas; superposición y medidas.
- Objetivo 30 FPS sujeto a medición; memoria y carga por modelo en Samsung real.
- Lint, pruebas, APK, hashes, licencias e instrucciones de instalación y depuración.

Criterio: informe que distinga aprobado, fallido y no ejecutado. No declarar pixel perfect,
probado en Samsung o rendimiento estable sin esas evidencias.

## Restricciones permanentes

Única geometría circular Samsung Wear OS. Sin selector de formato, carcasa, correas,
corona, flotación del dispositivo, WebView, pasos simulados, cuentas ni navegación adicional.
No se garantiza Tizen. Esta entrega es una **aplicación**, no una esfera seleccionable del sistema.
WFF es declarativo y no permite trasladar esta Activity, Three.js o un motor GLB arbitrario.
Una eventual variante WFF requiere acordar sus diferencias antes de implementarla.
