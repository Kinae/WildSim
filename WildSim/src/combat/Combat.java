package combat;

import classes.Ability;
import classes.RaidBuff;
import classes.RaidDebuff;
import classes.RuneSet;
import classes.WildstarClass;
import classes.WildstarMob;
import classes.general.buffs.*;
import classes.general.debuffs.*;
import classes.general.runesets.*;
import classes.stalker.Stalker;

/**
 * combat class handles gcd, dmg calculation, max combat duration.
 */
public class Combat implements Runnable {
	
	boolean combatlogdmg = false;
	
	int globalcd = 0;
	int maxtime = 36000000;
//	int maxtime = 120000;
	int currtime = 0;
	float dmgoverall;
	float currdmg;
	long startTime;
	
	CombatLog combatlog;
	
	WildstarMob target;
	WildstarClass wildclass;
	Ability nextAbility;
	Ability nextNonGCDAbility;
	
	//raidbuffs/raiddebuffs
	RaidBuff[] raidbuffs;
	RaidDebuff punish;
	RaidBuff powerlinkt4;
	RaidBuff powerlink;
	RaidBuff empowering;
	RaidBuff empoweringt4;
	RaidBuff empoweringaura;
	RaidBuff voidpact;
	RaidBuff surgical;
	RaidBuff pyrokinetict4;
	RaidBuff dualfire;
	RaidBuff bloodthirst;
	
	//statreducing raiddebuffs
	RaidDebuff[] raiddebuffs;
	RaidDebuff armorreduction;
	RaidDebuff reduceallresist;
	RaidDebuff reducephysresist;
	RaidDebuff reducemagresist;
	RaidDebuff reducetechresist;
	RaidDebuff reducedeflect;
	RaidDebuff weaponspecdebuff;
	RaidDebuff technophiledebuff;
	RaidDebuff spellweaverdebuff;
	
	//runesets
	RuneSet[] runesets;
	RuneSet assassin;
	RuneSet suckerpunch;
	RuneSet weaponspec;
	RuneSet specter;
	RuneSet unfairadvantage;
	
	public Combat() {
		target = new WildstarMob();
		wildclass = new Stalker(3929.0f, 1060.5f, 0.2147f, 2.1154f, 0.1710f, 0.3f, 0.0605f, 0.1778f, 0.15f, 0.06f, this);
		
		combatlog = new CombatLog(this, wildclass);
		
		punish = new PhysicalDamageTaken(true, 0.85f);
		powerlinkt4 = new PowerLinkT4(0.025f, false, 1.0f);
		powerlink = new PowerLink(0.14f, false, 1.0f);
		empowering = new EmpoweringProbes(0.1f, false, 1.0f);
		surgical = new Surgical(0.06f, false, 1.0f);
		
		//partice ejector 20%
		armorreduction = new ArmorReduction(0.20f, false, 1.0f);
		//smackdown + armorshred multip.
		reduceallresist = new ReduceAllResist(0.1774f, false, 1.0f);
		//frenzy (stacks multip.)
		reducephysresist = new ReducePhysResist(0.1670f, false, 1.0f);
		//haunt/arcance missiles/withering magic multip.
		reducemagresist = new ReduceMagResist(0.7256f, false, 1.0f);
		//bio shell t0
		reducetechresist = new ReduceTechResist(0.07f, false, 1.0f);
		//tk storm
		reducedeflect = new ReduceDeflectChance(0.08f, false, 1.0f);
		//weapon spec 12/12
		weaponspecdebuff = new WeaponSpecDebuff(480, false, 1.0f);
		//spellweaver 12/12
		spellweaverdebuff = new SpellweaverDebuff(480, false, 1.0f);
		//technophile 12/12
		technophiledebuff = new TechnophileDebuff(480, false, 1.0f);
		
		//class stat changing raidbuffs
		empoweringt4 = new EmpoweringProbesT4(0.03f, false, 1.0f);
		empoweringaura = new EmpoweringAura(0.06f, false, 1.0f);
		voidpact = new VoidPact(0.09f, false, 1.0f);
		pyrokinetict4 = new PyrokineticFlameT4(0.05f, false, 1.0f);
		dualfire = new DualFireT8(0.036f, false, 1.0f);
		bloodthirst = new BloodThirstT4(0.07f, false, 1.0f);
		
		fillRaidBuffArray();
		fillRaidDebuffArray();
		
		//runesets
		assassin = new Assassin(false, 12);
		suckerpunch = new SuckerPunch(false, 7);
		weaponspec = new WeaponSpecialist(false, 12);
		specter = new Specter(false, 12);
		unfairadvantage = new UnfairAdvantageSet(false, 15);
		
		fillRuneSetArray();
		
	}
	
