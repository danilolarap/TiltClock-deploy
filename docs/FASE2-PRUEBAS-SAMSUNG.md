# Comprobación física pendiente

Instalar `app/build/outputs/apk/debug/app-debug.apk` mediante depuración inalámbrica
en Galaxy Watch con Wear OS circular. Esta es una aplicación, no una esfera del sistema.
La pantalla intermedia de fase 2 no certifica fidelidad visual final.

1. Abrir y comprobar holo. No debe girar autónomamente. Lápiz permite elegir un preset.
2. Seleccionar sucesivamente Fox, Pájaro, Mapache, Moneda y Riot, esperando cada carga.
   Verificar texturas, encuadre, escala, iluminación y ausencia de cierres por memoria.
3. Inclinar ambos ejes del reloj y arrastrar el modelo sobre ambos ejes. Durante el
   arrastre el sensor no debe luchar; soltar con el reloj inclinado y comprobar continuidad.
4. Pausar Fox y repetir ambas rotaciones. Comprobar primer clip y reanudar sin saltos.
5. Tocar y sostener +/−; límites .3 y 3; cancelar saliendo del botón. Reiniciar la app
   y comprobar avatar y escala persistidos. No activar pinch.
6. Abrir el selector, apagar pantalla y enviar app a segundo plano. Comprobar en Logcat
   que se detienen frames; al regresar no se recupera tiempo de animación en segundo plano.
7. Repetir en modo avión. La app no descarga presets ni solicita permiso Internet.
8. Capturar `adb logcat -s TiltClockMetrics`, `adb shell dumpsys meminfo com.tiltclock.wear`
   y perfil GPU compatible con ese reloj. Separar carga, memoria CPU/GPU y frames presentados.
   La tasa de envío del log no es una medición de presentación real.

Probar dos diámetros circulares, escala del sistema ampliada, permisos y ausencia de
sensor. No hay edición de pasos, selector de forma ni opciones de otros relojes.
Los pasos siguen no disponibles hasta Health Services en fase 4.

## Prueba técnica aislada en un dispositivo Android

El APK debug incluye una Instrumentation sin interfaz; release la excluye:

```powershell
adb shell am instrument -w com.tiltclock.wear/.verification.RendererInstrumentation
adb exec-out run-as com.tiltclock.wear cat files/phase2-report.json
```

Carga realmente los seis avatares, usa cámara FOV 50 y pose fija, renderiza a un
swapchain offscreen 400 px, comprueba píxeles y registra tiempos/PSS. Comprueba
error de carga conservando el anterior. Las imágenes se guardan en files/phase2.
Su GPU por software y el tamaño de proceso no predicen el comportamiento de un reloj.
Comprobar la orientación visual de las imágenes exportadas antes de cualquier
comparación; no aplicar una inversión vertical automática por asumir la convención de OpenGL.
