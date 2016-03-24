package stilkin;

/**
 * 
 * @author stilkin
 *
 */
public class GoCoord {
    public int x;
    public int y;

    public GoCoord() {
	x = 0;
	y = 0;
    }

    public GoCoord(int x, int y) {
	this.x = x;
	this.y = y;
    }

    @Override
    public String toString() {
	return "(" + x + ", " + y + ")";
    }

    public int getX() {
	return x;
    }

    public void setX(int x) {
	this.x = x;
    }

    public int getY() {
	return y;
    }

    public void setY(int y) {
	this.y = y;
    }

}
