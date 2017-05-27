package stateInformation;

import bwapi.UnitType;

public class EnemyUnit {
	public UnitType type;
	public int number;
	
	public EnemyUnit(UnitType type){
		this.type = type;
		number = 1;
	}
}
