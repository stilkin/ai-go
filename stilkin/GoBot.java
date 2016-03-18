package stilkin;

import java.util.List;
import java.util.Random;

import field.Field;
import move.Move;

public class GoBot implements InputParser.IActionRequestListener {
    public static final int MAX_DIM = 19;
    private GoParser goParser;
    private Field gameField = new Field();

    public GoBot(final GoParser goParser) {
	if (goParser == null) {
	    throw new IllegalArgumentException("GoParser cannot be null!");
	}
	this.goParser = goParser;
    }

    /**
     * Your bot's output should always start with "place_move", followed by two numbers x (column index of whole field) and y (row index of whole field) coordinates of the square,
     * respectively. 0 0 is the top-left square, 18 18 is the bottom right square. "pass" can be output to pass the turn; nothing will happen on the board.
     */

    @Override
    public void OnActionRequested(final String type, final long time) {
	System.err.println("OnActionRequested Round" + goParser.getUpdate(GoConsts.Updates.ROUND_NR));

	// init field
	final int xMax = goParser.getSettingAsInt(GoConsts.Settings.FIELD_HEIGHT);
	gameField.setColumns(xMax);
	final int yMax = goParser.getSettingAsInt(GoConsts.Settings.FIELD_WIDTH);
	gameField.setRows(yMax);
	try {
	    gameField.initField();
	} catch (Exception e) {
	    e.printStackTrace();
	}
	final String fieldStr = goParser.getUpdate(GoConsts.Updates.GAME_FIELD);
	gameField.parseFromString(fieldStr);

	// get random, semi-valid move
	final List<Move> moves = gameField.getAvailableMoves();
	System.err.println("Moves: " + moves.size());
	if (moves.size() > 0) {
	    final Random rnd = new Random();
	    final Move move = moves.get(rnd.nextInt(moves.size()));
	    System.out.printf("%s %d %d\n", GoConsts.Actions.MOVE_ACTION, move.getX(), move.getY());
	    System.out.flush();
	} else {
	    System.out.println(GoConsts.Actions.PASS_ACTION);
	    System.out.flush();
	}
    }
}