	//starts combat for defined maxtime
	@Override
	public void run() {
		combatlog.resetLog();
		startTime = System.nanoTime();
		fillRuneSetArray();
		fillRaidBuffArray();
		fillRaidDebuffArray();
		wildclass.setRaidBuffs(raidbuffs);
		wildclass.setRuneSets(runesets);
		wildclass.lastCheck();
		
		dmgoverall = 0;
				
		while (currtime < maxtime) {			
			if (globalcd == 0) {
				
				nextAbility = wildclass.getNextGCDAbility();
				
				if (nextAbility != null) {
					currdmg = calculateDamage(wildclass, nextAbility, target);
					dmgoverall += currdmg;
					globalcd = nextAbility.getGCD();
				}
			} 
			
			nextNonGCDAbility = wildclass.getNextNonGCDAbility();
			
			while (nextNonGCDAbility != null) {
				
				currdmg = calculateDamage(wildclass, nextNonGCDAbility, target);
				dmgoverall += currdmg;
				
				if (globalcd < nextNonGCDAbility.getGCD()) {
					globalcd = nextNonGCDAbility.getGCD();
				}
				
				nextNonGCDAbility = wildclass.getNextNonGCDAbility();
				
			}
			
			nextTick();
			
		}
				
		if (combatlogdmg) {
			long endTime = System.nanoTime();
			combatlog.addRunTime(Double.toString((endTime - startTime)/1000));
		}		
		
		currtime = 0;

	}

