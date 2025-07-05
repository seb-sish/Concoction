package net.mcreator.concoction.mixins;

import net.mcreator.concoction.init.ConcoctionModMobEffects;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(net.minecraft.world.item.PotionItem.class)
public class PotionItemMixin {

    @Redirect(method = "finishUsingItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;addEffect(Lnet/minecraft/world/effect/MobEffectInstance;Lnet/minecraft/world/entity/Entity;)Z"))
    private boolean concoction$amplifyPotionEffect(MobEffectInstance originalEffect, @Nullable Entity source) {
        // Проверяем, есть ли у игрока эффект "Солёность"
        MobEffectInstance saltnessEffect = ((LivingEntity) source).getEffect(ConcoctionModMobEffects.SALTNESS);

        // Если эффект есть, и его уровень 2 или выше (усилитель >= 1)
        if (saltnessEffect != null && saltnessEffect.getAmplifier() >= 1) {
            
            // Создаем новый, усиленный эффект
            MobEffectInstance amplifiedEffect = new MobEffectInstance(
                    originalEffect.getEffect(),
                    originalEffect.getDuration(),
                    originalEffect.getAmplifier() + 1,
                    originalEffect.isAmbient(),
                    originalEffect.isVisible(),
                    originalEffect.showIcon()
            );

            // Применяем наш новый, усиленный эффект, передавая оригинальный 'source'
            return receiver.addEffect(amplifiedEffect, source);
        }

        // Если условия не выполнены, просто применяем оригинальный эффект без изменений
        return receiver.addEffect(originalEffect, source);
    }
} 