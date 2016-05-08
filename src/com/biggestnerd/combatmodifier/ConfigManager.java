package com.biggestnerd.combatmodifier;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class ConfigManager {

	private double bowBuff;
	private boolean eatEnchantedApples;
	private boolean eatGoldenApples;
	private List<PotionEffect> enchantedAppleEffects;
	private List<PotionEffect> goldenAppleEffects;
	private double attackSpeed;
	private double playerMaxHealth;
	private int strengthModifier;
	private double healthBuff;
	private int projectileSlowChance;
	private int projectileSlowTicks;
	private int protectionScale;
	
	public void load(FileConfiguration config) {
		bowBuff = config.getDouble("bowBuff", 1.0);
		eatEnchantedApples = config.getBoolean("eatEnchantedApples", true);
		eatGoldenApples = config.getBoolean("eatGoldenApples", true);
		enchantedAppleEffects = new ArrayList<PotionEffect>();
		if(config.contains("enchantedAppleEffects")) {
			for(String line : config.getStringList("enchantedAppleEffects")) {
				String[] parts = line.split(":");
				try {
					PotionEffectType type = PotionEffectType.getByName(parts[0]);
					int ticks = Integer.parseInt(parts[1]);
					int power = Integer.parseInt(parts[2]);
					enchantedAppleEffects.add(new PotionEffect(type, ticks, power));
				} catch (Exception ex) {
					//either an index out of bounds or number parse error
					continue;
				}
			}
		}
		goldenAppleEffects = new ArrayList<PotionEffect>();
		if(config.contains("goldenAppleEffects")) {
			for(String line : config.getStringList("goldenAppleEffects")) {
				String[] parts = line.split(":");
				try {
					PotionEffectType type = PotionEffectType.getByName(parts[0]);
					int ticks = Integer.parseInt(parts[1]);
					int power = Integer.parseInt(parts[2]);
					goldenAppleEffects.add(new PotionEffect(type, ticks, power));
				} catch (Exception ex) {
					//either an index out of bounds or number parse error
					continue;
				}
			}
		}
		attackSpeed = config.getDouble("attackSpeed", 1.0);
		playerMaxHealth = config.getDouble("playerMaxHealth", 20.0);
		strengthModifier = config.getInt("strengthModifier", 3);
		healthBuff = config.getDouble("healthBuff", 4.0);
		projectileSlowChance = config.getInt("projectileSlowChance", 30);
		projectileSlowTicks = config.getInt("projectileSlowTicks");
		protectionScale = config.getInt("protectionScale", 0);
	}

	public double getBowBuff() {
		return bowBuff;
	}

	public boolean isEatEnchantedApples() {
		return eatEnchantedApples;
	}

	public boolean isEatGoldenApples() {
		return eatGoldenApples;
	}

	public List<PotionEffect> getEnchantedAppleEffects() {
		return enchantedAppleEffects;
	}

	public List<PotionEffect> getGoldenAppleEffects() {
		return goldenAppleEffects;
	}
	
	public double getAttackSpeed() {
		return attackSpeed;
	}
	
	public double getPlayerMaxHealth() {
		return playerMaxHealth;
	}
	
	public int getStrengthModifier() {
		return strengthModifier;
	}
	
	public double getHealthBuff() {
		return healthBuff;
	}
	
	public int getProjectileSlowChance() {
		return projectileSlowChance;
	}
	
	public int getProjectileSlowTicks() {
		return projectileSlowTicks;
	}
	
	public int getProtectionScale() {
		return protectionScale;
	}
}