	private void nextTick() {
		currtime++;
		if (globalcd > 0) {
			globalcd--;
		}
		wildclass.afterTick(currtime);
	}
	
	
	
	
	//calculates dmg/crits/deflects from given actor with given ability to given target
	private float calculateDamage(WildstarClass wildclass, Ability ability, WildstarMob target) {

		checkRaidDebuffs();
		
		float tooltipdmg = ability.calculateTooltipDmg(wildclass.getAP(), wildclass.getSP());
		
		float actualdmg = (tooltipdmg - ((tooltipdmg * (target.getMitigation(ability.getType()) * (1-(wildclass.getArmorPierce()+ability.getArmorPierce()))))));
	
		actualdmg = checkRaidBuffs(actualdmg, ability);
		
		//DROP 6 - vigor
		actualdmg *= (1 + wildclass.getVigor());
		
		//class specific flat dmg buffs (AMPs etc.)
		if (wildclass.getFlatDamageBuff() != 0) {
			actualdmg *= (1 + wildclass.getFlatDamageBuff());
		}
	 		
		//1 roll for both
		double roll = Math.random();
		
		//deflect possible
		if (target.getDeflectchance() - wildclass.getStrikethrough() > 0) {
			//check for deflect
			if (ability.canDeflect() && roll < (double)(target.getDeflectchance() - wildclass.getStrikethrough())) {
				if (combatlogdmg) {
					combatlog.addAbilityHit(ability, actualdmg, false, true);
				}
				wildclass.afterHit(ability, false, true, false, 0);
				return 0;
			}
			//check for crit
			if (ability.canCrit() && (target.getDeflectchance() - wildclass.getStrikethrough()) < roll && roll < ((target.getDeflectchance() - wildclass.getStrikethrough()) + wildclass.getCrit())) {
				actualdmg *= wildclass.getCritSev();
				if (combatlogdmg) {
					combatlog.addAbilityHit(ability, actualdmg, true, false);
				}
				wildclass.afterHit(ability, true, false, false, actualdmg);
			//otherwise hit
			} else {
				if (combatlogdmg) {
					combatlog.addAbilityHit(ability, actualdmg, false, false);
				}
				wildclass.afterHit(ability, false, false, false, actualdmg);
			}
		//deflect impossible
		} else {
			//check for crit
			if (ability.canCrit() && roll < wildclass.getCrit()) {
				actualdmg *= wildclass.getCritSev();
				if (combatlogdmg) {
					combatlog.addAbilityHit(ability, actualdmg, true, false);
				}
				wildclass.afterHit(ability, true, false, false, actualdmg);
			//otherwise hit
			} else {
				if (combatlogdmg) {
					combatlog.addAbilityHit(ability, actualdmg, false, false);
				}
				wildclass.afterHit(ability, false, false, false, actualdmg);
			}
		}
		
		//TODO DROP 6 - check for multihit, specific roll. not possible at deflect (?)
		double multiroll = Math.random();
		
		if (multiroll < wildclass.getMultiHit() && ability.canMultiHit()) {
			float multihitdmg;
			double multicritroll = Math.random();
			//check for crit
			if (ability.canCrit() && multicritroll < wildclass.getCrit()) {
				multihitdmg = (actualdmg * wildclass.getMultiHitSev());
				actualdmg += multihitdmg;
				if (combatlogdmg) {
					combatlog.addMultiHit(ability, multihitdmg, true);
				}
				//TODO multihit doesn't proc anything on it's own (?)
				wildclass.afterHit(ability, true, false, true, multihitdmg);
			//otherwise hit
			} else {
				multihitdmg = (actualdmg * wildclass.getMultiHitSev());
				actualdmg += multihitdmg;
				if (combatlogdmg) {
					combatlog.addMultiHit(ability, multihitdmg, false);
				}
				//TODO multihit doesn't proc anything on it's own (?)
				wildclass.afterHit(ability, false, false, true, multihitdmg);
			}
		}
		
		return actualdmg;
		
	}
	
	

	private float checkRaidBuffs(float actualdmg, Ability ability) {
		
		double chance = Math.random();
		float buffeddmg = actualdmg;
		
		if (punish.isActive() && ability.getType() == 1 && punish.getUptime() >= chance) {
			buffeddmg *= (1+punish.getAmount());
		}
		if (powerlinkt4.isActive() && powerlinkt4.getUptime() >= chance) {
			buffeddmg *= (1+powerlinkt4.getAmount());
		}
		if (powerlink.isActive() && powerlink.getUptime() >= chance) {
			buffeddmg *= (1+powerlink.getAmount());
		}
		if (empowering.isActive() && empowering.getUptime() >= chance) {
			buffeddmg *= (1+empowering.getAmount());
		}
		if (surgical.isActive() && surgical.getUptime() >= chance) {
			buffeddmg *= (1+surgical.getAmount());
		}
		
		return buffeddmg;
	}

