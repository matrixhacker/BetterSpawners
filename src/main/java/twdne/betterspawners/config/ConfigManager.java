package twdne.betterspawners.config;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.entity.EntityType;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import twdne.betterspawners.BetterSpawners;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

public class ConfigManager {
	public final Config<Boolean> SILKTOUCH = new Config<>(
		"enable_silktouch_spawner",
		true,
		"Enable or disable the ability to mine spawners with a silktouch tool.",
		Boolean::parseBoolean
	);
	public final Config<Boolean> BLANK_SPAWNER = new Config<>(
		"enable_clear_spawner",
		true,
		"Enable or disable the ability to clear spawners using a specific item.",
		Boolean::parseBoolean
	);
	public final Config<Item> BLANK_SPAWNER_ITEM = new Config<>(
		"clear_spawner_item",
		Items.END_CRYSTAL,
		"The item that can be used to clear a spawner; ignored if disabled.",
		str -> Registry.ITEM.getOrEmpty(getIdentifier(str)).orElse(null)
	);
	public final Config<Boolean> SPAWN_HOSTILE_MOBS = new Config<>(
		"enable_spawner_hostile_mobs",
		true,
		"Enable or disable a spawners ability to spawn hostile mobs.",
		Boolean::parseBoolean
	);
	public final Config<Boolean> SPAWN_PASSIVE_MOBS = new Config<>(
		"enable_spawner_passive_mobs",
		false,
		"Enable or disable a spawners ability to spawn passive mobs.",
		Boolean::parseBoolean
	);
	public final Config<List<EntityType<?>>> SPAWN_SPECIFIC_MOBS = new Config<>(
		"enable_spawner_mob_list",
		new ArrayList<>(),
		"A list of mobs a spawner can be made to spawn. This overrides hostile and passive mob settings.",
		str ->  Arrays.stream(str.split(",")).map(mobStr -> Registry.ENTITY_TYPE.getOrEmpty(getIdentifier(mobStr))).flatMap(Optional::stream).distinct().collect(Collectors.toList())
	);

	private static final String configFilename = BetterSpawners.class.getSimpleName().toLowerCase() + ".json";
	private static ConfigManager configManager;
	private final HashMap<String, Config<?>> configMap;

	private ConfigManager() {
		configMap = new HashMap<>();
		configMap.put(SILKTOUCH.getName(), SILKTOUCH);
		configMap.put(BLANK_SPAWNER.getName(), BLANK_SPAWNER);
		configMap.put(BLANK_SPAWNER_ITEM.getName(), BLANK_SPAWNER_ITEM);
		configMap.put(SPAWN_HOSTILE_MOBS.getName(), SPAWN_HOSTILE_MOBS);
		configMap.put(SPAWN_PASSIVE_MOBS.getName(), SPAWN_PASSIVE_MOBS);
		configMap.put(SPAWN_SPECIFIC_MOBS.getName(), SPAWN_SPECIFIC_MOBS);
		SPAWN_SPECIFIC_MOBS.setGetStringFromType(list -> list.stream().map(type -> type.getName().getString()).collect(Collectors.joining(",", "", "")));

		loadConfigValues();
		saveConfigValues();
	}

	/**
	 * Get an instance of the config manager. There should only be one.
	 * @return configManager instance
	 */
	public static ConfigManager getInstance() {
		if (configManager == null)
			configManager = new ConfigManager();
		return configManager;
	}

	/**
	 * Load config values from file.
	 */
	private void loadConfigValues() {
		// Get config file and return if it does not exist
		File file = new File(FabricLoader.getInstance().getConfigDir().toFile(), configFilename);
		if(!file.exists())
			return;

		// Read config file, return on error
		JsonObject jsonConfig;
		try {
			BufferedReader reader = new BufferedReader(new FileReader(file));
			jsonConfig = JsonParser.parseReader(reader).getAsJsonObject();
			reader.close();
		} catch (IOException | JsonSyntaxException e) {
			BetterSpawners.LOGGER.warn("Error occurred reading config file '{}'; file will be reset.", configFilename);
			return;
		}

		// Update config values
		for (Config<?> config : configMap.values()) {
			Optional.ofNullable(jsonConfig.getAsJsonObject(config.getName()))
				.flatMap(jsonObject -> Optional.ofNullable(jsonConfig.getAsJsonObject(config.getName())).flatMap(configObject -> Optional.ofNullable(configObject.get("value"))))
				.ifPresent(configValue -> config.setStringValue(configValue.getAsString()));
		}
	}

	/**
	 * Save configuration values to the config file.
	 */
	private void saveConfigValues() {
		// Return if no configs to save
		if (configMap.size() == 0)
			return;

		// Generate JSON config object
		JsonObject jsonConfig = new JsonObject();
		for (Config<?> config : configMap.values()) {
			jsonConfig.add(config.getName(), config.getJson());
		}

		// Write config to file
		try {
			FileWriter writer = new FileWriter(new File(FabricLoader.getInstance().getConfigDir().toFile(), configFilename));
			writer.write(new GsonBuilder().setPrettyPrinting().create().toJson(jsonConfig));
			writer.close();
		} catch (IOException e) {
			BetterSpawners.LOGGER.warn("Error occurred writing config file '{}'.", configFilename);
		}
	}

	/**
	 * Remove spaces and uppercase characters from a potential identifier
	 * @param identifier - string to parse
	 * @return new Identifier
	 */
	private Identifier getIdentifier(String identifier) {
		return new Identifier(identifier.toLowerCase().trim().replaceAll("\s+", "_"));
	}
}
