package net.mcreator.concoction.item;

import net.minecraft.core.Holder;
import net.minecraft.world.level.Level;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.ArrayList;

public class MilkBottleItem extends Item {
    public MilkBottleItem() {
        super(new Item.Properties()
            .stacksTo(16)
            .rarity(Rarity.COMMON)
            .food((new FoodProperties.Builder())
                .nutrition(0)
                .saturationModifier(0f)
                .alwaysEdible()
                .build()
            )
        );
    }

    @Override
    public UseAnim getUseAnimation(ItemStack itemstack) {
        return UseAnim.DRINK;
    }

    @Override
    public @NotNull SoundEvent getEatingSound() {
        return SoundEvents.GENERIC_DRINK;
    }

    @Override
    public boolean hasCraftingRemainingItem() {
        return true;
    }

    @Override
    public @NotNull ItemStack getCraftingRemainingItem(@NotNull ItemStack stack) {
        return new ItemStack(Items.GLASS_BOTTLE);
    }

    @Override
    public ItemStack finishUsingItem(ItemStack itemstack, Level world, LivingEntity entity) {
        // Give back empty bottle as usual
        ItemStack retval = new ItemStack(Items.GLASS_BOTTLE);
        super.finishUsingItem(itemstack, world, entity);

        // Only apply effect removal on the server side
        if (!world.isClientSide && entity instanceof LivingEntity) {
            // Gather all active effects
            List<MobEffectInstance> effects = new ArrayList<>(entity.getActiveEffects());
            if (!effects.isEmpty()) {
                // Pick one at random
                RandomSource rand = world.getRandom();
                MobEffectInstance toRemove = effects.get(rand.nextInt(effects.size()));
                // Use the new Holder<MobEffect> API
                Holder<net.minecraft.world.effect.MobEffect> effectHolder = toRemove.getEffect();
                // Remove that effect
                entity.removeEffect(effectHolder);
            }
        }

        // Handle inventory return
        if (itemstack.isEmpty()) {
            return retval;
        } else {
            if (entity instanceof Player player && !player.getAbilities().instabuild) {
                if (!player.getInventory().add(retval))
                    player.drop(retval, false);
            }
            return itemstack;
        }
    }
}
