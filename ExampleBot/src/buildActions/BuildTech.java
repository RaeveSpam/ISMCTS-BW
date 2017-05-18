package buildActions;

import bwapi.Game;
import bwapi.TechType;
import bwapi.Unit;
import bwapi.UnitType;

// ACTIVE ABILITIES

public class BuildTech extends BuildAction<TechType> {

	UnitType researchedAt;
	
	public BuildTech(Game game, TechType tech) {
		super(game, tech);
		researchedAt = tech.whatResearches();
		// TODO Auto-generated constructor stub
	}

	@Override
	public boolean hasBeenBuilt() {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean canBeResearched(){
		//game.self().isre
		for(Unit u : game.self().getUnits()){
			if(u.getType() == researchedAt){
				return true;
			}
		}
		return false;
	}
	
	@Override
	public boolean build() {
		for(Unit u : game.self().getUnits()){
			if(u.getType() == researchedAt){
				return u.research(type);
			}
		}
		// TODO Auto-generated method stub
		return false;
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
	public boolean canBeBuilt() {
		// TODO Auto-generated method stub
		return false;
	}

}
