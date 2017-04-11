package bots;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import bwapi.*;
import bwta.BWTA;
import bwta.BaseLocation;

import managers.*;

public class TestBot1 extends DefaultBWListener {

    private Mirror mirror = new Mirror();

    private Game game;
    private Player self;

    private int scoutID;
    private boolean scouted = false;
    private List<Manager> managers;
    
    public TestBot1(){
    	managers = new ArrayList<Manager>();
    }
    
    public void run() {
        mirror.getModule().setEventListener(this);
        mirror.startGame();
    }

    @Override
    public void onUnitCreate(Unit unit) {
        System.out.println("New unit discovered " + unit.getType());
        for(Manager man : managers){
        	man.onUnitCreate(unit);
        }
    }

    @Override
    public void onStart() {
        game = mirror.getGame();
        self = game.self();
        game.setLocalSpeed(20);
        managers.add(new SupplyManager(game));
        managers.add(new BaseManager(game));
        System.out.println(managers.size() + " managers");
        for(Manager man : managers){
        	man.onStart();
        }
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
    	//System.out.println("onFrame");
        //game.setTextSize(10);
    	//System.out.println("---- Run " + managers.size() + " Managers ----");
        for(Manager man : managers){
        	man.onFrame();
        }
        game.drawTextScreen(10, 10, "Playing as " + self.getName() + " - " + self.getRace());

        StringBuilder units = new StringBuilder("My units:\n");
        if(self.minerals() > 400){
        	expand();
        }
        //iterate through my units
        for (Unit myUnit : self.getUnits()) {
            units.append(myUnit.getType()).append(" ").append(myUnit.getTilePosition()).append("\n");
        }
        
        //draw my units on screen
        game.drawTextScreen(10, 25, units.toString());
    }

public void onUnitComplete(Unit unit){
	for(Manager man : managers){
    	man.onUnitComplete(unit);
    }
}

public void onUnitDestroy(Unit unit){
	for(Manager man : managers){
    	man.onUnitDestroy(unit);
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
/*
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
}*/
    

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