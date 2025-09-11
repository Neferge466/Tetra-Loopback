package com.tetra_loopback;

import com.tetra_loopback.item.modular.ModularEmblem;
import com.tetra_loopback.item.modular.ModularGoggles;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import top.theillusivec4.curios.api.SlotContext;

public class TLbRegistry {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, Tetra_loopback.MODID);
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, Tetra_loopback.MODID);

    //ICurioItem
    public static final RegistryObject<Item> MODULAR_EMBLEM = ITEMS.register(ModularEmblem.identifier, () ->
            new ModularEmblem() {
                @Override
                public boolean canEquipFromUse(SlotContext slotContext, ItemStack stack) {
                    return true;
                }
            }
    );

    public static final RegistryObject<Item> MODULAR_GOGGLES = ITEMS.register(ModularGoggles.identifier, () ->
            new ModularGoggles() {
                @Override
                public boolean canEquipFromUse(SlotContext slotContext, ItemStack stack) {
                    return true;
                }
            }
    );

    //item
    public static final RegistryObject<Item> CURIOS_EMBLEM = ITEMS.register("curios_emblem",
            () -> new Item(new Item.Properties()));

    public static final RegistryObject<Item> CURIOS_GOGGLES = ITEMS.register("curios_goggles",
            () -> new Item(new Item.Properties()));






    public static final RegistryObject<Item> LOOPBACK_ITEM = ITEMS.register("loopback_item",
            () -> new Item(new Item.Properties().rarity(Rarity.EPIC).fireResistant()));


    public static final RegistryObject<Item> BLOODY_STAR = ITEMS.register("bloody_star",
            () -> new Item(new Item.Properties().rarity(Rarity.RARE)));

    public static final RegistryObject<Item> MISLEAD_STAR = ITEMS.register("mislead_star",
            () -> new Item(new Item.Properties().rarity(Rarity.RARE)));


    public static final RegistryObject<Item> FUSE_STEEL_INGOT = ITEMS.register("fuse_steel_ingot",
            () -> new Item(new Item.Properties().rarity(Rarity.RARE)));



}
