import dev.slne.surf.microservice.gradle.plugin.rabbit.RabbitModule

plugins {
    id("dev.slne.surf.api.gradle.standalone")
    id("dev.slne.surf.microservice")
}

surfStandaloneApi {
    withSurfDatabaseR2dbc("2.3.5", "dev.slne.surf.player.libs.database")
    withCoreCommon()
    withSurfRedis()
}

surfMicroservice {
    withMicroserviceApi()
    withRabbitModule(RabbitModule.SERVER_API, true)
}

dependencies {
    api(projects.surfPlayerCore.surfPlayerCoreCommon)
}

// The microservice ships as a Docker image; its fat jar is too large for reposilite and nothing
// depends on it as a Maven artifact.
tasks.withType<PublishToMavenRepository>().configureEach {
    enabled = false
}

sourceSets.test {
    compileClasspath += sourceSets.main.get().compileClasspath
    runtimeClasspath += sourceSets.main.get().compileClasspath
}
