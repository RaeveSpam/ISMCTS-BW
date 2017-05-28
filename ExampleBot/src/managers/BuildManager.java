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

import ISMCTS.Entity;
import ISMCTS.ISMCTS;
import actions.*;

public class BuildManager implements Manager {
	
	LinkedList<BuildAction> buildQueue;
	ArrayList<BuildBuilding> activeBuilds;
	ArrayList<BuildAction> unitsAndUpgrades;
	Game game;
	Builder builder;
	Builder pylonBuilder;
	int numWorkers;
	int timeOut = 100;
	
	private int projectedSupply;
	private int count;
	
	public BuildManager(Game game){
		 buildQueue = new LinkedList<BuildAction>();
		 this.game = game;
		 builder = new Builder(game, 20);
		 pylonBuilder = new Builder(game, 40, 1);
		 activeBuilds = new ArrayList<BuildBuilding>();
		 unitsAndUpgrades = new ArrayList<BuildAction>();
		 projectedSupply = 0;
		 numWorkers = 0;
		 count = 0;
		 
	}
	
	private int countWorkers(){
		List<Unit> units = game.self().getUnits();
		numWorkers = 0;
		for(Unit myUnit : units){
			if(myUnit.getType() == UnitType.Protoss_Probe){
				numWorkers++;
			}
		}
		return numWorkers;
	}
	
	private void manageWorkersAndBases(){
		// Find and assign idle workers
		
		countWorkers();
		
		List<Unit> units = game.self().getUnits();
		//System.out.println(units.size() + " units found");
		for(Unit myUnit : units){
			if(myUnit.getType() == UnitType.Protoss_Nexus){
			//	System.out.println("Manage Base");
				manageBase(myUnit);
			} else
			if(myUnit.getType() == UnitType.Protoss_Probe && (
				myUnit.getOrder() == Order.None || 
				myUnit.getOrder() == Order.Nothing ||
				myUnit.getOrder() == Order.PlayerGuard)){
				assignIdleWorker(myUnit);
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
					BuildBuilding bb = new BuildBuilding(game, Entity.Assimilator);
					bb.buildTile = u.getTilePosition();
					if(canAfford(bb)){
						buildQueue.addFirst(bb);
					}
				}
			}
		}

