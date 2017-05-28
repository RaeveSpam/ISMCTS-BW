package ISMCTS;

import java.util.ArrayList;
import java.util.List;

public enum Entity {
	Arbiter, Arbiter_Tribunal, Archon, Assimilator, Carrier,
	Citadel_of_Adun, Corsair, Cybernetics_Core, Dark_Archon,
	Dark_Templar, Dragoon, Fleet_Beacon, Forge, Gateway,
	High_Templar, Interceptor, Nexus, Observatory, Observer,
	Photon_Cannon, Probe, Pylon, Reaver, Robotics_Facility,
	Robotics_Support_Bay, Scarab, Scout, Shield_Battery, 
	Shuttle, Stargate, Templar_Archives, Zealot;
	
	
	public static List<Entity> getEntities(){
		List<Entity> result = new ArrayList<Entity>();
		result.add(Arbiter);
		result.add(Arbiter_Tribunal);
		//result.add(Archon);
		//result.add(Assimilator);
		result.add(Carrier);
		result.add(Citadel_of_Adun);
		result.add(Cybernetics_Core);
		result.add(Dark_Templar);
		result.add(Fleet_Beacon);
		result.add(Forge);
		result.add(Gateway);
		//result.add(High_Templar);
		//result.add(Interceptor);
		//result.add(Nexus);
		result.add(Observatory);
		result.add(Observer);
		result.add(Photon_Cannon);
		//result.add(Probe);
		//result.add(Pylon);
		result.add(Reaver);
		result.add(Robotics_Facility);
		result.add(Robotics_Support_Bay);
		//result.add(Scarab);
		result.add(Scout);
		//result.add(Shield_Battery);
		//result.add(Shuttle);
		result.add(Stargate);
		result.add(Templar_Archives);
		result.add(Zealot);
		return result;
	}
}
