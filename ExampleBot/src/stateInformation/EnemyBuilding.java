package stateInformation;

import bwapi.Game;
import bwapi.TilePosition;
import bwapi.Unit;
import bwapi.UnitType;

public class EnemyBuilding {
	
	public UnitType type;
	
	public TilePosition position;
	
	public String name;
	
	public EnemyBuilding(){
		
	}
	
	public EnemyBuilding(Unit unit){
		type = unit.getType();
		
		position = unit.getTilePosition();
	}
	
	/**
	 * Used for determinization.
	 * @param type
	 */
	public EnemyBuilding(UnitType type){
		name = "dickWingle";
		this.type = type;
		position = null;
	}
	
	public boolean stillExists(Game game){
		
		if(!game.isVisible(position)){
			// Not visible, assume it's still there
			return true;
		}
		if(game.isVisible(position)){
			// Is visible, check if still is on tile
			for(Unit unit : game.getUnitsOnTile(position)){
				if(unit.getType() == type){
					return true;
				}
			}
		} 
		return false;
	}
	@Override
	public boolean equals(Object other){
		return ((EnemyBuilding)other).position == position && ((EnemyBuilding)other).type == type;	
	}
}

