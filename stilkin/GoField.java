package stilkin;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

/**
 * 
 * @author stilkin
 *
 */
public class GoField {
    public static int MAX_WIDTH = 19;
    public static int MAX_HEIGHT = 19;
    private int[][] cells = new int[MAX_WIDTH][MAX_HEIGHT];
    private int[][] liberties = new int[MAX_WIDTH][MAX_HEIGHT];

    public void changeSize(final int width, final int height) {
	MAX_WIDTH = width;
	MAX_HEIGHT = height;
	cells = new int[MAX_WIDTH][MAX_HEIGHT];
	liberties = new int[MAX_WIDTH][MAX_HEIGHT];
    }

    public List<GoCoord> getNonEmptyCells() {
	final ArrayList<GoCoord> moveList = new ArrayList<GoCoord>();

	for (int y = 0; y < MAX_HEIGHT; y++) {
	    for (int x = 0; x < MAX_WIDTH; x++) {
		if (!isEmptyCell(x, y)) {
		    moveList.add(new GoCoord(x, y));
		}
	    }
	}
	return moveList;
    }

    /**
     * Get a list of coords with a specific value, with a minimal amount of liberties
     * 
     * @param value
     * @param liberties
     * @return
     */
    public List<GoCoord> getCoordsWithLiberties(final int value, final int liberties) {
	final ArrayList<GoCoord> coordList = new ArrayList<GoCoord>();

	for (int y = 0; y < MAX_HEIGHT; y++) {
	    for (int x = 0; x < MAX_WIDTH; x++) {
		if (cells[x][y] == value) {
		    if (getFreeNeighbours(x, y) == liberties) {
			coordList.add(new GoCoord(x, y));
		    }
		}
	    }
	}
	return coordList;
    }
    
    public void setCell(final int x, final int y, final int value){
	cells[x][y] = value;
    }

    /**
     * Get a list of all cells with this value (player id, or zero)
     * 
     * @param value
     * @return
     */
    public List<GoCoord> getCoordsWithValue(final int value) {
	final ArrayList<GoCoord> coordList = new ArrayList<GoCoord>();

	for (int y = 0; y < MAX_HEIGHT; y++) {
	    for (int x = 0; x < MAX_WIDTH; x++) {
		if (cells[x][y] == value) {
		    coordList.add(new GoCoord(x, y));
		}
	    }
	}
	return coordList;
    }

    public void updateLiberties() {
	for (int y = 0; y < MAX_HEIGHT; y++) {
	    for (int x = 0; x < MAX_WIDTH; x++) {
		liberties[x][y] = countFreeNeighbours(x, y);
	    }
	}
    }

    public int getFreeNeighbours(final int x, final int y) {
	if (isWithinBounds(x, y)) {
	    return liberties[x][y];
	} else {
	    return 0;
	}
    }

    public int countFreeNeighbours(final int x, final int y) {
	int liberties = 0;
	int x1, y1;
	x1 = x - 1;
	y1 = y;
	if (isWithinBounds(x1, y1)) {
	    if (isEmptyCell(x1, y1)) {
		liberties++;
	    }
	}
	x1 = x + 1;
	y1 = y;
	if (isWithinBounds(x1, y1)) {
	    if (isEmptyCell(x1, y1)) {
		liberties++;
	    }
	}
	x1 = x;
	y1 = y - 1;
	if (isWithinBounds(x1, y1)) {
	    if (isEmptyCell(x1, y1)) {
		liberties++;
	    }
	}
	x1 = x;
	y1 = y + 1;
	if (isWithinBounds(x1, y1)) {
	    if (isEmptyCell(x1, y1)) {
		liberties++;
	    }
	}

	return liberties;
    }

    public List<GoCoord> getAdjacendCoords(final int x, final int y, final int value) {
	final List<GoCoord> coordList = new ArrayList<GoCoord>();
	int x1, y1;
	x1 = x - 1;
	y1 = y;
	if (isWithinBounds(x1, y1)) {
	    if (cells[x1][y1] == value) {
		coordList.add(new GoCoord(x1, y1));
	    }
	}
	x1 = x + 1;
	y1 = y;
	if (isWithinBounds(x1, y1)) {
	    if (cells[x1][y1] == value) {
		coordList.add(new GoCoord(x1, y1));
	    }
	}
	x1 = x;
	y1 = y - 1;
	if (isWithinBounds(x1, y1)) {
	    if (cells[x1][y1] == value) {
		coordList.add(new GoCoord(x1, y1));
	    }
	}
	x1 = x;
	y1 = y + 1;
	if (isWithinBounds(x1, y1)) {
	    if (cells[x1][y1] == value) {
		coordList.add(new GoCoord(x1, y1));
	    }
	}
	return coordList;
    }

