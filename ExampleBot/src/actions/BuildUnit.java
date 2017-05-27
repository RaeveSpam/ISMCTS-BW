package actions;

import ISMCTS.Entity;
import ISMCTS.ISMCTS;
import bwapi.Game;
import bwapi.Unit;
import bwapi.UnitType;
import stateInformation.Player;
import bwapi.Pair;

public class BuildUnit extends BuildAction<Entity> {

	private transient boolean hasBeenBuilt;
	private transient Unit trainAt;
	
	public BuildUnit(){
		super();
	}
	
	public BuildUnit(Entity unit){
		super();
		type = unit;
	}
	
	public BuildUnit(Game game, Entity unit){
		super(game, unit);
		hasBeenBuilt = false;
		trainAt = null;
	}
	
	public BuildUnit(Game game, Entity unit, Player player){
		super(game, unit);
		hasBeenBuilt = false;
		trainAt = null;
		this.player = player;
	}
	
	public boolean setTrainAt(Unit unit){
		if(unit.exists() && unit.getType() == ISMCTS.entityToType(type).whatBuilds().first){
			trainAt = unit;
			return true;
		} else {
			return false;
		}
	}
	
	@Override
	public boolean hasBeenBuilt() {
		return hasBeenBuilt;
	}

	@Override
	public int getMinerals() {
		return ISMCTS.entityToType(type).mineralPrice();
	}

	@Override
	public int getGas() {
		return ISMCTS.entityToType(type).gasPrice();
	}

	
	
	@Override
	public boolean build() {
		//System.out.println("build " + type);
		if(trainAt != null){
			if(!trainAt.exists()){
				return false;
			}
			if(trainAt.getTrainingQueue().size() < 2){
				hasBeenBuilt = trainAt.train(ISMCTS.entityToType(type));
			}
		} else {
			Pair pair = ISMCTS.entityToType(type).whatBuilds();
			Unit best = null;
			for(Unit u : game.self().getUnits()){
				if(u.getType() == pair.first){
					if(best == null){
						best = u;
					} else if(u.getTrainingQueue().size() < best.getTrainingQueue().size()){
						best = u;
					}
					
				}
			}
			if(best != null && best.getTrainingQueue().size() < 2){
				hasBeenBuilt = best.train(ISMCTS.entityToType(type));
			}
		}
		return hasBeenBuilt;
	}

	@Override
	public boolean canBeBuilt() {
		/*if(trainAt != null){
			if(!trainAt.exists() || trainAt.getTrainingQueue().size() > 0){
				return false;
			}
		}*/
		//System.out.println(type + " " + game.self().isUnitAvailable(type));
		return game.self().isUnitAvailable(ISMCTS.entityToType(type));
	}

}
