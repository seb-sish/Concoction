
package net.mcreator.concoction.block;

import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.core.BlockPos;
import net.mcreator.concoction.init.ConcoctionModItems;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.common.SpecialPlantable;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FarmBlock;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import java.util.Random;
import net.minecraft.world.level.Level;
import net.minecraft.util.RandomSource;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.mcreator.concoction.init.ConcoctionModParticleTypes;


public class CropPuffballBlock extends CropBlock {
	public static final int MAX_AGE = 2;
	public static final IntegerProperty AGE = IntegerProperty.create("age", 0, MAX_AGE);

	public CropPuffballBlock() {
		super(BlockBehaviour.Properties.of().mapColor(MapColor.CLAY).sound(SoundType.GRASS).instabreak().noCollission().noOcclusion().randomTicks().pushReaction(PushReaction.DESTROY).isRedstoneConductor((bs, br, bp) -> false));
		this.registerDefaultState(this.stateDefinition.any().setValue(AGE, 0));
	}

	@Override
	public boolean propagatesSkylightDown(BlockState state, BlockGetter reader, BlockPos pos) {
		return true;
	}

	@Override
	public int getLightBlock(BlockState state, BlockGetter worldIn, BlockPos pos) {
		return 0;
	}

	@Override
	public VoxelShape getVisualShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
		return Shapes.empty();
	}

	@Override
	public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
		// Форма блока в зависимости от возраста
		return switch (state.getValue(AGE)) {
			default -> Block.box(0, 0, 0, 16, 1, 16);
			case 0 -> Block.box(0, 0, 0, 16, 1, 16);
			case 1 -> Block.box(6, 1, 6, 10, 4, 10);
			case 2 -> Block.box(4, 1, 4, 12, 7, 12);
		};
	}
	
	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		// Добавление свойства возраста в состояние блока
		builder.add(AGE);
	}
	
	@Override
	public ItemStack getCloneItemStack(LevelReader level, BlockPos pos, BlockState state) {
		// Предмет, получаемый при копировании блока на колёсико
		return new ItemStack(
            ConcoctionModItems.PUFFBALL_SPORES.get()
            );
	}

	@Override
	public boolean mayPlaceOn(BlockState state, BlockGetter worldIn, BlockPos pos) {
	    // Convert BlockGetter to LevelReader since canSurvive expects that
	    if (!(worldIn instanceof LevelReader)) return false;
	    return canSurvive(state, (LevelReader) worldIn, pos);
	}


	@Override
protected boolean canSurvive(BlockState state, LevelReader worldIn, BlockPos pos) {
    // Check if the block below can sustain this plant (usually farmland, dirt, etc)
    BlockPos blockBelow = pos.below();
    BlockState soil = worldIn.getBlockState(blockBelow);
    return soil.isSolidRender(worldIn, blockBelow) || soil.getBlock() instanceof FarmBlock || soil.getBlock() instanceof SoullandBlock;
}



	@Override
	public PathType getBlockPathType(BlockState state, BlockGetter world, BlockPos pos, Mob entity) {
		// Тип пути для мобов
		return PathType.OPEN;
	}
	
	@Override
	protected ItemLike getBaseSeedId() {
		// Возвращает семена для посадки растения
		return ConcoctionModItems.PUFFBALL_SPORES.get();
	}

	@Override
    public int getMaxAge() {
        // Возвращает максимальный возраст растения
        return MAX_AGE; // не менять
    }

	@Override
	public IntegerProperty getAgeProperty() {
		// Возвращает свойство возраста растения
		return AGE; // не менять
	}
	
	 @Override
    public void entityInside(BlockState state, Level world, BlockPos pos, net.minecraft.world.entity.Entity entity) {
        if (!world.isClientSide && entity instanceof LivingEntity) {
            int age = state.getValue(AGE);
            if (age == MAX_AGE) {
    			if (entity instanceof LivingEntity livingEntity && !livingEntity.isCrouching()) {
					livingEntity.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 30, 0));
					
                world.setBlock(pos, state.setValue(AGE, age - 1), 3);
				world.playSound(null, pos, SoundEvents.CROP_BREAK, SoundSource.BLOCKS, 1.0f, 1.0f);
                // Emit particles above the block
                if (world instanceof ServerLevel serverWorld) {
                    serverWorld.sendParticles(ConcoctionModParticleTypes.SPORE_CLOUD.get(), pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5,
                            10, 0.5, 0.5, 0.5, 0.01);
                }

                // Attempt to spawn copies around radius 4
                RandomSource random = world.getRandom();

                BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos();

                for (int i = 0; i < 20; i++) { // try 20 times to spawn
                    int dx = random.nextInt(9) - 4; // -4 to 4
                    int dy = random.nextInt(3) - 1; // -1 to 1 (some vertical range)
                    int dz = random.nextInt(9) - 4; // -4 to 4

                    mutablePos.set(pos.getX() + dx, pos.getY() + dy, pos.getZ() + dz);

                    BlockState targetState = world.getBlockState(mutablePos);
                    BlockPos belowPos = mutablePos.below();
                    BlockState soilState = world.getBlockState(belowPos);

                    // Check if position is air and can survive here
                    if (targetState.isAir() && this.canSurvive(this.defaultBlockState().setValue(AGE, 0), world, mutablePos)) {
                        world.setBlock(mutablePos, this.defaultBlockState().setValue(AGE, 0), 3);

                        // Spawn some particles at the spawn location too
                        if (world instanceof ServerLevel serverWorld2) {
                            serverWorld2.sendParticles(ConcoctionModParticleTypes.SPORE_CLOUD.get(), mutablePos.getX() + 0.5, mutablePos.getY() + 1.0, mutablePos.getZ() + 0.5,
                                    5, 0.3, 0.3, 0.3, 0.01);
                        }
                    }
                }
            }
            }
        }
        super.entityInside(state, world, pos, entity);
    }
}
