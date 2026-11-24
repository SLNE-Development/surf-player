plugins {
    id("dev.slne.surf.api.gradle.velocity")
}

surfVelocityApi {
    withCoreVelocity()
    withSurfRedis()
}

velocityPluginFile {
    main = "dev.slne.surf.player.velocity.PlayerVelocityPlugin"
    authors = listOf("Ammo", "red", "twisti")
}

dependencies {
    api(projects.surfPlayerCore.surfPlayerCoreClient)
}
