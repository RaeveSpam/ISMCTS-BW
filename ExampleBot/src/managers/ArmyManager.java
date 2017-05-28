package managers;

import bwapi.Unit;
import ISMCTS.Entity;
import ISMCTS.ISMCTS;
import bwapi.UnitType;
import bwapi.Unitset;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.sun.xml.internal.bind.v2.model.runtime.RuntimeArrayInfo;

import bots.ISMCTSBot;
import bwapi.Game;
import bwapi.Position;
import bwapi.TilePosition;
import bwta.Region;
import stateInformation.EnemyBuilding;
import stateInformation.EnemyUnit;
import unitControl.*;
import bwta.BWTA;
import bwta.BaseLocation;
import bwta.Chokepoint;

public class ArmyManager implements Manager {

	private Game game;
	
	private List<Group> groups;
	private List<UnitGroup> unitGroups;
	private List<UnitGroup> idleUnits;
	private List<UnitGroup> activeUnits;
	private ScoutGroup scout;
	private boolean isAttacking;
	private Position stagingArea;
	private Region targetRegion;
	private List<Region> ownedRegions;
	private List<Region> enemyRegions;
	private DefenceHelper defender;
	private EnemyAttack enemyAttack;
	private int count;
	private Position oldStagingArea;
	private int scoutID;
	
	private HashSet<EnemyBuilding> enemyBuildings;
    public ArrayList<EnemyUnit> enemyUnits;
    private EnemyBuilding target;
    private BaseLocation enemyMain;
	
