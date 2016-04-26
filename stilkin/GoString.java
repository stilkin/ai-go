package stilkin;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 
 * @author stilkin
 *
 */
public class GoString {
    private final GoField parentField;
    private final Set<GoCoord> coords = new HashSet<GoCoord>();
    private final Set<GoCoord> liberties = new HashSet<GoCoord>();

    public GoString(final GoField parentField) {
	this.parentField = parentField;
    }
    
    public void add(final GoCoord coord){
	coords.add(coord);
	liberties.addAll(parentField.getFreeNeighbours(coord));
    }

    public Set<GoCoord> getCoords() {
        return coords;
    }

    public Set<GoCoord> getLiberties() {
        return liberties;
    }

    public List<GoCoord> getCoordsList() {
	return new ArrayList<GoCoord>(coords);
    }
    
    public List<GoCoord> getLibertiesList() {
	return new ArrayList<GoCoord>(liberties);
    }

    public int size() {
	return coords.size();
    }
    
    public int libertyCount(){
	return liberties.size();
    }
    
    @Override
    public String toString() {
        return coords.toString();
    }
}
