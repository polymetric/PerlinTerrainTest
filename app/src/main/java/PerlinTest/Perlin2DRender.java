package PerlinTest;

import kaptainwutax.seedutils.mc.ChunkRand;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

import static java.lang.Math.*;
import static PerlinTest.utils.MathUtils.*;

public class Perlin2DRender {
    public static final int WIDTH = 1600;
    public static final int HEIGHT = 864;
    public static final int OCTAVES = 8;
    public static final double SCALE = 256D/(1<<OCTAVES);
    public static final int SOMETHING = 1<<(OCTAVES-1);

    public static void main(String[] args) throws Exception {
        JFrame frame = new JFrame();
        Canvas canvas = new Canvas();
        canvas.setSize(WIDTH, HEIGHT);
        frame.add(canvas);
        frame.pack();
        frame.setVisible(true);

        ChunkRand rand = new ChunkRand(1L);
//        Graphics g = canvas.getGraphics();
        OctaveStats noise = new OctaveStats(rand, OCTAVES);
        double z = 0;

        while (true) {
            BufferedImage tempBuffer = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_BYTE_GRAY);
            Graphics g = tempBuffer.getGraphics();
            for (int x = 0; x < WIDTH; x++) {
                for (int y = 0; y < HEIGHT; y++) {
                    double sample = noise.sample((double) x / SCALE, (double) y / SCALE, z);
                    sample = map(sample, -SOMETHING, SOMETHING, 0, 255);
                    setPixel(g, x, y, (int) sample);
                }
            }
            z += .1;
            canvas.getGraphics().drawImage(tempBuffer, 0, 0, null);
        }
    }

    public static void setPixel(Graphics g, int x, int y, int b) {
        b = clamp(b, 0, 255);
        g.setColor(new Color(b, b, b));
        g.drawLine(x, y, x, y);
    }

    public static int clamp(int n, int min, int max) {
        return min(max(n, min), max);
    }
}
