package managers;

import bwapi.*;

public class Builder {
	
	private Game game;
	private int stopDist;
	private int inc;
	
	public Builder(Game game){
		this.game = game;
		stopDist = 40;
		inc = 1;
	}
	
	public Builder(Game game, int dist){
		this.game = game;
		stopDist = dist;
		inc = 1;
	}
	
	public Builder(Game game, int dist, int increment){
		this.game = game;
		stopDist = dist;
		inc = increment;
	} 
	
	public TilePosition getProtossBuildTile(Unit builder, UnitType buildingType){
		if(buildingType != UnitType.Protoss_Pylon && buildingType != UnitType.Protoss_Nexus && buildingType != UnitType.Protoss_Assimilator){
			for(Unit p : game.self().getUnits()){
				if(p.getType() == UnitType.Protoss_Pylon){
					TilePosition result = getBuildTile(builder, buildingType, p.getTilePosition());
					if(result != null){
						return result;
					}
				}
			}
		} else {
			return getBuildTile(builder, buildingType, builder.getTilePosition());
		}
		return null;
	}
	
	public TilePosition getBuildTile(Unit builder, UnitType buildingType, TilePosition aroundTile) {
	 	TilePosition ret = null;
	 	int maxDist = 1;
	 	//int stopDist = 10;

	 	// Refinery, Assimilator, Extractor
	 	/*if (buildingType.isRefinery()) {
	 		for (Unit n : game.neutral().getUnits()) {
	 			if ((n.getType() == UnitType.Resource_Vespene_Geyser) &&
	 					( Math.abs(n.getTilePosition().getX() - aroundTile.getX()) < stopDist ) &&
	 					( Math.abs(n.getTilePosition().getY() - aroundTile.getY()) < stopDist )
	 					) return n.getTilePosition();
	 		}
	 	}*/

	 	while ((maxDist < stopDist) && (ret == null)) {
	 		for (int i=aroundTile.getX()-maxDist; i<=aroundTile.getX()+maxDist; i+=inc) {
	 			for (int j=aroundTile.getY()-maxDist; j<=aroundTile.getY()+maxDist; j+=inc) {
	 				if (game.canBuildHere(new TilePosition(i,j), buildingType, builder, false)) {
	 					// units that are blocking the tile
	 					boolean unitsInWay = false;
	 					for (Unit u : game.getAllUnits()) {
	 						if (u.getID() == builder.getID()) continue;
	 						if ((Math.abs(u.getTilePosition().getX()-i) < 4) && (Math.abs(u.getTilePosition().getY()-j) < 4)) unitsInWay = true;
	 					}
	 					if (!unitsInWay) {
	 						return new TilePosition(i, j);
	 					}
	 					if(buildingType.requiresPsi()){
	 						
	 					}
	 					// creep for Zerg
	 					if (buildingType.requiresCreep()) {
	 						boolean creepMissing = false;
	 						for (int k=i; k<=i+buildingType.tileWidth(); k++) {
	 							for (int l=j; l<=j+buildingType.tileHeight(); l++) {
	 								if (!game.hasCreep(k, l)) creepMissing = true;
	 								break;
	 							}
	 						}
	 						if (creepMissing) continue;
	 					}
	 				}
	 			}
	 		}
	 		maxDist += 1;
	 	}

	 	if (ret == null) game.printf("Unable to find suitable build position for "+buildingType.toString());
	 	return ret;
	 }
}
