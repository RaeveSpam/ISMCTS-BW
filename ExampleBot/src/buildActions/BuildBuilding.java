package buildActions;

import bwapi.Game;
import bwapi.TilePosition;
import bwapi.Unit;
import bwapi.UnitType;

public class BuildBuilding extends BuildAction<UnitType> {
	public TilePosition buildTile;
	public int count;
	public Unit probe;
	
	
	public BuildBuilding(Game game, UnitType building){
		super(game, building);
		count = 0;
		//type = building;
		buildTile = null;
		probe = null;
		isBuilding = true;
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
		return game.canBuildHere(buildTile, type);
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
			if(probe.build(type, buildTile)){
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
			return false;
		}
		for(Unit b : game.getUnitsOnTile(buildTile)){
			if(b.getType() == type){
				return true;
			}
		}
		return false;
	}


	@Override
	public int getMinerals() {
		return type.mineralPrice();
	}


	@Override
	public int getGas() {
		return type.gasPrice();
	}


	@Override
	public boolean canBeBuilt() {
		if(type == UnitType.Protoss_Assimilator){
			if(!game.canBuildHere(buildTile, type)){
				return false;
			}
		}
		//System.out.println(type + " " + game.self().isUnitAvailable(type));
		return game.self().isUnitAvailable(type);
		//return hasBuilder() && isBuildTileValid();
	}
	

}
