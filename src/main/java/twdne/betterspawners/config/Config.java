package twdne.betterspawners.config;

import com.google.gson.JsonObject;
import lombok.Getter;
import lombok.Setter;
import twdne.betterspawners.BetterSpawners;

import java.util.function.Function;

/**
 * Config class to contain a generic configuration item
 * @param <T> - config type
 */
public class Config<T> {
	private final @Getter String name;
	private @Getter @Setter T value;
	private final @Getter T defaultValue;
	private final @Getter String description;
	private final Function<String, T> getTypeFromString;
	private final Function<T, String> getStringFromType;

	/**
	 * Constructs a Config{@literal <}T{@literal >} object; typically should only be called by ConfigManager.
	 * Use createConfig from instance of ConfigManager.
	 * @param name - the configuration name; must be unique
	 * @param defaultValue - default value for this config
	 * @param description - description of this config
	 * @param getTypeFromString - function to translate the string value of this config into it's T type
	 * @param getStringFromType - function to translate the T type into a String; used when toString isn't enough
	 */
	public Config(String name, T defaultValue, String description, Function<String, T> getTypeFromString, Function<T, String> getStringFromType) {
		this.name = name;
		this.defaultValue = defaultValue;
		this.value = defaultValue;
		this.description = description;
		this.getTypeFromString = getTypeFromString;
		this.getStringFromType = getStringFromType;
	}

	/**
	 * Generate a JSON object representation of this config
	 * @return jsonObject
	 */
	public JsonObject getJson() {
		JsonObject jsonObject = new JsonObject();
		jsonObject.addProperty("value", getStringFromType.apply(value));
		jsonObject.addProperty("description", description);
		return jsonObject;
	}

	/**
	 * Set the config value from a given String
	 * @param value - the value to set
	 */
	public void setStringValue(String value) {
		T newValue = null;
		try {
			newValue = this.getTypeFromString.apply(value);
		} catch (Exception ignored) {}
		if (newValue == null)
			BetterSpawners.LOGGER.warn("Unable to set config '{}' using value [{}]", name, value);
		else
			this.value = newValue;
	}
}
