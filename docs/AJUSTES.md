# Ajustes para la pantalla del reloj

Al tocar el lápiz se muestran directamente **Modelos**, **Tema** e **Idioma**.
El título dejó de ser una fila que parecía un botón sin acción. La pantalla inicial
y las dos opciones de tema e idioma caben sin desplazamiento vertical.

- **Modelos:** miniaturas reales de los seis modelos, nombre, contador y flechas.
  También se puede deslizar horizontalmente o girar la corona/bisel que envíe eventos
  de desplazamiento. Recorrer las miniaturas no cambia el avatar guardado.
  **Usar modelo** vuelve al reloj para cargarlo; se guarda después de que el motor
  lo acepte. **Listo** cierra el selector si se está viendo el modelo actual.
  El avatar importado actual se conserva como una opción adicional, con icono genérico.
- **Tema:** Claro y Oscuro tienen muestras de color y una marca de selección.
  El cambio se aplica inmediatamente y se guarda.
- **Idioma:** Español y English muestran la selección; el cambio se aplica
  inmediatamente y se guarda.
- La flecha superior vuelve a Ajustes; la X cierra el menú. El botón Atrás
  del sistema hace lo mismo. Los controles tienen zonas táctiles de al menos 48 dp.

Las miniaturas `res/drawable-nodpi/avatar_*.png` proceden de los renders nativos
existentes en `docs/phase5-gpu`, generados con `RendererInstrumentation` a partir
de los mismos modelos empaquetados. `tools/prepare-avatar-thumbnails.ps1` recorta
el espacio transparente para que sean legibles en el reloj. Conservan la procedencia y atribuciones de
los modelos indicadas en `reference/NOTICE.md` y `assets/licenses`. El selector
utiliza imágenes locales, sin crear otro motor 3D ni cargar los seis GLB juntos.
Esta mejora se centra en el menú; no modifica la iluminación ni la geometría 3D.

## Verificación

```powershell
powershell -ExecutionPolicy Bypass -File tools/build-phase2.ps1 -Offline -LogPath docs/build-settings.log
node tools/test-settings.mjs emulator-5580
```

El segundo comando requiere un emulador dedicado iniciado y rechaza identificadores
de dispositivos físicos. Instala el APK debug, cambia preferencias mediante la
interfaz y comprueba descubrimiento sin desplazamiento, selección, idioma,
persistencia tras reiniciar y navegación por los modelos con flechas y arrastre.
Genera `docs/settings-verification.json` y `docs/captures/settings-*.png`.
Captura diámetros aproximados de 192, 229 y 298 dp, con letra normal y ampliada
a 1,3 en 192 dp. Restaura resolución, densidad, escala de letra y confirmación de
pantalla completa al terminar. Las preferencias de la app del emulador se cambian
como parte de la prueba. También desactiva temporalmente el lanzador de teléfono,
que falla al reducir su pantalla a 192 dp, y restaura su estado al terminar.

El host debug fuerza configuración circular Wear OS sobre un emulador de teléfono;
no sustituye la comprobación física en Samsung. La corona/bisel y TalkBack requieren
validación en el dispositivo. En este equipo se utiliza `-gpu host -feature -Vulkan`;
el renderizador SwiftShader no pudo compilar el programa FXAA de Filament.

Resultado de esta revisión: 35 pruebas unitarias y 15 comprobaciones de interfaz
aprobadas; Lint con 0 errores y 13 avisos. Se revisaron las capturas de letra ampliada
y se ajustaron los títulos de las tarjetas para que no se abrevien. El APK debug
firmado queda en `app/build/outputs/apk/debug/app-debug.apk`.
