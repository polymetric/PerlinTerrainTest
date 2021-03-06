package PerlinTest.renderer;

import static java.lang.Math.floorDiv;

public class Level {
    Chunk[] chunks = new Chunk[256*256];

    public static int chunkIndex(int chunkX, int chunkY) {
        return ((chunkX + 128) << 8) | (chunkY + 128);
    }

    public byte blockAt(int x, int y, int z) {
//        System.out.printf("blockat called with xyz %4d %4d %4d\n", x, y, z);
        Chunk chunk = chunks[chunkIndex(floorDiv(x, 16), floorDiv(z, 16))];
        if (chunk == null) {
            return 0;
        } else {
//            System.out.printf("blockat chunk xz %4d %4d\n", chunk.chunkX, chunk.chunkZ);
//            System.out.printf("blockat getting block at %4d %4d %4d\n", x & 15, y, z & 15);
            return chunk.blockAt(x & 15, y, z & 15);
        }
    }

    public void setBlock(int x, int y, int z, int type) {
        Chunk chunk = chunks[chunkIndex(floorDiv(x, 16), floorDiv(z, 16))];
        if (chunk != null) {
            chunk.blocks[Chunk.getIndexOf(x & 15, y & 127, z & 15)] = (byte) type;
        }
    }

    public boolean blockIsAir(int x, int y, int z) {
        return blockAt(x, y, z) == 0;
    }

    public void addChunk(Chunk chunk) {
        chunks[chunkIndex(chunk.chunkX, chunk.chunkZ)] = chunk;
    }

    public void render() {
        for (Chunk chunk : chunks) {
            if (chunk != null) {
                chunk.render();
            }
        }
    }

    public void clear() {
        chunks = new Chunk[256*256];
    }
}
