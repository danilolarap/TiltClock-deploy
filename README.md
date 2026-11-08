# TiltClock 3D para Samsung Galaxy Watch

Proyecto Android independiente, Kotlin y Gradle Kotlin DSL. **Entrega de validación: fase 5**.
La validación disponible en emulador está documentada; la aceptación física Samsung y
la fidelidad geométrica de un píxel siguen pendientes. No es una certificación de producto final.
Motor nativo Filament/gltfio 1.77.3, átomo procedural y cinco presets GLB locales;
rotación por arrastre y orientación fusionada, zoom persistente y primer clip animado.
Hora y batería proceden del sistema; pasos diarios de Health Services si el dispositivo
los ofrece y hay permiso, con estados de desconocido, permiso y no disponible.
La pantalla usa fuentes locales, cápsulas, anillo y ajustes circulares con tema e idioma persistentes.
Importación GLB autocontenida mediante documentos y copia privada; alternativa ADB
de desarrollo para relojes sin proveedor. La validación física permanece pendiente.
Es una aplicación Wear OS circular, no una esfera seleccionable del sistema.

- [Cinco fases y criterios de aceptación](docs/PLAN.md)
- [Resultados finales y límites](docs/FASE5-RESULTADOS.md)
- [Instalación en Galaxy Watch](docs/FASE5-INSTALACION.md)
- [Superposición visual](docs/phase5-overlay.html)
- [Matriz de validación](docs/phase5-matrix.json)
- [Pruebas físicas pendientes](docs/FASE5-SAMSUNG.md)
- [Hashes, firma y resultados automatizados](docs/phase5-verification.json)
- [Resultados de fase 4](docs/FASE4-RESULTADOS.md)
- [Procedencia de mediciones](docs/FASE4-DATOS.md)
- [Importar y transferir GLB](docs/FASE4-IMPORTACION.md)
- [Alcance de fase 3](docs/FASE3-ALCANCE.md)
- [Resultados de fase 3](docs/FASE3-RESULTADOS.md)
- [Auditoría y decisiones técnicas](docs/AUDITORIA.md)
- [Resultados y limitaciones de verificación](docs/VERIFICACION.md)
- [Procedencia y licencias](reference/NOTICE.md)
- [Revisión del prompt](docs/REVISION-PROMPT.md)
- [Arquitectura del motor](docs/FASE2-ARQUITECTURA.md)
- [Verificación de fase 2](docs/FASE2-RESULTADOS.md)
- [Pruebas pendientes en Samsung](docs/FASE2-PRUEBAS-SAMSUNG.md)

## Estado de la entrega

Versión `0.5.0-validation` (versionCode 5). La ronda de validación disponible de las
cinco fases está entregada; siguen abiertos los criterios físicos y visuales indicados abajo.

| Comprobación ejecutada | Resultado |
|---|---|
| Pruebas unitarias | 35 aprobadas, 0 fallos |
| Lint debug | 0 errores, 18 avisos |
| Seis avatares nativos | Carga y renderizado aprobados en emulador |
| Importación GLB | Copia, rechazo de inválidos, aceptación GPU y restauración sin red aprobados |
| APK debug | Compilado y firmado con esquema v2 |
| APK release | Compilado sin componentes de prueba; sin firmar |
| Original web | 20 archivos auditados sin cambios |

Entorno de pruebas: emulador de teléfono Android API 37 con host circular exclusivo
de debug. Se generaron capturas en aproximadamente 298 y 229 dp, ambos temas,
español/inglés y letra ampliada. Esto no equivale a una prueba en Wear OS o Samsung físico.
Los datos fijos de las capturas existen únicamente en herramientas de depuración.

## Abrir y compilar

Abrir esta carpeta en Android Studio, no la carpeta original TiltClock.
Configuración fijada: SDK 37, Build Tools 36.0.0 y Gradle 9.6. El entorno verificado
utiliza el JBR de Android Studio; los scripts contienen rutas locales que deben adaptarse
en otro equipo. El nivel de lenguaje Java del proyecto es 17.
`local.properties` contiene solo la ruta del SDK de esta máquina y no se versiona.

En este entorno, el script usa Gradle ya instalado, caché de dependencias de solo lectura
y directorios de trabajo dentro del proyecto:

```powershell
powershell -ExecutionPolicy Bypass -File tools/build-phase5.ps1
node tools/audit-reference.mjs --verify
```

