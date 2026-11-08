# Importación de avatares

El botón inferior abre ACTION_OPEN_DOCUMENT sólo si existe una Activity compatible.
El filtro MIME amplio permite proveedores que clasifican GLB como octet-stream;
la validación binaria decide qué es admisible. Cancelar no modifica el avatar.

Sólo se admite GLB 2 autocontenido, hasta 32 MiB. Un GLTF externo o BIN aislado
no es un modelo válido. Las imágenes externas se rechazan. Se valida el contenedor,
versión y longitud antes de la preparación de texturas y carga nativa gltfio.
El renderizador mantiene el modelo anterior hasta completar una carga válida.

La selección se copia por bloques a files/imports con nombre generado, validación,
fsync y renombrado desde .part. Los archivos parciales se borran si falla la copia.
El acceso durable depende de esta copia privada, no de permisos URI persistentes.
DataStore guarda la ruta sólo después de la aceptación por GPU. La aplicación no
necesita red para cargar presets ni modelos copiados. La cuota de importación es
128 MiB; las sustituciones aceptadas eliminan copias antiguas que ya no se necesitan.

Sin proveedor se muestra una explicación dentro de una vista circular. Alternativa
de desarrollo concreta, sin app móvil auxiliar: instalar el APK debug, autorizar
ADB inalámbrico y ejecutar:

```powershell
node tools/transfer-avatar.mjs DIRECCION_ADB ruta/al/modelo.glb
```

La herramienta usa adb push a un archivo temporal, copia a files/inbox/avatar.part,
comprueba SHA-256 y renombra a avatar.glb. Se retira el temporal de transferencia.
Tocar el botón inferior importa esa copia antes de intentar abrir un
proveedor. Sólo funciona con APK depurable y ADB autorizado; no se presenta como
una transferencia disponible en una versión release. Para distribución a usuarios
sin proveedor sigue pendiente acordar una transferencia apropiada al dispositivo.

La copia de inbox se consume también si es inválida para no bloquear selecciones
posteriores. El archivo original del PC se conserva; sólo se retira la copia temporal.

Las pruebas de documentos usan un ContentProvider exclusivo de debug, sin launcher
ni selector visible. No se añaden permisos de almacenamiento general, acceso a toda
la memoria, servidores de red ni dependencias CDN.
