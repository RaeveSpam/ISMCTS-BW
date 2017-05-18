package managers;

import bwapi.Game;
import bwapi.Unit;
import bwapi.UnitType;

public class ResourceManager implements Manager 

{
	private int mineralAcc;
	private int gasAcc;
	private Game game;
	private int count;
	
	public ResourceManager(Game game){
		this.game = game;
		mineralAcc = 0;
		gasAcc = 0;
		count = 0;
	}
	
	
	@Override
	public void onStart() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onFrame() {
		// TODO Auto-generated method stub
		/*if(count == 0){
			//System.out.println("BANK  " + game.self().minerals() + " | " + mineralsAvailable());
		} else if(count > 100) {
			count = 0;
		}*/
	}

	@Override
	public void onUnitComplete(Unit unit) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onUnitCreate(Unit unit) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onUnitDestroy(Unit unit) {
		// TODO Auto-generated method stub
		
	}
	
	public int mineralsAvailable(){
		//System.out.println(game.self().minerals()-mineralAcc);
		return game.self().minerals()-mineralAcc;
	}
	
	public int gasAvailable(){
		return game.self().gas()-gasAcc;
	}
	
	
	public boolean build(UnitType unit){
		//System.out.println(unit.mineralPrice() + "<" + mineralsAvailable());
		if(unit.mineralPrice() <= mineralsAvailable() &&
			unit.gasPrice() <= gasAvailable()){
			if(unit.isBuilding()){
				//mineralAcc += unit.mineralPrice();
				//gasAcc += unit.gasPrice();
			}
			return true;
		} else {
			return false;
		}
		
	}

	@Override
	public void onUnitDiscover(Unit unit) {
		
	/*	if(game.self() == unit.getPlayer() && unit.getType().isBuilding()){
			System.out.println(unit.getType() + " " + unit.getType().mineralPrice());
			mineralAcc -= unit.getType().mineralPrice();
			gasAcc -= unit.getType().gasPrice();
		}*/
	}

}
