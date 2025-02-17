package me.jellysquid.mods.lithium.mixin.ai.pathing;

import me.jellysquid.mods.lithium.common.ai.pathing.BlockStatePathingCache;
import me.jellysquid.mods.lithium.common.world.blockview.SingleBlockBlockView;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.ai.pathing.LandPathNodeMaker;
import net.minecraft.entity.ai.pathing.PathContext;
import net.minecraft.entity.ai.pathing.PathNodeType;
import net.minecraft.util.math.BlockPos;
import org.apache.commons.lang3.Validate;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractBlock.AbstractBlockState.class)
public abstract class AbstractBlockStateMixin implements BlockStatePathingCache {
    private PathNodeType pathNodeType = null;
    private PathNodeType pathNodeTypeNeighbor = null;

    @Inject(method = "initShapeCache()V", at = @At("RETURN"))
    private void init(CallbackInfo ci) {
        // Reset the cached path node types, to ensure they are re-calculated.
        this.pathNodeType = null;
        this.pathNodeTypeNeighbor = null;

        BlockState state = this.asBlockState();

        SingleBlockBlockView singleBlockBlockView = SingleBlockBlockView.of(state, BlockPos.ORIGIN);
        try {
            this.pathNodeType = Validate.notNull(LandPathNodeMaker.getCommonNodeType(singleBlockBlockView, BlockPos.ORIGIN), "Block has no common path node type!");
        } catch (SingleBlockBlockView.SingleBlockViewException | ClassCastException e) {
            //This is usually hit by shulker boxes, as their hitbox depends on the block entity, and the node type depends on the hitbox
            //Also catch ClassCastException in case some modded code casts BlockView to ChunkCache
            this.pathNodeType = null;
        }
        try {
            //Passing null as previous node type to the method signals to other lithium mixins that we only want the neighbor behavior of this block and not its neighbors
            //Using exceptions for control flow, but this way we do not need to copy the code for the cache initialization, reducing required maintenance and improving mod compatibility
            this.pathNodeTypeNeighbor = (LandPathNodeMaker.getNodeTypeFromNeighbors(new PathContext(singleBlockBlockView, null), 1, 1, 1, null));
            if (this.pathNodeTypeNeighbor == null) {
                this.pathNodeTypeNeighbor = PathNodeType.OPEN;
            }
        } catch (SingleBlockBlockView.SingleBlockViewException | NullPointerException | ClassCastException e) {
            this.pathNodeTypeNeighbor = null;
        }
    }

    @Override
    public PathNodeType lithium$getPathNodeType() {
        return this.pathNodeType;
    }

    @Override
    public PathNodeType lithium$getNeighborPathNodeType() {
        return this.pathNodeTypeNeighbor;
    }

    @Shadow
    protected abstract BlockState asBlockState();

    @Shadow
    public abstract Block getBlock();
}
