import java.util.Arrays;
import java.util.Comparator;

import bwapi.*;
import bwta.BWTA;
import bwta.BaseLocation;

public class TestBot1 extends DefaultBWListener {

    private Mirror mirror = new Mirror();

    private Game game;
    private Player self;

    private int scoutID;
    private int projectedSupply = 0;
    private boolean scouted = false;
    
    public void run() {
        mirror.getModule().setEventListener(this);
        mirror.startGame();
    }

    @Override
    public void onUnitCreate(Unit unit) {
        System.out.println("New unit discovered " + unit.getType());
    }

    @Override
    public void onStart() {
        game = mirror.getGame();
        self = game.self();
        game.setLocalSpeed(20);
        //Use BWTA to analyze map
        //This may take a few minutes if the map is processed first time!
        System.out.println("Analyzing map...");
        BWTA.readMap();
        BWTA.analyze();
        System.out.println("Map data ready");
        
        int i = 0;
        for(BaseLocation baseLocation : BWTA.getBaseLocations()){
        	System.out.println("Base location #" + (++i) + ". Printing location's region polygon:");
        	for(Position position : baseLocation.getRegion().getPolygon().getPoints()){
        		System.out.print(position + ", ");
        	}
        	System.out.println();
        }

    }

    @Override
    public void onFrame() {
    	//System.out.println("proj " + projectedSupply);
        //game.setTextSize(10);
        game.drawTextScreen(10, 10, "Playing as " + self.getName() + " - " + self.getRace());

        StringBuilder units = new StringBuilder("My units:\n");
        
        manageSupply();
        if(self.minerals() > 400){
        	expand();
        }
        //iterate through my units
        for (Unit myUnit : self.getUnits()) {
            units.append(myUnit.getType()).append(" ").append(myUnit.getTilePosition()).append("\n");

            
            //if there's enough minerals, train an SCV
            if (myUnit.getType() == UnitType.Protoss_Nexus && self.minerals() >= 50) {
                buildWorkers(myUnit);
            }

            //if it's a worker and it's idle, send it to the closest mineral patch
            if (myUnit.getType().isWorker() && myUnit.isIdle()) {
            	if(!scouted){
            		System.out.println("SCOUT");
                	scout(myUnit);
                	scouted = true;
                } else {
                	assignIdleWorker(myUnit);
                }
        	}
        }

        //draw my units on screen
        game.drawTextScreen(10, 25, units.toString());
    }

