package stilkin;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * 
 * @author stilkin
 *
 */
public class GoBot implements InputParser.IActionRequestListener {
    private GoParser goParser;
    private GoField goField;
    private int myId = 0;
    private int enemyId = 0;
    private long roundStart = 0;

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
	roundStart = System.currentTimeMillis();
	System.err.println("OnActionRequested Round" + goParser.getUpdate(GoConsts.Updates.ROUND_NR));

	if (goField == null) { // lazy loader
	    goField = new GoField();
	    final int xMax = goParser.getSettingAsInt(GoConsts.Settings.FIELD_HEIGHT);
	    final int yMax = goParser.getSettingAsInt(GoConsts.Settings.FIELD_WIDTH);
	    goField.changeSize(yMax, xMax);
	}

	if (myId == 0) {
	    myId = goParser.getSettingAsInt(GoConsts.Settings.YOUR_BOTID);
	    enemyId = 3 - myId; // TOOD: check
	    System.err.println("I am player " + myId);
	}

	final String fieldStr = goParser.getUpdate(GoConsts.Updates.GAME_FIELD);
	goField.parseFromString(fieldStr);
	System.err.println(goField.toPrettyString());

	// TODO: implement KO rule
	// get a list of all empty fields with liberties (= valid moves)
	final List<GoCoord> validMoves = new ArrayList<GoCoord>();
	validMoves.addAll(goField.getCoordsWithLiberties(0, 4));
	validMoves.addAll(goField.getCoordsWithLiberties(0, 3));
	validMoves.addAll(goField.getCoordsWithLiberties(0, 2));
	validMoves.addAll(goField.getCoordsWithLiberties(0, 1));
	// System.err.println("validMoves: " + validMoves.size());

	List<GoCoord> enemyStones, nearbyMoves;
	for (int l = 1; l <= 4; l++) {
	    // get a list of enemy stones with limited liberties
	    enemyStones = goField.getCoordsWithLiberties(enemyId, l);
	    // System.err.println("Found " + enemyStones.size() + " enemy stones with " + l + " liberties");
	    for (GoCoord eStone : enemyStones) {
		// get the empty fields around it
		nearbyMoves = goField.getAdjacendCoords(eStone.x, eStone.y, 0);
		for (GoCoord move : nearbyMoves) {
		    // check move for validity
		    if (validMoves.contains(move)) {
			printMove(move); // make a move
			return; // stop here
		    }
		}
	    }
	}

	// if we arrive here, there were no enemy stones (with liberties) we could move on
	GoCoord move = null;
	if (validMoves.size() > 0) {
	    final Random rnd = new Random();
	    move = validMoves.get(rnd.nextInt(validMoves.size()));
	}
	printMove(move);
    }

    private void printMove(final GoCoord move) {
	if (move != null) {
	    printMove(move.x, move.y);
	} else {
	    System.out.println(GoConsts.Actions.PASS_ACTION);
	    System.out.flush();
	}
	System.err.printf("Round took %d ms\n", (System.currentTimeMillis() - roundStart));
    }

    private void printMove(final int x, final int y) {
	System.out.printf("%s %d %d\n", GoConsts.Actions.MOVE_ACTION, x, y);
	System.out.flush();
    }

    private String mapToString(final float[][] map) {
	final int colMax = map[0].length;
	final int rowMax = map.length;
	float max = 0;
	for (int x = 0; x < colMax; x++) {
	    for (int y = 0; y < rowMax; y++) {
		if (map[x][y] > max) {
		    max = map[x][y];
		}
	    }
	}

	final StringBuilder strB = new StringBuilder();
	for (int x = 0; x < colMax; x++) {
	    for (int y = 0; y < rowMax; y++) {
		final float pct = map[x][y] / max;
		if (pct != 0) {
		    strB.append(String.format("%02d ", (int) (99 * pct))); // scale to 100
		} else {
		    strB.append("   ");
		}
	    }
	    strB.append(" | \n");
	}
	return strB.toString();
    }

}
