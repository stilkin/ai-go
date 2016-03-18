package stilkin;

import java.util.ArrayList;
import java.util.List;

/**
 * 
 * @author stilkin
 *
 */
public class InputParser {
    public static final String LINETYPE_SETTINGS = "settings";
    public static final String LINETYPE_UPDATE = "update";
    public static final String LINETYPE_ACTION = "action";
    private final List<IParseEventListener> observerList = new ArrayList<IParseEventListener>();

    public void parse(final String input) {
	final String[] words = input.split(" ");

	if (words.length < 3) {
	    return;
	}
	// all valid messages are at least 3 words long

	final String lineType = words[0];

	if (LINETYPE_ACTION.equals(lineType)) {
	    final String action = words[1];
	    final String timeStr = words[2];
	    long time = 500l;
	    try {
		time = Long.parseLong(timeStr);
	    } catch (NumberFormatException nfe) {
		nfe.printStackTrace();
	    }

	    // notify all observers
	    IParseEventListener observer;
	    for (int b = 0; b < observerList.size(); b++) {
		observer = observerList.get(b);
		observer.OnActionRequested(action, time);
	    }

	} else if (LINETYPE_SETTINGS.equals(lineType)) {
	    // store the settings
	    final String setting = words[1];
	    String value = input;
	    value = value.replace(lineType, "").trim();
	    value = value.replace(setting, "").trim();

	    // notify all observers
	    IParseEventListener observer;
	    for (int b = 0; b < observerList.size(); b++) {
		observer = observerList.get(b);
		observer.OnSettingUpdated(setting, value);
	    }

	} else if (LINETYPE_UPDATE.equals(lineType)) {
	    // store the update
	    final String update = String.format("%s %s", words[1], words[2]);
	    String value = input;
	    value = value.replace(lineType, "").trim();
	    value = value.replace(update, "").trim();

	    // notify all observers
	    IParseEventListener observer;
	    for (int b = 0; b < observerList.size(); b++) {
		observer = observerList.get(b);
		observer.OnGameStateUpdated(update, value);
	    }

	} else {
	    System.err.println("Input not recognized: " + input);
	}

    }

    /**
     * Implement this interface and register your observer if you want to be notified of an action requests, game state updates, or setting updates.
     */
    public interface IParseEventListener {
	void OnActionRequested(final String type, final long time);

	void OnSettingUpdated(final String setting, final String value);

	void OnGameStateUpdated(final String update, final String value);
    }

    public boolean addActionRequestListener(final IParseEventListener observer) {
	return observerList.add(observer);
    }

    public boolean removeActionRequestListener(final IParseEventListener observer) {
	return observerList.remove(observer);
    }
}
