# Instalar y comprobar en Samsung Galaxy Watch

Usar un Galaxy Watch circular con Wear OS y activar Opciones de desarrollador,
Depuración inalámbrica y emparejamiento. Los puertos de conexión y emparejamiento
son diferentes y cambian; utilizar los que muestra el reloj.

```powershell
adb pair IP_RELOJ:PUERTO_EMPAREJAR
adb connect IP_RELOJ:PUERTO_DEPURAR
adb -s DIRECCION install -r app/build/outputs/apk/debug/app-debug.apk
adb -s DIRECCION shell am start -n com.tiltclock.wear/.MainActivity
adb -s DIRECCION logcat -s TiltClockHealth TiltClockMetrics AndroidRuntime
```

Conceder actividad física al tocar el componente de pasos cuando lo solicite.
Esperar el total del proveedor; si no está disponible no debe aparecer cero.
La meta de 10.000 es propia de TiltClock. Comparar con Samsung Health sin asumir igualdad.
Importar desde el selector del reloj o seguir FASE4-IMPORTACION.md para ADB debug.

Cerrar ajustes con Atrás, comprobar zoom y animación, inclinar el reloj y arrastrar.
Apagar pantalla, volver a abrir y verificar que sólo se reanuden sensores y escena
en primer plano interactivo. No activar mantener pantalla encendida.

APK debug firmado para pruebas, no distribución final. Release unsigned se genera
para comprobar separación de componentes; requiere una clave privada propia para instalar.
Es una aplicación interactiva, no una esfera seleccionable del sistema.
