package com.ombremoon.playingcards.block;

import com.ombremoon.playingcards.block.base.BlockBase;
import com.ombremoon.playingcards.block.entity.CasinoTableBlockEntity;
import com.ombremoon.playingcards.init.ModBlockEntities;
import net.minecraft.block.Block;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.IntProperty;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import org.jetbrains.annotations.Nullable;

public class BlockCasinoTable extends BlockBase implements BlockEntityProvider {
    
    // Facing property for rotation
    public static final IntProperty FACING = IntProperty.of("facing", 0, 3);
    
    // Multi-block connection properties (like original poker table)
    private static final BooleanProperty NORTH = BooleanProperty.of("north");
    private static final BooleanProperty EAST = BooleanProperty.of("east");
    private static final BooleanProperty SOUTH = BooleanProperty.of("south");
    private static final BooleanProperty WEST = BooleanProperty.of("west");
    
    // Corner connection properties for seamless connection
    private static final BooleanProperty NORTHWEST = BooleanProperty.of("northwest");
    private static final BooleanProperty NORTHEAST = BooleanProperty.of("northeast");
    private static final BooleanProperty SOUTHWEST = BooleanProperty.of("southwest");
    private static final BooleanProperty SOUTHEAST = BooleanProperty.of("southeast");
    
    private static final VoxelShape SHAPE = Block.createCuboidShape(0.0D, 0.0D, 0.0D, 16.0D, 15.0D, 16.0D);

    public BlockCasinoTable() {
        super(FabricBlockSettings.create()
            .strength(2.0F)
            .sounds(BlockSoundGroup.WOOD)
            .nonOpaque());
        setDefaultState(getStateManager().getDefaultState()
            .with(FACING, 0)
            .with(NORTH, false)
            .with(EAST, false)
            .with(SOUTH, false)
            .with(WEST, false)
            .with(NORTHWEST, false)
            .with(NORTHEAST, false)
            .with(SOUTHWEST, false)
            .with(SOUTHEAST, false));
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return SHAPE;
    }

    @Override
    public void onPlaced(net.minecraft.world.World world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack itemStack) {
        super.onPlaced(world, pos, state, placer, itemStack);
        
        // Table entity ownership is handled via block entity - players can claim tables with sneak+right-click
        if (placer instanceof PlayerEntity) {
            // Tables start unclaimed - players must manually claim them for ownership
        }
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        // Calculate facing based on player rotation (0-3 for 4 cardinal directions)
        int facing = Math.round(ctx.getPlayerYaw() / 90.0F) & 3;
        return getState(ctx.getWorld(), ctx.getBlockPos()).with(FACING, facing);
    }

    /**
     * Checks if the Block at the given pos can connect to the Block given by the offsets.
     */
    private boolean canConnectTo(WorldAccess world, BlockPos pos, int offX, int offZ) {
        BlockPos otherPos = pos.add(offX, 0, offZ);
        Block otherBlock = world.getBlockState(otherPos).getBlock();
        return otherBlock instanceof BlockCasinoTable;
    }

    private BlockState getState(WorldAccess world, BlockPos pos) {
        boolean north = canConnectTo(world, pos, 0, -1);
        boolean east = canConnectTo(world, pos, 1, 0);
        boolean south = canConnectTo(world, pos, 0, 1);
        boolean west = canConnectTo(world, pos, -1, 0);
        
        boolean northwest = canConnectTo(world, pos, -1, -1);
        boolean northeast = canConnectTo(world, pos, 1, -1);
        boolean southwest = canConnectTo(world, pos, -1, 1);
        boolean southeast = canConnectTo(world, pos, 1, 1);
        
        return getDefaultState()
            .with(NORTH, north)
            .with(EAST, east)
            .with(SOUTH, south)
            .with(WEST, west)
            .with(NORTHWEST, northwest)
            .with(NORTHEAST, northeast)
            .with(SOUTHWEST, southwest)
            .with(SOUTHEAST, southeast);
    }

    @Override
    public BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState neighborState, 
                                               WorldAccess world, BlockPos pos, BlockPos neighborPos) {
        return getState(world, pos).with(FACING, state.get(FACING));
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(FACING, NORTH, SOUTH, EAST, WEST, NORTHWEST, NORTHEAST, SOUTHWEST, SOUTHEAST);
    }
    
    // ===== BLOCK ENTITY METHODS =====
    
    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new CasinoTableBlockEntity(ModBlockEntities.CASINO_TABLE, pos, state);
    }
    
    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        if (!world.isClient && player.isSneaking()) {
            BlockEntity blockEntity = world.getBlockEntity(pos);
            if (blockEntity instanceof CasinoTableBlockEntity tableEntity) {
                if (!tableEntity.hasOwner()) {
                    // Claim the table
                    tableEntity.setOwner(player.getUuid(), player.getDisplayName().getString());
                    player.sendMessage(Text.literal("You claimed this casino table!").formatted(Formatting.GREEN), true);
                    return ActionResult.SUCCESS;
                } else if (tableEntity.isOwner(player.getUuid())) {
                    // Release ownership
                    tableEntity.clearOwnership();
                    player.sendMessage(Text.literal("You released ownership of this table.").formatted(Formatting.YELLOW), true);
                    return ActionResult.SUCCESS;
                } else {
                    // Already owned by someone else
                    player.sendMessage(Text.literal("This table is owned by " + tableEntity.getOwnerName()).formatted(Formatting.RED), true);
                    return ActionResult.FAIL;
                }
            }
        }
        return ActionResult.PASS;
    }
}
