# Revisión del prompt antes de fase 2

La fase 1 aporta proyecto independiente, inventario de cuatro GLB, hashes del original,
hora/batería Android, contratos de estados desconocidos, DataStore y cinco pruebas.
No aporta renderizador, sensores, arrastre, animación ni zoom conectado. La Activity
diagnóstica no es una esfera del sistema ni la interfaz final solicitada.

La fase 2 implementa el motor y su integración ejecutable. La pantalla fiel, tipografías,
ajustes definitivos y comparaciones 2D pertenecen a fase 3; Health Services e importación
de documentos a fase 4. Los pasos siguen explícitamente no disponibles mientras tanto.
El ciclo de vida del motor y sensores se adelanta a esta fase porque es necesario para
usar el renderizador de forma responsable.

Riesgos verificados: los cuatro modelos tienen millones de triángulos; el soporte WebP
de gltfio es opcional en compilación. Se conservarán los originales y se decodificarán
las imágenes WebP sin reducir dimensiones o geometría cuando se prepare su carga.
La decisión definitiva sobre rendimiento en Samsung exige mediciones en ese hardware.

La referencia permanece fuera del proyecto y de solo lectura. Sin selector de dispositivo,
datos ficticios, WebView, carcasa ni giro autónomo del átomo. La aplicación no requiere
permiso de Internet para usar los presets empaquetados.