    // Send worker to closest vacant mineral patch or refinery
public void assignIdleWorker(Unit worker){
	Unit closestMineral = null;
	
    //find the closest mineral
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

public void harvestGas(Unit refinery){
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

public void buildWorkers(Unit nexus){
	if(nexus.getTrainingQueue().size() < 2 && self.supplyUsed() < self.supplyTotal()){
		nexus.train(UnitType.Protoss_Probe);
	}
		
	// Add workers to queues in bases (not really possible to iterate through bases)
	// No more than 2 workers in each queue
	// TODO: Max number of workers??
	//			iterate base location, mineral/gas count x3 = max workers?
	
	
}

public void manageSupply(){		

	System.out.println(self.supplyUsed() + "/" + self.supplyTotal() + " - " + projectedSupply);
	//if we're running out of supply and have enough minerals ...
    if (projectedSupply <= self.supplyTotal() && projectedSupply <= self.supplyUsed() && 
    		(self.supplyTotal() - self.supplyUsed() <= 4 + self.supplyTotal()/10) && (self.minerals() >= 100)) {
    	//iterate over units to find a worker
    	projectedSupply += 16;
    	for (Unit myUnit : self.getUnits()) {
    		if (myUnit.getType() == UnitType.Protoss_Probe && myUnit.getID() != scoutID) {
    			//get a nice place to build a supply depot
    			TilePosition buildTile =
    				getBuildTile(myUnit, UnitType.Protoss_Pylon, self.getStartLocation());
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

public void onUnitComplete(Unit unit){
	if(unit.getType() == UnitType.Protoss_Nexus){
		projectedSupply += 18;
	}
}

public void onUnitDestroy(Unit unit){
	if(unit.getType() == UnitType.Protoss_Pylon){
		projectedSupply -= 16;
		System.out.println("Pylon Destroyed");
	} else if(unit.getType() == UnitType.Protoss_Nexus){
		if(unit.canBuild()){
			projectedSupply -= 18;
		}
		System.out.println("Nexus Destroyed");
	}
}

public void expand(){
	// Find probe to build new expansion
	Unit probe = null;
	for(Unit myUnit : self.getUnits()){
		if(myUnit.getType() == UnitType.Protoss_Probe && myUnit.getID() != scoutID){
			probe = myUnit;
			break;
		}
	}
	// No probe, can't build, return
	if(probe == null){
		return;
	}
	// Find closest available base location
	TilePosition expansion = null;
	for(BaseLocation t : BWTA.getBaseLocations()){
		if(game.canBuildHere(t.getTilePosition(), UnitType.Protoss_Nexus, probe, false) && 
				(expansion == null || expansion.getDistance(self.getStartLocation()) > t.getTilePosition().getDistance(self.getStartLocation()))){
			expansion = t.getTilePosition();
		}
	}
	// No base location, return
	if(expansion == null) {
		return;
	}
	if(probe.build(UnitType.Protoss_Nexus, expansion)){
		//projectedSupply += 18;
	}
	//getBuildTile(probe, UnitType.Protoss_Nexus, expansion);
	
	// Find new base location
	// Build new base (in the right place)
}

public void scout(Unit scout){
	
	scoutID = scout.getID();
	BaseLocation[] bases = new BaseLocation[BWTA.getBaseLocations().size()];
	BWTA.getBaseLocations().toArray(bases);
	Arrays.sort(bases, BaseComparator);
	scout.move(bases[0].getTilePosition().toPosition());
	for(int i = 1; i < bases.length; i++){
		scout.move(bases[i].getTilePosition().toPosition(), true);
	}
	System.out.println("Scout " + bases.length + " bases");
}
    
public Comparator<BaseLocation> BaseComparator = new Comparator<BaseLocation>() {
	public int compare(BaseLocation first, BaseLocation second){
		return (int)(first.getTilePosition().getDistance(self.getStartLocation()) - second.getTilePosition().getDistance(self.getStartLocation())+0.5);
	}
};
 // Returns a suitable TilePosition to build a given building type near
 // specified TilePosition aroundTile, or null if not found. (builder parameter is our worker)
 public TilePosition getBuildTile(Unit builder, UnitType buildingType, TilePosition aroundTile) {
 	TilePosition ret = null;
 	int maxDist = 3;
 	int stopDist = 40;

 	// Refinery, Assimilator, Extractor
 	if (buildingType.isRefinery()) {
 		for (Unit n : game.neutral().getUnits()) {
 			if ((n.getType() == UnitType.Resource_Vespene_Geyser) &&
 					( Math.abs(n.getTilePosition().getX() - aroundTile.getX()) < stopDist ) &&
 					( Math.abs(n.getTilePosition().getY() - aroundTile.getY()) < stopDist )
 					) return n.getTilePosition();
 		}
 	}

 	while ((maxDist < stopDist) && (ret == null)) {
 		for (int i=aroundTile.getX()-maxDist; i<=aroundTile.getX()+maxDist; i++) {
 			for (int j=aroundTile.getY()-maxDist; j<=aroundTile.getY()+maxDist; j++) {
 				if (game.canBuildHere(new TilePosition(i,j), buildingType, builder, false)) {
 					// units that are blocking the tile
 					boolean unitsInWay = false;
 					for (Unit u : game.getAllUnits()) {
 						if (u.getID() == builder.getID()) continue;
 						if ((Math.abs(u.getTilePosition().getX()-i) < 4) && (Math.abs(u.getTilePosition().getY()-j) < 4)) unitsInWay = true;
 					}
 					if (!unitsInWay) {
 						return new TilePosition(i, j);
 					}
 					if(buildingType.requiresPsi()){
 						
 					}
 					// creep for Zerg
 					if (buildingType.requiresCreep()) {
 						boolean creepMissing = false;
 						for (int k=i; k<=i+buildingType.tileWidth(); k++) {
 							for (int l=j; l<=j+buildingType.tileHeight(); l++) {
 								if (!game.hasCreep(k, l)) creepMissing = true;
 								break;
 							}
 						}
 						if (creepMissing) continue;
 					}
 				}
 			}
 		}
 		maxDist += 2;
 	}

 	if (ret == null) game.printf("Unable to find suitable build position for "+buildingType.toString());
 	return ret;
 }
    
    public static void main(String[] args) {
        new TestBot1().run();
    }
}