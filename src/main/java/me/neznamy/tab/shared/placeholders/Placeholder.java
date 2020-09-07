package me.neznamy.tab.shared.placeholders;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import me.neznamy.tab.premium.Premium;
import me.neznamy.tab.shared.ITabPlayer;
import me.neznamy.tab.shared.Shared;

/**
 * Representation of any server/player placeholder
 */
public abstract class Placeholder {

	public int cooldown;
	protected String identifier;
	private Map<String, Object> replacements = new HashMap<String, Object>();
	private List<String> outputPlaceholders = new ArrayList<String>();
	
	public Placeholder(String identifier, int cooldown) {
		this.identifier = identifier;
		this.cooldown = cooldown;
		if (Premium.is()) {
			Map<Object, Object> original = Premium.premiumconfig.getConfigurationSection("placeholder-output-replacements." + identifier);
			for (Entry<Object, Object> entry : original.entrySet()) {
				replacements.put(entry.getKey().toString().replace('&', Placeholders.colorChar), entry.getValue());
				for (String id : Placeholders.detectAll(entry.getValue()+"")) {
					if (!outputPlaceholders.contains(id)) outputPlaceholders.add(id);
				}
			}
		}
	}
	public String getIdentifier() {
		return identifier;
	}
	public String[] getChilds(){
		return new String[0];
	}
	public String set(String s, ITabPlayer p) {
		try {
			String value = getLastValue(p);
			if (value == null) value = "";
			String newValue = setPlaceholders(findReplacement(value, p), p);
			if (newValue.contains("%value%")) {
				newValue = newValue.replace("%value%", value);
			}
			return s.replace(identifier, newValue);
		} catch (Throwable t) {
			return Shared.errorManager.printError(s, "An error occurred when setting placeholder " + identifier + (p == null ? "" : " for " + p.getName()), t);
		}
	}
	public String findReplacement(String originalOutput, ITabPlayer p) {
		if (replacements.isEmpty()) return originalOutput;
		if (replacements.containsKey(originalOutput)) {
			return replacements.get(originalOutput).toString();
		}
		for (Entry<String, Object> entry : replacements.entrySet()) {
			String key = entry.getKey();
			if (key.contains("-")) {
				try {
					float low = Float.parseFloat(key.split("-")[0]);
					float high = Float.parseFloat(key.split("-")[1]);
					float actualValue = Float.parseFloat(originalOutput);
					if (low <= actualValue && actualValue <= high) return entry.getValue().toString();
				} catch (NumberFormatException e) {
					//nope
				}
			}
		}
		if (replacements.containsKey("else")) return replacements.get("else").toString();
		return originalOutput;
	}
	private String setPlaceholders(String text, ITabPlayer p) {
		String replaced = text;
		for (String s : outputPlaceholders) {
			if (s.equals("%value%")) continue;
			Placeholder pl = Placeholders.getPlaceholder(s);
			if (pl != null && replaced.contains(pl.getIdentifier())) replaced = pl.set(replaced, p);
		}
		return replaced;
	}
	public abstract String getLastValue(ITabPlayer p);
}