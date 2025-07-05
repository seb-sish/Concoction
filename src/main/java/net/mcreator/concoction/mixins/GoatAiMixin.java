package net.mcreator.concoction.mixins;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Pair;
import net.mcreator.concoction.custom.FollowSaltyPlayerTask;
import net.minecraft.world.entity.animal.goat.Goat;
import net.minecraft.world.entity.animal.goat.GoatAi;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.schedule.Activity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(GoatAi.class)
public class GoatAiMixin {
    @Inject(method = "initCoreActivity", at = @At("TAIL"))
    private static void concoction$addSaltyFollow(Brain<Goat> brain, CallbackInfo ci) {
        brain.addActivity(Activity.CORE, 0, ImmutableList.of(new FollowSaltyPlayerTask<>()));
    }
} 