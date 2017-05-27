package actions;

import ISMCTS.Move;

public class Scout extends Action {

	public Scout(){
		move = Move.scout;
	}
	
	public Scout(int i){
		move = Move.scout;
		baseLocation = i;
	}
}
