package twdne.betterspawners.config;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.entity.EntityType;
import net.minecraft.item.Item;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import twdne.betterspawners.BetterSpawners;

import java.io.*;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ConfigManager {
	private final String configFilename;
	private final HashMap<String, Config<?>> configMap;

	/**
	 * Loads, saves, and tracks configs created using the createConfig function
	 * @param modId - used as the base for the config filename
	 */
	public ConfigManager(String modId) {
		configFilename = modId + ".json";
		configMap = new HashMap<>();
	}

	/**
	 * Load saved values from the config file then save the clean config file.
	 * Causes all configs to be reset to default values in the case of a syntax error in the config file.
	 * Any missing config will be added with its default value. Any invalid value will be updated with the default value.
	 */
	public void loadAndSaveConfigs() {
		loadConfigValues();
		saveConfigValues();
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
	 * Create a Config{@literal <}T{@literal >} object and add it to the configuration map. Will throw an exception after the configManager has been locked.
	 * @param name - the configuration name; must be unique
	 * @param defaultValue - default value for this config
	 * @param description - description of this config
	 * @param getTypeFromString - function to translate the string value of this config into it's T type; should return null on parse error
	 * @return the newly created Config{@literal <}T{@literal >} object
	 * @param <T> - type of Object stored in Config
	 */
	public <T> Config<T> createConfig(String name, T defaultValue, String description, Function<String, T> getTypeFromString) {
		return createConfig(name, defaultValue, description, getTypeFromString, Objects::toString);
	}

	/**
	 * Create a Config{@literal <}T{@literal >} object and add it to the configuration map.
	 * @param name - the configuration name; must be unique
	 * @param defaultValue - default value for this config
	 * @param description - description of this config
	 * @param getTypeFromString - function to translate the String value of this config into it's T type; should return null on parse error
	 * @param getStringFromType - function to translate the T type into a String; used when toString isn't enough
	 * @return the newly created Config{@literal <}T{@literal >} object
	 * @param <T> - type of Object stored in Config
	 */
	public <T> Config<T> createConfig(String name, T defaultValue, String description, Function<String, T> getTypeFromString, Function<T, String> getStringFromType) {
		Config<T> config = new Config<>(name, defaultValue, description, getTypeFromString, getStringFromType);
		if (configMap.putIfAbsent(config.getName(), config) != null)
			BetterSpawners.LOGGER.warn("Attempted to overwrite existing config {}", config.getName());
		return config;
	}

	/**
	 * Remove spaces and uppercase characters from a potential identifier
	 * @param identifier - string to parse
	 * @return new Identifier
	 */
	public static Identifier cleanIdentifier(String identifier) {
		return new Identifier(identifier.toLowerCase().trim().replaceAll("\s+", "_"));
	}

	/**
	 * Convenience function to parse an Item from its String representation
	 */
	public static final Function<String, Item> parseItem = str -> Registry.ITEM.getOrEmpty(cleanIdentifier(str)).orElse(null);

	/**
	 * Convenience function to parse an EntityType from its String representation
	 */
	public static final Function<String, EntityType<?>> parseEntityType = str -> Registry.ENTITY_TYPE.getOrEmpty(cleanIdentifier(str)).orElse(null);

	/**
	 * Convenience function to parse a List of any type from its String representation
	 * @param str - the String to parse
	 * @param parseTypeFromString - the Function used to parse a given type from a String
	 * @return a List of objects of the given type
	 * @param <T> - type of Object stored in the List
	 */
	public static <T> List<T> parseTypeList(String str, Function<String, T> parseTypeFromString) {
		return Arrays.stream(str.split(",")).map(parseTypeFromString).filter(Objects::nonNull).distinct().collect(Collectors.toList());
	}

	/**
	 * Convenience function to parse a List of Item(s) from its String representation
	 */
	public static final Function<String, List<Item>> parseItemList = str -> parseTypeList(str, parseItem);


	/**
	 * Convenience function to parse a List of EntityType(s) from its String representation
	 */
	public static final Function<String, List<EntityType<?>>> parseEntityTypeList = str -> parseTypeList(str, parseEntityType);

	/**
	 * Convenience function to write a List of any type as a String
	 * @param list - list to convert to a String
	 * @param typeToString - the function used to convert each object in the List to a String
	 * @return a String representation of the given List
	 * @param <T> - type of Object stored in List
	 */
	public static <T> String typeListToString(List<T> list, Function<T, String> typeToString) {
		return list.stream().map(typeToString).collect(Collectors.joining(",", "", ""));
	}

	/**
	 * Convenience function to write a List of Item(s) as a String
	 */
	public static final Function<List<Item>, String> itemListToString = list -> typeListToString(list, item -> item.getName().getString());

	/**
	 * Convenience function to write a List of EntityType(s) as a String
	 */
	public static final Function<List<EntityType<?>>, String> entityTypeListToString = list -> typeListToString(list, type -> type.getName().getString());
}
