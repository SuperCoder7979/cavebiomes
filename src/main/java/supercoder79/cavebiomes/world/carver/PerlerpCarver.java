package supercoder79.cavebiomes.world.carver;

import com.mojang.serialization.Codec;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.noise.OctavePerlinNoiseSampler;
import net.minecraft.util.math.noise.PerlinNoiseSampler;
import net.minecraft.world.Heightmap;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.chunk.ProtoChunk;
import net.minecraft.world.gen.ChunkRandom;
import net.minecraft.world.gen.carver.CarverContext;
import net.minecraft.world.gen.chunk.AquiferSampler;
import supercoder79.cavebiomes.mixin.ProtoChunkAccessor;

import java.util.BitSet;
import java.util.Random;
import java.util.function.Function;
import java.util.stream.IntStream;

public class PerlerpCarver extends BaseCarver {
    private long seed;
    private OctavePerlinNoiseSampler caveNoise;
    private OctavePerlinNoiseSampler offsetNoise;
    private OctavePerlinNoiseSampler scaleNoise;

    public PerlerpCarver(Codec<SimpleCarverConfig> configCodec) {
        super(configCodec);
    }

    @Override
    public boolean carve(CarverContext context, SimpleCarverConfig config, Chunk chunk, Function<BlockPos, Biome> posToBiome, Random random, AquiferSampler aquiferSampler, ChunkPos pos, BitSet carvingMask) {
        if (!(chunk.getPos().equals(pos))) {
            return false;
        }

        Heightmap floor = chunk.getHeightmap(Heightmap.Type.OCEAN_FLOOR_WG);

        // Get all heights in this chunk for thresholding
        int[] heights = new int[256];
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                heights[(x * 16) + z] = floor.get(x, z);
            }
        }

        long seed = ((ServerWorld)((ProtoChunkAccessor)(chunk)).getWorld()).getSeed();

        if (this.caveNoise == null || this.seed == seed) {
            ChunkRandom chunkRandom = new ChunkRandom(seed);
            this.caveNoise = new OctavePerlinNoiseSampler(chunkRandom, IntStream.rangeClosed(-5, 0));
            this.offsetNoise = new OctavePerlinNoiseSampler(chunkRandom, IntStream.rangeClosed(-2, 0));
            this.scaleNoise = new OctavePerlinNoiseSampler(chunkRandom, IntStream.rangeClosed(-0, 0));
            this.seed = seed;
        }

        int chunkStartX = pos.x << 4;
        int chunkStartZ = pos.z << 4;

        double[][][] noiseData = new double[2][5][9];

        for(int noiseZ = 0; noiseZ < 5; ++noiseZ) {
            noiseData[0][noiseZ] = new double[9];
            sampleNoiseColumn(noiseData[0][noiseZ], pos.x * 4, pos.z * 4 + noiseZ, this.caveNoise, this.offsetNoise, this.scaleNoise);
            noiseData[1][noiseZ] = new double[9];
        }

        // [0, 4] -> x noise chunks
        for(int noiseX = 0; noiseX < 4; ++noiseX) {
            // Initialize noise data on the x1 column
            int noiseZ;
            for (noiseZ = 0; noiseZ < 5; ++noiseZ) {
                sampleNoiseColumn(noiseData[1][noiseZ], pos.x * 4 + noiseX + 1, pos.z * 4 + noiseZ, this.caveNoise, this.offsetNoise, this.scaleNoise);
            }

            // [0, 4] -> z noise chunks
            for (noiseZ = 0; noiseZ < 4; ++noiseZ) {
                ChunkSection section = ((ProtoChunk)chunk).getSection(15);
//                section.lock();

                // [0, 32] -> y noise chunks
                for (int noiseY = 7; noiseY >= 0; --noiseY) {
                    // Lower samples
                    double x0z0y0 = noiseData[0][noiseZ][noiseY];
                    double x0z1y0 = noiseData[0][noiseZ + 1][noiseY];
                    double x1z0y0 = noiseData[1][noiseZ][noiseY];
                    double x1z1y0 = noiseData[1][noiseZ + 1][noiseY];
                    // Upper samples
                    double x0z0y1 = noiseData[0][noiseZ][noiseY + 1];
                    double x0z1y1 = noiseData[0][noiseZ + 1][noiseY + 1];
                    double x1z0y1 = noiseData[1][noiseZ][noiseY + 1];
                    double x1z1y1 = noiseData[1][noiseZ + 1][noiseY + 1];

                    // [0, 8] -> y noise pieces
                    for (int pieceY = 7; pieceY >= 0; --pieceY) {
                        int realY = noiseY * 8 + pieceY;
                        int localY = realY & 15;
                        int sectionY = realY >> 4;
                        if (section.getYOffset() >> 4 != sectionY) {
//                            section.unlock();
                            section = ((ProtoChunk)chunk).getSection(sectionY);
//                            section.lock();
                        }

                        // progress within loop
                        double yLerp = (double) pieceY / 8.0;

                        // Interpolate noise data based on y progress
                        double x0z0 = MathHelper.lerp(yLerp, x0z0y0, x0z0y1);
                        double x1z0 = MathHelper.lerp(yLerp, x1z0y0, x1z0y1);
                        double x0z1 = MathHelper.lerp(yLerp, x0z1y0, x0z1y1);
                        double x1z1 = MathHelper.lerp(yLerp, x1z1y0, x1z1y1);

                        // [0, 4] -> x noise pieces
                        for (int pieceX = 0; pieceX < 4; ++pieceX) {
                            int realX = chunkStartX + noiseX * 4 + pieceX;
                            int localX = realX & 15;
                            double xLerp = (double) pieceX / 4.0;
                            // Interpolate noise based on x progress
                            double z0 = MathHelper.lerp(xLerp, x0z0, x1z0);
                            double z1 = MathHelper.lerp(xLerp, x0z1, x1z1);

                            // [0, 4) -> z noise pieces
                            for (int pieceZ = 0; pieceZ < 4; ++pieceZ) {
                                int realZ = chunkStartZ + noiseZ * 4 + pieceZ;
                                int localZ = realZ & 15;
                                double zLerp = (double) pieceZ / 4.0;
                                // Get the real noise here by interpolating the last 2 noises together
                                double density = MathHelper.lerp(zLerp, z0, z1);

                                int heightAt = heights[localX * 16 + localZ];

                                // We're above the height, so we're in the ocean most likely
                                if (realY > heightAt - 12) {
                                    // Add to the density to prevent us from carving into the ocean
                                    // The 4.8 is a magic value but it seems to work well
                                    density += 4.8;
                                }

                                // skip if it's above the real height
                                if (realY > heightAt) {
                                    continue;
                                }

                                if (density < 0.0) {
                                    // TODO no new
                                    BlockState state = Blocks.CAVE_AIR.getDefaultState();
                                    if (realY < 11) {
                                        state = Blocks.LAVA.getDefaultState();
                                    }

                                    chunk.setBlockState(new BlockPos(localX, realY, localZ), state, false);

                                    int i = localX | localZ << 4 | realY << 8;
                                    carvingMask.set(i);
                                }
                            }
                        }
                    }
                }
            }

            // Reuse noise data from the previous column for speed
            double[][] xColumn = noiseData[0];
            noiseData[0] = noiseData[1];
            noiseData[1] = xColumn;
        }

        return true;
    }

    public static void sampleNoiseColumn(double[] buffer, int x, int z, OctavePerlinNoiseSampler caveNoise, OctavePerlinNoiseSampler offsetNoise, OctavePerlinNoiseSampler scaleNoise) {
        double offset = offsetNoise.sample(x / 128.0, 5423.434, z / 128.0) * 5.45;
        Random random = new Random(((long) x << 1) * 341873128712L + ((long) z << 1) * 132897987541L);

        // generate pillar
        if (random.nextInt(24) == 0) {
            // density: 4 is a stalactite/stalagmite, 10 is pillar
            offset += 4.0 + random.nextDouble() * 6;
        }

        for (int y = 0; y < buffer.length; y++) {
            buffer[y] = sampleNoise(caveNoise, scaleNoise, x, y, z) + getFalloff(offset, y);
        }
    }

    private static double sampleNoise(OctavePerlinNoiseSampler caveNoise, OctavePerlinNoiseSampler scaleNoise, int x, int y, int z) {
        double noise = 0;
        double amplitude = 1;

        for (int i = 0; i < 6; i++) {
            PerlinNoiseSampler sampler = caveNoise.getOctave(i);

            noise += sampler.sample(x * 2.63 * amplitude, y * 12.18 * amplitude, z * 2.63 * amplitude, 0, 0) / amplitude;

            amplitude /= 2.0;
        }

        noise /= 1.25;

        double scale = (scaleNoise.getOctave(0).sample(x / 96.0, y / 96.0, z / 96.0, 0, 0) + 0.2) * 30;
        noise += Math.min(scale, 0);

        return noise;
    }

    private static double getFalloff(double offset, int y) {
        double falloffScale = 21.5 + offset;

        double falloff = Math.max((falloffScale / y), 0); // lower bound
        falloff += Math.max((falloffScale / ((8) - y)), 0); // upper bound

        double scaledY = y + 10.0;

        falloff = (1.5 * falloff) - (0.1 * scaledY * scaledY) - (-4.0 * y);

        return falloff;
    }
}
