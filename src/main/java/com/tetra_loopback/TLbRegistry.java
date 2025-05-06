package com.tetra_loopback;

import com.tetra_loopback.item.modular.ModularEmblem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class TLbRegistry {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, Tetra_loopback.MODID);

    public static final RegistryObject<Item> MODULAR_EMBLEM = ITEMS.register(ModularEmblem.identifier, ModularEmblem::new);

    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, Tetra_loopback.MODID);



    /*
    block

    public static final RegistryObject<Block> EXAMPLE_BLOCK = BLOCKS.register("example_block",
            () -> new Block(Block.Properties.of()
                    .strength(2.0f, 6.0f)
                    .sound(SoundType.ANVIL)));
                    */





    public static final RegistryObject<Item> CURIOS_EMBLEM = ITEMS.register("curios_emblem",
            () -> new Item(new Item.Properties()));

    //loopback_item
    public static RegistryObject<Item> LOOPBACK_ITEM= ITEMS.register("loopback_item",
            () -> new Item(new Item.Properties().rarity(Rarity.EPIC).
                    fireResistant()));



    // CRYSTAL
    public static RegistryObject<Item> CRYSTAL= ITEMS.register("crystal",
            () -> new Item(new Item.Properties().rarity(Rarity.UNCOMMON).
                    fireResistant()));

    //MATERIALS
   //ink
    public static final RegistryObject<Item> INK_BURN = ITEMS.register("ink_burn",
            () -> new Item(new Item.Properties().rarity(Rarity.UNCOMMON)));
    public static final RegistryObject<Item> INK_FROZEN = ITEMS.register("ink_frozen",
            () -> new Item(new Item.Properties().rarity(Rarity.UNCOMMON)));
    public static final RegistryObject<Item> INK_TWINKLE = ITEMS.register("ink_twinkle",
            () -> new Item(new Item.Properties().rarity(Rarity.UNCOMMON)));
    public static final RegistryObject<Item> INK_RUNE = ITEMS.register("ink_rune",
            () -> new Item(new Item.Properties().rarity(Rarity.UNCOMMON)));

    //stars
    public static final RegistryObject<Item> BLOODY_STAR = ITEMS.register("bloody_star",
            () -> new Item(new Item.Properties().rarity(Rarity.RARE)));
    public static final RegistryObject<Item> MISLEAD_STAR = ITEMS.register("mislead_star",
            () -> new Item(new Item.Properties().rarity(Rarity.RARE)));



    //gem
    public static final RegistryObject<Item> ROUND_RUBY = ITEMS.register("round_ruby",
            () -> new Item(new Item.Properties().rarity(Rarity.RARE)));






    //ingot
    public static final RegistryObject<Item> SUNSET_INGOT = ITEMS.register("sunset_ingot",
            () -> new Item(new Item.Properties().rarity(Rarity.RARE)));
    public static final RegistryObject<Item> LIGHTNING_INGOT = ITEMS.register("lightning_ingot",
            () -> new Item(new Item.Properties().rarity(Rarity.RARE)));
    public static final RegistryObject<Item> LIGHTNING_INGOT_GRIT = ITEMS.register("lightning_ingot_grit",
            () -> new Item(new Item.Properties().rarity(Rarity.RARE)));
    public static final RegistryObject<Item> FUSE_STEEL_INGOT = ITEMS.register("fuse_steel_ingot",
            () -> new Item(new Item.Properties().rarity(Rarity.UNCOMMON)));









    //strange
    public static final RegistryObject<Item> LIGHTNING_LOGO = ITEMS.register("lightning_logo",
            () -> new Item(new Item.Properties().rarity(Rarity.RARE)));


}
