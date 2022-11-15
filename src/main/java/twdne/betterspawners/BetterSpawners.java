package twdne.betterspawners;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.entity.EntityType;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import twdne.betterspawners.block.BetterSpawnerBlock;
import twdne.betterspawners.config.Config;
import twdne.betterspawners.config.ConfigManager;
import twdne.betterspawners.event.EventHandler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * BetterSpawners mod main class
 */
public class BetterSpawners implements ModInitializer {
	private static final EventHandler eventHandler = new EventHandler();
	public static final Logger LOGGER = LogManager.getLogger(BetterSpawners.class.getSimpleName());

	public static Config<Boolean> SILKTOUCH;
	public static Config<Boolean> BLANK_SPAWNER;
	public static Config<Item> BLANK_SPAWNER_ITEM;
	public static Config<Boolean> SPAWN_HOSTILE_MOBS;
	public static Config<Boolean> SPAWN_PASSIVE_MOBS;
	public static Config<List<EntityType<?>>> SPAWN_SPECIFIC_MOBS;
	public static Config<Integer> SPAWNER_DEATH_RADIUS;
	public static Config<List<Item>> SPAWNER_MINING_TOOLS;

	/**
	 * Initialize mod
	 */
	@Override
	public void onInitialize() {
		PlayerBlockBreakEvents.BEFORE.register(eventHandler::onBlockBreak);
		UseBlockCallback.EVENT.register(eventHandler::onBlockInteract);

		initConfig();
		BetterSpawnerBlock.init();
	}

	/**
	 * Initialize all Configs
	 */
	private void initConfig() {
		ConfigManager configManager = new ConfigManager(BetterSpawners.class.getSimpleName().toLowerCase());
		SILKTOUCH = configManager.createConfig(
				"enable_silktouch_spawner",
				true,
				"Enable or disable the ability to mine spawners with a silktouch tool.",
				Boolean::parseBoolean
		);
		BLANK_SPAWNER = configManager.createConfig(
				"enable_clear_spawner",
				true,
				"Enable or disable the ability to clear spawners using a specific item.",
				Boolean::parseBoolean
		);
		BLANK_SPAWNER_ITEM = configManager.createConfig(
				"clear_spawner_item",
				Items.END_CRYSTAL,
				"The item that can be used to clear a spawner; ignored if disabled.",
				ConfigManager.parseItem
		);
		SPAWN_HOSTILE_MOBS = configManager.createConfig(
				"enable_spawner_hostile_mobs",
				true,
				"Enable or disable a spawners ability to spawn hostile mobs.",
				Boolean::parseBoolean
		);
		SPAWN_PASSIVE_MOBS = configManager.createConfig(
				"enable_spawner_passive_mobs",
				false,
				"Enable or disable a spawners ability to spawn passive mobs.",
				Boolean::parseBoolean
		);
		SPAWN_SPECIFIC_MOBS = configManager.createConfig(
				"enable_spawner_mob_list",
				new ArrayList<>(),
				"A list of mobs a spawner can be made to spawn. This overrides hostile and passive mob settings.",
				ConfigManager.parseEntityTypeList,
				ConfigManager.entityTypeListToString
		);
		SPAWNER_DEATH_RADIUS = configManager.createConfig(
				"spawner_death_radius",
				2,
				"The radius within a mob must die from a blank spawner for it to spawn that mob, if mob is configured to be spawnable. Min=1 Max=48",
				intStr -> {
					int radius = Integer.parseInt(intStr),
					minRadius = 1, maxRadius = 48;
					if (radius < minRadius || radius > maxRadius) {
						LOGGER.warn("Invalid value {}; {} <= radius <= {}", radius, minRadius, maxRadius);
						return null;
					}
					return radius;
				}
		);
		SPAWNER_MINING_TOOLS = configManager.createConfig(
				"spawner_mining_tools",
				new ArrayList<>(Arrays.asList(Items.STONE_PICKAXE, Items.IRON_PICKAXE, Items.GOLDEN_PICKAXE, Items.DIAMOND_PICKAXE, Items.NETHERITE_PICKAXE)),
				"The silktouch tools that can be used to mine a spawner. This does not affect mining speed.",
				ConfigManager.parseItemList,
				ConfigManager.itemListToString
		);
		configManager.loadAndSaveConfigs();
	}
}
