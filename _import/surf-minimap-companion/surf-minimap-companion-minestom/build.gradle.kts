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

// Lets Minestom's test environment connect players outside virtual threads.
tasks.test {
    systemProperty("minestom.inside-test", "true")
}
