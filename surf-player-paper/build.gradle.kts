import dev.slne.surf.api.gradle.util.registerRequired
import dev.slne.surf.api.gradle.util.registerSoft
import dev.slne.surf.microservice.gradle.plugin.rabbit.RabbitModule

plugins {
    id("dev.slne.surf.api.gradle.paper-plugin")
    id("dev.slne.surf.microservice")
}

surfMicroservice {
    withRabbitModule(RabbitModule.CLIENT_API)
}

surfPaperPluginApi {
    mainClass("dev.slne.surf.player.paper.PlayerPaperPlugin")
    foliaSupported(true)
    generateLibraryLoader(false)

    withCorePaper()
    withSurfRedis()

    bootstrapDependencies {
        registerRequired("surf-rabbitmq-paper")
    }

    serverDependencies {
        registerRequired("LuckPerms")
        registerRequired("MiniPlaceholders")
        registerRequired("surf-rabbitmq-paper")
        registerRequired("surf-punish-paper")
        registerRequired("surf-settings-paper")
        registerSoft("surf-playtime-paper")
        registerSoft("surf-vanish-paper")
    }

    authors.addAll(listOf("red", "twisti", "Ammo"))
}

dependencies {
    api(projects.surfPlayerCore.surfPlayerCoreClient)

    compileOnly(libs.mini.placeholders)
    compileOnly(libs.luckperms.api)

    compileOnly("dev.slne.surf.vanish:surf-vanish-api:+")
    compileOnly("dev.slne.surf.playtime:surf-playtime-api-paper:+")
}
