package net.mcreator.concoction.custom;

import com.google.common.collect.ImmutableMap;
import net.mcreator.concoction.init.ConcoctionModMobEffects;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.behavior.EntityTracker;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.entity.player.Player;

import java.util.Optional;
import java.util.function.Predicate;

public class FollowSaltyPlayerTask<E extends LivingEntity> extends Behavior<E> {
    private final float speedModifier;
    private Player targetPlayer;
    private final Predicate<Player> followPredicate;

    public FollowSaltyPlayerTask() {
        super(ImmutableMap.of(
                MemoryModuleType.WALK_TARGET, MemoryStatus.VALUE_ABSENT,
                MemoryModuleType.LOOK_TARGET, MemoryStatus.VALUE_ABSENT
        ));
        this.speedModifier = 1.1F;
        this.followPredicate = player -> player != null && player.isAlive() && player.hasEffect(ConcoctionModMobEffects.SALTNESS);
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel pLevel, E pOwner) {
        Optional<Player> optionalPlayer = pLevel.getEntitiesOfClass(Player.class, pOwner.getBoundingBox().inflate(10.0D), followPredicate).stream().findFirst();

        if (optionalPlayer.isPresent()) {
            this.targetPlayer = optionalPlayer.get();
            return true;
        }
        return false;
    }

    @Override
    protected boolean canStillUse(ServerLevel pLevel, E pEntity, long pGameTime) {
        return this.targetPlayer != null && this.followPredicate.test(this.targetPlayer) && pEntity.distanceToSqr(this.targetPlayer) < 100.0D; // 10 blocks radius
    }

    @Override
    protected void start(ServerLevel pLevel, E pEntity, long pGameTime) {
        updateWalkAndLookTarget(pEntity);
    }
    
    @Override
    protected void tick(ServerLevel pLevel, E pOwner, long pGameTime) {
        if (pOwner.distanceToSqr(this.targetPlayer) > 6.25D) { // ~2.5 blocks distance
            updateWalkAndLookTarget(pOwner);
        }
    }

    private void updateWalkAndLookTarget(E entity) {
        Brain<?> brain = entity.getBrain();
        brain.setMemory(MemoryModuleType.LOOK_TARGET, new EntityTracker(this.targetPlayer, true));
        brain.setMemory(MemoryModuleType.WALK_TARGET, new WalkTarget(this.targetPlayer, this.speedModifier, 2));
    }

    @Override
    protected void stop(ServerLevel pLevel, E pEntity, long pGameTime) {
        this.targetPlayer = null;
        pEntity.getBrain().eraseMemory(MemoryModuleType.WALK_TARGET);
        pEntity.getBrain().eraseMemory(MemoryModuleType.LOOK_TARGET);
    }
}