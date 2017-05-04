package unitControl;

import java.util.Set;
import java.util.HashSet;
import java.util.List;

import bwapi.Position;
import bwapi.Unit;
import bwta.BWTA;
import bwta.Region;

public class EnemyAttack {
	
	private Region target;
	
	private Set<Unit> units;
	private List<UnitGroup> defenders;
	
	public EnemyAttack(Region target){
		this.target = target;
		units = new HashSet<Unit>();
	}
	
	public Region getRegion(){
		return target;
	}
	
	public void addUnit(Unit unit){
		units.add(unit);	
	}
	
	public void removeUnit(Unit unit){
		units.remove(unit);
	}
	
	public int getSize(){
		return units.size();
	}
	
	public int getSupply(){
		int result = 0;
		for(Unit u : units){
			result += u.getType().supplyRequired();
		}
		return result;
	}
	
	
	public Position getPosition(){
		int x = 0;
		int y = 0;
		for(Unit u : units){
			x += u.getPosition().getX();
			y += u.getPosition().getY();
		}
		x = x/units.size();
		y = y/units.size();
		return new Position(x, y);
	}
	
	public boolean update(){
		for(Unit u : units){
			if(!u.exists()){
				units.remove(u);
			}
		}
		if(units.size() == 0){
			return false;
		}
		target = BWTA.getRegion(getPosition());
		return true;
	}
	
	public List<UnitGroup> getDefenders(){
		return defenders;
	}
	
	public void addDefenders(UnitGroup group){
		defenders.add(group);
	}
	
	
	
}
