plugins {
    id("dev.slne.surf.api.gradle.minestom")
}

minestomPluginFile {
    main = "dev.slne.surf.minimapcompanion.minestom.MinimapCompanionMinestomPlugin"
    authors = listOf("Keviro")
}

dependencies {
    api(projects.surfMinimapCompanionCore)
}
