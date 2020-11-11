package supercoder79.cavebiomes.world.decorator;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.ChunkRegion;
import net.minecraft.world.chunk.Chunk;

import java.util.Set;

public abstract class CaveDecorator {
    public abstract void decorate(ChunkRegion world, Chunk chunk, Set<BlockPos> positions);
}