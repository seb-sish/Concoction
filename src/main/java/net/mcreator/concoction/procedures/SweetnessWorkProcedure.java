package net.mcreator.concoction.procedures;

import net.neoforged.neoforge.event.entity.living.LivingEntityUseItemEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.bus.api.Event;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.core.component.DataComponents;

import net.mcreator.concoction.init.ConcoctionModMobEffects;

import javax.annotation.Nullable;

import static com.google.common.primitives.Floats.max;
import static com.google.common.primitives.Floats.min;
import static net.minecraft.util.Mth.ceil;

@EventBusSubscriber
public class SweetnessWorkProcedure {
	@SubscribeEvent
	public static void onUseItemFinish(LivingEntityUseItemEvent.Finish event) {
		if (event.getEntity() != null) {
			execute(event, event.getEntity(), event.getItem());
		}
	}

	public static void execute(Entity entity, ItemStack itemstack) {
		execute(null, entity, itemstack);
	}

	private static void execute(@Nullable Event event, Entity entity, ItemStack itemstack) {
		if (entity == null)
			return;
		if (entity instanceof LivingEntity _livEnt0 && _livEnt0.hasEffect(ConcoctionModMobEffects.SWEETNESS)) {
			if (itemstack.has(DataComponents.FOOD)) {
				if (entity instanceof Player _player) {
					int foodLevel = _player.getFoodData().getFoodLevel();
					int hunger = 20 - foodLevel;
					int effectLevel = _player.getEffect(ConcoctionModMobEffects.SWEETNESS).getAmplifier();
					float percents = min(0.35f + 0.15f * effectLevel, 1f);
					int newFoodLevel = ceil(foodLevel + hunger * percents);
					_player.getFoodData().setFoodLevel(newFoodLevel);
				}

//					_player.getFoodData().setFoodLevel((int) ((entity instanceof Player _plr ? _plr.getFoodData().getFoodLevel() : 0) + (entity instanceof LivingEntity _livEnt && _livEnt.hasEffect(ConcoctionModMobEffects.SWEETNESS) ? _livEnt.getEffect(ConcoctionModMobEffects.SWEETNESS).getAmplifier() : 0) + 1));
			}
		}
	}
}
