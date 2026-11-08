# Referencia geométrica

Viewport web 390 × 844 CSS px, DPR 1. Marco exterior 340 px; pantalla con borde
300 px; canvas útil 298 px. Las dimensiones se escalan respecto de 298.

| Elemento | Referencia implementada |
|---|---|
| Inicio cápsula superior | 24/298 del diámetro |
| Hora | 30 × diámetro/298 sp, Space Grotesk 700 |
| Anillo | radio 40%, trazos 1,25% y 1,5%, inicio −90° |
| Botón inferior | ancho 140/298, recorte elíptico 90% × 100% |
| Orden | fondo, escena, anillo, información, controles, ajustes |
| Filas de ajustes | 64 dp × fontScale, margen seguro 8 dp |

Las zonas de controles son de 48 dp; el dibujo permanece menor. Por ello la
separación vertical se amplía para evitar solapamiento de zonas táctiles.
La aproximación del cristal usa alfa, borde y sombra; no desenfoca la escena.
Las diferencias de blur, sombras, rasterización y materiales siguen pendientes
de ajuste fino. No se afirma una desviación de un píxel ni pixel perfect.
