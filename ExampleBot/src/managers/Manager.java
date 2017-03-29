package managers;

import bwapi.*;

public interface Manager {
	
	public void onStart();
	
	public void onFrame();
	
	public void onUnitComplete(Unit unit);
	
	public void onUnitCreate(Unit unit);
	
	public void onUnitDestroy(Unit unit);
	
}
