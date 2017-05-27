package actions;

import bwapi.Game;
import ISMCTS.Entity;
import ISMCTS.Move;
import stateInformation.Player;

public abstract class BuildAction<T> extends Action {
	//public T type;
	//public TechType tech;
	public boolean isBuilding;
	public transient Game game;
	public T type;
	
	public BuildAction(){
		super();
		isBuilding = true;
		move = Move.Build;
	}
	
	public BuildAction(Game game, T type){
		this.game = game;
		this.type = type;
		isBuilding = false;
		move = Move.Build;
	}
	
	public BuildAction(Game game, T type, Player player){
		move = Move.Build;
		this.game = game;
		this.type = type;
		isBuilding = false;
		this.player = player;
	}
	
	@Override
	public void print(){
		System.out.println(type);
	}
	
	public abstract boolean canBeBuilt();
	
	public abstract boolean hasBeenBuilt();
	
	public abstract int getMinerals();

	public abstract int getGas();
	
	public abstract boolean build();
}
