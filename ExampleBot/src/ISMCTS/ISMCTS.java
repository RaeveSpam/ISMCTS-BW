package ISMCTS;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import actions.*;
import bots.ISMCTSBot;
import bwapi.Game;
import bwapi.Unit;
import bwapi.UnitType;
import bwapi.UpgradeType;
import stateInformation.EnemyBuilding;
import stateInformation.EnemyUnit;
import stateInformation.Memory;
import bwta.BWTA;
import bwta.BaseLocation;

public class ISMCTS {
	
	private Node rootNode;
	
	private transient Node currentNode;
	
	private transient Edge currentEdge;
	
	private transient ArrayList<Edge> visitedEdges;
	
	private transient ArrayList<Node> visitedNodes;
	
	//private transient Memory currentKnowledge;
	
	private transient List<BaseLocation> baseLocations;

	private transient Random random; 
	
	public ISMCTS(Node root, Game game, List<BaseLocation> baseLocations){
		//System.out.println("ISMCTS Constructor ");
		//System.out.println("Root = " + root);
		this.baseLocations = baseLocations; 
		rootNode = root;
		currentEdge = null;
		currentNode= null;
		visitedEdges = new ArrayList<Edge>();
		visitedNodes = new ArrayList<Node>();
		if(rootNode == null){
			// default tree
			defaultTree(game);
		}
		random = new Random();
	}
	
	public void defaultTree(Game game){
		System.out.println("default tree");
		Memory memory = new Memory();
		
		memory.enemyBases.add(baseLocations.size()-1);
		
		memory.enemyBuildings.add(Entity.Nexus);
		
		
		rootNode = new Node(InformationSet.getDefault());
		//rootNode.getInformationSet().memory = memory;
		//System.out.println("root " + rootNode);
	}
	
	public void reset(){
		currentNode = rootNode;
	}
	
	public Node getRoot(){
		return rootNode;
	}
	

	
	private InformationSet captureInformationSet(Game game, ArrayList<EnemyUnit> enemyArmy, ArrayList<EnemyBuilding> enemyBuildings){
		//System.out.println("capture");
		InformationSet result = new InformationSet();
		result.collectInformation(game, baseLocations);
		result.enemyBuildings = enemyBuildings;
		result.enemyArmy = enemyArmy;
		//result.setMemory(memory);
		//result.setAttackTarget(targetBase);
		//System.out.println("done");
		//System.out.println(result.getBuildings());
		//System.out.println("InformationSet build");
		return result;
	}
	

	
	public Action step(Game game, ArrayList<EnemyUnit> enemyArmy, ArrayList<EnemyBuilding> enemyBuildings){
		// build current information set
		// compare with currentEdge.children
		//System.out.println("step");
		currentNode = getCurrentNode(game, enemyArmy, enemyBuildings);
		//System.out.println("current set");
		// select / expand		
		Action result = selection(game);
		visitedNodes.add(currentNode);
		visitedEdges.add(currentEdge);
	//	System.out.println(currentNode.informationSet.enemyArmy);
		return result; 
	}
		
	/**
	 * Using ISMCTS get the best move (Action) for the current InformationSet 
	 * @return
	 */
	
	private Node getCurrentNode(Game game, ArrayList<EnemyUnit> enemyArmy, ArrayList<EnemyBuilding> enemyBuildings){
		//System.out.println("getCurrentNode");
		if(currentEdge == null){
			return rootNode;
		}

		return currentEdge.getNodeFromSet(captureInformationSet(game, enemyArmy, enemyBuildings));
	}
	
	private Action selection(Game game){
		// if node is not fully expanded, expand.
		if(!currentNode.isFullyExpanded()){
			//System.out.println("expand");
			return expansion(game);
			
		}
		// Find best child
		Edge bestChild = null;
		for(Edge child : currentNode.children){
			if(bestChild == null || bestChild.UCBScore(currentNode.getVisits()) > child.UCBScore(currentNode.getVisits())){
				bestChild = child;
			}
		}
		currentEdge = bestChild;
		return bestChild.getAction();
	}
	
	private Action expansion(Game game){
		// Algorithm step
		if(currentNode.getVisits() == 0){
			// New node
			//System.out.println("New node");
			for(Action a : getPossibleActions(game)){
				currentNode.addChild(new Edge(a));
			}
			
		}
		//System.out.println("#edges " + currentNode.children.size());
		// Choose random not visited edge
		List<Edge> notVisited = new ArrayList<Edge>();
		for(Edge e : currentNode.children){
			if(!e.visited()){
				notVisited.add(e);
			}
		}
		//System.out.println("actions woo");
		//Random random = new Random();
		//System.out.println(notVisited.size());
		//System.out.println("Choose child from " + notVisited.size());
		if(notVisited.size() < 1){
			return null;
		}
		//System.out.println(notVisited.size());
		
		Edge chosen = notVisited.get(random.nextInt(notVisited.size()));
		currentEdge = chosen;
		//System.out.println(chosen.getAction().move);
		return chosen.getAction();
	}
	
