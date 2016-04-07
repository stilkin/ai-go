package stilkin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Random;

/**
 * 
 * @author stilkin
 *
 */
public class GoBot implements InputParser.IActionRequestListener {
    private HashSet<String> fieldHistory = new HashSet<String>();
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
	System.err.println("OnActionRequested round " + goParser.getUpdate(GoConsts.Updates.ROUND_NR));

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
	fieldHistory.add(goField.toString()); // for Ko rule
	System.err.println(goField.toPrettyString());

	// the strings I currently own
	final List<HashSet<GoCoord>> myStrings = goField.getStringSetOfType(myId);

	// calculate liberties for all my positions
	final HashMap<GoCoord, Integer> myLiberties = new HashMap<GoCoord, Integer>();
	for (HashSet<GoCoord> stringSet : myStrings) {
	    final List<GoCoord> liberties = goField.getLibertiesOfString(stringSet);
	    final int libertyCount = liberties.size();
	    for (GoCoord coord : stringSet) {
		myLiberties.put(coord, libertyCount);
	    }
	}

	final List<GoCoord> emptyPositions = goField.getCoordsWithValue(0);
	System.err.println("emptyPositions " + emptyPositions.size());

	// calculate all empty positions with direct and indirect liberties
	final List<GoCoord> posWithLiberties = new ArrayList<GoCoord>();
	for (GoCoord pos : emptyPositions) {
	    final List<GoCoord> emptyNeighbours = goField.getAdjacendCoords(pos.x, pos.y, 0);
	    if (!emptyNeighbours.isEmpty()) {
		posWithLiberties.add(pos);
	    } else {
		final List<GoCoord> neighbours = goField.getAdjacendCoords(pos.x, pos.y, myId);
		for (GoCoord nb : neighbours) {
		    final Integer libs = myLiberties.get(nb);
		    if (libs != null && libs > 1) { // has to be > 1 to take empty cell itself into account
			posWithLiberties.add(pos);
			break;
		    }
		}
	    }
	}
	System.err.println("posWithLiberties " + posWithLiberties.size());
	
	// TODO: calculate enemy strings and check if we can remove them -> no extra liberties required for those positions
	// TODO: prevent filling up of own eyes

	// implementation of KO rule
	final GoField tmpField = new GoField();
	tmpField.changeSize(GoField.MAX_WIDTH, GoField.MAX_HEIGHT);
	tmpField.parseFromString(fieldStr);
	GoCoord tmpCoord;
	for (int pl = 0; pl < posWithLiberties.size(); pl++) {
	    tmpCoord = posWithLiberties.get(pl);
	    tmpField.setCell(tmpCoord.x, tmpCoord.y, myId); // pretend we play this move
	    if (fieldHistory.contains(tmpField.toString())) { // position has been played already
		posWithLiberties.remove(pl);
		pl--;
		System.err.println("Move " + tmpCoord + " removed to prevent Ko problems.");
	    }
	    tmpField.setCell(tmpCoord.x, tmpCoord.y, 0); // reset
	}

	// the strings my enemy owns
	// final List<HashSet<GoCoord>> enemyStrings = goField.getStringSetOfType(enemyId);
	//
	// System.err.println(enemyStrings.size() + " sets");
	// for (HashSet<GoCoord> stringSet : enemyStrings) {
	// final List<GoCoord> liberties = goField.getLibertiesOfString(stringSet);
	// System.err.println(stringSet.toString() + " " + liberties.size());
	// }

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
		    if (posWithLiberties.contains(move)) {
			printMove(move); // make a move
			return; // stop here
		    }
		}
	    }
	}

	// if we arrive here, there were no enemy stones (with liberties) we could move on
	GoCoord move = null;
	if (posWithLiberties.size() > 0) {
	    final Random rnd = new Random();
	    move = posWithLiberties.get(rnd.nextInt(posWithLiberties.size()));
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
	System.err.printf("%s %d %d\n", GoConsts.Actions.MOVE_ACTION, x, y); // debug
    }

}
