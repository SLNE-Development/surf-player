plugins {
    id("dev.slne.surf.api.gradle.paper-plugin")
}

surfPaperPluginApi {
    mainClass("dev.slne.surf.minimapcompanion.paper.PaperMain")
    authors.add("Keviro")
    foliaSupported(true)
    generateLibraryLoader(false)
}

dependencies {
    api(projects.surfMinimapCompanionCore)
}
