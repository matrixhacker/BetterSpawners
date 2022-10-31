package twdne.silktouchspawners.mixin;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.MobSpawnerBlockEntity;
import net.minecraft.entity.AreaEffectCloudEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.poi.PointOfInterestStorage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import twdne.silktouchspawners.SilkTouchSpawners;

import java.util.Objects;

@Mixin(LivingEntity.class)
public abstract class OnDeathMixin {

	@Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;setPose(Lnet/minecraft/entity/EntityPose;)V"), method = "onDeath")
	public void checkDeathNearBlankSpawner(DamageSource damageSource, CallbackInfo ci) {
		LivingEntity entity = (LivingEntity) (Object) this;

		// Ignore if client or if death not caused by damage
		if (entity.getWorld().isClient() || damageSource.isOutOfWorld())
			return;

		// Ignore if entity is not of accepted type
		if (!(entity instanceof HostileEntity))
			return;

		// Search for nearby spawner and update with mob if found
		ServerWorld world = (ServerWorld) entity.getWorld();
		world.getPointOfInterestStorage().getPositions(
				pointOfInterestTypeRegistryEntry -> true,
				blockPos -> {
					// Check POI spawner. Must be blank (using AreaEffectCloud) and within the configured search distance.
					if (!(((MobSpawnerBlockEntity) Objects.requireNonNull(world.getBlockEntity(blockPos))).getLogic().getRenderedEntity(world) instanceof AreaEffectCloudEntity))
						return false;
					return blockPos.isWithinDistance(entity.getBlockPos(), SilkTouchSpawners.SPAWNER.searchDistance());
				},
				entity.getBlockPos(),
				SilkTouchSpawners.SPAWNER.searchDistance() + 1,
				PointOfInterestStorage.OccupationStatus.ANY
		).findAny().ifPresent(blockPos -> {
			// Found nearby blank spawner. Update with mob entity.
			BlockState spawnerState = world.getBlockState(blockPos);
			MobSpawnerBlockEntity spawner = (MobSpawnerBlockEntity) world.getBlockEntity(blockPos);
			assert spawner != null;
			spawner.getLogic().setEntityId(entity.getType());
			spawner.readNbt(spawner.getLogic().writeNbt(new NbtCompound()));
			spawner.markDirty();
			world.updateListeners(blockPos, spawnerState, spawnerState, Block.NOTIFY_ALL);
		});
	}
}
