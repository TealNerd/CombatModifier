package com.biggestnerd.combatmodifier;

import org.bukkit.plugin.java.JavaPlugin;

public class CombatModifier extends JavaPlugin {

	private static CombatModifier instance;
	
	private ConfigManager configMan;
	
	@Override
	public void onEnable() {
		instance = this;
		configMan = new ConfigManager();
		saveDefaultConfig();
		reloadConfig();
		configMan.load(getConfig());
	}
	
	public static ConfigManager getConfigManager() {
		return instance.configMan;
	}
}
