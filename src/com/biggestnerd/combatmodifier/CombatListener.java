package com.biggestnerd.combatmodifier;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;
import java.util.UUID;

import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.PotionSplashEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class CombatListener implements Listener {

	private ConfigManager config;
	private Random rng = new Random();
	private Map<UUID, Integer> bowMap = new TreeMap<UUID, Integer>();
	
	public CombatListener() {
		config = CombatModifier.getConfigManager();
	}
	
	@EventHandler
	public void onCombat(EntityDamageByEntityEvent event) {
		if(!(event.getEntity() instanceof LivingEntity)) return;
		DamageCause cause = event.getCause();
		if(event.getDamager() instanceof Arrow) {
			handleArrowDamage(event);
			handleProjectileSlow(event);
		}
		if(event.getDamager() instanceof Player) {
			handleStrengthBuff(event);
		}
		if(event.getEntityType() == EntityType.PLAYER && (cause == DamageCause.ENTITY_ATTACK || cause == DamageCause.PROJECTILE)) {
			scaleProtectionEnchant(event);
		}
	}

	@EventHandler
	public void onPlayerEat(PlayerItemConsumeEvent event) {
		if(event.getItem().getType() == Material.GOLDEN_APPLE) {
			short data = event.getItem().getDurability();
			switch(data) {
			case 0: handleConsumeGoldenApple(event); break;
			case 1: handleConsumeEnchantedApple(event); break;
			}
		}
		
	}
	
	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		AttributeInstance ai = event.getPlayer().getAttribute(Attribute.GENERIC_ATTACK_SPEED);
		ai.setBaseValue(config.getAttackSpeed());
		event.getPlayer().setMaxHealth(config.getPlayerMaxHealth());
	}
	
	@EventHandler
	public void onPotionSplash(PotionSplashEvent event) {
		for(PotionEffect effect : event.getEntity().getEffects()) {
			if(effect.getType() == PotionEffectType.HEAL) {
				buffHealthPotions(event);
			}
		}
	}
	
	@EventHandler
	public void onEntityShootBow(EntityShootBowEvent event) {
		if(!(event.getEntity() instanceof Player)) return;
		int enchantLevel = 0;
		ItemStack bow = event.getBow();
		Map<Enchantment, Integer> enchants = bow.getEnchantments();
		for(Enchantment ench : enchants.keySet()) {
			int tmp = 0;
			if(ench == Enchantment.KNOCKBACK || ench == Enchantment.ARROW_KNOCKBACK) {
				tmp = enchants.get(ench) * 2;
			} else if (ench == Enchantment.ARROW_DAMAGE) {
				tmp = enchants.get(ench);
			}
			if(tmp > enchantLevel) enchantLevel = tmp;
		}
		UUID id = ((Player) event.getEntity()).getUniqueId();
		bowMap.put(id, enchantLevel);
	}
	
	private void handleArrowDamage(EntityDamageByEntityEvent event) {
		Arrow arrow = (Arrow) event.getDamager();
		double damage = event.getDamage() * config.getBowBuff();
		int power = 0;
		if(arrow.hasMetadata("power")) {
			power = arrow.getMetadata("power").get(0).asInt();
		}
		damage *= Math.pow(1.25, power - 5);
		event.setDamage(damage);
	}
	
	private void handleProjectileSlow(EntityDamageByEntityEvent event) {
		if(!(event.getEntity() instanceof Player)) return;
		int rate = config.getProjectileSlowChance();
		if(rate < 0 || rate < 100) return;
		int scaleChance = 0;
		try {
			Class<?> damagerClass = event.getDamager().getClass();
			if(damagerClass.getName().endsWith(".CraftArrow")) {
				Method getShooter = damagerClass.getMethod("getShooter");
				Object result = getShooter.invoke(event.getDamager());
				if(!(result instanceof Player)) {
					return;
				}
				UUID player = ((Player)result).getUniqueId();
				scaleChance = bowMap.get(player);
			}
		} catch (Exception ex) {}
		rate += scaleChance + 5;
		int chance = rng.nextInt(100);
		if(chance < rate) {
			int ticks = config.getProjectileSlowTicks();
			Player player = (Player)event.getEntity();
			player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, ticks, 1, false));
		}
	}
	
	private void handleStrengthBuff(EntityDamageByEntityEvent event) {
		Player player = (Player)event.getDamager();
		if(player.hasPotionEffect(PotionEffectType.INCREASE_DAMAGE)) {
			for(PotionEffect effect : player.getActivePotionEffects()) {
				if(effect.getType() == PotionEffectType.INCREASE_DAMAGE) {
					int potionLevel = effect.getAmplifier() + 1;
					double unbuffed = event.getDamage() / (1.3 * potionLevel + 1);
					double newDamage = unbuffed + (potionLevel * config.getStrengthModifier());
					event.setDamage(newDamage);
					break;
				}
			}
		}
	}
	
	private void scaleProtectionEnchant(EntityDamageByEntityEvent event) {
		double damage = event.getDamage();
		if(damage <= 0.0000001D) return;
		int enchantLevel = 0;
		PlayerInventory inv = ((Player)event.getEntity()).getInventory();
		for(ItemStack armor : inv.getArmorContents()) {
			if(armor == null) continue;
			enchantLevel += armor.getEnchantmentLevel(Enchantment.PROTECTION_ENVIRONMENTAL);
		}
		int protScale = config.getProtectionScale();
		int damageAdjustment = 0;
		if(enchantLevel >= 3 && enchantLevel <= 6) damageAdjustment = rng.nextInt(3);
		else if(enchantLevel >= 7 && enchantLevel <= 10) damageAdjustment = rng.nextInt(4);
		else if(enchantLevel >= 11 && enchantLevel <= 14) damageAdjustment = rng.nextInt(3) + 1;
		else if(enchantLevel >= 15) damageAdjustment = rng.nextInt(3) + 2;
		damageAdjustment += protScale;
		damage = Math.max(damage - (double)damageAdjustment, 0.0D);
		event.setDamage(damage);
	}
	
	private void handleConsumeGoldenApple(PlayerItemConsumeEvent event) {
		if(config.isEatGoldenApples()) {
			if(config.getGoldenAppleEffects().size() != 0) {
				event.setCancelled(true);
				event.getItem().setAmount(event.getItem().getAmount() - 1);
				event.getPlayer().addPotionEffects(config.getGoldenAppleEffects());
			}
		} else {
			event.setCancelled(true);
		}
	}
	
	private void handleConsumeEnchantedApple(PlayerItemConsumeEvent event) {
		if(config.isEatEnchantedApples()) {
			if(config.getEnchantedAppleEffects().size() != 0) {
				event.setCancelled(true);
				event.getItem().setAmount(event.getItem().getAmount() - 1);
				event.getPlayer().addPotionEffects(config.getEnchantedAppleEffects());
			}
		} else {
			event.setCancelled(true);
		}
	}
	
	private void buffHealthPotions(PotionSplashEvent event) {
		for(LivingEntity entity : event.getAffectedEntities()) {
			if(entity instanceof Player) {
				if(((Damageable)entity).getHealth() > 0d) {
					double newHealth = Math.min(
					((Damageable)entity).getHealth() + config.getHealthBuff(), 
					((Damageable)entity).getMaxHealth());
					entity.setHealth(newHealth);
				}
			}
		}
	}
}
