package net.mcreator.concoction.custom;

import net.mcreator.concoction.init.ConcoctionModMobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

import java.util.Comparator;
import java.util.Optional;

public class FollowSaltyPlayerGoal extends Goal {
    private final Mob mob;
    private Optional<LivingEntity> target = Optional.empty();

    public FollowSaltyPlayerGoal(Mob mob) {
        this.mob = mob;
    }

    @Override
    public boolean canUse() {
        Level level = mob.level();
        if (level.isClientSide()) return false;
        AABB aabb = mob.getBoundingBox().inflate(8);
        target = level.getEntitiesOfClass(
                LivingEntity.class, aabb,
                livingEntity -> livingEntity.hasEffect(ConcoctionModMobEffects.SALTNESS)
        ).stream().min(Comparator.comparingDouble(entity -> entity.distanceToSqr(mob)));
        return target.isPresent();
    }

    @Override
    public void tick() {
        target.ifPresent(livingEntity -> mob.getNavigation().moveTo(livingEntity.getX(), livingEntity.getY() + 1.0, livingEntity.getZ(), 1.1));
    }

    @Override
    public boolean canContinueToUse() {
        return canUse();
    }
} 