# Ciclo de vida

RESUMED + PowerManager.isInteractive habilitan la consulta de pasos. Los ajustes
y documentos bloquean manipulación, sensores y renderizado; el selector externo
también provoca la pausa normal de la Activity. SCREEN_OFF/ON actualiza la decisión.
No se solicita mantener la pantalla encendida ni activar un modo ambiente 3D.

Al cerrar la colección se cancela el callback de Health Services y se retira el
receptor de fecha/hora. La fuente de pasos no mantiene un servicio en segundo plano.
StateFlow elimina su replay al detener la colección para no mostrar una medición
antigua durante el siguiente arranque de la interfaz.

Sensores y escena mantienen la gestión de recursos GPU y callbacks de fase 2.
Cambiar un modelo carga un candidato y conserva el actual hasta aceptar el nuevo.
La pausa de un clip continúa independiente de la rotación táctil y sensorial.

La validación de ambiente, ahorro energético y retorno en Samsung físico permanece
pendiente. El emulador de teléfono no certifica las transiciones específicas de Wear.