	public ArmyManager(Game game){
		this.game = game;
		isAttacking = false;
		groups = new ArrayList<Group>();
		unitGroups = new ArrayList<UnitGroup>();
		scout = new ScoutGroup(game);
		enemyRegions = new ArrayList<Region>();
		ownedRegions = new ArrayList<Region>();
		stagingArea = null;
		
		enemyBuildings = new HashSet<EnemyBuilding>();
		enemyUnits = new ArrayList<EnemyUnit>();
		idleUnits = new ArrayList<UnitGroup>();
		activeUnits = new ArrayList<UnitGroup>();
		enemyAttack = null;
		count = 0;
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
    		} else if(u.getType().isBuilding()){
    			EnemyBuilding newBuilding = new EnemyBuilding(u);
    			boolean exists = false;
    			for(EnemyBuilding existing : enemyBuildings){
    				if(existing.equals(newBuilding)){
    					exists = true;
    					break;
    				}
    			}
    			if(!exists){
    				enemyBuildings.add(newBuilding);
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
    	for(EnemyBuilding b : enemyBuildings){
    		if(!b.stillExists(game)){
    			enemyBuildings.remove(b);
    			if(target == b){
    				attack(enemyMain);
    			}
    		}
    	}
    	
    	
    }
	
	@Override
	public void onStart() {
		defender = new DefenceHelper(game);
		target = null;
		//updateStagingArea();
		// TODO Auto-generated method stub

	}

	public boolean isAttacking(){
		return isAttacking;
	}
	
	public ArrayList<EnemyBuilding> getEnemyBuildings(){
		updateEnemyUnits();
		ArrayList<EnemyBuilding> result = new ArrayList<EnemyBuilding>();
		for(EnemyBuilding b : enemyBuildings){
			result.add(b);
		}
		
		Comparator<EnemyBuilding> comp = new Comparator<EnemyBuilding>() {
			//Player self = game.self();
			public int compare(EnemyBuilding first, EnemyBuilding second){
				return first.type.compareTo(second.type);
			}
		};
		
		result.sort(comp);
		return result;
	}
	
	public void attackMoveAll(Position target){
		if(target != null) {
			List<Unit> units = game.self().getUnits();
			for(int i = count%4; i < units.size(); i+=4){
				Unit unit = units.get(i);
				if(	!unit.getType().isBuilding() &&
					!unit.getType().isWorker() &&
					unit.getID() != scoutID && 
					unit.exists()){
					unit.attack(target);
					if(!unit.getType().canAttack()){
						unit.move(target);
					}
					if(unit.getType() == UnitType.Protoss_Reaver){
						unit.train(UnitType.Protoss_Scarab);
					}
					if(unit.getType() == UnitType.Protoss_Carrier){
						unit.train(UnitType.Protoss_Interceptor);
					}
				} 
				
				
			}
		}
	}
	
	@Override
	public void onFrame() {
		// TODO Auto-generated method stub
		if(count % 15 == 0){
			
			ownedRegions = new ArrayList<Region>();
			for(Unit u : game.self().getUnits()){
				if(u.getType() == UnitType.Protoss_Nexus){
					ownedRegions.add(BWTA.getRegion(u.getPosition()));
				}
			}
			// defence
			if(enemyAttack == null){
				// no previous attack, detect attack
				EnemyAttack newAttack = detectAttack();
				if(newAttack != null){
					//System.out.println("No Attack");
					enemyAttack = newAttack;
				}
				attackMoveAll(stagingArea);
			} else {
				//System.out.println("XXXXX UNDER ATTACK XXXXX");
				if(!isThreat(enemyAttack)){
					// existing attack has been resolved
					enemyAttack = null;
				} else {
					// defend
					if(enemyAttack.getPosition() != null) {
						//enemyAttack.print();
						attackMoveAll(enemyAttack.getPosition());
					}

				}
				
			}
			if(count > 90){
				count = 0;
				if(isAttacking){
					attack(enemyMain);
				}
			}
		}
		count++;
		//System.out.println("Army done");
		
	
	}
	
	/**
	 * Simple Attack
	 */
	public void attack(BaseLocation enemyMainBase){
		isAttacking = true;
		enemyMain = enemyMainBase;
		if(enemyBuildings.size() < 1){
			attack(BWTA.getRegion(enemyMainBase.getTilePosition()));
		} else {
			for(EnemyBuilding b : enemyBuildings){
				if(target == null || ISMCTS.entityToType(b.type).mineralPrice() > ISMCTS.entityToType(target.type).mineralPrice()){
					target = b;
				}
			}
		}
		attack(BWTA.getRegion(target.position));
	}
	
	/*public void attack(BaseLocation base){
		attack(BWTA.getRegion(base.getTilePosition()));
	}*/
	
	/**
	 * Simple scout
	 */

	
	public void scout(Position position){
		getScout().move(position);
	}
	
	public Unit getScout(){
		Unit result = null;
		for(Unit unit : game.self().getUnits()){
			if(result == null && !unit.getType().isBuilding()){
				result = unit;
			} else if(unit.getType().topSpeed() > result.getType().topSpeed()){
				result = unit;
			} 
			
		}
		scoutID = result.getID();
		return result;
	}
	
	/*
	 * Detects and returns the strongest (most supply) attack in the owned regions.
	 * Return null if no relevant attack is found.
	 */
	public EnemyAttack detectAttack(){
		//System.out.println("Detect attacks");
		List<EnemyAttack> attacks = defender.getAttacks();
		//System.out.println(attacks.size() + " attacks");
		EnemyAttack result = null;
		int size = 0;
		for(EnemyAttack a : attacks){
			if(isThreat(a) && size < a.getSupply()){
				result = a;
				size = a.getSupply();
			}
		}
		
		return result;
	}
	
	public boolean isThreat(EnemyAttack attack){
		if(!attack.update()){
			return false;
		}
		
		if(isAttacking){
			for(Chokepoint chp : attack.getRegion().getChokepoints()){
				if(chp.getRegions().first.getCenter().getX() == targetRegion.getCenter().getX() && 
						chp.getRegions().first.getCenter().getY() == targetRegion.getCenter().getY()){
						return true;
				}
				if(chp.getRegions().second.getCenter().getX() == targetRegion.getCenter().getX() && 
					chp.getRegions().second.getCenter().getY() == targetRegion.getCenter().getY()){
					return true;
				}
			}
		}
		for(Chokepoint chp : attack.getRegion().getChokepoints()){				
			for(Region r : ownedRegions){
				/*System.out.println(chp.getRegions().first.getCenter().getX() + " == " + r.getCenter().getX() + " && " +
						chp.getRegions().first.getCenter().getY() + " == " + r.getCenter().getY());
				System.out.println(chp.getRegions().second.getCenter().getX() + " == " + r.getCenter().getX() + " && " +
						chp.getRegions().second.getCenter().getY() + " == " + r.getCenter().getY());*/
				if(chp.getRegions().first.getCenter().getX() == r.getCenter().getX() && 
					chp.getRegions().first.getCenter().getY() == r.getCenter().getY()){
					return true;
				}
				if(chp.getRegions().second.getCenter().getX() == r.getCenter().getX() && 
					chp.getRegions().second.getCenter().getY() == r.getCenter().getY()){
					return true;
				}
			}

		}
		//System.out.println("isThreat2");
		return false;
	}
	
	public void attack(Region reg){
		//System.out.println("Attack 2");
		if(!isAttacking) {
			oldStagingArea = stagingArea;
		}
		stagingArea = reg.getCenter();
		targetRegion = reg;
		isAttacking = true;
	//	System.out.println("Attack 3 " + isAttacking);	

	}
	
	
	
	public BaseLocation getTarget(){
		return BWTA.getNearestBaseLocation(targetRegion.getCenter());
	}
	
	public void withDraw(){
		isAttacking = false;
		stagingArea = oldStagingArea;
	}
	
	/**
	 * Updates the staging area for all controlled armies. 
	 * The area will be in the player owned region with the shortest ground distance to the enemy base.
	 * If the enemy base is unknown the center of map will be used instead of the enemy base.
	 */
	public void updateStagingArea(){
		TilePosition threatOrigin;
		BaseLocation enemy = scout.getEnemyMain();
		
		if(enemy != null){
			threatOrigin = enemy.getTilePosition();
		} else {
			System.out.println("set threat origin");
			threatOrigin = new TilePosition(game.mapWidth()/2, game.mapHeight()/2);
			
		}
		
		Region closestRegion = null;
		double shortestDistance = Double.MAX_VALUE;
		for(Region r : ownedRegions){
			
			double rDistance = BWTA.getGroundDistance(threatOrigin, r.getCenter().toTilePosition()); //r.getDistance(threatOrigin.toPosition());
			if(rDistance < shortestDistance){
				closestRegion = r;
				shortestDistance = rDistance;
			}
		}
		for(Chokepoint chp : closestRegion.getChokepoints()){
			if(!ownedRegions.contains(chp.getRegions().first) || !ownedRegions.contains(chp.getRegions().second)){
				System.out.println("new staging area");
				int x = closestRegion.getCenter().getX() + (chp.getCenter().getX() - closestRegion.getCenter().getX())/2;
				int y = closestRegion.getCenter().getY() + (chp.getCenter().getY() - closestRegion.getCenter().getY())/2;
				stagingArea = new Position(x, y);
			}
		}
		//stagingArea = closestRegion.getCenter();
	
				
	}
	
	public void setStagingRegion(Region reg){
		for(Chokepoint chp : reg.getChokepoints()){
			if(!ownedRegions.contains(chp.getRegions().first) || !ownedRegions.contains(chp.getRegions().second)){
				//System.out.println("new staging area");
				int x = (chp.getCenter().getX() + reg.getCenter().getX())/2;
				int y = (chp.getCenter().getY() + reg.getCenter().getY())/2;
				if(isAttacking){
					oldStagingArea = new Position(x, y);
					return;
				} else {
					stagingArea = new Position(x, y);
					return;
				}
			}
		}
		if(isAttacking){
			oldStagingArea = reg.getCenter();
		} else {
			stagingArea = reg.getCenter();
		}
	}
	
	public void setStagingArea(Position pos){
		stagingArea = pos;
	}
	
	public int getArmySupply(){
		int result = 0;
		for(Group g : groups){
			result += g.getSupply();
		}
		return result;
	}
	
	@Override
	public void onUnitComplete(Unit unit) {
		
		if(unit.getPlayer() == game.self() 
			&& unit.canMove() 
			&& unit.getType() != UnitType.Protoss_Probe  
			&& unit.getType() != UnitType.Protoss_Interceptor 
			&& unit.getType() != UnitType.Protoss_Scarab)
		{
			// if scout
			// add unit to unit group
			for(UnitGroup g : idleUnits){
				if(g.getType() == unit.getType() && !g.isFull()){
					g.addUnit(unit);
					return;
				}
			}
			UnitGroup g = new UnitGroup(game, unit.getType());
			g.addUnit(unit);
			idleUnits.add(g);
			unitGroups.add(g);
			groups.add(g);
		} 
	}

	public boolean transferScout(){
		if(scout.size() > 0){
			return true;
		}
		UnitGroup fastestGroup = null;
		double fastestSpeed = 0.0;
		for(UnitGroup g : unitGroups){
			if(g.size() > 0 && g.getSpeed() > fastestSpeed){
				fastestGroup = g;
				fastestSpeed = g.getSpeed();
			}
		}
		if(fastestGroup != null){
			Unit unit = fastestGroup.pop();
			scout.addUnit(unit);
			return true;
		}
		return false;
	}
	
	public void onUnitCreate(Unit unit) {
		if(unit.getType() == UnitType.Protoss_Nexus && unit.getPlayer() == game.self()){
			//System.out.println("update");
			
			setStagingRegion(BWTA.getRegion(unit.getPosition()));
			//updateStagingArea();
			//System.out.println("update 2");
		}	
	}

	@Override
	public void onUnitDestroy(Unit unit) {
		if(unit.getPlayer() == game.self()) {
			if(unit.getType() == UnitType.Protoss_Nexus){
				//Base lost;
			}
			for(Group g : groups){
				g.removeUnit(unit);
			}
		} else {
			defender.onUnitDestroy(unit);
		}
		if(unit.getPlayer() == game.enemy() 
				&& !unit.getType().isBuilding()
				&& !unit.getType().isWorker()){
				deadEnemyUnit(unit.getType());
			}
	}

	@Override
	public void onUnitDiscover(Unit unit) {
		// TODO Auto-generated method stub
		defender.onUnitDiscover(unit);
		scout.onUnitDiscover(unit);
		
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

}
