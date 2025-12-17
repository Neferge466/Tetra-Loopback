package com.tetra_loopback.block.ancientforge.fuel;

import com.tetra_loopback.Tetra_loopback;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

public class TLbFuelTags {
    public static final TagKey<Item> ANCIENT_FORGE_FUELS =
            create("ancient_forge_fuels");
    public static final TagKey<Item> HIGH_ENERGY_FUELS =
            create("high_energy_fuels");
    public static final TagKey<Item> CATALYSTS =
            create("catalysts");

    private static TagKey<Item> create(String name) {
        return TagKey.create(Registries.ITEM, new ResourceLocation(Tetra_loopback.MODID, name));
    }
}