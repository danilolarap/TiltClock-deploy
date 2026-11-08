# Fuentes empaquetadas

Inter y Space Grotesk se descargaron de google/fonts, directorios ofl/inter y
ofl/spacegrotesk, junto con sus archivos OFL.txt. Los binarios y las licencias
quedan fijados por este repositorio; no se descargan en ejecución.

- https://github.com/google/fonts/tree/main/ofl/inter
- https://github.com/google/fonts/tree/main/ofl/spacegrotesk

Se usan ejes variables explícitos wght=400/600/700 para Inter y wght=700 para
Space Grotesk. La hora usa números tabulares. Los iconos se dibujan con Canvas.
No dependen de Font Awesome ni de una fuente de iconos del dispositivo.

Compose Wear Foundation 1.5.6 está fijado y resuelto por Gradle; aporta la
cabecera curva de ajustes. Se conserva Compose Foundation 1.11.2 en el resto.
Fuente oficial: https://developer.android.com/jetpack/androidx/releases/wear-compose
