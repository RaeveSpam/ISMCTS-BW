package stateInformation;

import ISMCTS.Entity;
import bwapi.UnitType;

public class EnemyUnit {
	public Entity type;
	public int number;
	
	public EnemyUnit(){
		
	}
	
	public EnemyUnit(Entity type){
		this.type = type;
		number = 1;
	}
	
	public boolean equals(EnemyUnit other){
		return type == other.type && 
				(number/10 + 1 == other.number/10 + 1);
	}
}
