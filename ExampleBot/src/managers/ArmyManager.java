package managers;

import bwapi.Unit;
import bwapi.UnitType;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import bwapi.Game;
import bwapi.Position;
import bwapi.TilePosition;
import bwta.Region;
import unitControl.*;
import bwta.BWTA;
import bwta.BaseLocation;

public class ArmyManager implements Manager {

	private Game game;
	
	private List<Group> groups;
	private List<UnitGroup> unitGroups;
	private List<UnitGroup> idleUnits;
	private List<UnitGroup> activeUnits;
	private ScoutGroup scout;
	private boolean isAttacking;
	private Position stagingArea;
	private List<Region> ownedRegions;
	private List<Region> enemyRegions;
	private DefenceHelper defender;
	private EnemyAttack enemyAttack;
	
	public ArmyManager(Game game){
		this.game = game;
		isAttacking = false;
		groups = new ArrayList<Group>();
		unitGroups = new ArrayList<UnitGroup>();
		scout = new ScoutGroup(game);
		enemyRegions = new ArrayList<Region>();
		ownedRegions = new ArrayList<Region>();
		stagingArea = null;
		
		idleUnits = new ArrayList<UnitGroup>();
		activeUnits = new ArrayList<UnitGroup>();
		enemyAttack = null;
	}
	
	@Override
	public void onStart() {
		defender = new DefenceHelper(game);
		//updateStagingArea();
		// TODO Auto-generated method stub

	}

	@Override
	public void onFrame() {
		// TODO Auto-generated method stub
		
		// defence
		if(enemyAttack == null){
			// no previous attack, detect attack
			EnemyAttack newAttack = detectAttack();
			if(newAttack != null){
				enemyAttack = newAttack;
			}
		} else {
			System.out.println("XXXXX UNDER ATTACK XXXXX");
			if(!enemyAttack.update() || !ownedRegions.contains(enemyAttack.getRegion())){
				// existing attack has been resolved
				for(UnitGroup g : enemyAttack.getDefenders()){
					idleUnits.add(g);
					activeUnits.remove(g);
				}
				enemyAttack = null;
			} else {
				// defend
				for(UnitGroup g : idleUnits){
					// assign idle units to the defence
					enemyAttack.addDefenders(g);
				}
				for(UnitGroup g : enemyAttack.getDefenders()){
					// engage the defenders
					g.attackMoveOrder(enemyAttack.getPosition());
				}
			}
			
		}
		System.out.println(idleUnits.size() + " idle groups");
		//System.out.println(stagingArea.toString());
		System.out.println(stagingArea);
		for(UnitGroup g : idleUnits){
			// idle units move to the staging area
			g.moveOrder(stagingArea);
		}
		System.out.println("Army done");
		
	
	}
	/*
	 * Detects and returns the strongest (most supply) attack in the owned regions.
	 * Return null if no relevant attack is found.
	 */
	public EnemyAttack detectAttack(){
		
		List<EnemyAttack> attacks = defender.getAttacks();
		System.out.println(attacks.size() + " Attacks");
		EnemyAttack result = null;
		int size = 0;
		for(EnemyAttack a : attacks){
			if(ownedRegions.contains(a.getRegion()) && size < a.getSupply()){
				result = a;
				size = a.getSupply();
			}
		}
		
		return result;
	}
	
	public void attack(){
		
		// Select attacking units
		// select target for attack
		// Attack move
		
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
		stagingArea = closestRegion.getCenter();
	
				
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
			System.out.println("update");
			stagingArea = BWTA.getRegion(unit.getPosition()).getCenter();
			//updateStagingArea();
			System.out.println("update 2");
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
	}

	@Override
	public void onUnitDiscover(Unit unit) {
		// TODO Auto-generated method stub
		defender.onUnitDiscover(unit);
		scout.onUnitDiscover(unit);
		
	}

}
