package twdne.silktouchspawners.mixin;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.poi.PointOfInterestStorage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import twdne.silktouchspawners.block.BetterSpawnerBlock;

@Mixin(LivingEntity.class)
public abstract class OnDeathMixin {

	/**
	 * On entity death, if type of configured entity, look for nearby blank spawner blocks
	 * and update spawned entity type if found.
	 */
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
				// Check that located POI spawner is blank.
				return new BetterSpawnerBlock(world, blockPos).isBlankSpawner();
			},
			entity.getBlockPos(),
			BetterSpawnerBlock.POI_SPAWNER.searchDistance(),
			PointOfInterestStorage.OccupationStatus.ANY
		).findAny().ifPresent(blockPos -> {
			// Found nearby blank spawner. Update with mob entity.
			new BetterSpawnerBlock(world, blockPos).updateSpawnerEntity(entity.getType());
		});
	}
}