	public void backPropogate(boolean win){
		// Algorithm step
		for(Edge e : visitedEdges){
			e.visits++;
			if(win){
				e.wins++;
			}
		}

		for(Node n : visitedNodes){
			n.visits++;
			if(win){
				n.wins++;
			}
		}
	}
	
	private ArrayList<Action> getPossibleActions(Game game){
	//	System.out.println("get possible moves");
		ArrayList<Action> result = new ArrayList<Action>();
 		result.add(new NoAction());
		// Get possible moves
		for(UnitType u : getAllUnitTypes()){
			
			boolean fullfil = true;
		 	Map<UnitType, Integer> req = u.requiredUnits();
		 	for(UnitType r : req.keySet()){
		 		if(!currentNode.getInformationSet().getBuildings().contains(typeToEntity(r)) && r != UnitType.Protoss_Probe){
		 			fullfil = false;
		 			break;
		 		}
		 	}
			if(fullfil  && u != UnitType.Protoss_Shield_Battery){
				if(u.isBuilding()){
					//System.out.println("add " + ISMCTS.typeToEntity(u));
					result.add(new BuildBuilding(game, ISMCTS.typeToEntity(u)));
				} else {
					//(game.printf(u.toString());
					//System.out.print(u + "   ");
					result.add(new BuildUnit(game, ISMCTS.typeToEntity(u)));
					//System.out.println("add " + ISMCTS.typeToEntity(u));
				}
			}
		}
		
		// available upgrades
		for(Tech t : Tech.getAllTech()){ // for each upgrade in the game
			//System.out.println(t + "1");
			UpgradeType up = ISMCTS.techToUpgrade(t);
			//System.out.println(t + " " + up.toString());
			int level = game.self().getUpgradeLevel(up);
			//System.out.println(t + " " + level);
			int max = up.maxRepeats();
			//System.out.println(t + " " + max);
			boolean isUpgrading = game.self().isUpgrading(up);
			//System.out.println(t + " " + isUpgrading);
			boolean hasRequired = currentNode.getInformationSet().getBuildings().contains(typeToEntity(up.whatsRequired(level)));
			//System.out.println(t + " " + hasRequired);
			boolean noneRequired = (up.whatsRequired(level) == UnitType.None);
			//System.out.println(t + " " + noneRequired);
			boolean whatUpgrades = currentNode.getInformationSet().getBuildings().contains(typeToEntity(up.whatUpgrades())); // building that researches upgrade is present
			//System.out.println(t + " " + whatUpgrades);	
			
			
			
			if(level < max && !isUpgrading && (hasRequired || noneRequired) && whatUpgrades)// is not upgrading
			{
					result.add(new BuildUpgrade(game, t, level));
			}
		}
	
		
		// Check for potential available bases
		/*for(int i = 0; i < baseLocations.size(); i++){
			for(Integer base : currentNode.getInformationSet().getBases()){
				if(i != base && game.canBuildHere(baseLocations.get(i).getTilePosition(), UnitType.Protoss_Nexus)){
					BuildBuilding expansion = new BuildBuilding(game, Entity.Nexus);
					expansion.baseLocation = i;
					expansion.buildTile = baseLocations.get(i).getTilePosition();
					result.add(expansion);
					System.out.println("Can expand");
					break;
				}
			}
		}*/
		
		
		/*
		// if getAttackTarget > -1  add withdraw action
		if(currentNode.getInformationSet().getAttackTarget() > -1){
			
			result.add(new Withdraw());
		}*/
		
	
		//System.out.println(baseLocations);
		/*for(int i = 0; i < baseLocations.size(); i++){
			for(int base : currentNode.getInformationSet().getBases()){
				if(base != i){
					System.out.print("1");
					
					//result.add(new Scout(i));
					if(currentNode.getInformationSet().getArmy().size() > 0){
						//result.add(new Attack(i));
					}
					for(int enemy : currentNode.getInformationSet().getMemory().enemyBases){
						System.out.print("2");
						if(enemy != i && game.isVisible(baseLocations.get(i).getTilePosition()) && game.canBuildHere(baseLocations.get(i).getTilePosition(), UnitType.Protoss_Nexus)){
							// add expand
							BuildBuilding expand = new BuildBuilding(game, Entity.Nexus);
							expand.baseLocation = i;
							expand.buildTile = baseLocations.get(i).getTilePosition();
							result.add(expand);
						}
					}
				}
			}
		}*/
		
		/*	
		for(Action a : result){
			System.out.print(a.move + " ");
		} */
		
		//System.out.println("result size" + result.size());
		for(Action a : result){
		//	a.print();
		}
		return result;
	}
	
