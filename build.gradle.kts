plugins {
    // Мы оставляем только alias, так как это правильный современный подход
    // Версия будет подтягиваться из файла gradle/libs.versions.toml
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.google.services) apply false
}

tasks.register<Delete>("clean") {
    delete(rootProject.layout.buildDirectory)
}