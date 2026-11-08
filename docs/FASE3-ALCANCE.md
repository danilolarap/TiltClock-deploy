# Fase 3: pantalla y ajustes

Se sustituye la integración provisional de fase 2 por cápsulas, fuentes locales,
anillo, controles, batería, brillo y botón inferior con recorte elíptico.
La escena sigue siendo Filament nativo con GLB reales y control táctil/sensorial.

Los ajustes tienen Avatar, Tema, Idioma y Cerrar; cada submenú vuelve mediante
Atrás. Las preferencias se aplican inmediatamente y se guardan en DataStore.
La geometría de las filas se calcula con sus dos extremos verticales, margen de
8 dp y altura que crece con fontScale. Todas las filas pueden desplazarse al centro.
La entrada rotatoria desplaza la misma lista.

La integración de pasos diarios e importación de archivos sigue en fase 4.
El botón inferior permite abrir ajustes y cambiar presets locales en esta fase.
No se ha implementado todavía la selección de documentos GLB.
No hay mediciones ficticias en producción ni selector de dispositivo.