	private Set<UnitType> getAllUnitTypes(){
		HashSet<UnitType> result = new HashSet<UnitType>();
		for(Entity e : Entity.getEntities()){
			result.add(entityToType(e));
		}
		return result;
	}
	
	private Set<UpgradeType> getAllUpgrades(){
		HashSet<UpgradeType> result = new HashSet<UpgradeType>();
		/*for(Entity e : Entity.getEntities()){
			result.add(entityToType(e));
		}*/
		return result;
	}

	public static Entity typeToEntity(UnitType type){
		if(type == UnitType.Protoss_Arbiter) {
			return Entity.Arbiter;
		}
		if(type == UnitType.Protoss_Arbiter_Tribunal) {
			return Entity.Arbiter_Tribunal;
		}
		if(type == UnitType.Protoss_Archon) {
			return Entity.Archon;
		}
		if(type == UnitType.Protoss_Assimilator) {
			return Entity.Assimilator;
		}
		if(type == UnitType.Protoss_Carrier) {
			return Entity.Carrier;
		}
		if(type == UnitType.Protoss_Citadel_of_Adun) {
			return Entity.Citadel_of_Adun;
		}
		if(type == UnitType.Protoss_Corsair) {
			return Entity.Corsair;
		}
		if(type == UnitType.Protoss_Cybernetics_Core) {
			return Entity.Cybernetics_Core;
		}
		if(type == UnitType.Protoss_Dark_Archon) {
			return Entity.Dark_Archon;
		}
		if(type == UnitType.Protoss_Dark_Templar) {
			return Entity.Dark_Templar;
		}
		if(type == UnitType.Protoss_Dragoon) {
			return Entity.Dragoon;
		}
		if(type == UnitType.Protoss_Fleet_Beacon) {
			return Entity.Fleet_Beacon;
		}
		if(type == UnitType.Protoss_Forge) {
			return Entity.Forge;
		}
		if(type == UnitType.Protoss_Gateway) {
			return Entity.Gateway;
		}
		if(type == UnitType.Protoss_High_Templar) {
			return Entity.High_Templar;
		}
		if(type == UnitType.Protoss_Interceptor) {
			return Entity.Interceptor;
		}
		if(type == UnitType.Protoss_Nexus) {
			return Entity.Nexus;
		}
		if(type == UnitType.Protoss_Observatory) {
			return Entity.Observatory;
		}
		if(type == UnitType.Protoss_Observer) {
			return Entity.Observer;
		}
		if(type == UnitType.Protoss_Photon_Cannon) {
			return Entity.Photon_Cannon;
		}
		if(type == UnitType.Protoss_Probe) {
			return Entity.Probe;
		}
		if(type == UnitType.Protoss_Pylon) {
			return Entity.Pylon;
		}
		if(type == UnitType.Protoss_Reaver) {
			return Entity.Reaver;
		}
		if(type == UnitType.Protoss_Robotics_Facility) {
			return Entity.Robotics_Facility;
		}
		if(type == UnitType.Protoss_Robotics_Support_Bay) {
			return Entity.Robotics_Support_Bay;
		}
		if(type == UnitType.Protoss_Scarab) {
			return Entity.Scarab;
		}
		if(type == UnitType.Protoss_Scout) {
			return Entity.Scout;
		}
		if(type == UnitType.Protoss_Shield_Battery) {
			return Entity.Shield_Battery;
		}
		if(type == UnitType.Protoss_Shuttle) {
			return Entity.Shuttle;
		}
		if(type == UnitType.Protoss_Stargate) {
			return Entity.Stargate;
		}
		if(type == UnitType.Protoss_Templar_Archives) {
			return Entity.Templar_Archives;
		}
		if(type == UnitType.Protoss_Zealot) {
			return Entity.Zealot;
		}
		return null;
	}
	
