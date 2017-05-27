package ISMCTS;

import java.util.ArrayList;
import java.util.List;

public enum Tech {
	Apial_Sensors, Argus_Jewel, Argus_Talisman, Carrier_Capacity, 
	Gravitic_Boosters, Gravitic_Drive, Gravitic_Thrusters, Khaydarin_Amulet, 
	Khaydarin_Core, Leg_Enhancements, Protoss_Air_Armor, Protoss_Air_Weapons, 
	Protoss_Ground_Armor, Protoss_Ground_Weapons, Protoss_Plasma_Shields, 
	Reaver_Capacity, Scarab_Damage, Sensor_Array, Singularity_Charge;  
	
	public int level;
	
	
	public static List<Tech> getAllTech(){
		List<Tech> result = new ArrayList<Tech>();
		result.add(Apial_Sensors);
		result.add(Argus_Jewel);
		result.add(Argus_Talisman);
		result.add(Carrier_Capacity);
		result.add(Gravitic_Boosters);
		result.add(Gravitic_Drive);
		result.add(Gravitic_Thrusters);
		result.add(Khaydarin_Amulet);
		result.add(Khaydarin_Core);
		result.add(Leg_Enhancements);
		result.add(Protoss_Air_Weapons);
		result.add(Protoss_Air_Armor);
		result.add(Protoss_Ground_Armor);
		result.add(Protoss_Ground_Weapons);
		result.add(Protoss_Plasma_Shields);
		result.add(Reaver_Capacity);
		result.add(Scarab_Damage);
		result.add(Sensor_Array);
		result.add(Singularity_Charge);
		return result;
	}
}