	/**
	 * applies all raiddebuffs (with their respective uptime)
	 */
	public void checkRaidDebuffs() {
		target.resetStats();
		
		double chance = Math.random();
		
		if (weaponspecdebuff.isActive() && weaponspecdebuff.getUptime() >= chance) {
			target.setPhysRes(target.getPhysRes() - weaponspecdebuff.getAmount());
		}
		if (technophiledebuff.isActive() && technophiledebuff.getUptime() >= chance) {
			target.setTechRes(target.getTechRes() - technophiledebuff.getAmount());
		}
		if (spellweaverdebuff.isActive() && technophiledebuff.getUptime() >= chance) {
			target.setMagRes(target.getMagRes() - spellweaverdebuff.getAmount());
		}
		
		if (armorreduction.isActive() && armorreduction.getUptime() >= chance) {
			target.setArmor(target.getArmor() - (target.getArmor() * armorreduction.getAmount()));
		}
		if (reduceallresist.isActive() && reduceallresist.getUptime() >= chance) {
			target.setMagRes(target.getMagRes() - (target.getMagRes() * reduceallresist.getAmount()));
			target.setPhysRes(target.getPhysRes() - (target.getPhysRes() * reduceallresist.getAmount()));
			target.setTechRes(target.getTechRes() - (target.getTechRes() * reduceallresist.getAmount()));
		}
		if (reducephysresist.isActive() && reducephysresist.getUptime() >= chance) {
			target.setPhysRes(target.getPhysRes() - (target.getPhysRes() * reducephysresist.getAmount()));
		}
		if (reducemagresist.isActive() && reducemagresist.getUptime() >= chance) {
			target.setMagRes(target.getMagRes() - (target.getMagRes() * reducemagresist.getAmount()));
		}
		if (reducetechresist.isActive() && reducetechresist.getUptime() >= chance) {
			target.setTechRes(target.getTechRes() - (target.getTechRes() * reducetechresist.getAmount()));
		}
		if (reducedeflect.isActive() && reducedeflect.getUptime() >= chance) {
			if (target.getDeflectchance() - reducedeflect.getAmount() < 0) {
				target.setDeflectchance(0);
			} else {
				target.setDeflectchance(target.getDeflectchance() - (reducedeflect.getAmount()));
			}
			
		}
		
	}
	
	private void fillRaidBuffArray() {
		int amount = 0;
		if (empoweringt4.isActive()) amount++;
		if (empoweringaura.isActive()) amount++;
		if (voidpact.isActive()) amount++;
		if (pyrokinetict4.isActive()) amount++;
		if (dualfire.isActive()) amount++;
		if (bloodthirst.isActive()) amount++;
		raidbuffs = new RaidBuff[amount];
		amount = 0;
		if (empoweringt4.isActive()) {
			raidbuffs[amount] = empoweringt4;
			amount++;
		}
		if (empoweringaura.isActive()) {
			raidbuffs[amount] = empoweringaura;
			amount++;
		}
		if (voidpact.isActive()) {
			raidbuffs[amount] = voidpact;
			amount++;
		}
		if (pyrokinetict4.isActive()) {
			raidbuffs[amount] = pyrokinetict4;
			amount++;
		}
		if (dualfire.isActive()) {
			raidbuffs[amount] = dualfire;
			amount++;
		}
		if (bloodthirst.isActive()) {
			raidbuffs[amount] = bloodthirst;
			amount++;
		}		
	}
	
	private void fillRaidDebuffArray() {
		int amount = 0;
		if (punish.isActive()) amount++;
		if (armorreduction.isActive()) amount++;
		if (reduceallresist.isActive()) amount++;
		if (reducephysresist.isActive()) amount++;
		if (reducemagresist.isActive()) amount++;
		if (reducetechresist.isActive()) amount++;
		if (reducedeflect.isActive()) amount++;
		if (weaponspecdebuff.isActive()) amount++;
		if (technophiledebuff.isActive()) amount++;
		if (spellweaverdebuff.isActive()) amount++;
		raiddebuffs = new RaidDebuff[amount];
		amount = 0;
		if (punish.isActive()) {
			raiddebuffs[amount] = punish;
			amount++;
		}
		if (armorreduction.isActive()) {
			raiddebuffs[amount] = armorreduction;
			amount++;
		}
		if (reduceallresist.isActive()) {
			raiddebuffs[amount] = reduceallresist;
			amount++;
		}
		if (reducephysresist.isActive()) {
			raiddebuffs[amount] = reducephysresist;
			amount++;
		}
		if (reducemagresist.isActive()) {
			raiddebuffs[amount] = reducemagresist;
			amount++;
		}
		if (reducetechresist.isActive()) {
			raiddebuffs[amount] = reducetechresist;
			amount++;
		}
		if (reducedeflect.isActive()) {
			raiddebuffs[amount] = reducedeflect;
			amount++;
		}		
		if (weaponspecdebuff.isActive()) {
			raiddebuffs[amount] = weaponspecdebuff;
			amount++;
		}
		if (technophiledebuff.isActive()) {
			raiddebuffs[amount] = technophiledebuff;
			amount++;
		}
		if (spellweaverdebuff.isActive()) {
			raiddebuffs[amount] = spellweaverdebuff;
			amount++;
		}	
	}
	
	
	private void fillRuneSetArray() {
		int amount = 0;
		if (assassin.isActive()) amount++;
		if (suckerpunch.isActive()) amount++;
		if (weaponspec.isActive()) amount++;
		if (specter.isActive()) amount++;
		if (unfairadvantage.isActive()) amount++;
		runesets = new RuneSet[amount];
		amount = 0;
		if (assassin.isActive()) {
			runesets[amount] = assassin;
			amount++;
		}
		if (weaponspec.isActive()) {
			runesets[amount] = weaponspec;
			amount++;
		}
		if (specter.isActive()) {
			runesets[amount] = specter;
			amount++;
		}
		if (unfairadvantage.isActive()) {
			runesets[amount] = unfairadvantage;
			amount++;
		}
		if (suckerpunch.isActive()) {
			runesets[amount] = suckerpunch;
			amount++;
		}
	}


	
	public int getMaxtime() {
		return maxtime;
	}
	//set maxtime for combat
	public void setMaxtime(int maxtime) {
		this.maxtime = maxtime;
	}
	
