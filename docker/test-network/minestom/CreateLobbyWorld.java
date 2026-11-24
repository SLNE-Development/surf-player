import net.hollowcube.polar.PolarLoader;
import net.hollowcube.polar.PolarWorld;
import net.hollowcube.polar.PolarWriter;
import net.minestom.server.MinecraftServer;
import net.minestom.server.instance.Chunk;
import net.minestom.server.instance.InstanceContainer;
import net.minestom.server.instance.block.Block;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Writes a small flat Polar world for the surf-minestom-lobby test server, which refuses to start
 * without a world named "lobby" in its database. The result is committed as worlds/lobby.polar;
 * regenerate it after a Minecraft data version bump with (from docker/test-network):
 *
 * <pre>
 * docker run --rm -v surf-player-test_minestom-data:/server -v "$PWD/minestom:/work" -w /work \
 *     eclipse-temurin:25-jdk sh -c \
 *     'java -cp "$(find /server/libraries -name "*.jar" ! -name "*-relocation-*" | tr "\n" ":")" \
 *      CreateLobbyWorld.java worlds/lobby.polar'
 * </pre>
 *
 * The libraries come from the minestom volume, so the lobby has to have started once.
 */
public class CreateLobbyWorld {
    private static final int RADIUS = 6;

    public static void main(String[] args) throws Exception {
        Path out = Path.of(args.length > 0 ? args[0] : "lobby.polar");

        MinecraftServer.init();
        InstanceContainer instance = MinecraftServer.getInstanceManager().createInstanceContainer();
        PolarWorld world = new PolarWorld();
        instance.setChunkLoader(new PolarLoader(world));
        // Surface at y=63, so players stand at the lobby's default spawn height y=64.
        instance.setGenerator(unit -> {
            unit.modifier().fillHeight(0, 1, Block.BEDROCK);
            unit.modifier().fillHeight(1, 63, Block.STONE);
            unit.modifier().fillHeight(63, 64, Block.GRASS_BLOCK);
        });

        List<CompletableFuture<Chunk>> chunks = new ArrayList<>();
        for (int x = -RADIUS; x <= RADIUS; x++) {
            for (int z = -RADIUS; z <= RADIUS; z++) {
                chunks.add(instance.loadChunk(x, z));
            }
        }
        CompletableFuture.allOf(chunks.toArray(CompletableFuture[]::new)).join();
        instance.saveChunksToStorage().join();

        Files.write(out, PolarWriter.write(world));
        System.out.println("Wrote " + out.toAbsolutePath() + " (" + Files.size(out) + " bytes)");
        System.exit(0);
    }
}
