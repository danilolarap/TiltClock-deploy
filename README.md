# TiltClock 3D para Samsung Galaxy Watch

Proyecto Android independiente, Kotlin y Gradle Kotlin DSL. **Entrega incremental: fase 2**.
Motor nativo Filament/gltfio 1.77.3, átomo procedural y cinco presets GLB locales;
rotación por arrastre y orientación fusionada, zoom persistente y primer clip animado.
Hora y batería proceden del sistema; pasos explícitamente no disponibles hasta fase 4.
La pantalla intermedia permite usar el motor; diseño final y ajustes completos son fase 3.
Es una aplicación Wear OS circular, no una esfera seleccionable del sistema.

- [Cinco fases y criterios de aceptación](docs/PLAN.md)
- [Auditoría y decisiones técnicas](docs/AUDITORIA.md)
- [Resultados y limitaciones de verificación](docs/VERIFICACION.md)
- [Procedencia y licencias](reference/NOTICE.md)
- [Revisión del prompt](docs/REVISION-PROMPT.md)
- [Arquitectura del motor](docs/FASE2-ARQUITECTURA.md)
- [Verificación de fase 2](docs/FASE2-RESULTADOS.md)
- [Pruebas pendientes en Samsung](docs/FASE2-PRUEBAS-SAMSUNG.md)

## Abrir y compilar

Abrir esta carpeta en Android Studio, no la carpeta original TiltClock.
Requisitos fijados: SDK 37, Build Tools 36.0.0, JDK mínimo 17 compatible con Gradle 9.6.
`local.properties` contiene solo la ruta del SDK de esta máquina y no se versiona.

En este entorno, el script usa Gradle ya instalado, caché de dependencias de solo lectura
y directorios de trabajo dentro del proyecto:

```powershell
powershell -ExecutionPolicy Bypass -File tools/build-phase2.ps1
node tools/audit-reference.mjs --verify
```

Para otro equipo con red, crear la clave de depuración local con keytool (ver script),
configurar `local.properties` y ejecutar:

```powershell
.\gradlew.bat --gradle-user-home .gradle-user-home :app:assembleDebug :app:testDebugUnitTest :app:lintDebug
```

El modo offline es una adaptación al entorno actual; no se desactiva Lint en la configuración.
Para ejecutarlo con caché completa: `tools/build-phase2.ps1 -Offline`.
APK: `app/build/outputs/apk/debug/app-debug.apk`.

## Instalar en Galaxy Watch con Wear OS

Esta compilación permite comprobar el motor de fase 2; el diseño visual final está pendiente.
Activar Opciones de desarrollador y Depuración inalámbrica en el reloj. Con reloj y PC
en la misma red, abrir el diálogo de emparejamiento y usar sus direcciones y puertos reales:

```powershell
adb pair IP_DEL_RELOJ:PUERTO_DE_EMPAREJAMIENTO
adb connect IP_DEL_RELOJ:PUERTO_DE_DEPURACION
adb devices -l
adb -s IP_DEL_RELOJ:PUERTO_DE_DEPURACION install -r app/build/outputs/apk/debug/app-debug.apk
adb -s IP_DEL_RELOJ:PUERTO_DE_DEPURACION shell am start -n com.tiltclock.wear/.MainActivity
adb -s IP_DEL_RELOJ:PUERTO_DE_DEPURACION logcat -s AndroidRuntime
```

Los dos puertos pueden ser distintos y cambiar. También se puede usar Pair Devices Using Wi-Fi
desde Android Studio. Solo se admite geometría circular y modo reloj. No se fuerza pantalla encendida.
No se ha probado esta instalación en un Samsung físico en esta sesión.

## Retomar

Continuar con la fase 3 de `docs/PLAN.md`, conservando la auditoría y los hashes.
Las capturas web claro/oscuro ya están en `docs/captures`; faltan fuentes Android
empaquetadas, pantalla fiel y comparación 2D. La evaluación física del motor sigue
siendo necesaria antes de consolidar Filament para los modelos de millones de triángulos.
El original permanece de solo lectura durante todas las fases.
