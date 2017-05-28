package bots;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import ISMCTS.Entity;
import ISMCTS.ISMCTS;
import ISMCTS.Node;
import ISMCTS.Persistence;
import ISMCTS.Tech;
import actions.Action;
import actions.BuildAction;
import actions.BuildBuilding;
import actions.BuildUpgrade;
import bwapi.*;
import bwta.BWTA;
import bwta.BaseLocation;
import managers.ArmyManager;
import managers.BuildManager;
import stateInformation.EnemyBuilding;
import stateInformation.EnemyUnit;
import stateInformation.Memory;

public class ISMCTSBot extends DefaultBWListener {
	private Mirror mirror = new Mirror();
    private Game game;
    private Player self;
    private BuildManager buildManager;
    private ArmyManager armyManager;
    private ISMCTS ismcts;
    private HashSet<EnemyBuilding> enemyBuildings;
    private List<EnemyUnit> enemyUnits;
    private List<BaseLocation> baseLocations;
    private int count;
    private Node tree;
    private int timeoutCount;
    
    public ISMCTSBot(){
    	tree = Persistence.loadTree();
    	
    }
    
    public static void main(String[] args){
    	new ISMCTSBot().run();
    }
    
    public void run() {
        mirror.getModule().setEventListener(this);
        mirror.startGame(); 
    }
    
    public void onStart(){
    	count = 0;
    	game = mirror.getGame();
    	timeoutCount = 0;
    	self = game.self();
    	BWTA.readMap();
    	BWTA.analyze();
    	game.setLocalSpeed(5);
    	buildManager = new BuildManager(game);
    	armyManager = new ArmyManager(game);
    	buildManager.onStart();
 		armyManager.onStart();
 		enemyBuildings = new HashSet<EnemyBuilding>();
    	enemyUnits = new ArrayList<EnemyUnit>();
 		setDynamicBaseLocations(game);
 		//System.out.println(getBaseLocationSize());
 		
 		ismcts = new ISMCTS(tree, game, baseLocations);
    }
    
    public void onFrame(){
    	buildManager.onFrame();
    	//System.out.println("*   Army  Manager   *");
    	armyManager.onFrame();
    	//updateEnemyUnits();
    	timeoutCount++;
    	// ISMCTS
    	
    	if(count < 1) {    		
    		count = 450;
    		System.out.println("*      *");
    		//Memory memory = buildMemory();
    		//System.out.println("*   .............   *");
        	int targetBase = -1;
        	//System.out.println("attack " + armyManager.isAttacking());
        	/*
        	if(armyManager.isAttacking()){
        		//System.out.println("attack " + armyManager.isAttacking());
        		targetBase = getBaseLocationIndex(armyManager.getTarget());
        	}*/
        	//System.out.println(targetBase);
        	//System.out.println("Army " + armyManager.enemyUnits);
        	//System.out.println("buildings " + armyManager.getEnemyBuildings());
    		performAction(ismcts.step(game, armyManager.enemyUnits, armyManager.getEnemyBuildings()));
    		if(shouldExpand()){
    			System.out.println("Expand");
    			expansion();
    		}
    		manageAttack();
		}
    	count--;

    	// equivalent to 30 minutes 
    	if(timeoutCount > 54000){
    		game.leaveGame();
    	}
    }
    
    public boolean performAction(Action action){
    	System.out.print("DO ");
    	action.print();
    	switch (action.move) {
    		case Build:
    			if(((BuildAction)action).isBuilding){
    				buildManager.addBuild((BuildAction)action);
    			} else {
    				//System.out.println("build unit");
    				buildManager.addBuild((BuildAction)action);
    				buildManager.addBuild((BuildAction)action);
    				buildManager.addBuild((BuildAction)action);
    				buildManager.addBuild((BuildAction)action);
    				buildManager.addBuild((BuildAction)action);
    			}    				
    		case Upgrade: 
    			buildManager.addBuild((BuildUpgrade)action);
    			break;
    		case Attack: 
    			//armyManager.attack(getBaseLocation(action.baseLocation));
    			break;
    		case Withdraw: 
    			//armyManager.withDraw();
    			break;
    		case Expand:
    		case buildWorkers:
    		case stopBuildingWorkers:
    		case scout: {
    			//armyManager.scout(getBaseLocation(action.baseLocation).getPosition());
    		}    			
    		case none:
    			return true;
    		case unknown:
    			return true;
    	}
    	
    	return true;
    }
    