	public WildstarMob getTarget() {
		return target;
	}
	
	public WildstarClass getPlayer() {
		return wildclass;
	}
	
	public int getCurrTime() {
		return currtime;
	}
	
	public float getDmgOverall() {
		return dmgoverall;
	}
	
	
	//raidbuffs/raiddebuffs getter
	public RaidDebuff getPunish() {
		return punish;
	}
	public RaidDebuff getArmorRed() {
		return armorreduction;
	}
	public RaidDebuff getReduceAllRes() {
		return reduceallresist;
	}
	public RaidDebuff getReducePhysRes() {
		return reducephysresist;
	}
	public RaidDebuff getReduceMagRes() {
		return reducemagresist;
	}
	public RaidDebuff getReduceTechRes() {
		return reducetechresist;
	}
	public RaidDebuff getReduceDeflect() {
		return reducedeflect;
	}
	public RaidDebuff getWeaponSpecDebuff() {
		return weaponspecdebuff;
	}
	public RaidDebuff getTechnophileDebuff() {
		return technophiledebuff;
	}
	public RaidDebuff getSpellweaverDebuff() {
		return spellweaverdebuff;
	}
	public RaidDebuff[] getRaidDebuffs() {
		return raiddebuffs;
	}
	
	public RaidBuff getPowerLinkT4() {
		return powerlinkt4;
	}
	public RaidBuff getPowerLink() {
		return powerlink;
	}
	public RaidBuff getEmpowering() {
		return empowering;
	}
	public RaidBuff getEmpoweringT4() {
		return empoweringt4;
	}
	public RaidBuff getEmpoweringAura() {
		return empoweringaura;
	}
	public RaidBuff getVoidPact() {
		return voidpact;
	}
	public RaidBuff getSurgical() {
		return surgical;
	}
	public RaidBuff getPyroKineticT4() {
		return pyrokinetict4;
	}
	public RaidBuff getDualFire() {
		return dualfire;
	}
	public RaidBuff getBloodThirst() {
		return bloodthirst;
	}
	public RaidBuff[] getRaidBuffs() {
		return raidbuffs;
	}
	
	//runesets getter
	public RuneSet getAssassin() {
		return assassin;
	}
	public RuneSet getSuckerPunch() {
		return suckerpunch;
	}
	public RuneSet getWeaponSpec() {
		return weaponspec;
	}
	public RuneSet getSpecter() {
		return specter;
	}
	public RuneSet getUnfairAdvantage() {
		return unfairadvantage;
	}
	public RuneSet[] getRuneSets() {
		return runesets;
	}
	
	public CombatLog getCombatLog() {
		return combatlog;
	}

	public void setCombatlogactive(boolean combatlog) {
		combatlogdmg = combatlog;
	}
	
	public boolean getCombatlogactive() {
		return combatlogdmg;
	}
	
}

