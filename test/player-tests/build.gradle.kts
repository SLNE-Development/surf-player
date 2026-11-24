plugins {
    kotlin("jvm") version "2.4.21"
    application
}

group = "dev.slne.surf.player"
version = "1.0.0"

kotlin {
    jvmToolchain(25)
}

dependencies {
    // Headless Minecraft client. 26.3-x speaks protocol 777 (Minecraft 26.3), the version of the
    // test network (Paper 26.3, surf-minestom-lobby 26.3, Velocity 4.3.1).
    implementation("org.geysermc.mcprotocollib:protocol:26.3-4")
    implementation("net.kyori:adventure-text-serializer-plain:5.2.0")
    implementation("com.google.code.gson:gson:2.13.2")
    implementation("org.slf4j:slf4j-simple:2.0.17")
}

application {
    mainClass = "dev.slne.surf.player.tests.MainKt"
    applicationName = "player-tests"
}