    public void onEnd(boolean win){
    	if(win){
    		System.out.println("+---------------------------------------------------+");
    		System.out.println("| *       *  ***   ****  ***** 	 ***   ****	  *	  * |");
    		System.out.println("|  *     *    *   *   	   *    *	*  *   *   * *  |");
    		System.out.println("|   *   *     *   *        *    *   *  ****     *   |");
    		System.out.println("|    * *      *   *        *    *   *  *  *     *   |");
    		System.out.println("|     *      ***   ****    *     ***   *   *    *   |");
    		System.out.println("+---------------------------------------------------+");
    	}
    	// Back propogate
    	ismcts.backPropogate(win);
    	// Save tree
    	Persistence.saveTree(ismcts.getRoot());
    	tree = Persistence.loadTree(); 	
    	
    }
    
    
    public void manageAttack(){
    	if(self.supplyUsed() > 260){
    		armyManager.attack(baseLocations.get(baseLocations.size()-1));
    	} else if(self.supplyUsed() < 160){
    		armyManager.withDraw();
    	}
    }
    
    public void expansion(){

    	BaseLocation result = null;
    	double dist = Double.MAX_VALUE;
    	
    	//System.out.println(baseLocations.get(0).getTilePosition());
    	
    	for(BaseLocation b : BWTA.getBaseLocations()){
    		boolean isNotOccupied = game.canBuildHere(b.getTilePosition(), UnitType.Protoss_Nexus);
    		double newDist = BWTA.getGroundDistance(baseLocations.get(0).getTilePosition(), b.getTilePosition());
    		if(isNotOccupied && (result == null || dist > newDist)){
    			result = b;
    			dist = newDist;
    		}
    	}
    	if(result != null) {
    		//System.out.println(result.getTilePosition());
    		buildManager.addExpansion(result.getTilePosition());
    	}
    }
    
    public boolean shouldExpand(){
    	int bases = 0;
    	int workers = 0;
    	int resources = 0;
    	for(Unit myUnit : self.getUnits()){
    		if(myUnit.getType().isWorker()){
    			workers++;
    		} else if(myUnit.getType() == UnitType.Protoss_Nexus){
    			bases++;
    			for(Unit res : myUnit.getUnitsInRadius(200)){
    				if(res.getType().isMineralField()){
    					resources++;
    				}
    			}
    		}
    	}
    	//System.out.println(workers + " > " + resources + " * 2");
    	return (workers > resources * 2.2);
    }
    
    public Memory buildMemory(){
    	Memory result = new Memory();
    /*	//game.printf(enemyBuildings.size() + " Enemy buildings");
    	//game.printf(enemyUnits.size() + " Enemy units");
    	for(EnemyBuilding e : enemyBuildings){
    	//	result.enemyBuildings.add(ISMCTS.typeToEntity(e.type));
    	//	if(e.type == UnitType.Protoss_Nexus){
    			result.enemyBases.add(getBaseLocationIndex(BWTA.getNearestBaseLocation(e.position.toPosition())));
    		}
    	}
    	
    	for(EnemyUnit e : enemyUnits){
    		result.enemyArmy.add(ISMCTS.typeToEntity(e.type));
    	}*/
    	return result;
    }
    
