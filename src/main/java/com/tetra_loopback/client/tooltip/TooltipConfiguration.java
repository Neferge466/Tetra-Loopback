package com.tetra_loopback.client.tooltip;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class TooltipConfiguration {
    private final List<TooltipSection> sections = new ArrayList<>();
    private boolean addEmptyLineBefore = false;

    public TooltipConfiguration addSection(TooltipSection section) {
        this.sections.add(section);
        return this;
    }

    public TooltipConfiguration addEmptyLineBefore(boolean add) {
        this.addEmptyLineBefore = add;
        return this;
    }

    public List<Component> buildTooltip(ItemStack stack) {
        List<Component> tooltip = new ArrayList<>();

        for (int i = 0; i < sections.size(); i++) {
            TooltipSection section = sections.get(i);

            //在第一个段落前添加空行
            if (i == 0 && addEmptyLineBefore) {
                tooltip.add(Component.empty());
            }

            tooltip.addAll(section.buildComponents(stack));
        }

        return tooltip;
    }
}