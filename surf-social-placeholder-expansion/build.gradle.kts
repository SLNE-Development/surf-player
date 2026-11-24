plugins {
    id("dev.slne.surf.api.gradle.core")
}

dependencies {
    compileOnly(projects.surfSocialApi)
    compileOnly(libs.mini.placeholders)
}
