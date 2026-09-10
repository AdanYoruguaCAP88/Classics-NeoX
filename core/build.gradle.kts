// :core es Kotlin/JVM puro a propósito: nada acá importa un solo símbolo
// de android.*. Eso es lo que permite correr sus tests como tests de JVM
// comunes (rápidos, sin emulador ni Robolectric) - ver los tests en
// src/test y el README del repo, sección "Cómo correr los tests".
plugins {
    kotlin("jvm")
}

kotlin {
    jvmToolchain(17)
}

dependencies {
    testImplementation("junit:junit:4.13.2")
}

tasks.test {
    useJUnit()
    testLogging {
        events("passed", "skipped", "failed")
    }
}
