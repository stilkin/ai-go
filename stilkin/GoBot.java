package stilkin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.Set;

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
	System.err.println("OnActionRequested round " + goParser.getUpdate(GoConsts.Updates.ROUND_NR));

	if (goField == null) { // lazy loader
	    goField = new GoField();
	    final int xMax = goParser.getSettingAsInt(GoConsts.Settings.FIELD_HEIGHT);
	    final int yMax = goParser.getSettingAsInt(GoConsts.Settings.FIELD_WIDTH);
	    goField.changeSize(yMax, xMax);
	}

	if (myId == 0) {
	    myId = goParser.getSettingAsInt(GoConsts.Settings.YOUR_BOTID);
	    enemyId = 3 - myId;
	    System.err.println("I am player " + myId);
	}

	final String fieldStr = goParser.getUpdate(GoConsts.Updates.GAME_FIELD);
	goField.parseFromString(fieldStr);
	System.err.println(goField.toPrettyString());

	// STRATEGY: try to find enemy strings that can be removed in this turn
	final List<GoString> enemyStrings = goField.getStringsOfType(enemyId);
	GoString eString;
	for (int es = 0; es < enemyStrings.size(); es++) {
	    eString = enemyStrings.get(es);
	    final Set<GoCoord> liberties = eString.getLiberties();
	    final int libertyCount = eString.libertyCount();
	    if (libertyCount == 1) { // enemy string can be removed
		for (final GoCoord move : liberties) {
		    if (!goField.doesMoveViolateKo(move.x, move.y)) {
			printMove(move); // make a move
			System.err.println("Liberty " + liberties.toString() + " of enemy string " + eString.toString());
			return; // stop here
		    }
		}
	    } else if (libertyCount == 0) { // this string cannot be attacked
		// FILTER: remove enemy strings without liberties
		enemyStrings.remove(es);
		es--;
	    }
	}

	// get all empty positions, these are potential moves
	final List<GoCoord> potentialMoves = goField.getCoordsWithValue(0);
	System.err.println("emptyPositions " + potentialMoves.size());

	// FILTER: remove own 'eyes', they are not useful as a move
	// TODO: what about 3 neighbours on a side?
	// TODO: what about 3 of mine and one of enemy?
	GoCoord pos;
	for (int ep = 0; ep < potentialMoves.size(); ep++) {
	    pos = potentialMoves.get(ep);
	    final List<GoCoord> neighbours = goField.getNeighboursWithValue(pos.x, pos.y, myId);
	    if (neighbours.size() >= 4) {
		potentialMoves.remove(ep);
	    }
	}

	// FILTER: calculate all empty positions with direct and indirect liberties, these are the only valid moves
	final List<GoString> myStrings = goField.getStringsOfType(myId); // the strings I currently own
	// calculate liberties for all my positions
	final HashMap<GoCoord, Integer> myLiberties = new HashMap<GoCoord, Integer>();
	for (GoString stringSet : myStrings) {
	    final int libertyCount = stringSet.libertyCount();
	    for (GoCoord coord : stringSet.getCoords()) {
		myLiberties.put(coord, libertyCount);
	    }
	}

	final List<GoCoord> movesWithLiberties = new ArrayList<GoCoord>();
	for (GoCoord pom : potentialMoves) {
	    final List<GoCoord> emptyNeighbours = goField.getFreeNeighbours(pom.x, pom.y);
	    if (!emptyNeighbours.isEmpty()) { // direct empty neighbour -> valid move
		movesWithLiberties.add(pom);
	    } else {
		final List<GoCoord> neighbours = goField.getNeighboursWithValue(pom.x, pom.y, myId);
		for (GoCoord nb : neighbours) {
		    final Integer libs = myLiberties.get(nb);
		    if (libs != null && libs > 1) { // has to be > 1 to take empty cell itself into account
			movesWithLiberties.add(pom); // indirect empty neighbour -> valid move
			break;
		    }
		}
	    }
	}
	System.err.println("posWithLiberties " + movesWithLiberties.size());

	// FILTER: implementation of KO rule -> remove all moves that result in previous board positions
	GoCoord tmpCoord;
	for (int pl = 0; pl < movesWithLiberties.size(); pl++) {
	    tmpCoord = movesWithLiberties.get(pl);
	    if (goField.doesMoveViolateKo(tmpCoord.x, tmpCoord.y)) {
		movesWithLiberties.remove(pl);
		pl--;
		System.err.println("Move " + tmpCoord + " removed to prevent Ko problems.");
	    }
	}

	// STRATEGY: reduce enemy liberties in a greedy fashion
	if (enemyStrings.size() > 0) {
	    // sort enemy strings by liberties and size
	    Collections.sort(enemyStrings, new StringOrderer().reversed());

	    for (final GoString enString : enemyStrings) {
		for (final GoCoord move : enString.getLiberties()) {
		    // check move for validity
		    if (movesWithLiberties.contains(move)) {
			System.err.println("Going after: " + enString);
			printMove(move); // make a move
			return; // stop here
		    }
		}
	    }
	}

	// if we arrive here, there were no enemy stones (with liberties) we could move on
	GoCoord move = null;
	if (movesWithLiberties.size() > 0) {
	    final Random rnd = new Random();
	    move = movesWithLiberties.get(rnd.nextInt(movesWithLiberties.size()));
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

    private class StringOrderer implements Comparator<GoString> {
	/**
	 * Returns a negative integer, zero, or a positive integer as the first argument is less than, equal to, or greater than the second.
	 */
	@Override
	public int compare(final GoString str1, final GoString str2) {
	    // TODO: take 0 liberties into account?

	    // first string is worth less if it has more liberties
	    if (str1.libertyCount() > str2.libertyCount()) {
		return -1;
	    }
	    // first string is better if it is has fewer liberties
	    if (str1.libertyCount() < str2.libertyCount()) {
		return 1;
	    }
	    // if we get here they have similar amount of liberties

	    // first string is worth more if it has more stones
	    if (str1.size() > str2.size()) {
		return 1;
	    }
	    // first string is worth less if it has more stones
	    if (str1.size() < str2.size()) {
		return -1;
	    }

	    // no clear difference
	    return 0;
	}
    }

}
