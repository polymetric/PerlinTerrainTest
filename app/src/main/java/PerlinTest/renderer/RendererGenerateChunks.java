package PerlinTest.renderer;

import PerlinTest.CustomTerrainRenderer;
import PerlinTest.OctaveStats;
import kaptainwutax.noiseutils.perlin.OctavePerlinNoiseSampler;
import kaptainwutax.seedutils.mc.ChunkRand;
import org.joml.Vector3f;

import java.util.Random;

import static java.lang.Math.*;
import static PerlinTest.utils.MathUtils.*;

public class RendererGenerateChunks {
    public static final double xzScale = 1;
    public static final double yScale = 1;
    public static final double threshScale = 2;
    public static final double thresOffset = 64;
    public static final double noiseAmplitude = 1<<8;
    public static final int octaves = 8;

    public static void genChunksForRenderer(Level level, long seed, int radius, int centerX, int centerZ) {
        // GENERATE BLOCKS
        int chunkCount = 0;
        long timeStart = System.nanoTime();
        for (int chunkX = centerX-radius; chunkX <= centerX+radius; chunkX++) {
            for (int chunkZ = centerZ-radius; chunkZ <= centerZ+radius; chunkZ++) {
                Chunk chunk = new Chunk(level, chunkX, chunkZ);
                level.addChunk(chunk);
                byte[] blocks = new byte[Chunk.CHUNK_SIZE];
                chunk.blocks = blocks;
                double[] noise = fillChunkNoise(seed, chunkX, chunkZ);
                for (int x = 0; x < 16; x++) {
                    for (int z = 0; z < 16; z++) {
                        for (int y = 0; y < Chunk.CHUNK_HEIGHT; y++) {
                            if (noise[Chunk.getIndexOf(x, y, z)] > (y-thresOffset)*threshScale) {
                                blocks[Chunk.getIndexOf(x, y, z)] = 1;
                            }
                            // base terrain
//                            // place water
//                            if (y <= 64 && blocks[Chunk.getIndexOf(x, y, z)] == 0) {
//                                blocks[Chunk.getIndexOf(x, y, z)] = 9;
//                            }
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

    public static double[] fillNoiseColumn(long seed, int x, int z) {
        double[] column = new double[Chunk.CHUNK_HEIGHT/8+1];
        ChunkRand rand = new ChunkRand(seed, false);
        OctavePerlinNoiseSampler noise = new OctavePerlinNoiseSampler(rand, octaves);
        for (int y = 0; y < column.length; y++) {
            column[y] = noise.sample(x/xzScale, (y*8)/yScale, z/xzScale) / (1 << octaves) * noiseAmplitude;
        }
        return column;
    }

    public static double[] fillChunkNoise(long seed, int chunkX, int chunkZ) {
        double[] noise = new double[Chunk.CHUNK_SIZE];

        double[][][] columns = new double[5][5][Chunk.CHUNK_HEIGHT/8+1];
        double[][][] lerpedColumns = new double[5][5][Chunk.CHUNK_HEIGHT+1];
        for (int x = 0; x < 5; x++) {
            for (int z = 0; z < 5; z++) {
                columns[x][z] = fillNoiseColumn(seed, (chunkX * 16 + (x * 4)), (chunkZ * 16 + (z * 4)));
                for (int y = 0; y < 128; y++) {
                    lerpedColumns[x][z][y] = map(y&7, 0, 8, columns[x][z][y/8], columns[x][z][y/8+1]);
                }
            }
        }
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                int columnX = x/4;
                int columnZ = z/4;
                double[] column00 = lerpedColumns[columnX  ][columnZ  ];
                double[] column01 = lerpedColumns[columnX  ][columnZ+1];
                double[] column10 = lerpedColumns[columnX+1][columnZ  ];
                double[] column11 = lerpedColumns[columnX+1][columnZ+1];

                for (int y = 0; y < Chunk.CHUNK_HEIGHT; y++) {
                    noise[Chunk.getIndexOf(x, y, z)] = map(
                            z&3, 0, 4,
                            map(x&3, 0, 4, column00[y], column10[y]),
                            map(x&3, 0, 4, column01[y], column11[y])
                    );
                }
            }
        }

        return noise;
    }
}
