package PerlinTest.renderer;

import PerlinTest.CustomTerrainRenderer;
import PerlinTest.OctaveStats;
import kaptainwutax.noiseutils.perlin.OctavePerlinNoiseSampler;
import kaptainwutax.seedutils.mc.ChunkRand;
import org.joml.Vector3f;

import java.util.Random;

import static java.lang.Math.*;

public class RendererGenerateChunks {
    public static final double xzScale = 1;
    public static final double yScale = 1;
    public static final double threshScale = 4;
    public static final double thresOffset = 64;
    public static final double noiseAmplitude = 1<<8;
    public static final int octaves = 8;

    public static void genChunksForRenderer(Level level, long seed, int radius, int centerX, int centerZ) {
        // GENERATE BLOCKS
        int chunkCount = 0;
        long timeStart = System.nanoTime();
        ChunkRand rand = new ChunkRand(seed, false);
        OctavePerlinNoiseSampler noise = new OctavePerlinNoiseSampler(rand, octaves);
        for (int chunkX = centerX-radius; chunkX <= centerX+radius; chunkX++) {
            for (int chunkZ = centerZ-radius; chunkZ <= centerZ+radius; chunkZ++) {
                Chunk chunk = new Chunk(level, chunkX, chunkZ);
                level.addChunk(chunk);
                byte[] blocks = new byte[Chunk.CHUNK_SIZE];
                chunk.blocks = blocks;
                for (int x = 0; x < 16; x++) {
                    for (int z = 0; z < 16; z++) {
                        for (int y = 0; y < Chunk.CHUNK_HEIGHT; y++) {
                            // base terrain
                            if (noise.sample((chunkX * 16 + x)/xzScale, y/yScale, (chunkZ * 16 + z)/xzScale) / (1 << octaves) * noiseAmplitude > (y-thresOffset)*threshScale) {
                                blocks[Chunk.getIndexOf(x, y, z)] = 1;
                            }
                            // place water
                            if (y <= 64 && blocks[Chunk.getIndexOf(x, y, z)] == 0) {
                                blocks[Chunk.getIndexOf(x, y, z)] = 9;
                            }
                        }
                    }
                }

                chunkCount++;
            }
        }
        System.out.printf("generated %4d chunks in %8.3fms\n", chunkCount, (System.nanoTime() - timeStart) / 1e6);
        chunkCount = 0;
        timeStart = System.nanoTime();
        for (int chunkX = centerX-radius; chunkX <= centerX+radius; chunkX++) {
            for (int chunkZ = centerZ-radius; chunkZ <= centerZ+radius; chunkZ++) {
                level.chunks[Level.chunkIndex(chunkX, chunkZ)].rebuild();
                chunkCount++;
            }
        }
        System.out.printf("rendered %5d chunks in %8.3fms\n", chunkCount, (System.nanoTime() - timeStart) / 1e6);
    }
}
