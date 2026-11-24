plugins {
    id("dev.slne.surf.api.gradle.core")
}

surfCoreApi {
    withCoreCommon()
    withSurfRedis()
}

dependencies {
    api("dev.slne.surf.bitmap:surf-bitmap-provider-common:+")
}
