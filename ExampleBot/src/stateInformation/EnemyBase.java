package stateInformation;

import bwapi.Unit;
import bwapi.UnitType;
import bwta.BWTA;
import bwta.BaseLocation;
import ISMCTS.Entity;
import ISMCTS.ISMCTS;

public class EnemyBase extends EnemyBuilding {

	public EnemyBase(){
		
	}
	
	public EnemyBase(Unit nexus){
		super(nexus);
	}
	
	public EnemyBase(BaseLocation location){
		type = Entity.Nexus;
		position = location.getTilePosition();
	}
	
	public BaseLocation getBaseLocation(){
		return BWTA.getNearestBaseLocation(position);
	}
}
