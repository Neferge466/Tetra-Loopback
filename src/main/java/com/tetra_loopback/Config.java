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
            .comment("Emblem wearing position: LEFT, CENTER, RIGHT")
            .define("emblemPosition", "RIGHT", Config::validatePosition);

    //depth
    private static final ForgeConfigSpec.DoubleValue EMBLEM_DEPTH = BUILDER
            .comment("Emblem depth (0.0-1.0, lower value = closer to body)")
            .defineInRange("emblemDepth", 0.1, 0.0, 1.0);

    //scale
    private static final ForgeConfigSpec.DoubleValue EMBLEM_SCALE = BUILDER
            .comment("Emblem scale (0.1-2.0)")
            .defineInRange("emblemScale", 0.25, 0.1, 2.0);

    //垂直
    private static final ForgeConfigSpec.DoubleValue GOGGLES_VERTICAL_OFFSET = BUILDER
            .comment("Goggles vertical offset (positive = up, negative = down)")
            .defineInRange("gogglesVerticalOffset", -0.02, -0.5, 0.5);

    //深度
    private static final ForgeConfigSpec.DoubleValue GOGGLES_DEPTH_OFFSET = BUILDER
            .comment("Goggles depth offset (positive = forward, negative = backward)")
            .defineInRange("gogglesDepthOffset", -0.21, -0.3, 0.3);

    //缩放
    private static final ForgeConfigSpec.DoubleValue GOGGLES_SCALE = BUILDER
            .comment("Goggles scale (0.5-2.0)")
            .defineInRange("gogglesScale", 0.8, 0.1, 2.0);

    //X轴旋转
    private static final ForgeConfigSpec.DoubleValue GOGGLES_ROTATION_X = BUILDER
            .comment("Goggles X-axis rotation (degrees)")
            .defineInRange("gogglesRotationX", 0.0, 0.0, 0.0);

    //Y轴旋转
    private static final ForgeConfigSpec.DoubleValue GOGGLES_ROTATION_Y = BUILDER
            .comment("Goggles Y-axis rotation (degrees)")
            .defineInRange("gogglesRotationY", 0.0, 0.0, 0.0);


    static final ForgeConfigSpec SPEC = BUILDER.build();

    //Render
    //Emblem
    public static String emblemPosition;
    public static double emblemDepth;
    public static double emblemScale;

    //Goggles
    public static double gogglesVerticalOffset;
    public static double gogglesDepthOffset;
    public static double gogglesScale;
    public static double gogglesRotationX;
    public static double gogglesRotationY;

    //总配置
    public static Set<Item> items;

    private static boolean validatePosition(final Object obj) {
        if (obj instanceof String) {
            String position = ((String) obj).toUpperCase();
            return position.equals("LEFT") || position.equals("CENTER") || position.equals("RIGHT");
        }
        return false;
    }



    //配置加载
    @SubscribeEvent
    static void onLoad(final ModConfigEvent event) {
        emblemPosition = EMBLEM_POSITION.get().toUpperCase();
        emblemDepth = EMBLEM_DEPTH.get();
        emblemScale = EMBLEM_SCALE.get();

        gogglesVerticalOffset = GOGGLES_VERTICAL_OFFSET.get();
        gogglesDepthOffset = GOGGLES_DEPTH_OFFSET.get();
        gogglesScale = GOGGLES_SCALE.get();
        gogglesRotationX = GOGGLES_ROTATION_X.get();
        gogglesRotationY = GOGGLES_ROTATION_Y.get();


    }
}