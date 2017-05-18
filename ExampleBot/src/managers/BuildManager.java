package managers;

import bwapi.Game;
import bwapi.Order;
import bwapi.TilePosition;
import bwapi.Unit;
import bwapi.UnitType;
import bwapi.UpgradeType;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import buildActions.*;

public class BuildManager implements Manager {
	
	LinkedList<BuildAction> buildQueue;
	ArrayList<BuildBuilding> activeBuilds;
	Game game;
	Builder builder;
	Builder pylonBuilder;
	int timeOut = 100;
	
	private int projectedSupply;
	private int count;
	
	public BuildManager(Game game){
		 buildQueue = new LinkedList<BuildAction>();
		 this.game = game;
		 builder = new Builder(game, 20);
		 pylonBuilder = new Builder(game, 40, 1);
		 activeBuilds = new ArrayList<BuildBuilding>();
		 
		 projectedSupply = 0;
		 count = 0;
	}
	
	private void manageWorkersAndBases(){
		// Find and assign idle workers
		List<Unit> units = game.self().getUnits();
		//System.out.println(units.size() + " units found");
		for(Unit myUnit : units){
			if(myUnit.getType() == UnitType.Protoss_Probe && ( 	
					myUnit.getOrder() == Order.None || 
					myUnit.getOrder() == Order.Nothing ||
					myUnit.getOrder() == Order.PlayerGuard)){
				assignIdleWorker(myUnit);
			} else if(myUnit.getType() == UnitType.Protoss_Nexus){
				manageBase(myUnit);
			}
		}
	}
	
	private void assignIdleWorker(Unit worker){
		//find the closest mineral
		Unit closestMineral = null;    
	    for (Unit neutralUnit : game.neutral().getUnits()) {
	        if (neutralUnit.getType().isMineralField()) {
	            if (closestMineral == null || worker.getDistance(neutralUnit) < worker.getDistance(closestMineral)) {
	                closestMineral = neutralUnit;
	            }
	        }
	    }

	    //if a mineral patch was found, send the worker to gather it
	    if (closestMineral != null) {
	        worker.gather(closestMineral, false);
	    }
		//TODO send to nearest mineral
		//TODO max 3? workers on each mineral
	}
	
	private void manageBase(Unit nexus){
		
		if(nexus.getType() != UnitType.Protoss_Nexus){
			return;
		}
		// Count workers & available resources
		List<Unit> units = nexus.getUnitsInRadius(200);
		int workers = 0;
		int resources = 0;
		for(Unit u : units){
			if(u.getType() == UnitType.Protoss_Probe){
				workers++;
			} else if(u.getType().isMineralField() ||
					u.getType() == UnitType.Protoss_Assimilator){
				resources++;
			} else if(u.getType() == UnitType.Resource_Vespene_Geyser){
				if(game.self().supplyUsed() > 30){
					//System.out.println("Build Assimilator");
					BuildBuilding bb = new BuildBuilding(game, UnitType.Protoss_Assimilator);
					bb.buildTile = u.getTilePosition();
					if(canAfford(bb)){
						buildQueue.addFirst(bb);
					}
				}
			}
		}
		if(workers < resources*3 && nexus.getTrainingQueue().size() < 1 && game.self().supplyUsed() < game.self().supplyTotal() && canAfford(UnitType.Protoss_Probe)){
			nexus.train(UnitType.Protoss_Probe);
		}
	}
	
	@Override
	public void onStart() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onFrame() {
		count++;
		if(count == 5){
			manageWorkersAndBases();
		} else if(count == 25){
			manageSupply();
		} else if(count % 10 == 0){
			manageBuildQueue();
		} else if(count >= 30){
			count = 0;
		}
	}
	
	private void manageBuildQueue(){
		// Check ongoing builds
		for(BuildBuilding a : activeBuilds){
			a.count++;//System.out.println(a.type);
			if(a.hasBeenBuilt()){
				activeBuilds.remove(a);
			} else if(a.count > timeOut){
				a.count = 0;
				build(a);
				break;
			}
		}		
		// start new builds
	/*	System.out.print("[");
		for(BuildAction b : buildQueue){
			System.out.print(b.type + " ; ");
		}
		System.out.println("]"); */ 
		boolean cont = true;
		
		//System.out.println(buildQueue.size());
		while(cont && !buildQueue.isEmpty()){
			BuildAction ba = buildQueue.peek();
			//System.out.print(ba.type + ", ");
			if(!ba.canBeBuilt()){ // doesn't have requirements, remove.
				buildQueue.pop();
				/*if(buildQueue.size() > 2){
					break;
				}*/
			} else {
				if(canAfford(ba)){
					if(ba.isBuilding){
						if(build((BuildBuilding)ba)){;
							activeBuilds.add((BuildBuilding)buildQueue.pop());
						} else {
							cont = false;
						}
					} else {
						if(ba.build()){
							buildQueue.pop();
						} else {
							buildQueue.addLast(buildQueue.pop());
							cont = false;
						}
					}
				} else {
					cont = false;
				}
			}
		}
	}
	
