plugins {
    id("dev.slne.surf.api.gradle.minestom")
}

surfMinestomApi {
    withCoreMinestom()
    withSurfRedis()
}

minestomPluginFile {
    main = "dev.slne.surf.player.minestom.PlayerMinestomPlugin"
    authors = listOf("red", "twisti")

    pluginDependencies {
        register("surf-rabbitmq-minestom")
        register("surf-redis-minestom")
    }
}

dependencies {
    api(projects.surfPlayerCore.surfPlayerCoreClient)

    compileOnly(libs.luckperms.api)
    compileOnly(libs.surf.transaction.api)
}

// Lets Minestom's test environment connect players outside virtual threads (minimap tests).
tasks.test {
    systemProperty("minestom.inside-test", "true")
}
