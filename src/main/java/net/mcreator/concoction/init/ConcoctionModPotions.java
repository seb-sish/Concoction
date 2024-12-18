
/*
 *    MCreator note: This file will be REGENERATED on each build.
 */
package net.mcreator.concoction.init;

import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;

import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.core.registries.Registries;

import net.mcreator.concoction.ConcoctionMod;

public class ConcoctionModPotions {
	public static final DeferredRegister<Potion> REGISTRY = DeferredRegister.create(Registries.POTION, ConcoctionMod.MODID);
	public static final DeferredHolder<Potion, Potion> SNOWFLAKE = REGISTRY.register("snowflake", () -> new Potion(new MobEffectInstance(ConcoctionModMobEffects.FROST_TOUCH, 3600, 0, false, true)));
	public static final DeferredHolder<Potion, Potion> SNOWFLAKE_EXTENDED = REGISTRY.register("snowflake_extended", () -> new Potion(new MobEffectInstance(ConcoctionModMobEffects.FROST_TOUCH, 9600, 0, false, true)));
	public static final DeferredHolder<Potion, Potion> FLAME = REGISTRY.register("flame", () -> new Potion(new MobEffectInstance(ConcoctionModMobEffects.FIERY_TOUCH, 3600, 0, false, true)));
	public static final DeferredHolder<Potion, Potion> FLAME_EXTENDED = REGISTRY.register("flame_extended", () -> new Potion(new MobEffectInstance(ConcoctionModMobEffects.FIERY_TOUCH, 9600, 0, false, true)));
}
