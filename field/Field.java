// // Copyright 2016 theaigames.com (developers@theaigames.com)

//    Licensed under the Apache License, Version 2.0 (the "License");
//    you may not use this file except in compliance with the License.
//    You may obtain a copy of the License at

//        http://www.apache.org/licenses/LICENSE-2.0

//    Unless required by applicable law or agreed to in writing, software
//    distributed under the License is distributed on an "AS IS" BASIS,
//    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//    See the License for the specific language governing permissions and
//    limitations under the License.
//  
//    For the full copyright and license information, please view the LICENSE
//    file that was distributed with this source code.

package field;

import java.util.ArrayList;

import move.Move;

/**
 * Field class
 * 
 * Handles everything that has to do with the field, such as storing the current state and performing calculations on the field.
 * 
 * @author Jim van Eeden <jim@starapple.nl>, Joost de Meij <joost@starapple.nl>
 */

public class Field {
    private int myId;
    private int opponentId;
    private int rows;
    private int cols;

    private int[][] field;

    public Field() {}

    /**
     * Initializes field
     * 
     * @throws Exception
     */
    public void initField() throws Exception {
	try {
	    this.field = new int[this.cols][this.rows];
	} catch (Exception e) {
	    throw new Exception("Error: trying to initialize field while field settings have not been parsed yet.");
	}
	clearField();
    }

    /**
     * Parse field from comma separated String
     * 
     * @param String
     *            : input from engine
     */
    public void parseFromString(String s) {
	s = s.replace(";", ",");
	String[] r = s.split(",");
	int counter = 0;
	for (int y = 0; y < rows; y++) {
	    for (int x = 0; x < cols; x++) {
		field[x][y] = Integer.parseInt(r[counter]);
		counter++;
	    }
	}
    }

    /**
     * Sets the whole field to empty
     */
    public void clearField() {
	for (int y = 0; y < rows; y++) {
	    for (int x = 0; x < cols; x++) {
		field[x][y] = 0;
	    }
	}
    }

    /**
     * Returns a list of all available moves. i.e. empty cells that will not result in a suicide move. This does *not* take the Ko rule in to account. TODO: implement Ko rule and
     * remove moves from available moves list that violate this rule
     * 
     * @return : a list of all available moves in this game state
     */
    public ArrayList<Move> getAvailableMoves() {
	final ArrayList<Move> moves = new ArrayList<Move>();

	for (int y = 0; y < rows; y++) {
	    for (int x = 0; x < cols; x++) {
		if (isEmptyPoint(x, y)) {
		    moves.add(new Move(x, y));
		}
	    }
	}

	return moves;
    }

    public boolean isEmptyPoint(int x, int y) {
	return field[x][y] == 0;
    }

    /**
     * Checks if move if a suicide move
     *  TODO: DOES NOT WORK
     * @param x
     *            : point x
     * @param y
     *            : point y
     * @return : True if move is a suicide move, false otherwise
     */
    public boolean isSuicideMove(int x, int y) {
	final boolean[][] mark = new boolean[rows][cols];
	for (int ty = 0; ty < rows; ty++) {
	    for (int tx = 0; tx < cols; tx++) {
		mark[tx][ty] = false;
	    }
	}

	field[x][y] = this.myId; // temporarily place stone on the board
	final int liberties = flood(mark, x, y, this.myId, 0);
	field[x][y] = 0; // remove temporary stone again

	return (liberties <= 0);
    }

    /**
     * Recursive function to count the amount of liberties a string of stones has
     * 
     * @param mark
     *            : visited points on the board
     * @param x
     *            : current point x
     * @param y
     *            : current point y
     * @param player
     *            : color of stone string
     * @param stackCounter
     *            : count the amount of recursions
     * @return : the count of liberties of stone string at point x,y. Liberties maybe be counted double. TODO: if needed, mark found liberties as well and don't count those
     */
    private int flood(boolean[][] mark, int x, int y, int player, int stackCounter) {
	// Stop if outside the board
	if (x < 0)
	    return 0;
	if (y < 0)
	    return 0;
	if (x >= rows)
	    return 0;
	if (y >= cols)
	    return 0;
	// Stop if we find a point that already was visited
	if (mark[x][y])
	    return 0;

	if (field[x][y] != player) {
	    if (field[x][y] == 0) {
		return 1; // Count +1 if we found a liberty
	    }
	    return 0; // Stop if we find opponent stone
	}

	// mark as visited
	mark[x][y] = true;

	// Recursively fill surrounding fields
	int neighborLibertyCount = 0;
	if (stackCounter < rows * cols) {
	    neighborLibertyCount += flood(mark, x - 1, y, player, stackCounter + 1);
	    neighborLibertyCount += flood(mark, x + 1, y, player, stackCounter + 1);
	    neighborLibertyCount += flood(mark, x, y - 1, player, stackCounter + 1);
	    neighborLibertyCount += flood(mark, x, y + 1, player, stackCounter + 1);
	}
	return neighborLibertyCount;
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
	for (int y = 0; y < rows; y++) {
	    for (int x = 0; x < cols; x++) {
		if (counter > 0) {
		    s += ",";
		}
		s += field[x][y];
		counter++;
	    }
	}
	return s;
    }

    public void setColumns(int value) {
	this.cols = value;
    }

    public int getColumns() {
	return this.cols;
    }

    public void setRows(int value) {
	this.rows = value;
    }

    public int getRows() {
	return this.rows;
    }

    public void setMyId(int id) {
	this.myId = id;
    }

    public void setOpponentId(int id) {
	this.opponentId = id;
    }
}