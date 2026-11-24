import dev.slne.surf.api.gradle.util.registerSoft

plugins {
    id("dev.slne.surf.api.gradle.paper-plugin")
}

surfPaperPluginApi {
    mainClass("dev.slne.surf.minimapcompanion.paper.PaperMain")
    authors.add("Keviro")
    foliaSupported(true)
    generateLibraryLoader(false)

    serverDependencies {
        registerSoft("surf-friends-paper")
        registerSoft("surf-clan-paper")
        registerSoft("surf-vanish-paper")
    }
}

dependencies {
    api(projects.surfMinimapCompanionCore)
    compileOnly("dev.slne.surf.friends:surf-friends-api:+")
    compileOnly("dev.slne.surf.clan:surf-clan-api:+")
    compileOnly("dev.slne.surf.vanish:surf-vanish-api:+")
}
