package net.mcreator.concoction.procedures;

import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.Level;
import net.minecraft.util.RandomSource;
import net.minecraft.util.Mth;
import net.minecraft.sounds.SoundSource;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.BlockPos;

public class NetherPepperCropOnTickUpdateProcedure {
    public static void execute(LevelAccessor world, double x, double y, double z) {
        // Normalize to day cycle
        long time = world.dayTime() % 24000;
        if (time >= 17950 && time <= 18050) {
            // Soul particles
            if (Math.random() < 0.65) {
                world.addParticle(
                    ParticleTypes.SOUL,
                    x + Mth.nextDouble(RandomSource.create(), 0.2, 0.8),
                    y + Mth.nextDouble(RandomSource.create(), 0.2, 0.8),
                    z + Mth.nextDouble(RandomSource.create(), 0.2, 0.8),
                    Mth.nextDouble(RandomSource.create(), -0.13, 0.13),
                    Mth.nextDouble(RandomSource.create(), 0.13, 0.2),
                    Mth.nextDouble(RandomSource.create(), -0.13, 0.13)
                );
            }
            // Soul escape sound
            if (Math.random() < 0.1) {
                if (world instanceof Level _level) {
                    ResourceLocation soulEscape = ResourceLocation.parse("particle.soul_escape");
                    float pitch = (float)(0.8 + Math.random() * 0.4); // Random pitch between 0.8 and 1.2
                    if (!_level.isClientSide()) {
                        _level.playSound(
                            null,
                            BlockPos.containing(x, y, z),
                            BuiltInRegistries.SOUND_EVENT.get(soulEscape),
                            SoundSource.BLOCKS,
                            0.6F, // 60% volume
                            pitch
                        );
                    } else {
                        _level.playLocalSound(
                            x, y, z,
                            BuiltInRegistries.SOUND_EVENT.get(soulEscape),
                            SoundSource.BLOCKS,
                            0.6F,
                            pitch,
                            false
                        );
                    }
                }
            }
            // Small chance for Ghast scream
            if (Math.random() < 0.02) {
                if (world instanceof Level _level) {
                    float pitch = (float)(0.5 + ( Math.random() * 0.5 )); // Random pitch between 0.8 and 1.2
                    if (!_level.isClientSide()) {
                        _level.playSound(
                            null,
                            BlockPos.containing(x, y, z),
                            SoundEvents.GHAST_HURT,
                            SoundSource.HOSTILE,
                            0.4F, // 60% volume
                            pitch
                        );
                    } else {
                        _level.playLocalSound(
                            x, y, z,
                            SoundEvents.GHAST_HURT,
                            SoundSource.HOSTILE,
                            0.4F,
                            pitch,
                            false
                        );
                    }
                }
            }
        }
    }
}
