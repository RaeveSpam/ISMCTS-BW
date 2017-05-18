package buildActions;

import bwapi.Game;
import bwapi.Unit;
import bwapi.UnitType;
import bwapi.Pair;

public class BuildUnit extends BuildAction<UnitType> {

	private boolean hasBeenBuilt;
	private Unit trainAt;
	
	public BuildUnit(Game game, UnitType unit){
		super(game, unit);
		hasBeenBuilt = false;
		trainAt = null;
	}
	
	public boolean setTrainAt(Unit unit){
		if(unit.exists() && unit.getType() == type.whatBuilds().first){
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
		return type.mineralPrice();
	}

	@Override
	public int getGas() {
		return type.gasPrice();
	}

	
	
	@Override
	public boolean build() {
		//System.out.println("build " + type);
		if(trainAt != null){
			if(!trainAt.exists()){
				return false;
			}
			if(trainAt.getTrainingQueue().size() < 1){
				hasBeenBuilt = trainAt.train(type);
			}
		} else {
			Pair pair = type.whatBuilds();
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
			if(best != null && best.getTrainingQueue().size() < 1){
				hasBeenBuilt = best.train(type);
			}
		}
		return hasBeenBuilt;
	}

	@Override
	public boolean canBeBuilt() {
		if(trainAt != null){
			if(!trainAt.exists() || trainAt.getTrainingQueue().size() > 0){
				return false;
			}
		}
		//System.out.println(type + " " + game.self().isUnitAvailable(type));
		return game.self().isUnitAvailable(type);
	}

}
