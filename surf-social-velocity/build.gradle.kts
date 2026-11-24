plugins {
    id("dev.slne.surf.api.gradle.velocity")
}

surfVelocityApi {
    withCoreVelocity()
    withSurfRedis()
}

velocityPluginFile {
    main = "dev.slne.surf.social.velocity.SocialVelocityPlugin"
    authors = listOf("Ammo", "red", "twisti")
}

dependencies {
    api(projects.surfSocialCore.surfSocialCoreClient)
}
