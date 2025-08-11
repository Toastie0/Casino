package com.toastie01.casino.block;

import com.toastie01.casino.block.base.BlockBase;
import com.toastie01.casino.entity.EntitySeat;
import com.toastie01.casino.init.ModEntityTypes;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.CarpetBlock;
import net.minecraft.block.ShapeContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
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
import net.minecraft.world.WorldView;
import net.minecraft.item.ItemPlacementContext;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;

import java.util.List;

public class BlockBarStool extends BlockBase {
    
    public static final IntProperty FACING = IntProperty.of("facing", 0, 3);
    public static final BooleanProperty ON_CARPET = BooleanProperty.of("on_carpet");
    private static final VoxelShape SHAPE = Block.createCuboidShape(2.0D, 0.0D, 2.0D, 14.0D, 16.0D, 14.0D);

    public BlockBarStool() {
        super(FabricBlockSettings.create()
            .strength(1.5F)
            .sounds(BlockSoundGroup.WOOD)
            .nonOpaque());
        setDefaultState(getStateManager().getDefaultState().with(FACING, 0).with(ON_CARPET, false));
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        // If on carpet, adjust the shape to match where the visual model actually is
        if (state.get(ON_CARPET)) {
            // The visual model is shifted down by 15 pixels, so the collision should match
            // Original shape was Y=0 to Y=16, now it should be Y=-15 to Y=1 (shifted down by 15 pixels)
            return Block.createCuboidShape(2.0D, -15.0D, 2.0D, 14.0D, 1.0D, 14.0D);
        }
        return SHAPE;
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        // Use the same logic as outline shape for collision
        return getOutlineShape(state, world, pos, context);
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        // Check if placement is valid first
        if (!canPlaceAt(getDefaultState(), ctx.getWorld(), ctx.getBlockPos())) {
            return null; // Prevent placement if invalid
        }
        
        // Calculate facing based on player rotation (0-3 for 4 cardinal directions)
        int facing = Math.round(ctx.getPlayerYaw() / 90.0F) & 3;
        
        // Check if we're placing on a carpet
        BlockState belowState = ctx.getWorld().getBlockState(ctx.getBlockPos().down());
        Block belowBlock = belowState.getBlock();
        boolean onCarpet = belowBlock instanceof CarpetBlock || 
                          belowBlock instanceof BlockCasinoCarpet || 
                          belowBlock instanceof BlockVipCasinoCarpet;
        
        return getDefaultState().with(FACING, facing).with(ON_CARPET, onCarpet);
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        if (!world.isClient) {
            // Check if player is already sitting
            if (player.hasVehicle()) {
                return ActionResult.FAIL;
            }
            
            // Check if stool is already occupied
            // Calculate seat position, adjusting for carpets if needed
            double seatY = pos.getY() + 0.5;
            
            // Use the blockstate to determine if we're on a carpet
            if (state.get(ON_CARPET)) {
                // Adjust seat position down by carpet height (15/16 of a block)
                seatY -= 15.0/16.0;
            }
            
            Vec3d seatPos = new Vec3d(pos.getX() + 0.5, seatY, pos.getZ() + 0.5);
            List<EntitySeat> seats = world.getEntitiesByClass(EntitySeat.class, 
                new Box(seatPos.x - 0.5, seatPos.y - 0.5, seatPos.z - 0.5, 
                       seatPos.x + 0.5, seatPos.y + 0.5, seatPos.z + 0.5), 
                entity -> true);
            
            if (!seats.isEmpty()) {
                return ActionResult.FAIL; // Already occupied
            }
            
            // Create seat entity
            EntitySeat seatEntity = new EntitySeat(ModEntityTypes.ENTITY_SEAT, world);
            seatEntity.setPosition(seatPos.x, seatPos.y - 0.4, seatPos.z); // Adjusted to account for getMountedHeightOffset
            
            world.spawnEntity(seatEntity);
            player.startRiding(seatEntity);
            
            return ActionResult.SUCCESS;
        }
        
        return ActionResult.CONSUME;
    }

    @Override
    public boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos) {
        BlockState belowState = world.getBlockState(pos.down());
        Block belowBlock = belowState.getBlock();
        
        // Allow placement on vanilla carpets
        if (belowBlock instanceof CarpetBlock) {
            return true;
        }
        
        // Allow placement on modded casino carpets (these extend BlockBase, not CarpetBlock)
        if (belowBlock instanceof BlockCasinoCarpet || belowBlock instanceof BlockVipCasinoCarpet) {
            return true;
        }
        
        // Allow placement on full blocks (default behavior)
        return belowState.isSideSolidFullSquare(world, pos.down(), net.minecraft.util.math.Direction.UP);
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(FACING, ON_CARPET);
    }
}
