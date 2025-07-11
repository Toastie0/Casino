package com.ombremoon.playingcards.entity.data;

import com.ombremoon.playingcards.util.ArrayHelper;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.entity.data.TrackedDataHandler;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;

/**
 * Custom data serializers for entity tracking
 */
public class PCDataSerializers {

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

    public static void register() {
        TrackedDataHandlerRegistry.register(STACK);
    }
}
