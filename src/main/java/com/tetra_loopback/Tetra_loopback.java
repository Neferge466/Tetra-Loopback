package com.tetra_loopback;

import com.mojang.logging.LogUtils;
import com.tetra_loopback.block.ancientforge.entity.TLbBlockEntities;
import com.tetra_loopback.client.ClientSetup;
import com.tetra_loopback.effects.TLbEffects;
import com.tetra_loopback.effects.common.earthenshield.EarthenShieldEffectHandler;
import com.tetra_loopback.effects.curio.supercooling.SupercoolingEffectHandler;
import com.tetra_loopback.effects.curio.vision.VisionFieldEffect;
import com.tetra_loopback.effects.curio.windlight.WindLightEffectHandler;
import com.tetra_loopback.block.ancientforge.inventory.TLbMenus;
import com.tetra_loopback.item.creative.TLbCreativeModeTab;
import com.tetra_loopback.block.ancientforge.recipe.TLbRecipes;
import com.tetra_loopback.network.TLbNetwork;
import com.tetra_loopback.sound.TLbSoundEvents;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.DistExecutor;
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
        //公共事件监听
        bus.addListener(this::onCommonSetup);

        TLbRegistry.BLOCKS.register(bus);
        TLbRegistry.ITEMS.register(bus);
        TLbCreativeModeTab.register(bus);
        TLbEffects.register(bus);

        //初始化网络
        TLbNetwork.register();

        TLbSoundEvents.register(bus);

        TLbBlockEntities.BLOCK_ENTITIES.register(bus);

        //注册配方
        TLbRecipes.SERIALIZERS.register(bus);
        TLbRecipes.RECIPE_TYPES.register(bus);

        //注册菜单
        TLbMenus.MENUS.register(bus);

        //事件总线
        MinecraftForge.EVENT_BUS.register(this);
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Config.SPEC);

        //客户端初始化
        DistExecutor.unsafeRunWhenOn(net.minecraftforge.api.distmarker.Dist.CLIENT, () -> ClientSetup::init);
    }

    private void onCommonSetup(final FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            //进行通用初始化
            items.forEach(init -> init.commonInit(TetraMod.packetHandler));
            items.clear();
        });
    }

    @SubscribeEvent
    public void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        //通用清理
        VisionFieldEffect.cleanupPlayer(event.getEntity().getUUID());
        WindLightEffectHandler.cleanupPlayer(event.getEntity().getUUID());
        SupercoolingEffectHandler.cleanupPlayer(event.getEntity().getUUID());
        EarthenShieldEffectHandler.cleanupPlayer(event.getEntity().getUUID());

        //客户端清理
        DistExecutor.unsafeRunWhenOn(net.minecraftforge.api.distmarker.Dist.CLIENT, () -> () -> {
            if (event.getEntity().level().isClientSide()) {
                com.tetra_loopback.effects.curio.supercooling.client.ClientSupercoolingData.cleanup();
            }
        });
    }
}