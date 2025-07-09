package com.tetra_loopback;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Mod.EventBusSubscriber(modid = Tetra_loopback.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class Config {
    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();

    //position
    private static final ForgeConfigSpec.ConfigValue<String> EMBLEM_POSITION = BUILDER
            .comment("Wearing position: LEFT, CENTER, RIGHT")
            .define("emblemPosition", "RIGHT", Config::validatePosition);

    //depth
    private static final ForgeConfigSpec.DoubleValue EMBLEM_DEPTH = BUILDER
            .comment("depth (0.0-1.0, The lower the value, the closer it is to the body)")
            .defineInRange("emblemDepth", 0.1, 0.0, 1.0);

    //scale
    private static final ForgeConfigSpec.DoubleValue EMBLEM_SCALE = BUILDER
            .comment("emblem scale (0.1-2.0)")
            .defineInRange("emblemScale", 0.25, 0.1, 2.0);

    private static final ForgeConfigSpec.ConfigValue<List<? extends String>> ITEM_STRINGS = BUILDER.comment("A list of items to log on common setup.").defineListAllowEmpty("items", List.of("minecraft:iron_ingot"), Config::validateItemName);

    static final ForgeConfigSpec SPEC = BUILDER.build();

    //render variable
    public static String emblemPosition;
    public static double emblemDepth;
    public static double emblemScale;
    public static Set<Item> items;

    //Verify the position config
    private static boolean validatePosition(final Object obj) {
        if (obj instanceof String) {
            String position = ((String) obj).toUpperCase();
            return position.equals("LEFT") || position.equals("CENTER") || position.equals("RIGHT");
        }
        return false;
    }

    private static boolean validateItemName(final Object obj) {
        return obj instanceof final String itemName && ForgeRegistries.ITEMS.containsKey(new ResourceLocation(itemName));
    }

    @SubscribeEvent
    static void onLoad(final ModConfigEvent event) {
        //load
        emblemPosition = EMBLEM_POSITION.get().toUpperCase();
        emblemDepth = EMBLEM_DEPTH.get();
        emblemScale = EMBLEM_SCALE.get();

        items = ITEM_STRINGS.get().stream()
                .map(itemName -> ForgeRegistries.ITEMS.getValue(new ResourceLocation(itemName)))
                .collect(Collectors.toSet());
    }
}
