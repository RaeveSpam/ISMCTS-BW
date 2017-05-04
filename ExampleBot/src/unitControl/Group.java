package unitControl;

import java.util.ArrayList;
import java.util.List;

import bwapi.Game;
import bwapi.Position;
import bwapi.Unit;

public abstract class Group {

	protected Game game;
	protected List<Unit> units;
	protected Position lastOrder;
	
	public Group(Game game){
		this.game = game;
		units = new ArrayList<Unit>();
	}
	
	public void moveOrder(Position target){
		//lastOrder = target;
		for(Unit i : units)
		{
			i.move(target);
		}	
	}
	
	public void attackMoveOrder(Position target){
		lastOrder = target;
		for(Unit i : units)
		{
			i.attack(target);
		}	
	}
	
	public void attackOrder(Unit unit){
		lastOrder = unit.getPosition();
		for(Unit i : units){
			i.attack(unit);
		}
	}
	
	public void holdPositionOrder(){
		for(Unit i : units){
			i.holdPosition();
		}		
	}
	
	public void addUnit(Unit unit){
		units.add(unit);
	}
	
	public void removeUnit(Unit unit){
		units.remove(unit);
	}
	
	public int size(){
		return units.size();
	}
	
	public abstract int getSupply();
	
	public abstract void onFrame();
}
