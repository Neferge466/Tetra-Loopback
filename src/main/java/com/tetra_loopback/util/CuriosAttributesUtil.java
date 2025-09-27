package com.tetra_loopback.util;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import top.theillusivec4.curios.api.SlotContext;

import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class CuriosAttributesUtil {
    public static Multimap<Attribute, AttributeModifier> Curios$fixIdentifiers(SlotContext slotContext, Multimap<Attribute, AttributeModifier> modifiers) {
        return Optional.ofNullable(modifiers)
                .map(Multimap::entries)
                .map(Collection::stream)
                .map((entries) -> entries.collect(
                        Multimaps.toMultimap(
                                Map.Entry::getKey,
                                (entry) -> {
                                    UUID newUUID = UUID.nameUUIDFromBytes(
                                            (entry.getValue().getId().toString() +
                                                    slotContext.identifier() +
                                                    slotContext.index()).getBytes(StandardCharsets.UTF_8)
                                    );

                                    return new AttributeModifier(
                                            newUUID,
                                            entry.getValue().getName() + slotContext.identifier() + slotContext.index(),
                                            entry.getValue().getAmount(),
                                            entry.getValue().getOperation()
                                    );
                                },
                                ArrayListMultimap::create))
                ).orElse(ArrayListMultimap.create());
    }
}