plugins {
    id("dev.slne.surf.api.gradle.core")
}

dependencies {
    compileOnly(projects.surfPlayerApi)
    compileOnly(libs.mini.placeholders)
}
