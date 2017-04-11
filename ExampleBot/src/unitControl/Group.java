package unitControl;

import java.util.ArrayList;
import java.util.List;

import bwapi.Game;
import bwapi.Position;
import bwapi.Unit;

public abstract class Group {

	Game game;
	List<Integer> units;
	
	public Group(Game game){
		this.game = game;
		units = new ArrayList<Integer>();
	}
	
	public void moveOrder(Position target){
		for(Integer i : units)
		{
			game.getUnit(units.get(i)).move(target);
		}	
	}
	
	public void attackMoveOrder(Position target){
		for(Integer i : units)
		{
			game.getUnit(units.get(i)).attack(target);
		}	
	}
	
	public void attackOrder(Unit unit){
		for(Integer i : units){
			game.getUnit(units.get(i)).attack(unit);
		}
	}
	
	public void holdPositionOrder(){
		for(Integer i : units){
			game.getUnit(units.get(i)).holdPosition();
		}		
	}
	
	public void addUnit(Unit unit){
		units.add(unit.getID());
	}
	
	public void removeUnit(Unit unit){
		if(units.contains(unit.getID()))
		{
			units.remove(unit.getID());
		}
	}
	
	public int size(){
		return units.size();
	}
	
	public abstract int getSupply();
	
	public abstract void onFrame();
}
