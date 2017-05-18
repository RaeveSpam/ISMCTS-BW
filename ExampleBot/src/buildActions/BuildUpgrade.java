package buildActions;

import bwapi.Game;
import bwapi.Unit;
import bwapi.UnitType;
import bwapi.UpgradeType;

public class BuildUpgrade extends BuildAction<UpgradeType> {

	private boolean hasBeenBuilt;
	private int level;
	
	public BuildUpgrade(Game game, UpgradeType upgrade){
		super(game, upgrade);
		hasBeenBuilt = false;
		level = 0;
	}
	
	public BuildUpgrade(Game game, UpgradeType upgrade, int level){
		super(game, upgrade);
		hasBeenBuilt = false;
		this.level = level;
	}
	
	@Override
	public boolean canBeBuilt(){
		UnitType at = type.whatUpgrades();
		UnitType re = type.whatsRequired(level);
		
		boolean hasAt = false;
		boolean hasRe = false;
		
		if(type.whatsRequired(level) == UnitType.None){
			hasRe = true;
		}
		for(Unit u : game.self().getUnits()){
			if(!hasAt && u.getType() == at){
				hasAt = true;
			}
			if(!hasRe && u.getType() == re){
				hasRe = true;
			}
			if(hasAt && hasRe){
				return true;
			}
		}
		return false;
	}

	
	@Override
	public boolean hasBeenBuilt() {
		return hasBeenBuilt;
	}

	@Override
	public int getMinerals() {
		return type.mineralPrice();
	}

	@Override
	public int getGas() {
		return type.gasPrice();
	}

	@Override
	public boolean build() {
		for(Unit u : game.self().getUnits()){
			if(u.getType() == type.whatUpgrades()){
				hasBeenBuilt = u.upgrade(type);
				return hasBeenBuilt;
			}
		}
		// TODO Auto-generated method stub
		return false;
	}
}
