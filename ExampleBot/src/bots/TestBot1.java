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
    private boolean scouted;
    private List<Manager> managers;
    private int bases;
    private int gateways;
    private List<Unit> gw;
    private ResourceManager bank;
    private ArmyManager army;
    private int count;
    private boolean core;
    private boolean robot;
    private boolean obs;
    private BuildManager buildManager;
    private Position start;
    public TestBot1(){
    	
    }
    
    public void run() {
        mirror.getModule().setEventListener(this);
        mirror.startGame();
    }

    @Override
    public void onUnitCreate(Unit unit) {
        //System.out.println("New unit discovered " + unit.getType());
        for(Manager man : managers){
        	man.onUnitCreate(unit);
        }
    }

    @Override
    public void onStart() {
    	managers = new ArrayList<Manager>();
    	count = 0;
    	core = false;
    	robot = false;
    	obs = false;
    	scouted = false;
    	gw = new ArrayList<Unit>();
    	bases = 1;
    	gateways = 0;
        game = mirror.getGame();
        self = game.self();
        game.setLocalSpeed(15);
        bank = new ResourceManager(game);
        buildManager = new BuildManager(game);
        //managers.add(new SupplyManager(game, bank));
        //managers.add(new BaseManager(game, bank));
        army = new ArmyManager(game);
        managers.add(army);
        managers.add(buildManager);
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
        for(Unit u : self.getUnits()){
        	if(u.getType() == UnitType.Protoss_Nexus){
        		start = u.getPosition();
        	}
        }
        game.printf(Integer.toString(BWTA.getStartLocations().size()));
        game.printf(start.toString());
        //System.out.println(start);
        int i = 0;
        for(BaseLocation baseLocation : BWTA.getBaseLocations()){
        	//System.out.println("Base location #" + (++i) + ". Printing location's region polygon:");
        	for(Position position : baseLocation.getRegion().getPolygon().getPoints()){
        		//System.out.print(position + ", ");
        	}
        	//System.out.println();
        }
        TilePosition a = new TilePosition(10,10);
        TilePosition b = new TilePosition(10,10);
        //System.out.println("a ==  b = " + (a == b));
        
        
    }

    @Override
    public void onFrame() {
    	//System.out.println("onFrame");
        //game.setTextSize(10);
    	//System.out.println("---- Run " + managers.size() + " Managers ----");
    	if(count > 20000){
    		scouted = false;
    		count = 0;
    	}
    	count++;
    	bank.onFrame();
    	for(Manager man : managers){
        	man.onFrame();
        }
    	//System.out.println("Managers Done");

    	//System.out.println(UpgradeType.Singularity_Charge.whatsRequired(0));
    	if(!scouted){
    		
    		for(Unit u : self.getUnits()){
            	if(u.getType() == UnitType.Protoss_Probe){
            		
            		scoutID = u.getID();
            		scout(u);
            		scouted = true;
            		System.out.println("SCOUT");
            		break;
            	}
            }
    	}
    	//System.out.println(game.getUnit(scoutID));
        //game.drawTextScreen(10, 10, "Playing as " + self.getName() + " - " + self.getRace());

        //StringBuilder units = new StringBuilder("My units:\n");
    	//System.out.println(self.minerals() + " > 100");
        
        	//System.out.println(gw.size());
    	boolean gway = false;
    	for(Unit g : self.getUnits()){
    		if(g.getType() == UnitType.Protoss_Gateway){
    			gway = true;
	    		if(g.canTrain() && g.getTrainingQueue().size() < 1){
    				if(buildManager.canAfford(UnitType.Protoss_Dragoon)){
    					buildManager.addBuild(UnitType.Protoss_Dragoon);
    				} else if (buildManager.canAfford(UnitType.Protoss_Zealot)){
    					buildManager.addBuild(UnitType.Protoss_Zealot);
    				}
    			}
    		}
		}
    	
        if(self.supplyUsed() > gateways * 15  + 20){
        	gateways++;
        	buildManager.addBuild(UnitType.Protoss_Gateway);
        } 
        
        
        if(obs && self.minerals() > 25 && self.gas() > 75){
        	for(Unit myUnit : self.getUnits()){
        		if(myUnit.getType() == UnitType.Protoss_Robotics_Facility && myUnit.getTrainingQueue().size() < 1){
        			myUnit.train(UnitType.Protoss_Observer);
        			break;
        		}
        	}
        }
        if(!core && self.isUnitAvailable(UnitType.Protoss_Cybernetics_Core) && self.supplyUsed() > 30){
        	core = true;
        	buildManager.addBuild(UnitType.Protoss_Cybernetics_Core);
        }
        
        if(!robot && self.isUnitAvailable(UnitType.Protoss_Robotics_Facility) && self.supplyUsed() > 50){
        	robot = true;
        	buildManager.addBuild(UpgradeType.Singularity_Charge);
        	buildManager.addBuild(UnitType.Protoss_Robotics_Facility);
        }
        
        if(self.isUnitAvailable(UnitType.Protoss_Observatory) && !obs && self.supplyUsed() > 60){
        	obs = true;
        	buildManager.addBuild(UnitType.Protoss_Observatory);
        }
        //System.out.println(self.minerals() + " > 400");
        if(self.minerals() > 400 && count % 907 == 0){
        	expand();
        }
        if(self.supplyUsed() > 200 && !army.isAttacking()){
        	System.out.println("ATTACK");
        	attack();
        }
        //iterate through my units
        /*for (Unit myUnit : self.getUnits()) {
            units.append(myUnit.getType()).append(" ").append(myUnit.getTilePosition()).append("\n");
        }*/

        //draw my units on screen
        //game.drawTextScreen(10, 25, units.toString());
    }

    public void attack(){
    	for(BaseLocation b : BWTA.getStartLocations()){
    		if(!game.isVisible(b.getTilePosition())){
    			army.attack(b);
    		}
    	}
    }
    
