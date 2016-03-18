package stilkin;

import java.util.HashMap;

/**
 * 
 * @author stilkin
 *
 */
public class GoParser implements InputParser.IParserUpdateListener {
    private final HashMap<String, String> settings = new HashMap<String, String>();
    private final HashMap<String, String> updates = new HashMap<String, String>();

    @Override
    public void OnSettingUpdated(final String setting, final String value) {
//	System.err.println("OnSettingUpdated " + setting + " " + value);
	settings.put(setting, value);
    }

    @Override
    public void OnGameStateUpdated(final String update, final String value) {
//	System.err.println("OnGameStateUpdated " + update + " " + value);
	updates.put(update, value);
    }

    public int getSettingAsInt(final String key) {
	int value = -1;
	final String valueStr = settings.get(key);
	if (valueStr != null) {
	    try {
		value = Integer.parseInt(valueStr);
	    } catch (NumberFormatException nfe) {
		System.err.println("Could not parse value for " + key);
	    }
	}
	return value;
    }

    public String getUpdate(final String key) {
	return updates.get(key);
    }

    public String getSetting(final String key) {
	return settings.get(key);
    }

}
