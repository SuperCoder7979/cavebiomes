package supercoder79.cavebiomes.world.layer.cave;

import supercoder79.cavebiomes.CaveBiomes;
import supercoder79.cavebiomes.api.CaveBiomesAPI;
import supercoder79.cavebiomes.util.LayerRandom;
import supercoder79.cavebiomes.world.decorator.CaveDecorators;
import supercoder79.cavebiomes.world.layer.SamplingCaveLayer;

public class RareCaveLayer extends SamplingCaveLayer {
    public RareCaveLayer(long worldSeed, int salt) {
        super(worldSeed, salt);
    }

    @Override
    protected int operate(LayerRandom random, int x, int z, int sample) {
        if (random.nextInt(5) == 0) {
            int i = random.nextInt(3);
            if (i == 0) return CaveBiomes.CONFIG.generateFullObsidianCaves ? CaveBiomesAPI.indexOf(CaveDecorators.FULL_OBSIDIAN) : CaveBiomesAPI.indexOf(CaveDecorators.OBSIDIAN);
            if (i == 1) return CaveBiomesAPI.indexOf(CaveDecorators.MUSHROOM);
            return CaveBiomes.CONFIG.generateWebCaves ? CaveBiomesAPI.indexOf(CaveDecorators.COBWEB) : sample;
        }

        return sample;
    }
}