    public void updateEnemyUnits(){
    	// Current information on enemy units
    	List<EnemyUnit> current = new ArrayList<EnemyUnit>();
    	
    	for(Unit u : game.enemy().getUnits()){
    		if(!u.getType().isBuilding() && !u.getType().isWorker() && u.getType() != UnitType.Unknown){
    			boolean found = false;
    			for(EnemyUnit eu : current){
    				if(eu.type == ISMCTS.typeToEntity(u.getType())){
    					found = true;
    					eu.number++;
    					break;
    				}
    			}
    			if(!found){
    				current.add(new EnemyUnit(ISMCTS.typeToEntity(u.getType())));
    			}
    		}
    	}
    	//game.printf("Current enemy units " + current.size());
    	// Compare to previous knowledge
    	for(EnemyUnit c : current){
    		boolean found = false;
    		for(EnemyUnit o : enemyUnits){
    			//game.printf(o.type.toString());
    			if(c.type == o.type){  
    				found = true;
    				if(c.number > o.number){
    					o.number = c.number;
    				}
    				break;
    			}
    		}
    		if(!found){
    			enemyUnits.add(c);
    		}
    	}
    	for(EnemyUnit eu : enemyUnits){
    		if(eu.number < 1){
    			enemyUnits.remove(eu);
    		}
    	}
    	
    }
    
    private void deadEnemyUnit(UnitType type){
    	boolean found = false;
		for(EnemyUnit eu : enemyUnits){
			if(eu.type == ISMCTS.typeToEntity(type)){
				found = true;
				eu.number--;
				break;
			}
		}
    }
    
    public int getBaseLocationIndex(BaseLocation base){
    	if(baseLocations == null){
    		//System.out.println("NULL BASE");
    		return -1;
    	}
    	for(BaseLocation b : baseLocations){
    		if(b.getX() == base.getX() && b.getY() == base.getY()){
    			return baseLocations.indexOf(b);
    		}
    	}
    	return -1;
    }
    
    public int getBaseLocationSize(){
    	return baseLocations.size();
    }
    
    public  BaseLocation getBaseLocation(int index){
    	return baseLocations.get(index);
    }
    
    public void setDynamicBaseLocations(Game game){
		BaseLocation start = null;
		// Determine starting BaseLocation
		for(BaseLocation b : BWTA.getStartLocations()){
    		if(!game.isVisible(b.getTilePosition())){
    			start = b;
    		} else {
    			EnemyBuilding eb = new EnemyBuilding(Entity.Nexus);
    			eb.position = b.getTilePosition();
    			enemyBuildings.add(eb);
    		}
    	}
		// Sort BaseLocation based on their distance from the starting BaseLocation
		if(start != null){
			Comparator<BaseLocation> comp = new Comparator<BaseLocation>() {
				//Player self = game.self();
				public int compare(BaseLocation first, BaseLocation second){
					return (int)(first.getTilePosition().getDistance(game.self().getStartLocation()) - second.getTilePosition().getDistance(game.self().getStartLocation())+0.5);
				}
			};
			baseLocations = BWTA.getBaseLocations();
			baseLocations.sort(comp);
		}
		//System.out.println(baseLocations.size());
		//System.out.println("Dynamic base locations set");
	}
    
	public void onUnitComplete(Unit unit){
		buildManager.onUnitComplete(unit);
    	armyManager.onUnitComplete(unit);
	}
		
	public void onUnitCreate(Unit unit){
		buildManager.onUnitCreate(unit);
    	armyManager.onUnitCreate(unit);
	}
	
	public void onUnitDestroy(Unit unit){
		buildManager.onUnitDestroy(unit);
		armyManager.onUnitDestroy(unit);
		// Enemy unit decrement known enemy units;
		if(unit.getPlayer() == game.enemy() 
			&& !unit.getType().isBuilding()
			&& !unit.getType().isWorker()){
			deadEnemyUnit(unit.getType());
		}
	}
	
	public void onUnitDiscover(Unit unit){
		buildManager.onUnitDiscover(unit);
    	armyManager.onUnitDiscover(unit);
	}
    
}
