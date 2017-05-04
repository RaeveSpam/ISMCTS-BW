package unitControl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import bwapi.Game;
import bwapi.Player;
import bwapi.Position;
import bwapi.Unit;
import bwapi.UnitType;
import bwta.BWTA;
import bwta.BaseLocation;
import bwta.Region;

public class ScoutGroup extends Group {

	private List<BaseLocation> enemyBases;
	private BaseLocation enemyMain;
	private Set<Region> enemyRegions;
	private List<Unit> enemyBuildings;
	private Set<UnitType> enemyUnits;
	
	public ScoutGroup(Game game){
		super(game);
		enemyMain = null;
		enemyBases = new ArrayList<BaseLocation>();
		enemyRegions = new HashSet<Region>();
		enemyBuildings = new ArrayList<Unit>();
		enemyUnits = new HashSet<UnitType>(); //????
	}

	@Override
	public void onFrame() {
		// TODO Auto-generated method stub

	}
	
	public boolean scoutBases(){
		if(!hasScouts()){
			return false;
		}
		Unit scout = getScout();
		BaseLocation[] bases = new BaseLocation[BWTA.getBaseLocations().size()];
		BWTA.getBaseLocations().toArray(bases);
		Arrays.sort(bases, BaseComparator);
		scout.move(bases[0].getTilePosition().toPosition());
		for(int i = 1; i < bases.length; i++){
			scout.move(bases[i].getTilePosition().toPosition(), true);
		}
		System.out.println("Scout " + bases.length + " bases");
		return true;
	}
	
	public void scoutEnemy(){
		// send scout to enemy main/bases
	}
	
	public Set<Region> getEnemyRegions(){
		return enemyRegions;
	}
	
	public List<BaseLocation> getEnemyBases(){
		return enemyBases;
	}
	
	public BaseLocation getEnemyMain(){
		return enemyMain;
	}
	
	public void onUnitDiscover(Unit unit) {
		
		// enemy army?
		if(!unit.getPlayer().equals(game.self())){
			if(unit.getType().isBuilding()){
				if(unit.getType().isResourceDepot()){
					enemyRegions.add(BWTA.getRegion(unit.getPosition()));
				}
				enemyBuildings.add(unit);
			}
		}
		if(unit.getType().isResourceDepot() && !unit.getPlayer().equals(game.self())){
			
		}
	}
	
	private Unit getScout(){
		
		return null;
	}
	
	public Comparator<BaseLocation> BaseComparator = new Comparator<BaseLocation>() {
		Player self = game.self();
		public int compare(BaseLocation first, BaseLocation second){
			return (int)(first.getTilePosition().getDistance(self.getStartLocation()) - second.getTilePosition().getDistance(self.getStartLocation())+0.5);
		}
	};
	
	private boolean hasScouts(){
		return (units.size() != 0);
	}

	@Override
	public int getSupply() {
		int result = 0;
		for(Unit u : units){
			result += u.getType().supplyRequired();
		}
		return result;
	}
}
