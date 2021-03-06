package supercoder79.cavebiomes.world.decorator;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.ChunkRegion;
import supercoder79.cavebiomes.api.CaveDecorator;
import supercoder79.cavebiomes.world.noise.OpenSimplexNoise;

import java.util.Random;

public class RandomBlockStateCaveDecorator extends CaveDecorator {
    private final BlockState state;
    private final int chance;

    public RandomBlockStateCaveDecorator(BlockState state, int chance) {
        this.state = state;
        this.chance = chance;
    }

    @Override
    public void decorate(ChunkRegion world, Random random, OpenSimplexNoise noise, BlockPos pos, DecorationContext context) {
        // Try to set a block in every direction
        for (Direction direction : Direction.values()) {
            trySet(world, random, pos.offset(direction));
        }
    }

    private void trySet(ChunkRegion world, Random random, BlockPos pos) {
        if (random.nextInt(this.chance) == 0) {
            if (world.getBlockState(pos).isOf(Blocks.STONE)) {
                world.setBlockState(pos, this.state, 3);
            }
        }
    }
}