Para otro equipo con red, crear la clave de depuración local con keytool (ver script),
configurar `local.properties` y ejecutar:

```powershell
.\gradlew.bat --gradle-user-home .gradle-user-home :app:assembleDebug :app:assembleRelease :app:testDebugUnitTest :app:lintDebug
```

El modo offline es una adaptación al entorno actual; no se desactiva Lint en la configuración.
Para ejecutarlo con caché completa: `tools/build-phase5.ps1 -Offline`.

- APK instalable de pruebas: `app/build/outputs/apk/debug/app-debug.apk`.
- Release sin firmar: `app/build/outputs/apk/release/app-release-unsigned.apk`.

Los APK son resultados locales de compilación y no se incluyen en Git. El release
requiere una clave de firma propia antes de instalarlo o distribuirlo.

## Repetir la verificación

Con un emulador de pruebas ya iniciado, adaptar el identificador del dispositivo:

```powershell
powershell -ExecutionPolicy Bypass -File tools/test-phase5.ps1 -Serial emulator-5580
powershell -ExecutionPolicy Bypass -File tools/verify-phase5.ps1
```

La matriz instala el APK debug, ejecuta las pruebas nativas e importación, cambia
temporalmente red, resolución, densidad y tamaño de letra del emulador y genera
evidencias en `docs/phase5-*` y `docs/captures/phase5-*`. Usar una instancia destinada
a pruebas. `verify-phase5.ps1` necesita esos resultados y las compilaciones previas.
Abrir `docs/phase5-overlay.html` localmente para comparar las capturas superpuestas;
GitHub muestra su código, no ejecuta esa herramienta.

## Instalar en Galaxy Watch con Wear OS

Esta compilación incluye la interfaz, el motor y las integraciones de las fases 1–4,
con las comprobaciones disponibles de fase 5. La validación física sigue pendiente.
Activar Opciones de desarrollador y Depuración inalámbrica en el reloj. Con reloj y PC
en la misma red, abrir el diálogo de emparejamiento y usar sus direcciones y puertos reales:

```powershell
adb pair IP_DEL_RELOJ:PUERTO_DE_EMPAREJAMIENTO
adb connect IP_DEL_RELOJ:PUERTO_DE_DEPURACION
adb devices -l
adb -s IP_DEL_RELOJ:PUERTO_DE_DEPURACION install -r app/build/outputs/apk/debug/app-debug.apk
adb -s IP_DEL_RELOJ:PUERTO_DE_DEPURACION shell am start -n com.tiltclock.wear/.MainActivity
adb -s IP_DEL_RELOJ:PUERTO_DE_DEPURACION logcat -s TiltClockHealth TiltClockMetrics AndroidRuntime
```

Los dos puertos pueden ser distintos y cambiar. También se puede usar Pair Devices Using Wi-Fi
desde Android Studio. Solo se admite geometría circular y modo reloj. No se fuerza pantalla encendida.
No se ha probado esta instalación en un Samsung físico en esta sesión.

## Datos e importación

Los pasos proceden de `Health Services STEPS_DAILY` cuando el reloj ofrece esa
capacidad y se concede actividad física. Los totales sustituyen al anterior; las
lecturas antiguas o de otro día se retiran. Sin datos se muestra un estado de
desconocido, permiso o no disponible. La meta de 10.000 es propia de TiltClock;
no se garantiza igualdad con Samsung Health.

El botón inferior acepta GLB 2 autocontenido de hasta 32 MiB mediante un selector
compatible y conserva una copia privada. Si falla, mantiene el avatar anterior.
Para APK debug sin proveedor existe la transferencia ADB documentada en
[Importación](docs/FASE4-IMPORTACION.md). La alternativa de transferencia para una
distribución release sin proveedor aún está pendiente.

## Pendientes para la aceptación final

Validar en Samsung físico sensores y arrastre combinados, pasos diarios, permisos,
batería, bisel, ambiente, autonomía y rendimiento por modelo. No se han demostrado
30 FPS estables en el reloj. Persisten diferencias visuales de geometría, tipografía,
blur e iluminación; el objetivo de un píxel no está cumplido.

Las fuentes Inter y Space Grotesk ya están empaquetadas con sus licencias, y existen
capturas y superposición comparativa. Continuar desde la matriz de
[pruebas Samsung](docs/FASE5-SAMSUNG.md) y los [resultados finales](docs/FASE5-RESULTADOS.md).
El proyecto web original permanece de sólo lectura.
