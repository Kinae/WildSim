package classes.general.runesets;

import classes.Ability;
import classes.WildstarClass;

public class SuckerPunchHit implements Ability {

	int amountHits = 0;
	int amountCrits = 0;
	int amountDeflects = 0;
	
	float amountHitDamage = 0;
	float amountCritDamage = 0;
	
	boolean isActive;
	boolean triggered;
	int tier;
	
	public SuckerPunchHit(int level, boolean active) {
		this.tier = level;
		isActive = active;
	}

	public void setTriggered(boolean triggered) {
		this.triggered = triggered;
	}
	
	public boolean isTriggered() {
		return triggered;
	}
	
	@Override
	public String getName() {
		return "SuckerHit";
	}

	@Override
	public int getGCD() {
		return 0;
	}

	@Override
	public int getCost() {
		return 0;
	}

	@Override
	public float calculateTooltipDmg(float ap, float sp) {
		if (tier >= 15) {
			return 127;
		} else if (tier >= 11) {
			return 105;
		} else if (tier >= 6) {
			return 77;
		} else {
			return 0;
		}
	}

	@Override
	public int getCurrentCD() {
		return 0;
	}

	@Override
	public void setCurrentCD(int cd) {
		return;
	}

	@Override
	public void redCDC() {
		return;
	}

	@Override
	public void setCooldown(int cd) {
		return;		
	}

	@Override
	public int getCooldown() {
		return 0;
	}

	@Override
	public boolean canCrit() {
		return false;
	}

	@Override
	public boolean canDeflect() {
		return false;
	}

	@Override
	public void addHit(float dmg) {
		amountHits++;
		amountHitDamage += dmg;
	}

	@Override
	public void addCrit(float dmg) {
		amountCrits++;
		amountCritDamage += dmg;
	}

	@Override
	public void addDeflect() {
		amountDeflects++;
	}

	@Override
	public int amountDeflects() {
		return amountDeflects;
	}

	@Override
	public int amountHits() {
		return amountHits;
	}

	@Override
	public float amountHitDamage() {
		return amountHitDamage;
	}

	@Override
	public int amountCrits() {
		return amountCrits;
	}

	@Override
	public float amountCritDamage() {
		return amountCritDamage;
	}


	@Override
	public int getTier() {
		return tier;
	}


	@Override
	public boolean isReady(WildstarClass actor) {
		return true;
	}


	@Override
	public void afterHit(WildstarClass actor) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void beforeHit(WildstarClass actor) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public float getArmorPierce() {
		// TODO Auto-generated method stub
		return 0;
	}
	

	@Override
	public int getType() {
		return 1;
	}


	@Override
	public void setTier(int tier) {
		this.tier = tier;
	}


	@Override
	public void setActive(boolean active) {
		isActive = active;
	}


	@Override
	public boolean isActive() {
		return isActive;
	}


	@Override
	public int compareTo(Ability arg0) {
		if (arg0.amountHitDamage() + arg0.amountCritDamage() > amountHitDamage + amountCritDamage) {
			return -1;
		} else if (arg0.amountHitDamage() + arg0.amountCritDamage() == amountHitDamage + amountCritDamage){
			return 0;
		} else {
			return 1;
		}
	}


	@Override
	public void resetValues() {
		amountHits = 0;
		amountCrits = 0;
		amountDeflects = 0;
		
		amountHitDamage = 0;
		amountCritDamage = 0;
	}
	
	@Override
	public int getPrio() {
		// TODO Auto-generated method stub
		return 0;
	}


	@Override
	public void setPrio(int prio) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean canMultiHit() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void addMultiHitCrit(float damage) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void addMultiHit(float damage) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public int amountMultiHits() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public float amountMultiHitDamage() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int amountMultiHitCrits() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public float amountMultiHitCritDamage() {
		// TODO Auto-generated method stub
		return 0;
	}

}
