package com.payangar.collegeofwinterhold.registry;

import com.payangar.collegeofwinterhold.CollegeOfWinterhold;
import com.payangar.collegeofwinterhold.entity.villager.CapturedState;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

public final class ModAttachments {

    private static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES =
            DeferredRegister.create(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, CollegeOfWinterhold.MODID);

    public static final DeferredHolder<AttachmentType<?>, AttachmentType<CapturedState>> CAPTURED_STATE =
            ATTACHMENT_TYPES.register("captured_state",
                    () -> AttachmentType.builder(holder -> new CapturedState())
                            .serialize(new CapturedStateSerializer())
                            .build());

    public static void register(IEventBus modEventBus) {
        ATTACHMENT_TYPES.register(modEventBus);
    }

    private ModAttachments() {}

    /**
     * Codec-free serializer : read/write to a flat CompoundTag via the
     * methods on {@link CapturedState} itself.
     */
    private static final class CapturedStateSerializer
            implements net.neoforged.neoforge.attachment.IAttachmentSerializer<net.minecraft.nbt.CompoundTag, CapturedState> {

        @Override
        public CapturedState read(net.neoforged.neoforge.attachment.IAttachmentHolder holder,
                                  net.minecraft.nbt.CompoundTag tag,
                                  net.minecraft.core.HolderLookup.Provider provider) {
            return CapturedState.load(tag);
        }

        @Override
        public net.minecraft.nbt.CompoundTag write(CapturedState state,
                                                    net.minecraft.core.HolderLookup.Provider provider) {
            return state.save();
        }
    }
}
