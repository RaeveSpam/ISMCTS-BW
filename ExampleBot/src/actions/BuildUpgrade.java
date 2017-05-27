package actions;

import ISMCTS.ISMCTS;
import ISMCTS.Tech;
import bwapi.Game;
import bwapi.Unit;
import bwapi.UnitType;
import bwapi.UpgradeType;
import stateInformation.Player;
import ISMCTS.Move;

public class BuildUpgrade extends BuildAction<Tech> {

	private boolean hasBeenBuilt;
	private int level;
	
	public BuildUpgrade(){
		super();
		move = Move.Upgrade;
	}
	
	public BuildUpgrade(Game game, Tech upgrade){
		super(game, upgrade);
		hasBeenBuilt = false;
		level = 0;
		move = Move.Upgrade;
	}
	
	public BuildUpgrade(Game game, Tech upgrade, int level){
		super(game, upgrade);
		hasBeenBuilt = false;
		this.level = level;
		move = Move.Upgrade;
	}
	
	public BuildUpgrade(Game game, Tech upgrade, int level, Player player){
		super(game, upgrade);
		hasBeenBuilt = false;
		this.level = level;
		this.player = player;
		move = Move.Upgrade;
	}
	
	@Override
	public boolean canBeBuilt(){
		UnitType at = ISMCTS.techToUpgrade(type).whatUpgrades();
		UnitType re = ISMCTS.techToUpgrade(type).whatsRequired(level);
		
		boolean hasAt = false;
		boolean hasRe = false;
		
		if(ISMCTS.techToUpgrade(type).whatsRequired(level) == UnitType.None){
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
		return ISMCTS.techToUpgrade(type).mineralPrice();
	}

	@Override
	public int getGas() {
		return ISMCTS.techToUpgrade(type).gasPrice();
	}

	@Override
	public boolean build() {
		for(Unit u : game.self().getUnits()){
			if(u.getType() == ISMCTS.techToUpgrade(type).whatUpgrades()){
				hasBeenBuilt = u.upgrade(ISMCTS.techToUpgrade(type));
				if(hasBeenBuilt) {
					return true;
				}
			}
		}
		// TODO Auto-generated method stub
		return false;
	}
}
