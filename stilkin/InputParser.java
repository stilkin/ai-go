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
    private final List<IParserUpdateListener> updateObservers = new ArrayList<IParserUpdateListener>();
    private final List<IActionRequestListener> actionObservers = new ArrayList<IActionRequestListener>();

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
	    IActionRequestListener observer;
	    for (int b = 0; b < actionObservers.size(); b++) {
		observer = actionObservers.get(b);
		observer.OnActionRequested(action, time);
	    }

	} else if (LINETYPE_SETTINGS.equals(lineType)) {
	    // store the settings
	    final String setting = words[1];
	    String value = input;
	    value = value.replace(lineType, "").trim();
	    value = value.replace(setting, "").trim();

	    // notify all observers
	    IParserUpdateListener observer;
	    for (int b = 0; b < updateObservers.size(); b++) {
		observer = updateObservers.get(b);
		observer.OnSettingUpdated(setting, value);
	    }

	} else if (LINETYPE_UPDATE.equals(lineType)) {
	    // store the update
	    final String update = String.format("%s %s", words[1], words[2]);
	    String value = input;
	    value = value.replace(lineType, "").trim();
	    value = value.replace(update, "").trim();

	    // notify all observers
	    IParserUpdateListener observer;
	    for (int b = 0; b < updateObservers.size(); b++) {
		observer = updateObservers.get(b);
		observer.OnGameStateUpdated(update, value);
	    }

	} else {
	    System.err.println("Input not recognized: " + input);
	}

    }

    /**
     * Implement this interface and register your observer if you want to be notified of game state updates or setting updates.
     */
    public interface IParserUpdateListener {
	void OnSettingUpdated(final String setting, final String value);

	void OnGameStateUpdated(final String update, final String value);
    }
    
    /**
     * Implement this interface and register your observer if you want to be notified of an action request.
     */
    public interface IActionRequestListener {
	void OnActionRequested(final String type, final long time);
    }
    

    public boolean addParserUpdateListener(final IParserUpdateListener observer) {
	return updateObservers.add(observer);
    }

    public boolean removeParserUpdateListener(final IParserUpdateListener observer) {
	return updateObservers.remove(observer);
    }
    
    public boolean addActionRequestListener(final IActionRequestListener observer) {
	return actionObservers.add(observer);
    }

    public boolean removeActionRequestListener(final IActionRequestListener observer) {
	return actionObservers.remove(observer);
    }
}
