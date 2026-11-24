import dev.slne.surf.microservice.gradle.plugin.rabbit.RabbitModule

plugins {
    id("dev.slne.surf.api.gradle.core")
    id("dev.slne.surf.microservice")
}

surfCoreApi {
    withCoreCommon()
    withSurfRedis()
}

surfMicroservice {
    withRabbitModule(RabbitModule.CLIENT_API)
}

dependencies {
    api(projects.surfSocialCore.surfSocialCoreCommon)

    compileOnly(libs.luckperms.api)
    compileOnly(libs.mini.placeholders)
    compileOnlyApi("dev.slne.surf.settings:surf-settings-api:+")
    compileOnly("dev.slne.surf.punish:surf-punish-api-common:+")
    compileOnly("dev.slne.surf.playtime:surf-playtime-api-common:+")
    compileOnly("dev.slne.surf.content.creator:surf-content-creator-api:+")

    api(libs.openai) {
        exclude(group = "org.apache.httpcomponents.client5")
        exclude(group = "org.apache.httpcomponents.core5")
        exclude(group = "org.jetbrains.kotlin")
    }
}

sourceSets.test {
    compileClasspath += sourceSets.main.get().compileClasspath
    runtimeClasspath += sourceSets.main.get().compileClasspath
}
