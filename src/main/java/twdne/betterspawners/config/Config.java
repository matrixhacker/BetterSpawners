package twdne.betterspawners.config;

import com.google.gson.JsonObject;
import lombok.Getter;
import lombok.Setter;
import twdne.betterspawners.BetterSpawners;

import java.util.function.Function;

public class Config<T> {
	private final @Getter String name;
	private @Getter @Setter T value;
	private final @Getter T defaultValue;
	private final @Getter String description;
	private final Function<String, T> getTypeFromString;
	private @Setter Function<T, String> getStringFromType;

	Config(String name, T defaultValue, String description, Function<String, T> getTypeFromString) {
		this.name = name;
		this.defaultValue = defaultValue;
		this.value = defaultValue;
		this.description = description;
		this.getTypeFromString = getTypeFromString;
		this.getStringFromType = Object::toString;
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
		T newValue = this.getTypeFromString.apply(value);
		if (newValue == null)
			BetterSpawners.LOGGER.warn("Unable to set config '{}' using value [{}]", name, value);
		else
			this.value = newValue;
	}
}
