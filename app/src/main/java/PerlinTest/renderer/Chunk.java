package PerlinTest.renderer;

import static org.lwjgl.opengl.GL46.*;

public class Chunk {
    public static final int CHUNK_WIDTH_X = 16;
    public static final int CHUNK_WIDTH_Z = 16;
    public static final int CHUNK_HEIGHT = 128;
    public static final int CHUNK_SIZE = CHUNK_WIDTH_X * CHUNK_WIDTH_Z * CHUNK_HEIGHT;

    Level level;
    byte[] blocks = new byte[CHUNK_SIZE];
    Tessellator tessellator;
    int renderList;
    int chunkX, chunkZ;
    public Chunk(Level level, int chunkX, int chunkZ) {
        this.level = level;
        this.chunkX = chunkX;
        this.chunkZ = chunkZ;
        tessellator = new Tessellator();
        renderList = glGenLists(1);
    }

    public int topBlock(int x, int z) {
        for (int y = CHUNK_HEIGHT-1; y >= 0; y--) {
            if (blocks[getIndexOf(x, y, z)] != 0) {
                return y;
            }
        }
        return -1;
    }

    public void rebuild() {
        glNewList(renderList, GL_COMPILE);
        tessellator.clear();
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                for (int y = 0; y < CHUNK_HEIGHT; y++) {
                    int index = getIndexOf(x, y, z);
                    if (blocks[index] != 0) {
                        Block.render(level, tessellator, blocks[index], chunkX * 16 + x, y, chunkZ * 16 + z);
                    }
                }
            }
        }
        tessellator.flush();
        glEndList();
    }

    public void render() {
        glCallList(renderList);
    }

    public static int getIndexOf(int x, int y, int z) {
        return x << 11 | z << 7 | y;
    }
//    public static int getIndexOf(int x, int y, int z) {
//        return x << 12 | z << 8 | y;
//    }

    public byte blockAt(int x, int y, int z) {
        if(x < 0 || x >= CHUNK_WIDTH_X || y < 0 || y >= CHUNK_HEIGHT || z < 0 || z >= CHUNK_WIDTH_Z) {
            return 0;
        }
        return blocks[getIndexOf(x, y, z)];
    }
}
