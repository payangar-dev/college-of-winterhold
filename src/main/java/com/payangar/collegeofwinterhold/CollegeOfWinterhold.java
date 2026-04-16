package com.payangar.collegeofwinterhold;

import com.mojang.logging.LogUtils;
import com.payangar.collegeofwinterhold.config.ModServerConfig;
import com.payangar.collegeofwinterhold.registry.ModAttachments;
import com.payangar.collegeofwinterhold.registry.ModCreativeTabs;
import com.payangar.collegeofwinterhold.registry.ModEntityTypes;
import com.payangar.collegeofwinterhold.registry.ModItems;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import org.slf4j.Logger;

@Mod(CollegeOfWinterhold.MODID)
public class CollegeOfWinterhold {
    public static final String MODID = "college_of_winterhold";
    public static final Logger LOGGER = LogUtils.getLogger();

    public CollegeOfWinterhold(IEventBus modEventBus, ModContainer modContainer) {
        LOGGER.info("College of Winterhold is loading");
        ModEntityTypes.register(modEventBus);
        ModItems.register(modEventBus);
        ModCreativeTabs.register(modEventBus);
        ModAttachments.register(modEventBus);

        modContainer.registerConfig(
                ModConfig.Type.SERVER,
                ModServerConfig.SPEC,
                MODID + "-server.toml"
        );
    }
}
