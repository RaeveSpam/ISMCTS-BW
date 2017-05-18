package managers;

import java.util.ArrayList;
import java.util.List;

import bwapi.*;
import bwta.BWTA;
import bwta.BaseLocation;

/**
 * @author Raev
 *	Manages worker production and worker allocation.
 */
public class BaseManager implements Manager {

	private Game game;
	private Player self;
	private List<Unit> bases;
	private ResourceManager bank;
	private int count = 0;
	
	public BaseManager(Game game, ResourceManager resMan){
		this.game = game;
		self = game.self();
		bases = new ArrayList<Unit>();
		bank = resMan;
	}
	
	@Override
	public void onStart() {
		List<Unit> units = self.getUnits();
		for(Unit u : units){
			if(u.getType() == UnitType.Protoss_Nexus){
				bases.add(u);
			}
		}
	}

	@Override
	public void onFrame() {
		if(count > 59){
			// Find and assign idle workers
			List<Unit> units = self.getUnits();
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
			count = 0;
		}
		count++;
		// Manage bases
		/*for(Unit base : bases){
			manageBase(base);
		}*/
	}

	@Override
	public void onUnitComplete(Unit unit) {
		if(unit.getPlayer() != game.self()){
			return;
		}
		if(unit.getType() == UnitType.Protoss_Assimilator){
			//System.out.println("HARVEST");
			harvestGas(unit);
		}
		if(unit.getType() == UnitType.Protoss_Nexus){
			bases.add(unit);
		}

	}

	@Override
	public void onUnitCreate(Unit unit) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onUnitDestroy(Unit unit) {
		// TODO Auto-generated method stub

	}
	
	private void harvestGas(Unit refinery){
		int i = 0;
		for(Unit u : self.getUnits()){
			if(i < 3 && u.getType() == UnitType.Protoss_Probe && (u.getOrder() == Order.MoveToMinerals || u.getOrder() == Order.ReturnMinerals)){
				i++;
				u.gather(refinery);
			}
		}
		/*
		Unit[] closestWorkers = new Unit[3];
		closestWorkers[0] = null;
		closestWorkers[1] = null;
		closestWorkers[2] = null;
		for(Unit u : self.getUnits()){
			if(u.getType() == UnitType.Protoss_Probe && u.getOrder() == Order.MoveToMinerals || u.getOrder() == Order.ReturnMinerals){
				int dist = u.getDistance(refinery); 
				if(closestWorkers[0] == null || dist < closestWorkers[0].getDistance(refinery)) {
					closestWorkers[2] = closestWorkers[1];
					closestWorkers[1] = closestWorkers[0];
					closestWorkers[0] = u;
				} else if(dist < closestWorkers[1].getDistance(refinery)){
					closestWorkers[2] = closestWorkers[1];
					closestWorkers[1] = u;
				} else if(closestWorkers[2] == null || dist < closestWorkers[2].getDistance(refinery)){
					closestWorkers[2] = u;
				}
			}
		}
		for(int i = 0; i < closestWorkers.length; i++){
			if(closestWorkers[i] != null){
				//System.out.println("GO GAS");
				closestWorkers[i].gather(refinery);
			}
		}*/
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
			//System.out.print(u.getType() + ", ");
			if(u.getType() == UnitType.Protoss_Probe){
				workers++;
			} else if(u.getType().isMineralField() ||
					u.getType() == UnitType.Protoss_Assimilator){
				resources++;
				/*if(u.getType() == UnitType.Protoss_Assimilator){
					if(!u.isBeingGathered()){
						harvestGas(u);
					}
				}*/
			} else if(u.getType() == UnitType.Resource_Vespene_Geyser){
				//System.out.print("Geyser found ");
				if(game.self().supplyUsed() > 30 && bank.build(UnitType.Protoss_Assimilator)){
					//System.out.print(" - Supply and bank ");
					Builder builder = new Builder(game);
					for(Unit b : nexus.getUnitsInRadius(160)){
						if(b.getOrder() == Order.MoveToMinerals){
							//System.out.print(" - Found worker ");
							//TilePosition buildTile = builder.getBuildTile(b, UnitType.Protoss_Assimilator, u.getTilePosition());
							//if (buildTile != null) {
								//System.out.print(" - BuildTile found ");
			    					b.build(UnitType.Protoss_Assimilator, u.getTilePosition());
			    					
							//}
		    				break;
		    			}
					}
				}
			}
		}
		//System.out.println("");
	
		// Train more workers if base is not full
		//System.out.println(workers + " < " + resources*3);
		if(workers < resources*3){
			buildWorkers(nexus);
		}
		
	}
	
	private void buildWorkers(Unit nexus){
		if(nexus.getTrainingQueue().size() < 1 && bank.build(UnitType.Protoss_Probe)){
			nexus.train(UnitType.Protoss_Probe);
		}		
	}
	
	public void assignIdleWorker(Unit worker){
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

	@Override
	public void onUnitDiscover(Unit unit) {
	
	}
	
	
}