	public static UnitType entityToType(Entity entity){
		switch(entity){
		case Arbiter: 
			return UnitType.Protoss_Arbiter;
		case Arbiter_Tribunal: 
			return UnitType.Protoss_Arbiter_Tribunal;
		case Archon:
			return UnitType.Protoss_Archon;
		case Assimilator:	
			return UnitType.Protoss_Assimilator;
		case Carrier:	
			return UnitType.Protoss_Carrier;
		case Citadel_of_Adun:	
			return UnitType.Protoss_Citadel_of_Adun;
		case Corsair:
			return UnitType.Protoss_Corsair;
		case Cybernetics_Core:
			return UnitType.Protoss_Cybernetics_Core;
		case Dark_Archon:
			return UnitType.Protoss_Dark_Archon;
		case Dark_Templar:
			return UnitType.Protoss_Dark_Templar;
		case Dragoon:
			return UnitType.Protoss_Dragoon;
		case Fleet_Beacon:
			return	UnitType.Protoss_Fleet_Beacon;
		case Forge:
			return UnitType.Protoss_Forge;
		case Gateway: 
			return UnitType.Protoss_Gateway;
		case High_Templar:
			return UnitType.Protoss_High_Templar;
		case Interceptor:
			return UnitType.Protoss_Interceptor;
		case Nexus:
			return UnitType.Protoss_Nexus;
		case Observatory:
			return UnitType.Protoss_Observatory;
		case Observer: 
			return UnitType.Protoss_Observer;
		case Photon_Cannon:
			return UnitType.Protoss_Photon_Cannon;
		case Probe:
			return UnitType.Protoss_Probe;
		case Pylon:
			return UnitType.Protoss_Pylon;
		case Reaver:
			return UnitType.Protoss_Reaver;
		case Robotics_Facility:
			return UnitType.Protoss_Robotics_Facility;
		case Robotics_Support_Bay:
			return UnitType.Protoss_Robotics_Support_Bay;
		case Scarab:
			return UnitType.Protoss_Scarab;
		case Scout:
			return UnitType.Protoss_Scout;
		case Shield_Battery:
			return UnitType.Protoss_Shield_Battery;
		case Shuttle:
			return UnitType.Protoss_Shuttle;
		case Stargate:
			return UnitType.Protoss_Stargate;
		case Templar_Archives:
			return UnitType.Protoss_Templar_Archives;
		case Zealot:
			return UnitType.Protoss_Zealot;
		}
		return null;
	}
	
	public static Tech upgradeToTech(UpgradeType type){
		if(type == UpgradeType.Apial_Sensors){
			return Tech.Apial_Sensors;

		}
		if(type == UpgradeType.Argus_Jewel){
			return Tech.Argus_Jewel;
		}
		if(type == UpgradeType.Argus_Talisman){
			return Tech.Argus_Talisman;
		}
		if(type == UpgradeType.Carrier_Capacity){
			return Tech.Carrier_Capacity;
		}
		if(type == UpgradeType.Gravitic_Boosters){
			return Tech.Gravitic_Boosters;
		}
		if(type == UpgradeType.Gravitic_Drive){
			return Tech.Gravitic_Drive;
		}
		if(type == UpgradeType.Gravitic_Thrusters){
			return Tech.Gravitic_Thrusters;
		}
		if(type == UpgradeType.Khaydarin_Amulet){
			return Tech.Khaydarin_Amulet;
		}
		if(type == UpgradeType.Khaydarin_Core){
			return Tech.Khaydarin_Core;
		}
		if(type == UpgradeType.Leg_Enhancements){
			return Tech.Leg_Enhancements;
		}
		if(type == UpgradeType.Protoss_Air_Armor){
			return Tech.Protoss_Air_Armor;
		}
		if(type == UpgradeType.Protoss_Air_Weapons){
			return Tech.Protoss_Air_Weapons;
		}
		if(type == UpgradeType.Protoss_Ground_Armor){
			return Tech.Protoss_Ground_Armor;
		}
		if(type == UpgradeType.Protoss_Ground_Weapons){
			return Tech.Protoss_Ground_Weapons;
		}
		if(type == UpgradeType.Protoss_Plasma_Shields){
			return Tech.Protoss_Plasma_Shields;
		}
		if(type == UpgradeType.Reaver_Capacity){
			return Tech.Reaver_Capacity;
		}
		if(type == UpgradeType.Sensor_Array){
			return Tech.Sensor_Array;
		}
		if(type == UpgradeType.Singularity_Charge){
			return Tech.Singularity_Charge;
		}
		if(type == UpgradeType.Scarab_Damage){
			return Tech.Scarab_Damage;
		}
		return null;
	}
	
