package unitControl;
import java.util.List;

import bwapi.Unit;
import bwapi.Game;
import bwapi.Position;
import bwapi.Region;

public class UnitCluster {
	private List<Unit> units;
	private Game game;
	
	
	public Region getRegion(){
		return game.getRegionAt(getPosition());
	}
	
	public void addUnit(Unit unit){
		units.add(unit);
	}
	
	public int getSize(){
		return units.size();
	}
	
	public int getPopulation(){
		int result = 0;
		for(Unit u : units){
			result += u.getType().supplyRequired();
		}
		return result;
	}
	
	public Position getPosition(){
		return null;
	}
	
	public boolean update(){
		Unit u = units.get(0);
	
		if(getSize() == 0){
			return false;
		} else {
			return true;
		}
	}
}
