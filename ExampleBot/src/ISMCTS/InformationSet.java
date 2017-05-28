package ISMCTS;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import bots.ISMCTSBot;
import bwapi.Game;
import bwapi.Unit;
import bwapi.UnitType;
import bwta.BWTA;
import bwta.BaseLocation;
import stateInformation.EnemyBuilding;
import stateInformation.EnemyUnit;
import stateInformation.Memory;

public class InformationSet {
	
	//private ArrayList<Action> enemyActions;
	public transient ArrayList<EnemyUnit> enemyArmy;
	public ArrayList<Entity> buildings;
	public ArrayList<Integer> bases;
	public ArrayList<EnemyUnit> army;
	public ArrayList<Tech> upgrades;
	public ArrayList<EnemyBuilding> enemyBuildings;
	
	
	public InformationSet(){
		buildings = new ArrayList<Entity>();
		bases = new ArrayList<Integer>();
		army = new ArrayList<EnemyUnit>();
		upgrades = new ArrayList<Tech>();
		enemyArmy = new ArrayList<EnemyUnit>();
		enemyBuildings = new ArrayList<EnemyBuilding>();
	}
	
	public InformationSet(Game game, Memory memory, List<BaseLocation> baseLocations){
		collectInformation(game, baseLocations);
		//this.memory = memory;
	} 
	
	public static InformationSet getDefault(){
		InformationSet result = new InformationSet();
		result.bases.add(0);
		result.buildings.add(Entity.Nexus);
	//	result.attacking = -1;
	//	result.supply = 5;
		return result;
	}
	
	public void collectInformation(Game game, List<BaseLocation> baseLocations){
		// collect units
		for(Unit u : game.self().getUnits()){
			if(u.getType().isBuilding()){
				if(u.getType() == UnitType.Protoss_Nexus){
					for(BaseLocation b : baseLocations){
						BaseLocation closest = BWTA.getNearestBaseLocation(u.getPosition());
						if(b.getX() == closest.getX() && b.getY() == closest.getY()){
							bases.add(baseLocations.indexOf(b));
							break;
						}
					}
				}
				buildings.add(ISMCTS.typeToEntity(u.getType()));	
			} else if(!u.getType().isWorker() && u.getType() != UnitType.Protoss_Scarab && u.getType() != UnitType.Protoss_Interceptor){
				boolean exists = false;
				for(EnemyUnit myUnit : army){
					if(myUnit.type == ISMCTS.typeToEntity(u.getType())){
						myUnit.number++;
						exists = true;
						break;
					}
				}
				if(!exists){
					army.add(new EnemyUnit(ISMCTS.typeToEntity(u.getType())));
				}
			}
		}
		//supply = game.self().supplyUsed();
		
		// collect upgrades
		for(Tech t : Tech.getAllTech()){
			if(game.self().getUpgradeLevel(ISMCTS.techToUpgrade(t)) > 0){
				t.level = game.self().getUpgradeLevel(ISMCTS.techToUpgrade(t));
				upgrades.add(t);
			}
		}
		// collect bases
		
	}
	
	public Memory getMemory(){
		return null; //memory;
	}
	
	public ArrayList<Tech> getUpgrades(){
		return upgrades;
	}
	
	public ArrayList<EnemyUnit> getArmy(){
		return army;
	}
	
	public ArrayList<Entity> getBuildings(){
		//System.out.println(buildings);
		return buildings;
	}
	
	public void setEnemyBuildings(ArrayList<EnemyBuilding> enemyBuildings){
		this.enemyBuildings = enemyBuildings;
	}
	
	public void setEnemyArmy(ArrayList<EnemyUnit> enemyArmy){
		this.enemyArmy = enemyArmy;
	}
	/*
	public void setAttackTarget(int targetBase){
		//attacking = targetBase;
	}*/
	
	public void setMemory(Memory memory){
	//	this.memory = memory;
	}
	/*
	public int getAttackTarget(){
		return attacking;
	}*/
	
	public List<Integer> getBases(){
		return bases;
	}
	
	public boolean equals(InformationSet other){
	/*	if(supply != other.supply){
			return false;
		} */
		/*if(!memory.equals(other.memory)){
			return false;
		}*/
		
		// compare bases
		if(bases.size() != other.bases.size()){
			return false;
		}
		bases.sort(null);
		other.bases.sort(null);
		for(int i = 0; i < bases.size(); i++){
			if(bases.get(i) != other.bases.get(i)){
				return false;
			}
		}
		
		// compare buildings
		if(buildings.size() != other.buildings.size()){
			return false;
		}
		
		buildings.sort(null);
		other.buildings.sort(null);
		for(int i = 0; i < buildings.size(); i++){
			if(buildings.get(i) != other.buildings.get(i)){
				return false;
			}
		}
		
		// compare armies
		if(army.size() != other.army.size()){
			return false;
		}
		for(EnemyUnit a : army){
			boolean found = false;
			for(EnemyUnit b : other.army){
				if(a.equals(b)){
					found = true;
					break;
				}
			}
			if(!found) {
				return false;
			}
		}
		
		// compare enemy buildings
		if(enemyBuildings.size() != other.enemyBuildings.size()){
			return false;
		}
		Comparator<EnemyBuilding> comp = new Comparator<EnemyBuilding>() {
			//Player self = game.self();
			public int compare(EnemyBuilding first, EnemyBuilding second){
				return first.type.compareTo(second.type);
			}
		};
		enemyBuildings.sort(comp);
		other.enemyBuildings.sort(comp);
		for(int i = 0; i < enemyBuildings.size(); i++){
			if(enemyBuildings.get(i).type != other.enemyBuildings.get(i).type){
				return false;
			}
		}
		
		// compare enemy units
		/*
		if(enemyArmy.size() != other.enemyArmy.size()){
			return false;
		}
		for(EnemyUnit a : enemyArmy){
			boolean found = false;
			for(EnemyUnit b : other.enemyArmy){
				if(a.equals(b)){
					found = true;
					break;
				}
			}
			if(!found) {
				return false;
			}
		}*/
		
		
		return true;
	}
	
}
