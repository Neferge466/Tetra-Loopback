package com.tetra_loopback.item.display;

import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class ScrollDisplayDefinitions {

    public static List<ItemStack> getAllDisplayScrolls() {
        List<ItemStack> scrolls = new ArrayList<>();

        scrolls.add(createScroll("astral_remold", "tetra_loopback",
                new String[]{"astral_remold"}, false, 1, 13396874, 4, 7, 6, 5));

        scrolls.add(createScroll("buckle_adjust", "tetra_loopback",
                new String[]{"buckle_adjust"}, false, 1, 8685461, 4, 7, 6, 5));





        return scrolls;
    }

    private static ItemStack createScroll(String key, String category, String[] schematics,
                                          boolean intricate, int material, int ribbon, Integer... glyphs) {
        return ScrollDisplayHelper.createDisplayScroll(key, category, schematics,
                intricate, material, ribbon, glyphs);
    }
}