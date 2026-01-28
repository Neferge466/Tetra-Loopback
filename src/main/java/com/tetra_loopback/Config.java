package com.tetra_loopback;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;

@Mod.EventBusSubscriber(modid = Tetra_loopback.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class Config {
    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();


    private static final ForgeConfigSpec.DoubleValue FAST_FALL_SPEED = BUILDER
            .comment("Fast fall speed (negative value = downward)")
            .defineInRange("windlight.fastFallSpeed", -2.0, -5.0, -0.1);

    private static final ForgeConfigSpec.DoubleValue DOUBLE_JUMP_FORCE = BUILDER
            .comment("Double jump upward force")
            .defineInRange("windlight.doubleJumpForce", 0.8, 0.1, 2.0);

    private static final ForgeConfigSpec.DoubleValue DASH_SPEED = BUILDER
            .comment("Dash movement speed")
            .defineInRange("windlight.dashSpeed", 2.5, 0.5, 10.0);

    private static final ForgeConfigSpec.DoubleValue DASH_VERTICAL_BOOST = BUILDER
            .comment("Dash vertical boost (upward force)")
            .defineInRange("windlight.dashVerticalBoost", 0.25, -1.0, 2.0);

    private static final ForgeConfigSpec.IntValue DASH_DURATION = BUILDER
            .comment("Dash duration in ticks (20 ticks = 1 second)")
            .defineInRange("windlight.dashDuration", 5, 1, 40);

    private static final ForgeConfigSpec.IntValue DASH_COOLDOWN = BUILDER
            .comment("Dash cooldown in ticks (20 ticks = 1 second)")
            .defineInRange("windlight.dashCooldown", 60, 0, 100);

    private static final ForgeConfigSpec.DoubleValue GROUND_DASH_THRESHOLD = BUILDER
            .comment("Minimum horizontal speed required for ground dash")
            .defineInRange("windlight.groundDashThreshold", 0.25, 0.0, 2.0);

    private static final ForgeConfigSpec.BooleanValue ENABLE_TOOLTIPS = BUILDER
            .comment("Enable custom tooltips for items")
            .define("enableTooltips", true);

    private static final ForgeConfigSpec.BooleanValue SHOW_SEPARATORS = BUILDER
            .comment("Show separators in tooltips")
            .define("showSeparators", true);

    private static final ForgeConfigSpec.ConfigValue<String> EMBLEM_POSITION = BUILDER
            .comment("Emblem wearing position: LEFT, CENTER, RIGHT")
            .define("emblemPosition", "RIGHT", Config::validatePosition);

    private static final ForgeConfigSpec.DoubleValue EMBLEM_DEPTH = BUILDER
            .comment("Emblem depth (0.0-1.0, lower value = closer to body)")
            .defineInRange("emblemDepth", 0.1, 0.0, 1.0);

    private static final ForgeConfigSpec.DoubleValue EMBLEM_SCALE = BUILDER
            .comment("Emblem scale (0.1-2.0)")
            .defineInRange("emblemScale", 0.25, 0.1, 2.0);

    private static final ForgeConfigSpec.DoubleValue GOGGLES_VERTICAL_OFFSET = BUILDER
            .comment("Goggles vertical offset (positive = up, negative = down)")
            .defineInRange("gogglesVerticalOffset", -0.02, -0.5, 0.5);

    private static final ForgeConfigSpec.DoubleValue GOGGLES_DEPTH_OFFSET = BUILDER
            .comment("Goggles depth offset (positive = forward, negative = backward)")
            .defineInRange("gogglesDepthOffset", -0.21, -0.3, 0.3);

    private static final ForgeConfigSpec.DoubleValue GOGGLES_SCALE = BUILDER
            .comment("Goggles scale (0.5-2.0)")
            .defineInRange("gogglesScale", 0.8, 0.1, 2.0);

    private static final ForgeConfigSpec.DoubleValue GOGGLES_ROTATION_X = BUILDER
            .comment("Goggles X-axis rotation (degrees)")
            .defineInRange("gogglesRotationX", 0.0, 0.0, 0.0);

    private static final ForgeConfigSpec.DoubleValue GOGGLES_ROTATION_Y = BUILDER
            .comment("Goggles Y-axis rotation (degrees)")
            .defineInRange("gogglesRotationY", 0.0, 0.0, 0.0);

    static final ForgeConfigSpec SPEC = BUILDER.build();

    //Wind Light
    public static float fastFallSpeed;
    public static float doubleJumpForce;
    public static float dashSpeed;
    public static float dashVerticalBoost;
    public static int dashDuration;
    public static int dashCooldown;
    public static float groundDashThreshold;

    //Render
    public static String emblemPosition;
    public static double emblemDepth;
    public static double emblemScale;

    //Goggles
    public static double gogglesVerticalOffset;
    public static double gogglesDepthOffset;
    public static double gogglesScale;
    public static double gogglesRotationX;
    public static double gogglesRotationY;

    //Tooltip
    public static boolean enableTooltips = true;
    public static boolean showSeparators = true;

    private static boolean validatePosition(final Object obj) {
        if (obj instanceof String) {
            String position = ((String) obj).toUpperCase();
            return position.equals("LEFT") || position.equals("CENTER") || position.equals("RIGHT");
        }
        return false;
    }

    //loading
    @SubscribeEvent
    static void onLoad(final ModConfigEvent event) {
        //Wind Light
        fastFallSpeed = FAST_FALL_SPEED.get().floatValue();
        doubleJumpForce = DOUBLE_JUMP_FORCE.get().floatValue();
        dashSpeed = DASH_SPEED.get().floatValue();
        dashVerticalBoost = DASH_VERTICAL_BOOST.get().floatValue();
        dashDuration = DASH_DURATION.get();
        dashCooldown = DASH_COOLDOWN.get();
        groundDashThreshold = GROUND_DASH_THRESHOLD.get().floatValue();

        //Tooltip
        enableTooltips = ENABLE_TOOLTIPS.get();
        showSeparators = SHOW_SEPARATORS.get();

        //Render
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