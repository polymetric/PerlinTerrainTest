package PerlinTest.renderer;

import PerlinTest.CustomTerrainRenderer;
import PerlinTest.OctaveStats;
import kaptainwutax.noiseutils.perlin.OctavePerlinNoiseSampler;
import kaptainwutax.seedutils.mc.ChunkRand;
import org.joml.Vector3f;

import java.util.Random;

import static java.lang.Math.*;

public class RendererGenerateChunks {
    // good terrain
//    public static final double xzScale = 4;
//    public static final double yScale = 16;
//    public static final double threshScale = 1.0D/1;
//    public static final double thresOffset = 128;
//    public static final double noiseAmplitude = 1<<8;
//    public static final int octaves = 8;

    public static final double xzScale = 4;
    public static final double yScale = 16;
    public static final double threshScale = 1.0D/1;
    public static final double thresOffset = 128;
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
                            // place
                            if (y <= 64+3 && blocks[Chunk.getIndexOf(x, y, z)] == 1) {
                                blocks[Chunk.getIndexOf(x, y, z)] = 12;
                            }
                        }
                        for (int y = Chunk.CHUNK_HEIGHT; y >= 0; y--) {
                            if (blocks[Chunk.getIndexOf(x, y, z)] == 1) {
                                blocks[Chunk.getIndexOf(x, y, z)] = 2;
                                for (int y2 = 0; y2 < 3; y2++) {
                                    blocks[Chunk.getIndexOf(x, y-y2-1, z)] = 3;
                                }
                                break;
                            }
                        }
                    }
                }

                long chunkSeed = chunkX * 12345618234812L ^ chunkZ * 99333211234L;
//                System.out.println((noise.sample(chunkX, 0, chunkZ) / (1 << octaves)) * 10 + 3);
                Random r = new Random(chunkSeed);
                outerTree:
                for (int i = 0; i < 5; i++) {

                    int baseX = r.nextInt(16);
                    int baseZ = r.nextInt(16);
                    int baseY = chunk.topBlock(baseX, baseZ)+1;

                    if (baseY < 64) {
                        continue;
                    }

                    int trunkHeight = 4+r.nextInt(3);
                    int topY = baseY + trunkHeight;

                    if (blocks[Chunk.getIndexOf(baseX, baseY-1, baseZ)] != 2) {
                        continue outerTree;
                    }

                    for (int y = baseY + trunkHeight - 3; y <= topY; y++) {
                        int distFromTop = y-topY;
                        int treeRadius = distFromTop/2;
                        for (int x = baseX - treeRadius; x <= baseX + treeRadius; x++) {
                            for (int z = baseZ - treeRadius; z <= baseZ + treeRadius; z++) {
                                if (blocks[Chunk.getIndexOf(x, y, z)] != 0) {
                                    continue outerTree;
                                }
                            }
                        }
                    }

                    CustomTerrainRenderer.pos = new Vector3f(baseX, baseY, baseZ);
                    for (int y = baseY + trunkHeight - 3; y <= topY; y++) {
                        int distFromTop = y-topY;
                        int treeRadius = 1-distFromTop/2;
                        for (int x = baseX - treeRadius; x <= baseX + treeRadius; x++) {
                            for (int z = baseZ - treeRadius; z <= baseZ + treeRadius; z++) {
                                int relX = x - baseX;
                                int relZ = z - baseZ;
                                if (abs(relX) != radius || abs(relZ) != radius || r.nextInt(2) != 0) {
                                    level.setBlock(chunkX * 16 + x, y, chunkZ * 16 + z,18);
                                }
                            }
                        }
                    }
                    for (int y = baseY; y < topY-1; y++) {
                        level.setBlock(chunkX * 16 + baseX, y, chunkZ * 16 + baseZ, 17);
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
