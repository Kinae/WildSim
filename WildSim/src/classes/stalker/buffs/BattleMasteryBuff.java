package classes.stalker.buffs;

import combat.CombatLog;

import classes.Buff;

public class BattleMasteryBuff implements Buff {
	
	boolean isActive;
	int duration = 5000;
	int uptime = 0;
	CombatLog combatlog;
	int durationLeft = 0;
	int cooldown = 15000;
	int currCooldown = 0;
	
	public BattleMasteryBuff(CombatLog combatlog) {
		this.combatlog = combatlog;
	}
	
	
	@Override
	public String getName() {
		return "BattleMasteryBuff";
	}

	@Override
	public boolean isActive() {
		return isActive;
	}

	@Override
	public void apply() {
		if (currCooldown <= 0) {
			combatlog.addBuffEvent(this, true);
			isActive = true;
			durationLeft = duration;
			currCooldown = cooldown;
		}
	}

	@Override
	public void remove() {
		combatlog.addBuffEvent(this, false);
		isActive = false;
		durationLeft = 0;
	}

	@Override
	public void setDuration(int time) {
		durationLeft = time;
	}

	@Override
	public int getDuration() {
		return duration;
	}

	@Override
	public int durationLeft() {
		return durationLeft;
	}

	@Override
	public void reduceCurrDuration() {
		if (durationLeft > 0) {
			uptime++;
			
			durationLeft--;
			if (durationLeft <= 0)  {
				durationLeft = 0;
				remove();
			}
		}
		
		if (currCooldown > 0) currCooldown--;
		
		

	}

	@Override
	public int getStacks() {
		return 1;
	}

	@Override
	public int getUptime() {
		return uptime;
	}
	
	
}
