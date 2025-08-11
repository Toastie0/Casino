package com.toastie01.casino.entity.data;

import com.toastie01.casino.util.ArrayHelper;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.entity.data.TrackedDataHandler;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;

/**
 * Custom data serializers for entity tracking in the Casino mod.
 * Provides specialized handlers for stacked entity data.
 */
public final class PCDataSerializers {

    /**
     * Data handler for Byte array stacks used in stacked entities.
     */
    public static final TrackedDataHandler<Byte[]> STACK = new TrackedDataHandler<>() {
        @Override
        public void write(PacketByteBuf buf, Byte[] value) {
            buf.writeByteArray(ArrayHelper.toPrimitive(value));
        }

        @Override
        public Byte[] read(PacketByteBuf buf) {
            return ArrayHelper.toObject(buf.readByteArray());
        }

        @Override
        public Byte[] copy(Byte[] value) {
            return value == null ? null : value.clone();
        }
    };

    /**
     * Registers all custom data serializers.
     * Must be called during mod initialization.
     */
    public static void register() {
        TrackedDataHandlerRegistry.register(STACK);
    }
    
    private PCDataSerializers() {
        // Utility class - prevent instantiation
    }
}
