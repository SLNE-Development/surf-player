import dev.slne.surf.api.gradle.util.slneReleases

buildscript {
    repositories {
        gradlePluginPortal()
        maven("https://reposilite.slne.dev/public")
    }
    dependencies {
        classpath("dev.slne.surf.api:surf-api-gradle-plugin:+")
        classpath("dev.slne.surf.microservice:surf-microservice-gradle-plugin:2.+")
    }
}

allprojects {
    group = "dev.slne.surf.player"
    version = findProperty("version") as String
}

/** Only the public API is published to reposilite; plugins and the microservice ship as jars/images. */
val publishedProjects = setOf(":surf-player-api")

subprojects {
    afterEvaluate {
        plugins.withType<PublishingPlugin> {
            configure<PublishingExtension> {
                repositories {
                    slneReleases()
                }
            }
        }

        if (path !in publishedProjects) {
            tasks.withType<PublishToMavenRepository>().configureEach {
                enabled = false
            }
        }
    }
}
