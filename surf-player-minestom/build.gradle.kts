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
        register("surf-settings-minestom")
        register("surf-playtime-minestom")
    }
}

dependencies {
    api(projects.surfPlayerCore.surfPlayerCoreClient)

    compileOnly(libs.luckperms.api)
    compileOnly("dev.slne.surf.playtime:surf-playtime-api-minestom:+")
}
