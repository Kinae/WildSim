package classes.stalker.buffs;

import combat.CombatLog;

import classes.Buff;

public class ShredBuff implements Buff {
	
	boolean isActive;
	int duration = 5000;
	int currDuration = 0;
	int stacks;
	int uptime = 0;
	CombatLog combatlog;
	
	public ShredBuff(CombatLog combatlog) {
		this.combatlog = combatlog;
	}
	

	@Override
	public String getName() {
		return "ShredBuff/Invigorate";
	}

	@Override
	public boolean isActive() {
		return isActive;
	}

	@Override
	public void apply() {
		stacks += 1;
		if (stacks > 2) stacks = 2;
		
		isActive = true;
		currDuration = duration;
		combatlog.addBuffEvent(this, true);
	}

	@Override
	public void remove() {
		isActive = false;
		currDuration = 0;
		stacks = 0;
		combatlog.addBuffEvent(this, false);
	}

	@Override
	public void setDuration(int time) {
		currDuration = time;
	}

	@Override
	public int getDuration() {
		return duration;
	}

	@Override
	public int durationLeft() {
		return currDuration;
	}

	@Override
	public void reduceCurrDuration() {
		if (currDuration > 0) uptime++;
		currDuration--;
		if (currDuration <= 0)  {
			currDuration = 0;
			remove();
		}
	}

	@Override
	public int getStacks() {
		return stacks;
	}


	@Override
	public int getUptime() {
		return uptime;
	}
	


}
