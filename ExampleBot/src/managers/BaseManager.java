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
	
	public BaseManager(Game game){
		this.game = game;
		self = game.self();
		bases = new ArrayList<Unit>();
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
		// Find and assign idle workers
		List<Unit> units = self.getUnits();
		//System.out.println(units.size() + " units found");
		for(Unit myUnit : units){
			if(myUnit.getType() == UnitType.Protoss_Probe && myUnit.isIdle()){
				assignIdleWorker(myUnit);
			}
		}
		// Manage bases
		for(Unit base : bases){
			manageBase(base);
		}
	}

	@Override
	public void onUnitComplete(Unit unit) {
		if(unit.getPlayer() != game.self()){
			return;
		}
		if(unit.getType() == UnitType.Protoss_Assimilator){
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
		Unit[] closestWorkers = new Unit[3];
		closestWorkers[0] = null;
		closestWorkers[1] = null;
		closestWorkers[2] = null;
		for(Unit u : self.getUnits()){
			if(u.getType() == UnitType.Protoss_Probe){
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
	}
	
	private void manageBase(Unit nexus){
		if(nexus.getType() != UnitType.Protoss_Nexus){
			return;
		}
		// Count workers & available resources
		List<Unit> units = nexus.getUnitsInRadius(160);
		int workers = 0;
		int resources = 0;
		for(Unit u : units){
			if(u.getType() == UnitType.Protoss_Probe){
				workers++;
			} else if(u.getType().isMineralField() ||
					u.getType() == UnitType.Protoss_Assimilator){
				resources++;
			}
		}
		// Train more workers if base is not full
		//System.out.println(workers + " < " + resources*3);
		if(workers < resources*3){
			buildWorkers(nexus);
		}
	}
	
	private void buildWorkers(Unit nexus){
		if(nexus.getTrainingQueue().size() < 2 && self.supplyUsed() < self.supplyTotal()){
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
