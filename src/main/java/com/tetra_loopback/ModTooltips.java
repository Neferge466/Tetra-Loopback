package com.tetra_loopback;

import com.tetra_loopback.TLbRegistry;
import com.tetra_loopback.client.tooltip.TooltipBuilder;
import net.minecraft.ChatFormatting;

public class ModTooltips {

    public static void registerAll() {
        registerCrystalTooltips();
        registerIngotTooltips();
        registerPearlTooltips();
        registerAstralTooltips();
        registerCurioTooltips();
        registerBlockTooltips();
        registerModluarTooltips();
    }

    private static void registerCrystalTooltips() {
        //Loopback Crystal
        TooltipBuilder.create()
                .addDescription("item.tetra_loopback.loopback_crystal.desc")
                .addTrait("trait.echo_resonance")
                .addUsage("usage.forge_material")
                .register(TLbRegistry.LOOPBACK_CRYSTAL.get());

        //Vitality Crystal
        TooltipBuilder.create()
                .addDescription("item.tetra_loopback.vitality_crystal.desc")
                .addTrait("trait.life_essence")
                .addUsage("usage.forge_material")
                .register(TLbRegistry.VITALITY_CRYSTAL.get());

        //Earth Vein Crystal
        TooltipBuilder.create()
                .addDescription("item.tetra_loopback.earth_vein_crystal.desc")
                .addTrait("trait.earth_vein_power")
                .addUsage("usage.forge_material")
                .register(TLbRegistry.EARTH_VEIN_CRYSTAL.get());

        //Empty Crystal
        TooltipBuilder.create()
                .addDescription("item.tetra_loopback.empty_crystal.desc")
                .addUsage("usage.forge_material")
                .register(TLbRegistry.EMPTY_CRYSTAL.get());
    }

    private static void registerIngotTooltips() {
        //Fuse Steel Ingot
        TooltipBuilder.create()
                .addDescription("item.tetra_loopback.fuse_steel_ingot.desc")
                .addTrait("trait.heat_resistant")
                .addTrait("trait.fusion")
                .addUsage("usage.forge_material")
                .register(TLbRegistry.FUSE_STEEL_INGOT.get());

        //Earth Vein Ingot
        TooltipBuilder.create()
                .addDescription("item.tetra_loopback.earth_vein_ingot.desc")
                .addTrait("trait.earth_vein_power")
                .addUsage("usage.forge_material")
                .register(TLbRegistry.EARTH_VEIN_INGOT.get());

        //Cold Steel Ingot
        TooltipBuilder.create()
                .addDescription("item.tetra_loopback.cold_steel_ingot.desc")
                .addTrait("trait.tempered_cold")
                .addUsage("usage.forge_material")
                .register(TLbRegistry.COLD_STEEL_INGOT.get());

        //Vitality Ingot
        TooltipBuilder.create()
                .addDescription("item.tetra_loopback.vitality_ingot.desc")
                .addTrait("trait.pulsating_life")
                .addUsage("usage.forge_material")
                .register(TLbRegistry.VITALITY_INGOT.get());

        //Cold Wind Ingot
        TooltipBuilder.create()
                .addDescription("item.tetra_loopback.cold_wind_ingot.desc")
                .addTrait("trait.wind_light")
                .addUsage("usage.forge_material")
                .register(TLbRegistry.COLD_WIND_INGOT.get());

        //Star Ingot
        TooltipBuilder.create()
                .addDescription("item.tetra_loopback.star_ingot.desc")
                .addTrait("trait.astral_power")
                .addUsage("usage.forge_material")
                .register(TLbRegistry.STAR_INGOT.get());

    }

    private static void registerPearlTooltips() {
        //Rainstorm Pearl
        TooltipBuilder.create()
                .addDescription("item.tetra_loopback.rainstorm_pearl.desc")
                .addTrait("trait.rainstorm")
                .register(TLbRegistry.RAINSTORM_PEARL.get());

        // Lava Pearl
        TooltipBuilder.create()
                .addDescription("item.tetra_loopback.lava_pearl.desc")
                .addTrait("trait.lava")
                .register(TLbRegistry.LAVA_PEARL.get());

        // Lightning Pearl
        TooltipBuilder.create()
                .addDescription("item.tetra_loopback.lightning_pearl.desc")
                .addTrait("trait.lightning")
                .register(TLbRegistry.LIGHTNING_PEARL.get());

        // Snow Pearl
        TooltipBuilder.create()
                .addDescription("item.tetra_loopback.snow_pearl.desc")
                .addTrait("trait.winter_snow")
                .register(TLbRegistry.SNOW_PEARL.get());

        // Fuse Pearl
        TooltipBuilder.create()
                .addDescription("item.tetra_loopback.fuse_pearl.desc")
                .addTrait("trait.stable_fusion")
                .register(TLbRegistry.FUSE_PEARL.get());
    }

    private static void registerAstralTooltips() {
        // Bloody Star
        TooltipBuilder.create()
                .addDescription("item.tetra_loopback.bloody_star.desc")
                .addTrait("trait.bloody_disaster")
                .addUsage("usage.ancient_forge")
                .register(TLbRegistry.BLOODY_STAR.get());

        // Mislead Star
        TooltipBuilder.create()
                .addDescription("item.tetra_loopback.mislead_star.desc")
                .addTrait("trait.strife")
                .addUsage("usage.ancient_forge")
                .register(TLbRegistry.MISLEAD_STAR.get());

        //Broken Moon Shard
        TooltipBuilder.create()
                .addDescription("item.tetra_loopback.broken_moon_shard.desc")
                .addTrait("trait.mani")
                .addUsage("usage.ancient_forge")
                .register(TLbRegistry.BROKEN_MOON_SHARD.get());

    }

    private static void registerCurioTooltips() {
        //Curios Emblem
        TooltipBuilder.create()
                .addDescription("item.tetra_loopback.curios_emblem.desc")
                .addTrait("trait.curios")
                .addUsage("usage.curio_slot")
                .register(TLbRegistry.CURIOS_EMBLEM.get());

        //Curios Goggles
        TooltipBuilder.create()
                .addDescription("item.tetra_loopback.curios_goggles.desc")
                .addTrait("trait.curios")
                .addUsage("usage.curio_slot")
                .register(TLbRegistry.CURIOS_GOGGLES.get());
    }

    private static void registerBlockTooltips() {
        //Ancient Forge
        TooltipBuilder.create()
                .addDescription("block.tetra_loopback.ancient_forge.desc")
                .addTrait("trait.ancient_forge")
                .addTrait("trait.crafts_special")
                .addUsage("usage.right_click")
                .register(TLbRegistry.ANCIENT_FORGE_ITEM.get());
    }

    private static void registerModluarTooltips() {
        //Modular Emblem
        TooltipBuilder.create()
                .addDescription("item.tetra_loopback.curios_emblem.desc")
                .addTrait("trait.curios")
                .addUsage("usage.curio_slot")
                .register(TLbRegistry.MODULAR_EMBLEM.get());

        //Modular Goggles
        TooltipBuilder.create()
                .addDescription("item.tetra_loopback.curios_goggles.desc")
                .addTrait("trait.curios")
                .addUsage("usage.curio_slot")
                .register(TLbRegistry.MODULAR_GOGGLES.get());


    }

}