package com.tetra_loopback.item.display;

import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * 定义所有要在创造标签页中显示的卷轴
 */
public class ScrollDisplayDefinitions {

    public static List<ItemStack> getAllDisplayScrolls() {
        List<ItemStack> scrolls = new ArrayList<>();

//        scrolls.add(createScroll("addbdg_blade", "tetra_loopback",
//                new String[]{"sword/addbdg_blade"}, false, 2, 16750098, 6, 15, 4, 7));
//

        return scrolls;
    }

    private static ItemStack createScroll(String key, String category, String[] schematics,
                                          boolean intricate, int material, int ribbon, Integer... glyphs) {
        return ScrollDisplayHelper.createDisplayScroll(key, category, schematics,
                intricate, material, ribbon, glyphs);
    }
}