package buildActions;

import bwapi.Game;
import bwapi.UnitType;

public abstract class BuildAction<T> {
	public T type;
	//public TechType tech;
	public boolean isBuilding;
	Game game;
	
	public BuildAction(Game game, T type){
		this.game = game;
		this.type = type;
		isBuilding = false;
	}
	
	public abstract boolean canBeBuilt();
	
	public abstract boolean hasBeenBuilt();
	
	public abstract int getMinerals();

	public abstract int getGas();
	
	public abstract boolean build();
}
