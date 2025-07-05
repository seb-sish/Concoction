package net.mcreator.concoction.mixins;

import net.mcreator.concoction.custom.FollowSaltyPlayerGoal;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.horse.Horse;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Horse.class)
public abstract class HorseMixin {

    @Inject(method = "<init>", at = @At("RETURN"))
    private void concoction$addSaltyFollow(EntityType<? extends Horse> entityType, Level level, CallbackInfo ci) {
        Horse horse = (Horse) (Object) this;
        horse.goalSelector.addGoal(5, new FollowSaltyPlayerGoal(horse));
    }
}
