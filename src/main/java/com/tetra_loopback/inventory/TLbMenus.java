package com.tetra_loopback.inventory;


import com.tetra_loopback.Tetra_loopback;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class TLbMenus {
    public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister
            .create(ForgeRegistries
                    .MENU_TYPES, Tetra_loopback.MODID);
    public static final RegistryObject<MenuType<AncientForgeMenu>> ANCIENT_FORGE_MENU = MENUS
            .register("ancient_forge", () -> IForgeMenuType
                    .create(AncientForgeMenu::new));
}
