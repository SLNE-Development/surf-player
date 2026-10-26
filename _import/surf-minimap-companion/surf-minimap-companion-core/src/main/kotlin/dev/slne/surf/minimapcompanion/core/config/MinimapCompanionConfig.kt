package dev.slne.surf.minimapcompanion.core.config

import org.spongepowered.configurate.objectmapping.ConfigSerializable
import org.spongepowered.configurate.objectmapping.meta.Comment

@ConfigSerializable
data class MinimapCompanionConfig(
    @param:Comment("Mixed into every computed level ID. Set it only when servers share worlds copied from one template, since copies keep the same world UUID.")
    val serverId: String = "",

    @param:Comment("Fixed level IDs by world key, as Xaero shows it (for example \"minecraft:overworld\"). An entry here replaces the computed ID.")
    val worldIds: Map<String, Int> = emptyMap(),

    @param:Comment("Which other players each player sees on Xaero's maps beyond their view distance. Only players on this server are shown.")
    val tracker: TrackerConfig = TrackerConfig(),

    @param:Comment("Xaero settings this server enforces, as a server with Xaero's mods installed would. Clients cannot change enforced settings.")
    val enforcedOptions: EnforcedOptionsConfig = EnforcedOptionsConfig(),
)
