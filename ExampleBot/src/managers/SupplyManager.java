package managers;

import bwapi.*;

/**
 * @author Raev
 *	Manages the supply capacity of the AI. Building new Supply depots when necessary.
 */
public class SupplyManager implements Manager {


	private int projectedSupply;
	private Game game;
	private Builder builder;
	private Player self;
	
	public SupplyManager(Game game){
		this.game = game;
		builder = new Builder(game);
		projectedSupply = 0;
	}
	
	
	
	public void onStart() {
		//projectedSupply = game.self().supplyTotal();
		self = game.self();
	}

	
	public void onFrame() {
		manageSupply();
	}
	
	public void onUnitCreate(Unit unit) {
		
	}
	
	public void onUnitComplete(Unit unit){
		// If a Nexus has been build increase projected supply accordingly
		if(unit.getType() == UnitType.Protoss_Nexus){
			projectedSupply += 18;
		}
	}
	
	public void onUnitDestroy(Unit unit){
		// If a Nexus or Pylon has been destroyed decreased projected supply accordingly
		if(unit.getType() == UnitType.Protoss_Pylon){
			projectedSupply -= 16;
		} else if(unit.getType() == UnitType.Protoss_Nexus) {
			projectedSupply -= 18;
		}
	}
	
	public void manageSupply(){		
		int k = projectedSupply/10; // variable allowing building pylons just before hitting the supply limit
		System.out.println(self.supplyUsed() + "/" + self.supplyTotal() + " - " + projectedSupply);
		//if we're running out of supply and have enough minerals ...
	    if (self.supplyUsed() > projectedSupply - k && self.minerals() > 100) {
	    	//iterate over units to find a free worker
	    	projectedSupply += 16;
	    	for (Unit myUnit : self.getUnits()) {
	    		if (myUnit.getType() == UnitType.Protoss_Probe && myUnit.getOrder() == Order.MoveToMinerals) {
	    			//get a nice place to build a pylon
	    			TilePosition buildTile =
	    				builder.getBuildTile(myUnit, UnitType.Protoss_Pylon, myUnit.getTilePosition());
	    			//and, if found, send the worker to build it (and leave others alone - break;)
	    			if (buildTile != null) {
	    				if(!myUnit.build(UnitType.Protoss_Pylon, buildTile)){
	    					projectedSupply -=16;
	    				}
	    				break;
	    			}
	    		}
	    	}
	    }
	}

}