public void onUnitComplete(Unit unit){
	for(Manager man : managers){
    	man.onUnitComplete(unit);
    }
	if(unit.getPlayer() == self && unit.getType() == UnitType.Protoss_Gateway){
		gw.add(unit);
	}
}

public void onUnitDestroy(Unit unit){
	for(Manager man : managers){
    	man.onUnitDestroy(unit);
    }
	if(unit.getPlayer() == game.self() && unit.getType() == UnitType.Protoss_Cybernetics_Core){
		core = false;
	} else
	if(unit.getPlayer() == game.self() && unit.getType() == UnitType.Protoss_Robotics_Facility){
		robot = true;
	} else
	if(unit.getPlayer() == game.self() && unit.getType() == UnitType.Protoss_Observatory){
		obs = true;
	}
}

public void onUnitDiscover(Unit unit){
	bank.onUnitDiscover(unit);
	if(unit.getPlayer() == game.self() && unit.getType() == UnitType.Protoss_Gateway){
		//gateways++;
	} else
	if(unit.getPlayer() == game.self() && unit.getType() == UnitType.Protoss_Nexus){
		//gateways = 0;
		bases++;
	} else
	if(unit.getPlayer() == game.self() && unit.getType() == UnitType.Protoss_Cybernetics_Core){
		core = true;
	} else
	if(unit.getPlayer() == game.self() && unit.getType() == UnitType.Protoss_Robotics_Facility){
		robot = true;
	} else
	if(unit.getPlayer() == game.self() && unit.getType() == UnitType.Protoss_Observatory){
		obs = true;
	}
	for(Manager man : managers){
		man.onUnitDiscover(unit);
	}
}

public boolean buildGateway(Unit builder){
	
	Builder b = new Builder(game, 10);
	TilePosition buildTile = null;
	
	for(Unit p : self.getUnits()){
		if(p.getType() == UnitType.Protoss_Pylon){
			buildTile = b.getBuildTile(builder, UnitType.Protoss_Gateway, p.getTilePosition());
			if(buildTile != null){
				break;
			}
		}
	}
	if(buildTile != null) {
		builder.build(UnitType.Protoss_Gateway, buildTile);
		return true;
	}
	return false;
}
	
public boolean buildBuilding(Unit builder, UnitType type){
	
	Builder b = new Builder(game, 10);
	TilePosition buildTile = null;
	
	for(Unit p : self.getUnits()){
		if(p.getType() == UnitType.Protoss_Pylon){
			buildTile = b.getBuildTile(builder, type, p.getTilePosition());
			if(buildTile != null){
				break;
			}
		}
	}

if(buildTile != null) {
	builder.build(type, buildTile);
	return true;
}
return false;
}

public boolean buildCore(Unit builder){
		
		Builder b = new Builder(game, 10);
		TilePosition buildTile = null;
		
		for(Unit p : self.getUnits()){
			if(p.getType() == UnitType.Protoss_Pylon){
				buildTile = b.getBuildTile(builder, UnitType.Protoss_Cybernetics_Core, p.getTilePosition());
				if(buildTile != null){
					break;
				}
			}
		}
	
	if(buildTile != null) {
		builder.build(UnitType.Protoss_Cybernetics_Core, buildTile);
		return true;
	}
	return false;
}

public void expand(){
	System.out.println("EXPAND");
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
	//System.out.println("Scout " + bases.length + " bases");
}
    

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
    
    public Comparator<BaseLocation> BaseComparator = new Comparator<BaseLocation>() {
		//Player self = game.self();
		public int compare(BaseLocation first, BaseLocation second){
			return (int)(first.getTilePosition().getDistance(self.getStartLocation()) - second.getTilePosition().getDistance(self.getStartLocation())+0.5);
		}
	};
}