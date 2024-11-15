package net.mcreator.concoction.procedures;

import net.neoforged.neoforge.event.entity.living.LivingEntityUseItemEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.bus.api.Event;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.tags.ItemTags;
import net.minecraft.resources.ResourceLocation;

import net.mcreator.concoction.init.ConcoctionModMobEffects;

import javax.annotation.Nullable;

@EventBusSubscriber
public class SweetnessFoodEatenProcedure {
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
			if (itemstack.is(ItemTags.create(ResourceLocation.parse("c:foods")))) {
				if (!(itemstack.is(ItemTags.create(ResourceLocation.parse("c:sweetened_foods"))) || itemstack.is(ItemTags.create(ResourceLocation.parse("c:sweet_foods"))) || itemstack.is(ItemTags.create(ResourceLocation.parse("c:sugary_foods")))
						|| itemstack.is(ItemTags.create(ResourceLocation.parse("c:candied_foods"))))) {
					if (entity instanceof Player _player)
						_player.getFoodData().setFoodLevel((int) ((entity instanceof Player _plr ? _plr.getFoodData().getFoodLevel() : 0)
								+ (entity instanceof LivingEntity _livEnt && _livEnt.hasEffect(ConcoctionModMobEffects.SWEETNESS) ? _livEnt.getEffect(ConcoctionModMobEffects.SWEETNESS).getAmplifier() : 0) + 1));
				}
			}
		}
	}
}
