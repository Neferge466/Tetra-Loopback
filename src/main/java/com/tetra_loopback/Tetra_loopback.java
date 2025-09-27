package com.tetra_loopback;

import com.mojang.logging.LogUtils;
import com.tetra_loopback.block.entity.TLbBlockEntities;
import com.tetra_loopback.effects.TLbEffects;
import com.tetra_loopback.effects.gui.ModEffectStats;
import com.tetra_loopback.inventory.TLbMenus;
import com.tetra_loopback.item.creative.TLbCreativeModeTab;
import com.tetra_loopback.recipe.TLbRecipes;
import com.tetra_loopback.util.ClientProxy;
import net.minecraft.world.item.Item;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;
import se.mickelus.tetra.TetraMod;
import se.mickelus.tetra.items.InitializableItem;

import java.util.ArrayList;
import java.util.List;

@Mod(Tetra_loopback.MODID)
public class Tetra_loopback {
    public static final String MODID = "tetra_loopback";
    private static final Logger LOGGER = LogUtils.getLogger();

    public static List<InitializableItem> items = new ArrayList<>();


    public Tetra_loopback() {
        var bus = FMLJavaModLoadingContext.get().getModEventBus();
        bus.addListener(this::onClientSetup);

        ClientProxy.init();

        //register
        TLbRegistry.BLOCKS.register(bus);
        TLbRegistry.ITEMS.register(bus);
        TLbCreativeModeTab.register(bus);
        TLbEffects.register(bus);

        // 注册方块实体
        TLbBlockEntities.BLOCK_ENTITIES.register(bus);


        // 注册配方系统
        TLbRecipes.SERIALIZERS.register(bus);
        TLbRecipes.RECIPE_TYPES.register(bus);

        // 注册菜单
        TLbMenus.MENUS.register(bus);

        //bus
        MinecraftForge.EVENT_BUS.register(this);
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Config.SPEC);

        bus.addListener(EventPriority.LOWEST,this::onCommonSetup);
    }

    public void onCommonSetup(final FMLClientSetupEvent event) {
        items.forEach(init -> init.commonInit(TetraMod.packetHandler));
        items.clear();
    }


    private void onClientSetup(final FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            ModEffectStats.safeInit();
        });
    }
}
