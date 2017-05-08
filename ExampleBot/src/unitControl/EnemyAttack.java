package unitControl;

import java.util.Set;
import java.util.ArrayList;
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
		defenders = new ArrayList<UnitGroup>();
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
		//System.out.println("attack.getPosition()");
		int x = 0;
		int y = 0;
		for(Unit u : units){
			if(!u.isVisible()){
				units.remove(u);
			} else {
				x += u.getPosition().getX();
				y += u.getPosition().getY();
			}
		}
		if(units.size() == 0){
			return null;
		}
		x = x/units.size();
		y = y/units.size();
		//System.out.println("Attack.position(" + x + ", " + y +")");
		return new Position(x, y);
	}
	
	public boolean update(){
		//System.out.println("attack.update()");
		for(Unit u : units){
			//System.out.print("....");
			//System.out.println(u);
			if(u == null){
				units.remove(u);
			}
		}
		if(units.size() == 0){
			//System.out.println("Attack gone");
			return false;
		}
		Position p = getPosition();
		if(p != null){
			target = BWTA.getRegion(p);
			return true;
		}
		return false;
		
	}
	
	public List<UnitGroup> getDefenders(){
		return defenders;
	}
	
	public void addDefenders(UnitGroup group){
		defenders.add(group);
	}
	
	
	
}
