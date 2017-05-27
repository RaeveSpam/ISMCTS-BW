package actions;

import ISMCTS.Move;

public class Attack extends Action {

	public Attack(int i){
		baseLocation = i;
		move = Move.Attack;
	}
}