    public List<HashSet<GoCoord>> getStringSetOfType(final int value) {
	final List<HashSet<GoCoord>> stringSet = new ArrayList<HashSet<GoCoord>>();
	final List<GoCoord> enemyPieces = getCoordsWithValue(value);
	final List<GoCoord> usedPieces = new ArrayList<GoCoord>();

	GoCoord ePiece;
	while (!enemyPieces.isEmpty()) {
	    // get a piece
	    ePiece = enemyPieces.get(0);
	    enemyPieces.remove(ePiece);
	    usedPieces.add(ePiece);

	    // start a string
	    final HashSet<GoCoord> string = new HashSet<GoCoord>();
	    string.add(ePiece);

	    addNeighBours(ePiece, value, string, enemyPieces, usedPieces);
	    stringSet.add(string);
	}

	return stringSet;
    }

    private void addNeighBours(final GoCoord ePiece, final int value, final HashSet<GoCoord> string, final List<GoCoord> enemyPieces, final List<GoCoord> usedPieces) {
	// get all neighbours of same color
	final List<GoCoord> neighbours = getAdjacendCoords(ePiece.x, ePiece.y, value);
	for (GoCoord nb : neighbours) {
	    // add to string
	    if (enemyPieces.contains(nb)) {
		string.add(nb);
		enemyPieces.remove(nb);
		usedPieces.add(nb);
		// recurse for the neighbours
		addNeighBours(nb, value, string, enemyPieces, usedPieces);
	    }

	}
    }
    
    public List<GoCoord> getLibertiesOfString(final Collection<GoCoord> stringSet){
	final List<GoCoord> liberties = new ArrayList<GoCoord>();
	
	for(GoCoord piece: stringSet) {
	    liberties.addAll(getAdjacendCoords(piece.x,piece.y, 0));
	}
	
	return liberties;
    }

    private boolean isWithinBounds(final int x, final int y) {
	boolean xWithin = false;
	boolean yWithin = false;

	if (x >= 0 && x < MAX_WIDTH) {
	    xWithin = true;
	}

	if (y >= 0 && y < MAX_HEIGHT) {
	    yWithin = true;
	}

	return xWithin && yWithin;
    }

    public int[][] getCells() {
	return cells;
    }

    public boolean isEmptyCell(final int x, final int y) {
	return (cells[x][y] == 0);
    }

    /**
     * Parse field from comma separated String
     * 
     * @author Jim van Eeden <jim@starapple.nl>, Joost de Meij <joost@starapple.nl>
     * @param String
     *            : input from engine
     */
    public void parseFromString(String s) {
	s = s.replace(";", ",");
	String[] r = s.split(",");
	int counter = 0;
	for (int y = 0; y < MAX_HEIGHT; y++) {
	    for (int x = 0; x < MAX_WIDTH; x++) {
		cells[x][y] = Integer.parseInt(r[counter]);
		counter++;
	    }
	}
	updateLiberties(); // TODO: check if this is not too slow
    }

    @Override
    /**
     * Creates comma separated String that represents the field
     * 
     * @param args
     *            :
     * @return : Comma separated string of player Id's or 0 for empty.
     */
    public String toString() {
	String s = "";
	int counter = 0;
	for (int y = 0; y < MAX_HEIGHT; y++) {
	    for (int x = 0; x < MAX_WIDTH; x++) {
		if (counter > 0) {
		    s += ",";
		}
		s += cells[x][y];
		counter++;
	    }
	}
	return s;
    }

    public String toPrettyString() {
	String s = "   >";
	
	for (int x = 0; x < MAX_WIDTH; x++) {
	    if (x < 10 || x % 2 != 0){
		s += String.format("%2d", x);
	    } else {
		s += "  ";
	    }
	}
	s += "\n";
	for (int y = 0; y < MAX_HEIGHT; y++) {
	    s += String.format("%2d|  ", y);
	    for (int x = 0; x < MAX_WIDTH; x++) {
		s += cells[x][y] + " ";
	    }
	    s += "\n";
	}
	return s;
    }

}
