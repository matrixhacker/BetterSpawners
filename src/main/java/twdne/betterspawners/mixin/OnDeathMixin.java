package twdne.betterspawners.mixin;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.poi.PointOfInterestStorage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import twdne.betterspawners.block.BetterSpawnerBlock;
import twdne.betterspawners.config.ConfigManager;

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
		ConfigManager configManager = ConfigManager.getInstance();
		if (!(configManager.SPAWN_SPECIFIC_MOBS.getValue().contains(entity.getType()) || (entity instanceof HostileEntity && configManager.SPAWN_HOSTILE_MOBS.getValue()) || (entity instanceof PassiveEntity && configManager.SPAWN_PASSIVE_MOBS.getValue())))
			return;

		// Search for nearby spawner and update with mob if found
		ServerWorld world = (ServerWorld) entity.getWorld();
		world.getPointOfInterestStorage().getPosition(
			// Check that the POI type is correct
			pointOfInterestTypeRegistryEntry -> pointOfInterestTypeRegistryEntry.value() == BetterSpawnerBlock.POI_SPAWNER,
			// Check that located POI spawner is blank.
			blockPos -> new BetterSpawnerBlock(world, blockPos).isBlankSpawner(),
			entity.getBlockPos(),
			BetterSpawnerBlock.POI_SPAWNER.searchDistance(),
			PointOfInterestStorage.OccupationStatus.ANY
		).ifPresent(
			// Found nearby blank spawner. Update with mob entity.
			blockPos -> new BetterSpawnerBlock(world, blockPos).updateSpawnerEntity(entity.getType())
		);
	}
}
