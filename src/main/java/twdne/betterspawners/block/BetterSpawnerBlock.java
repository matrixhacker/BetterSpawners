package twdne.betterspawners.block;

import net.fabricmc.fabric.api.object.builder.v1.world.poi.PointOfInterestHelper;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.MobSpawnerBlockEntity;
import net.minecraft.entity.AreaEffectCloudEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.MobSpawnerLogic;
import net.minecraft.world.World;
import net.minecraft.world.poi.PointOfInterestType;
import twdne.betterspawners.BetterSpawners;

import java.util.Objects;

/**
 * Contains logic for handling Spawner blocks when broken or interacted with.
 */
public class BetterSpawnerBlock {
	private final World world;
	private final BlockPos spawnerBlockPos;
	private final EntityType<?> blankSpawnerType = EntityType.AREA_EFFECT_CLOUD;

	/**
	 * Register a Spawner block as a PointOfInterestType that it is searchable
	 */
	public static final PointOfInterestType POI_SPAWNER = PointOfInterestHelper.register(new Identifier("spawner"), 1, BetterSpawners.SPAWNER_DEATH_RADIUS.getValue(), Blocks.SPAWNER);

	/**
	 * Needed so that the POI is initialized before the registry is frozen.
	 */
	public static void init() {}

	/**
	 * Saves the details for a spawner block found in the world.
	 * Throws an IllegalArgumentException if the block at spawnerBlockPos is not a spawner
	 * @param world - the world the spawner block is in
	 * @param spawnerBlockPos - the position of the spawner block
	 */
	public BetterSpawnerBlock(World world, BlockPos spawnerBlockPos) {
		this.world = world;
		this.spawnerBlockPos = spawnerBlockPos;
		if (world.getBlockState(spawnerBlockPos).getBlock() != Blocks.SPAWNER)
			throw new IllegalArgumentException("Block at pos " + spawnerBlockPos + " is not of type SPAWNER.");
	}

	/**
	 * I wanted to do this in a loot table as json files are simpler.
	 * Unfortunately I was unable to drop a spawner with a custom name based on the spawned entity.
	 * This method is less simplistic but perfectly functional.
	 * @param player - the player
	 * @param entity - the entity
	 * @return always true since false would break the callback cycle.
	 */
	public boolean onBlockBreak(PlayerEntity player, BlockEntity entity) {
		// Pass if disabled in config
		if (!BetterSpawners.SILKTOUCH.getValue())
			return true;

		// Pass if item is not in list of mining tools or does not have silktouch
		ItemStack stack = player.getMainHandStack();//Iterables.get(player.getHandItems(), 0);
		if (!BetterSpawners.SPAWNER_MINING_TOOLS.getValue().contains(stack.getItem()) || !stack.getEnchantments().asString().contains("silk_touch"))
			return true;

		// Spawn spawner with correct name and data
		MobSpawnerLogic logic = ((MobSpawnerBlockEntity) entity).getLogic();

		// Copy current spawner data to new spawner
		NbtCompound nbt = logic.writeNbt(new NbtCompound());
		ItemStack itemStack = new ItemStack(Registry.ITEM.get(new Identifier("spawner")));
		NbtCompound new_nbt = new NbtCompound();
		new_nbt.put("SpawnData", nbt.get("SpawnData"));
		new_nbt.put("SpawnPotentials", nbt.get("SpawnPotentials"));
		itemStack.setSubNbt("BlockEntityTag", new_nbt);

		// Apply custom name
		Entity spawn_entity = Objects.requireNonNull(logic.getRenderedEntity(world));
		if (!(spawn_entity instanceof AreaEffectCloudEntity)) {
			String entity_name = spawn_entity.getName().getString();
			itemStack.setCustomName(Text.of(entity_name + " Spawner"));
		}

		// Get random fly-out position offsets
		double d0 = (double) (world.getRandom().nextFloat() * 0.6F) + (double) 0.12F;
		double d1 = (double) (world.getRandom().nextFloat() * 0.6F) + (double) 0.07F + 0.7D;
		double d2 = (double) (world.getRandom().nextFloat() * 0.6F) + (double) 0.12F;

		// Create entity item and spawn
		ItemEntity entityItem = new ItemEntity(world, (double) spawnerBlockPos.getX() + d0, (double) spawnerBlockPos.getY() + d1, (double) spawnerBlockPos.getZ() + d2, itemStack);
		entityItem.setToDefaultPickupDelay();
		world.spawnEntity(entityItem);

		return true;
	}

	/**
	 * Called when player right-clicks a spawner block.
	 * Will clear the spawn entity from a spawner if holding an end crystal.
	 * @param player - the player
	 * @param hand - the player's hand
	 * @return PASS when no action is required, SUCCESS otherwise
	 */
	public ActionResult onBlockInteract(PlayerEntity player, Hand hand) {
		// Pass if disabled in config
		if (!BetterSpawners.BLANK_SPAWNER.getValue())
            return ActionResult.PASS;

		// Pass if off-hand action or not BLANK_SPAWNER_ITEM
		if (hand == Hand.OFF_HAND || !(player.getMainHandStack().getItem().equals(BetterSpawners.BLANK_SPAWNER_ITEM.getValue())))
			return ActionResult.PASS;

		// Clear entity from spawner. Since a spawner must have an entity use blankSpawnerType.
		updateSpawnerEntity(blankSpawnerType);

		// Consume item if not in creative mode
		if (!player.isCreative())
			player.getMainHandStack().decrement(1);

		return ActionResult.SUCCESS;
	}

	/**
	 * Update the spawner with the supplied entity type.
	 * @param spawnEntityType - the EntityType being spawned
	 */
	public void updateSpawnerEntity(EntityType<?> spawnEntityType) {
		BlockState spawnerState = world.getBlockState(spawnerBlockPos);
		MobSpawnerBlockEntity spawner = Objects.requireNonNull((MobSpawnerBlockEntity) world.getBlockEntity(spawnerBlockPos));
		spawner.getLogic().setEntityId(spawnEntityType);
		spawner.readNbt(spawner.getLogic().writeNbt(new NbtCompound()));
		spawner.markDirty();
		world.updateListeners(spawnerBlockPos, spawnerState, spawnerState, Block.NOTIFY_ALL);
	}

	/**
	 * Check to see if this spawner is blank and close enough to the given mob.
	 * @return true if blank and within configured distance; false otherwise
	 */
	public boolean isBlankSpawner() {
		return Objects.requireNonNull(((MobSpawnerBlockEntity) Objects.requireNonNull(world.getBlockEntity(spawnerBlockPos))).getLogic().getRenderedEntity(world)).getType() == blankSpawnerType;
	}
}
