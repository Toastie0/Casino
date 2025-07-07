package com.ombremoon.playingcards.block;

import com.ombremoon.playingcards.block.base.BlockBase;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.IntProperty;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.item.ItemPlacementContext;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;

public class BlockCasinoTable extends BlockBase {
    
    public static final IntProperty FACING = IntProperty.of("facing", 0, 3);
    private static final VoxelShape SHAPE = Block.createCuboidShape(0.0D, 0.0D, 0.0D, 16.0D, 15.0D, 16.0D);

    public BlockCasinoTable() {
        super(FabricBlockSettings.create()
            .strength(2.0F)
            .sounds(BlockSoundGroup.WOOD)
            .nonOpaque());
        setDefaultState(getStateManager().getDefaultState().with(FACING, 0));
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return SHAPE;
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        // Calculate facing based on player rotation (0-3 for 4 cardinal directions)
        int facing = Math.round(ctx.getPlayerYaw() / 90.0F) & 3;
        return getDefaultState().with(FACING, facing);
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }
}
