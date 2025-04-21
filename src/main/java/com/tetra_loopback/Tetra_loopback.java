package com.tetra_loopback;

import com.mojang.logging.LogUtils;
import com.tetra_loopback.effects.TLbEffects;
import com.tetra_loopback.item.creative.TLbCreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;


@Mod(Tetra_loopback.MODID)
public class Tetra_loopback {




    public static final String MODID = "tetra_loopback";
    private static final Logger LOGGER = LogUtils.getLogger();
    public Tetra_loopback() {
        var bus = FMLJavaModLoadingContext.get().getModEventBus();
        //IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();




        // Curios
        //bus.addListener(this::enqueueIMC);


        TLbRegistry.BLOCKS.register(bus);
        // Items //
        TLbRegistry.ITEMS.register(bus);

        // Creative Tab //
        TLbCreativeModeTab.register(bus);

        // Potion Effects //
        TLbEffects.register(bus);



        MinecraftForge.EVENT_BUS.register(this);



        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Config.SPEC);


    }


}


