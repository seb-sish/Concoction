package net.mcreator.concoction.procedures;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.effect.MobEffectInstance;

import net.mcreator.concoction.init.ConcoctionModMobEffects;

public class ObsidianTearsBottlePlayerFinishesUsingItemProcedure {
	public static void execute(Entity entity) {
		if (entity == null)
			return;
		if (entity instanceof LivingEntity _entity && !_entity.level().isClientSide())
			_entity.addEffect(new MobEffectInstance(ConcoctionModMobEffects.INSTABILITY, 3600, 0, false, true));
	}
}
