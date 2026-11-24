import dev.slne.surf.microservice.gradle.plugin.rabbit.RabbitModule

plugins {
    id("dev.slne.surf.api.gradle.minestom")
    id("dev.slne.surf.microservice")
}

surfMinestomApi {
    withCoreMinestom()
    withSurfRedis()
}

surfMicroservice {
    withRabbitModule(RabbitModule.CLIENT_API)
}

minestomPluginFile {
    main = "dev.slne.surf.social.minestom.SocialMinestomPlugin"
    authors = listOf("red", "twisti")

    pluginDependencies {
        register("surf-rabbitmq-minestom")
        register("surf-redis-minestom")
        register("surf-settings-minestom")
        register("surf-playtime-minestom")
    }
}

dependencies {
    api(projects.surfSocialCore.surfSocialCoreClient)

    compileOnly(libs.luckperms.api)
    compileOnly("dev.slne.surf.punish:surf-punish-api-common:+")
    compileOnly("dev.slne.surf.playtime:surf-playtime-api-minestom:+")
}
