package net.mcreator.concoction.procedures;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.effect.MobEffectInstance;

import net.mcreator.concoction.init.ConcoctionModMobEffects;

public class MintBrewDrinkProcedure {
	public static void execute(Entity entity) {
		if (entity == null)
			return;
		if (entity instanceof LivingEntity _entity && !_entity.level().isClientSide())
			_entity.addEffect(new MobEffectInstance(ConcoctionModMobEffects.MINTY_BREATH, 60*20, 2-1, false, false, true, null));
	}
}
