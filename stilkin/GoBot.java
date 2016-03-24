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
    private static final long MAX_TIME_MS = 200;
    private static final float COHESION_VALUE = 1;
    private static final float SURROUND_VALUE = 100;
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
	    System.err.println("I am number " + myId);
	}

	final String fieldStr = goParser.getUpdate(GoConsts.Updates.GAME_FIELD);
	goField.parseFromString(fieldStr);
	calculateMoves(goField);

	// get random, semi-valid move
	 final List<GoCoord> moves = calculateMoves(goField);
	System.err.println("Moves: " + moves.size());
	if (moves.size() > 0) {
	    final Random rnd = new Random();
	    final GoCoord move = moves.get(rnd.nextInt(moves.size()));
	    System.out.printf("%s %d %d\n", GoConsts.Actions.MOVE_ACTION, move.getX(), move.getY());
	    System.out.flush();
	} else {
	    System.out.println(GoConsts.Actions.PASS_ACTION);
	    System.out.flush();
	}

	System.err.printf("Round took %d ms\n", (System.currentTimeMillis() - roundStart));
    }

    private List<GoCoord> calculateMoves(final GoField goField) {
	final List<GoCoord> moveList = new ArrayList<GoCoord>();
	final int[][] cells = goField.getCells();
	final int colMax = cells[0].length;
	float[][] map = new float[colMax][cells.length];

	for (int x = 0; x < colMax; x++) {
	    for (int y = 0; y < cells.length; y++) {
		if (cells[x][y] == myId) {
		    map[x][y] = COHESION_VALUE;
		}
		if (cells[x][y] == enemyId) {
		    map[x][y] = SURROUND_VALUE;
		}
	    }
	}

	map = diffuse(map);
	
	float max = 0;
	for (int x = 0; x < colMax; x++) {
	    for (int y = 0; y < cells.length; y++) {
		if (cells[x][y] == myId || cells[x][y] == enemyId) {
		    map[x][y] = 0; // position in use
		} else if(map[x][y] > max) {
		    max = map[x][y];
		}
	    }
	}
	// System.err.println(mapToString(map));
	
	// put the best moves in a list
	for (int x = 0; x < colMax; x++) {
	    for (int y = 0; y < cells.length; y++) {
		if(map[x][y] == max) {
		    moveList.add(new GoCoord(x,y));
		}
	    }
	}
	
	// TODO: get sorted list of moves instead?
	return moveList;
    }



    private float[][] diffuse(float[][] map) {
	long elapsed = 0;
	final int colMax = map[0].length;
	final int rowMax = map.length;
	float[][] newMap = new float[colMax][rowMax];
	float[][] tmp;
	int cnt = 40;
	boolean hasEmpty = true;
	do {
	    hasEmpty = false;
	    for (int x = 0; x < colMax; x++) {
		for (int y = 0; y < rowMax; y++) {
		    newMap[x][y] = averageAround(map, x, y);
		    if (newMap[x][y] == 0) {
			hasEmpty = true;
		    }
		}
	    }

	    // swap
	    tmp = map;
	    map = newMap;
	    newMap = tmp;
	   // System.err.println(mapToString(map));
	    elapsed = System.currentTimeMillis() - roundStart;
	    cnt--;
	} while(false);  // (hasEmpty && elapsed < MAX_TIME_MS);
	// TODO: set minimal / maximal amount of diffusion?

	return map;
    }

    private float averageAround(final float[][] map, final int x, final int y) {
	float total = 0;
	int count = 0;
	int x1, y1;

	for (int ox = -1; ox <= 1; ox++) {
	    for (int oy = -1; oy <= 1; oy++) {
		x1 = x + ox;
		y1 = y + oy;
		if (x1 == x || y1 == y) {
		    if (isWithinBounds(map, x1, y1)) {
			count++;
			total += map[x1][y1];
		    }
		}
	    }
	}
	return (total / 8);
    }

    private boolean isWithinBounds(final float[][] map, final int x, final int y) {
	final int colMax = map[0].length;
	final int rowMax = map.length;

	boolean xWithin = false;
	boolean yWithin = false;

	if (x >= 0 && x < colMax) {
	    xWithin = true;
	}

	if (y >= 0 && y < rowMax) {
	    yWithin = true;
	}

	return xWithin && yWithin;
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
