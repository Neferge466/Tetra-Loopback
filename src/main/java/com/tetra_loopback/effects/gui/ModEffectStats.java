package com.tetra_loopback.effects.gui;

import com.tetra_loopback.Tetra_loopback;
import com.tetra_loopback.effects.gui.client.ClientGuiRegistry;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import se.mickelus.tetra.effect.ItemEffect;

public class ModEffectStats {
    public static final ItemEffect guardianEffect = ItemEffect.get(Tetra_loopback.MODID + ":guardian");
    public static final ItemEffect flameAdmonitionEffect = ItemEffect.get(Tetra_loopback.MODID + ":flame_admonition");
    public static final ItemEffect frostCrownEffect = ItemEffect.get(Tetra_loopback.MODID + ":frost_crown");
    public static final ItemEffect etherealGlowEffect = ItemEffect.get(Tetra_loopback.MODID + ":ethereal_glow");
    public static final ItemEffect runeCreedEffect = ItemEffect.get(Tetra_loopback.MODID + ":rune_creed");
    public static final ItemEffect crimsonScourgeEffect = ItemEffect.get(Tetra_loopback.MODID + ":crimson_scourge");
    public static final ItemEffect strifeEffect = ItemEffect.get(Tetra_loopback.MODID + ":strife");

    public static final ItemEffect resonanceEffect = ItemEffect.get(Tetra_loopback.MODID + ":resonance");
    public static final ItemEffect visionFieldEffect = ItemEffect.get(Tetra_loopback.MODID + ":vision_field");

    public static final ItemEffect rainstormEffect = ItemEffect.get(Tetra_loopback.MODID + ":rainstorm");
    public static final ItemEffect windLightEffect = ItemEffect.get(Tetra_loopback.MODID + ":wind_light");

    public static final ItemEffect temperedColdEffect = ItemEffect.get(Tetra_loopback.MODID + ":tempered_cold");
    public static final ItemEffect supercoolingEffect = ItemEffect.get(Tetra_loopback.MODID + ":supercooling");
    public static final ItemEffect lifeEssenceEffect = ItemEffect.get(Tetra_loopback.MODID + ":life_essence");
    public static final ItemEffect primalLeechEffect = ItemEffect.get(Tetra_loopback.MODID + ":primal_leech");
    public static final ItemEffect telluricAnchorEffect = ItemEffect.get(Tetra_loopback.MODID + ":telluric_anchor");
    public static final ItemEffect astralPhaseEffect = ItemEffect.get(Tetra_loopback.MODID + ":astral_phase");
    public static final ItemEffect earthenShieldEffect = ItemEffect.get(Tetra_loopback.MODID + ":earthen_shield");
    public static final ItemEffect lightningStrikeEffect = ItemEffect.get(Tetra_loopback.MODID + ":lightning_strike");
    public static final ItemEffect vitalShieldEffect = ItemEffect.get(Tetra_loopback.MODID + ":vital_shield");

    public static void safeInit() {
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> ClientGuiRegistry::registerAllBars);
    }
}
