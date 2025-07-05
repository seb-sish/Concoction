package net.mcreator.concoction.mixins;

import net.mcreator.concoction.init.ConcoctionModMobEffects;
import net.minecraft.core.Holder;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.monster.Slime;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(LivingEntity.class)
public class LivingEntityMixin {
    @ModifyVariable(
            method = "hurt",
            at = @At(value = "HEAD"),
            argsOnly = true
    )
    private float concoction$applySaltnessDamage(float amount, DamageSource source) {
        if (source.getEntity() instanceof Player player) {
            MobEffectInstance saltnessEffect = player.getEffect(ConcoctionModMobEffects.SALTNESS);
            if (saltnessEffect != null) {
                LivingEntity target = (LivingEntity) (Object) this;
                if (target.getType().getCategory() == MobCategory.MONSTER || target instanceof Slime) {
                    int amplifier = saltnessEffect.getAmplifier();
                    return amount * (1.25f + 0.25f * (amplifier + 1));
                }
            }
        }
        return amount;
    }
} 