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

    // Kotlin and Caffeine 3 are provided at runtime by surf-api; shading twitch4j's older copies
    // (Kotlin via okhttp, Caffeine 2 via its cache provider) would clash with them.
    api(libs.twitch4j) {
        exclude(group = "org.jetbrains.kotlin")
        exclude(group = "com.github.ben-manes.caffeine")
        exclude(group = "io.github.xanthic.cache", module = "cache-provider-caffeine")
    }
    implementation(libs.xanthic.cache.caffeine3) {
        exclude(group = "com.github.ben-manes.caffeine")
    }
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
