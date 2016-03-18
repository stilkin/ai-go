package stilkin;

import java.util.HashMap;
import java.util.Random;

/**
 * 
 * @author stilkin
 *
 */
public class GoBot implements InputParser.IParseEventListener {
    public static final int MAX_DIM = 19;
    private final HashMap<String, String> settings = new HashMap<String, String>();
    private final HashMap<String, String> updates = new HashMap<String, String>();

    /**
     * Your bot's output should always start with "place_move", followed by two numbers x (column index of whole field) and y (row index of whole field) coordinates of the square,
     * respectively. 0 0 is the top-left square, 18 18 is the bottom right square. "pass" can be outputted to pass the turn; nothing will happen on the board.
     */

    @Override
    public void OnActionRequested(final String type, final long time) {
	System.err.println("OnActionRequested " + type + " " + time);
	// TODO Auto-generated method stub
	final Random rnd = new Random();
	final int x = rnd.nextInt(MAX_DIM);
	final int y = rnd.nextInt(MAX_DIM);
	System.out.printf("place_move %d %d\n", x, y);
	System.out.flush();

    }

    @Override
    public void OnSettingUpdated(final String setting, final String value) {
	System.err.println("OnSettingUpdated " + setting + " " + value);
	settings.put(setting, value);
    }

    @Override
    public void OnGameStateUpdated(final String update, final String value) {
	System.err.println("OnGameStateUpdated " + update + " " + value);
	updates.put(update, value);
    }

    private int getIntegerSetting(final String key) {
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
    
    public class SettingConsts {
	public static final String TIMEBANK = "timebank";
	public static final String TIME_PER_MOVE = "time_per_move";
	public static final String PLAYER_NAMES = "player_names";
	public static final String YOUR_BOT = "your_bot";
	public static final String YOUR_BOTID = "your_botid";
	public static final String FIELD_WIDTH = "field_width";
	public static final String FIELD_HEIGHT = "field_height";
    }
    
    public class UpdateConsts {
	public static final String ROUND_NR = "game round";
	public static final String MOVE_NR = "game move";
	public static final String GAME_FIELD = "game field";
	public static final String POINTS_P1 = "player1 points";
	public static final String POINTS_P2 = "player2 points";
    }
    
    public class ActionConsts {
	public static final String MOVE_REQUEST = "move";
	public static final String MOVE_ACTION = "place_move";
	public static final String PASS_ACTION = "pass";
    }
}
