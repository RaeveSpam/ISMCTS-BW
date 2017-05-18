package managers;

import bwapi.Unit;
import bwapi.UnitType;
import bwapi.Unitset;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.sun.xml.internal.bind.v2.model.runtime.RuntimeArrayInfo;

import bwapi.Game;
import bwapi.Position;
import bwapi.TilePosition;
import bwta.Region;
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
		count = 0;
	}
	
	@Override
	public void onStart() {
		defender = new DefenceHelper(game);
		//updateStagingArea();
		// TODO Auto-generated method stub

	}

	public boolean isAttacking(){
		return isAttacking;
	}
	
	public void attackMoveAll(Position target){
		
		List<Unit> units = game.self().getUnits();
		for(int i = count%4; i < units.size(); i+=4){
			Unit unit = units.get(i);
			if(	!unit.getType().isBuilding() &&
				!unit.getType().isWorker()){
				unit.attack(target);
				if(unit.getType() == UnitType.Protoss_Observer){
					unit.move(target);
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
						enemyAttack.print();
						attackMoveAll(enemyAttack.getPosition());
					}

				}
				
			}
			if(count > 90){
				count = 0;
			}
		}
		count++;
		//System.out.println("Army done");
		
	
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
		isAttacking = true;
		oldStagingArea = stagingArea;
		stagingArea = reg.getCenter();
		targetRegion = reg;
		

	}
	
	public void attack(BaseLocation base){
		isAttacking = true;
		oldStagingArea = stagingArea;
		stagingArea = base.getPosition();
		targetRegion = BWTA.getRegion(stagingArea);
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
	}

	@Override
	public void onUnitDiscover(Unit unit) {
		// TODO Auto-generated method stub
		defender.onUnitDiscover(unit);
		scout.onUnitDiscover(unit);
		
	}

}
