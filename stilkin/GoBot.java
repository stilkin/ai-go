package stilkin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

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
	    enemyId = 3 - myId;
	    System.err.println("I am player " + myId);
	}

	final String fieldStr = goParser.getUpdate(GoConsts.Updates.GAME_FIELD);
	goField.parseFromString(fieldStr);
	System.err.println(goField.toPrettyString());	
	fieldHistory.add(goField.toString()); // field history for Ko rule

	// messy field, for editing
	final GoField tmpField = new GoField();
	tmpField.changeSize(GoField.MAX_WIDTH, GoField.MAX_HEIGHT);
	tmpField.parseFromString(fieldStr);

	// STRATEGY: try to find enemy strings that can be removed
	final List<HashSet<GoCoord>> enemyStrings = goField.getStringSetOfType(enemyId);
	// calculate liberties for enemy strings
	for (HashSet<GoCoord> stringSet : enemyStrings) {
	    final Set<GoCoord> liberties = goField.getLibertiesOfString(stringSet);
	    final int libertyCount = liberties.size();
	    if(libertyCount == 1) { // enemy string can be removed
		for(GoCoord move : liberties) {
		    if (!hasBoardBeenPlayed(tmpField, move)) {
			printMove(move); // make a move
			System.err.println("Liberty " + liberties.toString() + " of enemy string " + stringSet.toString());
			return; // stop here
		    }
		}
	    }
	}	

	// get all empty positions, these are potential moves
	final List<GoCoord> potentialMoves = goField.getCoordsWithValue(0);
	System.err.println("emptyPositions " + potentialMoves.size());

	// FILTER: remove own 'eyes', they are not useful as a move
	GoCoord pos;
	for (int ep = 0; ep < potentialMoves.size(); ep++) {
	    pos = potentialMoves.get(ep);
	    final List<GoCoord> neighbours = goField.getNeighboursWithValue(pos.x, pos.y, myId);
	    if (neighbours.size() >= 4) {
		potentialMoves.remove(ep);
	    }
	}

	// FILTER: calculate all empty positions with direct and indirect liberties, these are the only valid moves
	final List<HashSet<GoCoord>> myStrings = goField.getStringSetOfType(myId); // the strings I currently own
	// calculate liberties for all my positions
	final HashMap<GoCoord, Integer> myLiberties = new HashMap<GoCoord, Integer>();
	for (HashSet<GoCoord> stringSet : myStrings) {
	    final Set<GoCoord> liberties = goField.getLibertiesOfString(stringSet);
	    final int libertyCount = liberties.size();
	    for (GoCoord coord : stringSet) {
		myLiberties.put(coord, libertyCount);
	    }
	}

	
	final List<GoCoord> posWithLiberties = new ArrayList<GoCoord>();
	for (GoCoord pom : potentialMoves) {
	    final List<GoCoord> emptyNeighbours = goField.getFreeNeighbours(pom.x, pom.y);
	    if (!emptyNeighbours.isEmpty()) { // direct empty neighbour -> valid move
		posWithLiberties.add(pom);
	    } else {
		final List<GoCoord> neighbours = goField.getNeighboursWithValue(pom.x, pom.y, myId);
		for (GoCoord nb : neighbours) {
		    final Integer libs = myLiberties.get(nb);
		    if (libs != null && libs > 1) { // has to be > 1 to take empty cell itself into account
			posWithLiberties.add(pom); // indirect empty neighbour -> valid move
			break;
		    }
		}
	    }
	}
	System.err.println("posWithLiberties " + posWithLiberties.size());


	// FILTER: implementation of KO rule -> remove all moves that result in previous board positions
	GoCoord tmpCoord;
	for (int pl = 0; pl < posWithLiberties.size(); pl++) {
	    tmpCoord = posWithLiberties.get(pl);
	    if(hasBoardBeenPlayed(tmpField, tmpCoord)) {
		posWithLiberties.remove(pl);
		pl--;
		System.err.println("Move " + tmpCoord + " removed to prevent Ko problems.");
	    }
	}
	
	// STRATEGY: reduce enemy liberties in a greedy fashion
	List<GoCoord> enemyStones, nearbyMoves;
	for (int l = 1; l <= 4; l++) {
	    // get a list of enemy stones with limited liberties
	    enemyStones = goField.getCoordsWithLiberties(enemyId, l);
	    // System.err.println("Found " + enemyStones.size() + " enemy stones with " + l + " liberties");
	    for (GoCoord eStone : enemyStones) {
		// get the empty fields around it
		nearbyMoves = goField.getFreeNeighbours(eStone.x, eStone.y);
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
    
    private boolean hasBoardBeenPlayed(final GoField goField, final GoCoord move){
	if (goField.getCell(move.x, move.y) < 0) { 
	    System.err.println(move + " value < 0");
	    return true; // TODO: fix this -1 stuff  
	}
	
	goField.setCell(move.x, move.y, myId); // pretend we play this move
	final boolean hasBeenPlayed = fieldHistory.contains(goField.toString()); // position has been played already
	goField.setCell(move.x, move.y, 0); // reset field
	return hasBeenPlayed;	    
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
