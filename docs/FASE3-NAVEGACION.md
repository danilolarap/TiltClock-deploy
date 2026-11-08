# Navegación comprobada

La secuencia `tools/test-phase3-navigation.ps1` se ejecutó en el host circular
debug sin fixtures de mediciones. Abrió el lápiz, desplazó la lista, abrió Tema,
seleccionó Claro, volvió con Atrás, abrió Idioma, seleccionó English y volvió
dos veces hasta el reloj. Las etiquetas cambiaron a inglés.

Se forzó la detención y se abrió de nuevo: English y Claro se conservaron.
Evidencias: `captures/phase3-preferences.png` y `captures/phase3-restored.png`.
La hora cambia entre ambas capturas porque procede del sistema; la batería del
emulador es 100% y los pasos aparecen desconocidos, sin número de sustitución.

En la matriz visual se comprobaron tema oscuro y claro, idioma español e inglés,
aproximadamente 298 y 229 dp, y fontScale 1,3. En los extremos de la lista el texto
se acota y puede llevarse al centro mediante desplazamiento.

Pendientes en dispositivo: bisel físico, TalkBack, escalas de fuente extremas,
apariencia OLED real, sensores y comportamiento de servicios de Samsung.
Las capturas actuales no certifican fidelidad de un píxel.