		if(numWorkers < 70 && workers < resources*3 && nexus.getTrainingQueue().size() < 1 && game.self().supplyUsed() < game.self().supplyTotal() && canAfford(UnitType.Protoss_Probe)){
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
		//System.out.print("[");
		for(BuildBuilding a : activeBuilds){
			a.count++;
			a.game = game;
			//System.out.print(a.type + ", ");
			if(a.hasBeenBuilt() || !a.canBeBuilt()){
				activeBuilds.remove(a);
			} else if(a.count > timeOut){
				a.reset();
				a.count = 0;
				build(a);
				//break;
			}
		}
		//System.out.println("]");
	//	System.out.println("***********");
		for(BuildAction a : unitsAndUpgrades){
			if(a.canBeBuilt()){
				if(a.build()){
					unitsAndUpgrades.remove(a);
					//break;
				}  				
			} else {
				unitsAndUpgrades.remove(a);
				//break;
			}
			
		}
		boolean cont = true;
		while(cont && !buildQueue.isEmpty()){
			BuildAction ba = buildQueue.peek();
			ba.game = game;	
			if(!ba.canBeBuilt()){ // doesn't have requirements, remove.
				System.out.println("Can't build pop " + ba.type);
				buildQueue.pop();
				/*if(buildQueue.size() > 2){
					break;
				}*/
			} else {
				//System.out.println("Can afford " + ba.type + " " + canAfford(ba));
				if(canAfford(ba)){
					if(ba.isBuilding){
						boolean startBuilding = build((BuildBuilding)ba);
						//System.out.println(startBuilding);
						if(startBuilding){;
							activeBuilds.add((BuildBuilding)buildQueue.pop());
						} else {
							cont = false;
						}
					} else {
						if(ba.build()){
							buildQueue.pop();
						} else {
							unitsAndUpgrades.add(buildQueue.pop());
							//buildQueue.addLast(buildQueue.pop());
							//cont = false;
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
		for(BuildAction a : unitsAndUpgrades){
			minerals -= a.getMinerals();
			gas -= a.getGas();
		}
		for(BuildAction a : activeBuilds){
			minerals -= a.getMinerals();
			gas -= a.getGas();
		}
		
		return type.mineralPrice() <= minerals && type.gasPrice() <= gas;
	}
	
	private boolean canAfford(BuildAction buildAction){
		// Calculate available resources
		//System.out.println("canAfford " + buildAction.type);
		buildAction.game = game;
		int minerals = game.self().minerals();
		int gas = game.self().gas();
		for(BuildAction a : activeBuilds){
			minerals -= a.getMinerals();
			gas -= a.getGas();
		}
		
		return buildAction.getMinerals() <= minerals && buildAction.getGas() <= gas;
	}
	
	private boolean build(BuildBuilding ba){
		ba.game = game;
		if(!ba.hasBuilder()){
			Unit probe = getProbe();
			if(probe != null){
				ba.assignBuilder(probe);
			} else {
				System.out.println("No Probe");
				return false;
			}
		}
		//System.out.println(ba.type + "tile == " + ba.isBuildTileValid());
		TilePosition pos;
		if(ba.type == Entity.Nexus){
			if(game.isVisible(ba.buildTile) && ba.isBuildTileValid()){
				pos = game.getBuildLocation(ISMCTS.entityToType(ba.type), ba.probe.getTilePosition(), 20);
			} else if(!game.isVisible(ba.buildTile)){
				ba.probe.move(ba.buildTile.toPosition());
				return true;
			} else {
				return true;
			}
		} else if(!ba.isBuildTileValid()){
			if(ISMCTS.entityToType(ba.type) == UnitType.Protoss_Pylon){
				//pos = pylonBuilder.getProtossBuildTile(ba.probe, ISMCTS.entityToType(ba.type));
				pos = game.getBuildLocation(ISMCTS.entityToType(ba.type), ba.probe.getTilePosition(), 20);
			} else {
				//pos = builder.getProtossBuildTile(ba.probe, ISMCTS.entityToType(ba.type));
				pos = game.getBuildLocation(ISMCTS.entityToType(ba.type), ba.probe.getTilePosition(), 20);
			}
			if(pos != null){
				ba.buildTile = pos;
			} else {
				//buildPylon();
				System.out.println("No buildTile");
				return false;
			}
		}
		return ba.build();
	}
	
	private void buildPylon(){
		BuildBuilding b = new BuildBuilding(game, Entity.Pylon);
		buildQueue.addFirst(b);
	}
	
	/**
	 * Add one instance of UnitType type to build queue
	 * @param type
	 */
	public void addBuild(UnitType type){
		BuildAction b;
		if(type.isBuilding()){
			b = new BuildBuilding(game, ISMCTS.typeToEntity(type));
		} else {
			b = new BuildUnit(game, ISMCTS.typeToEntity(type));
		}
		buildQueue.addLast(b);
	}
	
	public void addBuild(BuildAction action){
		buildQueue.addLast(action);
	}

	
	/**
	 * Add one instance of UpgradeType type to build queue
	 * @param type
	 */
	public void addBuild(UpgradeType type){
		buildQueue.addLast(new BuildUpgrade(game, ISMCTS.upgradeToTech(type)));
	}
	
	/**
	 * Queue building an expansion on TilePosition pos
	 * @param type
	 */
	public void addExpansion(TilePosition pos){
		BuildBuilding b = new BuildBuilding(game, Entity.Nexus);
		b.buildTile = pos;
		buildQueue.add(b);
		//System.out.println("Expanding (" + b.buildTile.getX() + ", " + b.buildTile.getY() + ")");
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
