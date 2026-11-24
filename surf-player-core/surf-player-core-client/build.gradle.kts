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
    api(projects.surfPlayerCore.surfPlayerCoreCommon)

    compileOnly(libs.luckperms.api)
    compileOnly(libs.mini.placeholders)
    compileOnly("dev.slne.surf.punish:surf-punish-api-common:+")
    compileOnly(libs.surf.transaction.api)

    api(libs.twitch4j)
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
