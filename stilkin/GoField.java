package stilkin;

import java.util.ArrayList;
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

    public void changeSize(final int width, final int height) {
	MAX_WIDTH = width;
	MAX_HEIGHT = height;
	cells = new int[MAX_WIDTH][MAX_HEIGHT];
    }

    public List<GoCoord> getSortedMoveList() {
	final ArrayList<GoCoord> moveList = new ArrayList<GoCoord>();

	for (int y = 0; y < MAX_HEIGHT; y++) {
	    for (int x = 0; x < MAX_WIDTH; x++) {
		if (isEmptyCell(x, y)) {
		    moveList.add(new GoCoord(x, y));
		}
	    }
	}

	return moveList;
    }
    
    public int[][] getCells(){
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
	String s = " ";
	for (int y = 0; y < MAX_HEIGHT; y++) {
	    for (int x = 0; x < MAX_WIDTH; x++) {
		s += cells[x][y] + " ";
	    }
	    s += "\n ";
	}
	return s;
    }

}
