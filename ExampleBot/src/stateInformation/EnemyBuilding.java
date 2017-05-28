package stateInformation;

import bots.ISMCTSBot;
import bwapi.Game;
import bwapi.TilePosition;
import bwapi.Unit;
import bwapi.UnitType;
import ISMCTS.Entity;
import ISMCTS.ISMCTS;

public class EnemyBuilding {
	
	public Entity type;
	
	
	public transient TilePosition position;
	
	public transient String name;
	
	public EnemyBuilding(){
		
	}
	
	public EnemyBuilding(Unit unit){
		type = ISMCTS.typeToEntity(unit.getType());
		
		position = unit.getTilePosition();
	}
	
	/**
	 * Used for determinization.
	 * @param type
	 */
	public EnemyBuilding(Entity type){
		//name = "dickWingle";
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
				if(ISMCTS.typeToEntity(unit.getType())  == type){
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

