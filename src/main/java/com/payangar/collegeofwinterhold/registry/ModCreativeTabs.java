package com.payangar.collegeofwinterhold.registry;

import com.payangar.collegeofwinterhold.CollegeOfWinterhold;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, CollegeOfWinterhold.MODID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> COLLEGE_OF_WINTERHOLD =
            CREATIVE_TABS.register("college_of_winterhold", () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.college_of_winterhold"))
                    .icon(() -> new ItemStack(ModItems.LIGHTNING_NOVICE_SPAWN_EGG.get()))
                    .displayItems((params, output) -> {
                        output.accept(ModItems.LIGHTNING_NOVICE_SPAWN_EGG.get());
                        output.accept(ModItems.LIGHTNING_APPRENTICE_SPAWN_EGG.get());
                        output.accept(ModItems.LIGHTNING_ADEPT_SPAWN_EGG.get());
                        output.accept(ModItems.LIGHTNING_EXPERT_SPAWN_EGG.get());
                        output.accept(ModItems.LIGHTNING_MASTER_SPAWN_EGG.get());
                        output.accept(ModItems.FIRE_NOVICE_SPAWN_EGG.get());
                        output.accept(ModItems.FIRE_APPRENTICE_SPAWN_EGG.get());
                        output.accept(ModItems.FIRE_ADEPT_SPAWN_EGG.get());
                        output.accept(ModItems.FIRE_EXPERT_SPAWN_EGG.get());
                        output.accept(ModItems.FIRE_MASTER_SPAWN_EGG.get());
                        output.accept(ModItems.ICE_NOVICE_SPAWN_EGG.get());
                        output.accept(ModItems.ICE_APPRENTICE_SPAWN_EGG.get());
                        output.accept(ModItems.ICE_ADEPT_SPAWN_EGG.get());
                        output.accept(ModItems.ICE_EXPERT_SPAWN_EGG.get());
                        output.accept(ModItems.ICE_MASTER_SPAWN_EGG.get());
                        output.accept(ModItems.ENDER_NOVICE_SPAWN_EGG.get());
                        output.accept(ModItems.ENDER_APPRENTICE_SPAWN_EGG.get());
                        output.accept(ModItems.ENDER_ADEPT_SPAWN_EGG.get());
                        output.accept(ModItems.ENDER_EXPERT_SPAWN_EGG.get());
                        output.accept(ModItems.ENDER_MASTER_SPAWN_EGG.get());
                        output.accept(ModItems.NATURE_NOVICE_SPAWN_EGG.get());
                        output.accept(ModItems.NATURE_APPRENTICE_SPAWN_EGG.get());
                        output.accept(ModItems.NATURE_ADEPT_SPAWN_EGG.get());
                        output.accept(ModItems.NATURE_EXPERT_SPAWN_EGG.get());
                        output.accept(ModItems.NATURE_MASTER_SPAWN_EGG.get());
                    })
                    .build());

    public static void register(IEventBus bus) {
        CREATIVE_TABS.register(bus);
    }

    private ModCreativeTabs() {}
}
