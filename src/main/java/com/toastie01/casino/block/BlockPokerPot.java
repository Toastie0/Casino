package com.toastie01.casino.block;

import com.toastie01.casino.block.base.BaseConnectableTable;
import com.toastie01.casino.block.entity.CasinoTableBlockEntity;
import com.toastie01.casino.init.ModBlockEntities;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;

/**
 * Poker pot table that can connect seamlessly with other casino table variants.
 * Uses the simplified BaseConnectableTable architecture.
 */
public class BlockPokerPot extends BaseConnectableTable {
    
    public BlockPokerPot() {
        super(FabricBlockSettings.create()
            .hardness(2.0f)
            .resistance(3.0f)
            .sounds(BlockSoundGroup.WOOD)
            .nonOpaque());
    }
    
    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new CasinoTableBlockEntity(ModBlockEntities.CASINO_TABLE, pos, state);
    }
}
