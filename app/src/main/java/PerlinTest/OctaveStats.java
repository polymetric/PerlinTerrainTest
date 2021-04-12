package PerlinTest;


import kaptainwutax.noiseutils.noise.NoiseSampler;
import kaptainwutax.noiseutils.perlin.PerlinNoiseSampler;
import kaptainwutax.seedutils.lcg.rand.JRand;
import kaptainwutax.seedutils.mc.ChunkRand;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class OctaveStats implements NoiseSampler {
    public static void main(String[] args) {
        for (int octave = 1; octave < 9; octave++) {
            ChunkRand rand = new ChunkRand(1L);
            OctaveStats octaveStats = new OctaveStats(rand, octave);
            System.out.println(octaveStats.sample(0, 0, 0));
        }
    }

    private final PerlinNoiseSampler[] octaveSamplers;

    public final double lacunarity;
    public final double persistence;

    public OctaveStats(JRand random, int octaveCount) {
        this.octaveSamplers = new PerlinNoiseSampler[octaveCount];
        for (int i = 0; i < octaveCount; i++) {
            this.octaveSamplers[i] = new PerlinNoiseSampler(random);
        }
        this.lacunarity = 1.0;
        this.persistence = 1.0;
    }

    public OctaveStats(ChunkRand rand, IntStream octaves) {
        this(rand, octaves.boxed().collect(Collectors.toList()));
    }

    public OctaveStats(ChunkRand rand, List<Integer> octaves) {
        octaves = octaves.stream().sorted(Integer::compareTo).collect(Collectors.toList());

        if(octaves.isEmpty()) {
            throw new IllegalArgumentException("Need some octaves!");
        }

        int start = -octaves.get(0);
        int end = octaves.get(octaves.size() - 1);
        int length = start + end + 1;

        if(length < 1) {
            throw new IllegalArgumentException("Total number of octaves needs to be >= 1");
        }

        PerlinNoiseSampler perlin = new PerlinNoiseSampler(rand);

        this.octaveSamplers = new PerlinNoiseSampler[length];

        if(end >= 0 && end < length && octaves.contains(0)) {
            this.octaveSamplers[end] = perlin;
        }

        for(int idx = end + 1; idx < length; ++idx) {
            if(idx >= 0 && octaves.contains(end - idx)) {
                this.octaveSamplers[idx] = new PerlinNoiseSampler(rand);
            } else {
                rand.advance(SKIP_262);
            }
        }

        if(end > 0) {
            long noiseSeed = (long)(perlin.sample(0.0D, 0.0D, 0.0D, 0.0D, 0.0D) * 9.223372036854776E18D);
            rand.setSeed(noiseSeed);
            for(int index = end - 1; index >= 0; --index) {
                if(index < length && octaves.contains(end - index)) {
                    this.octaveSamplers[index] = new PerlinNoiseSampler(rand);
                } else {
                    rand.advance(SKIP_262);
                }
            }
        }

        this.persistence = Math.pow(2.0D, end);
        this.lacunarity = 1.0D / (Math.pow(2.0D, length) - 1.0D);
    }

    public double sample(double x, double y, double z) {
        return this.sample(x, y, z, 0.0D, 0.0D, false);
    }

    public double sample(double x, double y, double z, double yAmplification, double minY, boolean useDefaultY) {
        double noise = 0.0D;
        double scale = this.persistence;
        double weight = this.lacunarity;

        for(PerlinNoiseSampler sampler: this.octaveSamplers) {
            if(sampler != null){
                noise += 1 * weight;
            }
            scale /= 2.0D;
            weight *= 2.0D;
        }

        return noise;
    }

    public PerlinNoiseSampler getOctave(int octave) {
        return this.octaveSamplers[octave];
    }

    @Override
    public double sample(double x, double y, double yAmplification, double minY) {
        return this.sample(x, y, 0.0D, yAmplification, minY, false);
    }
}