package com.toastie01.casino.block.base;

import net.minecraft.block.Block;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.IntProperty;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import org.jetbrains.annotations.Nullable;

/**
 * Base class for all casino table types that can connect seamlessly to each other.
 * Handles all connection logic in one place to simplify the system.
 */
public abstract class BaseConnectableTable extends BlockBase implements BlockEntityProvider {
    
    // Facing property for rotation
    public static final IntProperty FACING = IntProperty.of("facing", 0, 3);
    
    // Connection properties - simplified to just the four cardinal directions
    public static final BooleanProperty NORTH = BooleanProperty.of("north");
    public static final BooleanProperty EAST = BooleanProperty.of("east");
    public static final BooleanProperty SOUTH = BooleanProperty.of("south");
    public static final BooleanProperty WEST = BooleanProperty.of("west");
    
    // Corner connection properties for seamless visuals
    public static final BooleanProperty NORTHWEST = BooleanProperty.of("northwest");
    public static final BooleanProperty NORTHEAST = BooleanProperty.of("northeast");
    public static final BooleanProperty SOUTHWEST = BooleanProperty.of("southwest");
    public static final BooleanProperty SOUTHEAST = BooleanProperty.of("southeast");
    
    public BaseConnectableTable(Settings settings) {
        super(settings);
        // Set default state - no connections initially
        this.setDefaultState(this.stateManager.getDefaultState()
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
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(FACING, NORTH, EAST, SOUTH, WEST, NORTHWEST, NORTHEAST, SOUTHWEST, SOUTHEAST);
    }
    
    /**
     * Check if a block can connect to this table type.
     * All casino table variants can connect to each other.
     */
    public static boolean canConnectTo(Block block) {
        return block instanceof BaseConnectableTable;
    }
    
    /**
     * Get the state with proper connections based on surrounding blocks.
     * This is the core connection logic used by all table types.
     */
    public BlockState getConnectedState(World world, BlockPos pos) {
        BlockState state = this.getDefaultState().with(FACING, world.getBlockState(pos).get(FACING));
        
        // Check cardinal directions
        boolean north = canConnectTo(world.getBlockState(pos.north()).getBlock());
        boolean east = canConnectTo(world.getBlockState(pos.east()).getBlock());
        boolean south = canConnectTo(world.getBlockState(pos.south()).getBlock());
        boolean west = canConnectTo(world.getBlockState(pos.west()).getBlock());
        
        // Check corner connections (only if both adjacent sides are connected)
        boolean northwest = north && west && canConnectTo(world.getBlockState(pos.north().west()).getBlock());
        boolean northeast = north && east && canConnectTo(world.getBlockState(pos.north().east()).getBlock());
        boolean southwest = south && west && canConnectTo(world.getBlockState(pos.south().west()).getBlock());
        boolean southeast = south && east && canConnectTo(world.getBlockState(pos.south().east()).getBlock());
        
        return state
            .with(NORTH, north)
            .with(EAST, east)
            .with(SOUTH, south)
            .with(WEST, west)
            .with(NORTHWEST, northwest)
            .with(NORTHEAST, northeast)
            .with(SOUTHWEST, southwest)
            .with(SOUTHEAST, southeast);
    }
    
    /**
     * Simplified neighbor update - just recalculate this block's connections.
     * No more complex cascading updates that caused infinite recursion.
     */
    @Override
    public BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState neighborState, WorldAccess world, BlockPos pos, BlockPos neighborPos) {
        // Only update connections, don't trigger cascading updates
        if (world instanceof World) {
            return getConnectedState((World) world, pos);
        }
        return state;
    }
    
    /**
     * Called when the block is placed - set initial connections.
     */
    @Override
    public void onPlaced(World world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack itemStack) {
        super.onPlaced(world, pos, state, placer, itemStack);
        
        // Set the connected state and update surrounding tables
        if (!world.isClient) {
            BlockState connectedState = getConnectedState(world, pos);
            world.setBlockState(pos, connectedState, Block.NOTIFY_ALL);
            
            // Update neighboring tables (but only immediate neighbors)
            updateNeighboringTables(world, pos);
        }
    }
    
    /**
     * Simplified update method - only updates immediate neighbors, no recursion.
     */
    private void updateNeighboringTables(World world, BlockPos centerPos) {
        // Only update the 4 cardinal neighbors - no 3x3 area, no recursion
        Direction[] directions = {Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST};
        
        for (Direction direction : directions) {
            BlockPos neighborPos = centerPos.offset(direction);
            BlockState neighborState = world.getBlockState(neighborPos);
            
            if (canConnectTo(neighborState.getBlock())) {
                // Get the neighbor's updated state and apply it
                if (neighborState.getBlock() instanceof BaseConnectableTable) {
                    BaseConnectableTable neighborTable = (BaseConnectableTable) neighborState.getBlock();
                    BlockState newNeighborState = neighborTable.getConnectedState(world, neighborPos);
                    world.setBlockState(neighborPos, newNeighborState, Block.NOTIFY_NEIGHBORS);
                }
            }
        }
    }
}
