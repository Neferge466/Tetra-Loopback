package com.tetra_loopback.block.entity;

import com.tetra_loopback.TLbRegistry;
import com.tetra_loopback.Tetra_loopback;
import com.tetra_loopback.block.AncientForgeBlock;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class TLbBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, Tetra_loopback.MODID);

    public static final RegistryObject<BlockEntityType<AncientForgeBlockEntity>> ANCIENT_FORGE =
            BLOCK_ENTITIES.register("ancient_forge",
                    () -> BlockEntityType.Builder.of(AncientForgeBlockEntity::new, TLbRegistry.ANCIENT_FORGE.get()).build(null));
}