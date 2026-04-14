package com.payangar.collegeofwinterhold.event;

import com.payangar.collegeofwinterhold.CollegeOfWinterhold;
import com.payangar.collegeofwinterhold.entity.wizard.lightning.LightningNoviceEntity;
import com.payangar.collegeofwinterhold.registry.ModEntityTypes;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;

@EventBusSubscriber(modid = CollegeOfWinterhold.MODID)
public final class ModBusEvents {

    @SubscribeEvent
    public static void onAttributeCreate(EntityAttributeCreationEvent event) {
        event.put(ModEntityTypes.LIGHTNING_NOVICE.get(), LightningNoviceEntity.prepareAttributes().build());
    }

    private ModBusEvents() {}
}