	public static Tech upgradeToTech(UpgradeType type, int level){
		if(type == UpgradeType.Apial_Sensors){
			Tech result = Tech.Apial_Sensors;
			result.level = level;
			return result;
		}
		if(type == UpgradeType.Argus_Jewel){
			Tech result = Tech.Argus_Jewel;
			result.level = level;
			return result;
		}
		if(type == UpgradeType.Argus_Talisman){
			Tech result = Tech.Argus_Talisman;
			result.level = level;
			return result;
		}
		if(type == UpgradeType.Carrier_Capacity){
			Tech result = Tech.Carrier_Capacity;
			result.level = level;
			return result;
		}
		if(type == UpgradeType.Gravitic_Boosters){
			Tech result = Tech.Gravitic_Boosters;
			result.level = level;
			return result;
		}
		if(type == UpgradeType.Gravitic_Drive){
			Tech result = Tech.Gravitic_Drive;
			result.level = level;
			return result;
		}
		if(type == UpgradeType.Gravitic_Thrusters){
			Tech result = Tech.Gravitic_Thrusters;
			result.level = level;
			return result;
		}
		if(type == UpgradeType.Khaydarin_Amulet){
			Tech result = Tech.Khaydarin_Amulet;
			result.level = level;
			return result;
		}
		if(type == UpgradeType.Khaydarin_Core){
			Tech result = Tech.Khaydarin_Core;
			result.level = level;
			return result;
		}
		if(type == UpgradeType.Leg_Enhancements){
			Tech result = Tech.Leg_Enhancements;
			result.level = level;
			return result;
		}
		if(type == UpgradeType.Protoss_Air_Armor){
			Tech result = Tech.Protoss_Air_Armor;
			result.level = level;
			return result;
		}
		if(type == UpgradeType.Protoss_Air_Weapons){
			Tech result = Tech.Protoss_Air_Weapons;
			result.level = level;
			return result;
		}
		if(type == UpgradeType.Protoss_Ground_Armor){
			Tech result = Tech.Protoss_Ground_Armor;
			result.level = level;
			return result;
		}
		if(type == UpgradeType.Protoss_Ground_Weapons){
			Tech result = Tech.Protoss_Ground_Weapons;
			result.level = level;
			return result;
		}
		if(type == UpgradeType.Protoss_Plasma_Shields){
			Tech result = Tech.Protoss_Plasma_Shields;
			result.level = level;
			return result;
		}
		if(type == UpgradeType.Reaver_Capacity){
			Tech result = Tech.Reaver_Capacity;
			result.level = level;
			return result;
		}
		if(type == UpgradeType.Sensor_Array){
			Tech result = Tech.Sensor_Array;
			result.level = level;
			return result;
		}
		if(type == UpgradeType.Singularity_Charge){
			Tech result = Tech.Singularity_Charge;
			result.level = level;
			return result;
		}if(type == UpgradeType.Scarab_Damage){
			Tech result = Tech.Scarab_Damage;
			result.level = level;
			return result;
		}
		return null;
	}
	
	public static UpgradeType techToUpgrade(Tech tech){
		switch(tech){
		case Apial_Sensors: 
			return UpgradeType.Apial_Sensors;
		case Argus_Jewel: 
			return UpgradeType.Argus_Jewel;
		case Argus_Talisman:
			return UpgradeType.Argus_Talisman;
		case Carrier_Capacity:
			return UpgradeType.Carrier_Capacity;
		case Gravitic_Boosters: 
			return UpgradeType.Gravitic_Boosters;
		case Gravitic_Drive: 
			return UpgradeType.Gravitic_Drive;
		case Gravitic_Thrusters: 
			return UpgradeType.Gravitic_Thrusters;
		case Khaydarin_Amulet:
			return UpgradeType.Khaydarin_Amulet;
		case Khaydarin_Core: 
			return UpgradeType.Khaydarin_Core;
		case Leg_Enhancements: 
			return UpgradeType.Leg_Enhancements;
		case Protoss_Air_Armor: 
			return UpgradeType.Protoss_Air_Armor;
		case Protoss_Air_Weapons: 
			return UpgradeType.Protoss_Air_Weapons;
		case Protoss_Ground_Armor: 
			return UpgradeType.Protoss_Ground_Armor;
		case Protoss_Ground_Weapons: 
			return UpgradeType.Protoss_Ground_Weapons;
		case Protoss_Plasma_Shields: 
			return UpgradeType.Protoss_Plasma_Shields;
		case Reaver_Capacity: 
			return UpgradeType.Reaver_Capacity;
		case Sensor_Array: 
			return UpgradeType.Sensor_Array;
		case Singularity_Charge: 
			return UpgradeType.Singularity_Charge;
		case Scarab_Damage:  
			return UpgradeType.Scarab_Damage;
		}
		return null;
	}

	

}