	private void harvestGas(Unit refinery){
		int i = 0;
		for(Unit u : game.self().getUnits()){
			if(i < 3 && u.getType() == UnitType.Protoss_Probe && (u.getOrder() == Order.MoveToMinerals || u.getOrder() == Order.ReturnMinerals)){
				i++;
				u.gather(refinery);
			}
		}
	}
	
	public boolean canReallyAfford(UnitType type){
		// Calculate available resources
		int minerals = game.self().minerals();
		int gas = game.self().gas();
		for(BuildAction a : activeBuilds){
			minerals -= a.getMinerals();
			gas -= a.getGas();
		}
		for(BuildAction a : buildQueue){
			minerals -= a.getMinerals();
			gas -= a.getGas();
		}
		return type.mineralPrice() <= minerals && type.gasPrice() <= gas;
	}
	
	public boolean canAfford(UnitType type){
		// Calculate available resources
		int minerals = game.self().minerals();
		int gas = game.self().gas();
		for(BuildAction a : activeBuilds){
			minerals -= a.getMinerals();
			gas -= a.getGas();
		}
		
		return type.mineralPrice() <= minerals && type.gasPrice() <= gas;
	}
	
	private boolean canAfford(BuildAction buildAction){
		// Calculate available resources
		//System.out.println("canAfford " + buildAction.type);
		int minerals = game.self().minerals();
		int gas = game.self().gas();
		for(BuildAction a : activeBuilds){
			minerals -= a.getMinerals();
			gas -= a.getGas();
		}
		
		return buildAction.getMinerals() <= minerals && buildAction.getGas() <= gas;
	}
	
	private boolean build(BuildBuilding ba){
		if(!ba.hasBuilder()){
			Unit probe = getProbe();
			if(probe != null){
				ba.assignBuilder(probe);
			} else {
				return false;
			}
		}
		//System.out.println(ba.type + "tile == " + ba.isBuildTileValid());
		if(!ba.isBuildTileValid()){
			TilePosition pos;
			if(ba.type == UnitType.Protoss_Pylon){
				pos = pylonBuilder.getProtossBuildTile(ba.probe, ba.type);
			} else {
				pos = builder.getProtossBuildTile(ba.probe, ba.type);
			}
			if(pos != null){
				ba.buildTile = pos;
			} else {
				//buildPylon();
				return false;
			}
		}
		return ba.build();
	}
	
	private void buildPylon(){
		BuildBuilding b = new BuildBuilding(game, UnitType.Protoss_Pylon);
		buildQueue.addFirst(b);
	}
	
	/**
	 * Add one instance of UnitType type to build queue
	 * @param type
	 */
	public void addBuild(UnitType type){
		BuildAction b;
		if(type.isBuilding()){
			b = new BuildBuilding(game, type);
		} else {
			b = new BuildUnit(game, type);
		}
		buildQueue.addLast(b);
	}
	
	/**
	 * Add one instance of UpgradeType type to build queue
	 * @param type
	 */
	public void addBuild(UpgradeType type){
		buildQueue.addLast(new BuildUpgrade(game, type));
	}
	
	/**
	 * Queue building an expansion on TilePosition pos
	 * @param type
	 */
	public void addExpansion(TilePosition pos){
		BuildBuilding b = new BuildBuilding(game, UnitType.Protoss_Nexus);
		b.buildTile = pos;
	}
	
	private Unit getProbe(){
		for(Unit u : game.self().getUnits()){
			if(u.getOrder() == Order.MoveToMinerals){
				return u;
			}
		}
		return null;
	}
	
	@Override
	public void onUnitComplete(Unit unit) {
		
		if(unit.getPlayer() == game.self()){
			
			if(unit.getType() == UnitType.Protoss_Nexus){
				// If a Nexus has been build increase projected supply accordingly
				projectedSupply += 18;
			} else if(unit.getType() == UnitType.Protoss_Assimilator){
				// If Assimilator assign workers to it.
				harvestGas(unit);
			}
		}
	}

	@Override
	public void onUnitCreate(Unit unit) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onUnitDestroy(Unit unit) {
		// If a Nexus or Pylon has been destroyed decreased projected supply accordingly
				if(unit.getType() == UnitType.Protoss_Pylon){
					projectedSupply -= 16;
				} else if(unit.getType() == UnitType.Protoss_Nexus) {
					projectedSupply -= 18;
				}
	}

	
	@Override
	public void onUnitDiscover(Unit unit) {
		if(unit.getPlayer() == game.self() && unit.getType().isBuilding()){
			for(BuildBuilding b : activeBuilds){
				if(b.isBuiltOnTile(unit.getTilePosition())){
					activeBuilds.remove(b);
				}
			}
			for(BuildAction b : buildQueue){
				if(b.isBuilding){
					if(((BuildBuilding)b).isBuiltOnTile(unit.getTilePosition())){
						buildQueue.remove(b);
					}
				}
			}
		}
	}
	
	private void manageSupply(){		
		int k = projectedSupply/5; // variable allowing building pylons just before hitting the supply limit
		//System.out.println(self.supplyUsed() + "/" + self.supplyTotal() + " - " + projectedSupply);
		//if we're running out of supply and have enough minerals ...
	    if (game.self().supplyUsed() > projectedSupply - k) {
	    	projectedSupply += 16;
	    	buildPylon();
	    }
	}

}
