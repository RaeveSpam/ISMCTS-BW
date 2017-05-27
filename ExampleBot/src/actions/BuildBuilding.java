package actions;

import ISMCTS.Entity;
import ISMCTS.ISMCTS;
import bwapi.Game;
import bwapi.TilePosition;
import bwapi.Unit;
import bwapi.UnitType;
import stateInformation.Player;

public class BuildBuilding extends BuildAction<Entity> {
	public transient TilePosition buildTile;
	public transient int  count;
	public transient Unit probe;
	
	public BuildBuilding(){

	}
	
	public void reset(){
		if(type != Entity.Nexus) {
			buildTile = null;
		}
		probe = null;
	}
	
	public BuildBuilding(Game game, Entity building){
		super(game, building);
		count = 0;
		//type = building;
		buildTile = null;
		probe = null;
		isBuilding = true;
	}
	
	public void setBaseLocation(int i){
		baseLocation = i;
	}
	
	public BuildBuilding(Game game, Entity building, Player player){
		super(game, building);
		count = 0;
		//type = building;
		buildTile = null;
		probe = null;
		isBuilding = true;
		this.player = player;
	}
	
	public void assignBuilder(Unit probe){
		this.probe = probe;
	}
	
	public void setBuildTile(TilePosition buildTile){
		this.buildTile = buildTile;
	}
	
	public boolean isBuildTileValid(){
		if(buildTile == null){
			return false;
		}
		return game.canBuildHere(buildTile, ISMCTS.entityToType(type));
	}
	
	public boolean hasBuilder(){
		if(probe == null){
			return false;
		} else if(!probe.exists()){
			return false;
		}
		return true;
	}
	
	@Override
	public boolean build(){
		if(hasBeenBuilt()){
			return true;
		}
		if(hasBuilder() && isBuildTileValid()){
			if(probe.build(ISMCTS.entityToType(type), buildTile)){
				count = 0;
				return true;
			}
		}
		return false;
	}

	
	public boolean isBuiltOnTile(TilePosition pos){
		
		if(buildTile == null){
			return false;
		}
		//System.out.println(pos + " " + buildTile);
		if(buildTile.getX() != pos.getX()){
			return false;
		}
		if(buildTile.getY() != pos.getY()){
			return false;
		}
		return true;
	}
	
	@Override
	public boolean hasBeenBuilt(){
		if(buildTile == null){
			if(type == Entity.Nexus){
				return true;
			}
			return false;
		}
		for(Unit b : game.getUnitsOnTile(buildTile)){
			if(b.getType() == ISMCTS.entityToType(type)){
				return true;
			}
		}
		return false;
	}


	@Override
	public int getMinerals() {
		return ISMCTS.entityToType(type).mineralPrice();
	}


	@Override
	public int getGas() {
		return ISMCTS.entityToType(type).gasPrice();
	}


	@Override
	public boolean canBeBuilt() {
		if(ISMCTS.entityToType(type) == UnitType.Protoss_Assimilator){
			if(!game.canBuildHere(buildTile, ISMCTS.entityToType(type))){
				return false;
			}
		}
		//System.out.println(type + " " + game.self().isUnitAvailable(type));
		return game.self().isUnitAvailable(ISMCTS.entityToType(type));
		//return hasBuilder() && isBuildTileValid();
	}
	

}
