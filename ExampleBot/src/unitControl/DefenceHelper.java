package unitControl;

import java.util.ArrayList;
import java.util.List;

import bwapi.Game;
import bwapi.Unit;
import bwta.BWTA;

public class DefenceHelper {
	List<EnemyAttack> attacks;
	
	Game game;
	
	public DefenceHelper(Game game){
		this.game = game;
		attacks = new ArrayList<EnemyAttack>();
	}
	
	public DefenceHelper(){
		
	}
	
	
	public void addEnemyUnit(Unit unit){
		boolean existingAttack = false;
		// find existing attack in the occupied region
		for(EnemyAttack a : attacks){
			if(a.getRegion() == BWTA.getRegion(unit.getPosition())){
				a.addUnit(unit);
				existingAttack = true;
				break;
			}
		}
		// create new attack if no existing attack was found
		if(!existingAttack){
			EnemyAttack newAttack = new EnemyAttack(BWTA.getRegion(unit.getPosition()));
			newAttack.addUnit(unit);
			attacks.add(newAttack);
		}
	}
	
	public List<UnitGroup> update(){
		//System.out.println("defender.update()");
		List<UnitGroup> groups = new ArrayList<UnitGroup>();
		for(EnemyAttack a : attacks){
			if(!a.update()) {
				groups.addAll(a.getDefenders());
				attacks.remove(a);
			}
		}
		
		return groups;
	}
	
	public List<EnemyAttack> getAttacks(){
		//System.out.println("defender.getAttacks()");
		update();
		return attacks;
	}
	
	public void onUnitDestroy(Unit unit) {
		for(EnemyAttack a : attacks){
			a.removeUnit(unit);
		}
	}
	
	public void onUnitDiscover(Unit unit){
		if(unit.getPlayer() == game.enemy() && !unit.getType().isBuilding() && unit.getType().canAttack()){
			addEnemyUnit(unit);
		}
	}
}
