package com.ombremoon.playingcards.block;

import com.ombremoon.playingcards.block.base.BlockBase;
import com.ombremoon.playingcards.entity.EntitySeat;
import com.ombremoon.playingcards.init.ModEntityTypes;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.IntProperty;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.item.ItemPlacementContext;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;

import java.util.List;

public class BlockBarStool extends BlockBase {
    
    public static final IntProperty FACING = IntProperty.of("facing", 0, 3);
    private static final VoxelShape SHAPE = Block.createCuboidShape(2.0D, 0.0D, 2.0D, 14.0D, 16.0D, 14.0D);

    public BlockBarStool() {
        super(FabricBlockSettings.create()
            .strength(1.5F)
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
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        if (!world.isClient) {
            // Check if player is already sitting
            if (player.hasVehicle()) {
                return ActionResult.FAIL;
            }
            
            // Check if stool is already occupied
            Vec3d seatPos = new Vec3d(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
            List<EntitySeat> seats = world.getEntitiesByClass(EntitySeat.class, 
                new Box(seatPos.x - 0.5, seatPos.y - 0.5, seatPos.z - 0.5, 
                       seatPos.x + 0.5, seatPos.y + 0.5, seatPos.z + 0.5), 
                entity -> true);
            
            if (!seats.isEmpty()) {
                return ActionResult.FAIL; // Already occupied
            }
            
            // Create seat entity
            EntitySeat seatEntity = new EntitySeat(ModEntityTypes.ENTITY_SEAT, world);
            seatEntity.setPosition(seatPos.x, seatPos.y - 0.2, seatPos.z);
            
            world.spawnEntity(seatEntity);
            player.startRiding(seatEntity);
            
            return ActionResult.SUCCESS;
        }
        
        return ActionResult.CONSUME;
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }
}
