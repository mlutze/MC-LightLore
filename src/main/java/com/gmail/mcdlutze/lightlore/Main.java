package com.gmail.mcdlutze.lightlore;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import com.google.common.collect.Lists;

public class Main extends JavaPlugin {

	public static final char SECTION = '\u00A7';
	private static final String[] HELP_MESSAGE = { "/lore name {name}", "/lore add {line}", "/lore remove {line}",
			"/lore change {line number} {line}", "/lore insert {line number} {line}", "/lore help" };
	private static final String[] commands = { "name", "add", "remove", "change", "insert", "help" };

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (command.getName().equalsIgnoreCase("lore")) {
			return subCommand(sender, args);
		}
		return false;
	}

	private boolean subCommand(CommandSender sender, String[] args) {
		if (!(sender instanceof Player)) {
			sender.sendMessage("Only a player may use this command.");
			return false;
		}
		Player player = (Player) sender;

		String subCommand = args.length > 0 ? args[0] : "help";

		try {
			switch (subCommand) {
			case ("name"):
				setItemName(player, encode(joinArgs(1, args)));
				break;
			case ("add"):
				addItemLore(player, encode(joinArgs(1, args)));
				break;
			case ("change"):
				changeItemLore(player, Integer.parseInt(args[1]) - 1, encode(joinArgs(2, args)));
				break;
			case ("remove"):
				removeItemLore(player, Integer.parseInt(args[1]) - 1);
				break;
			case ("insert"):
				insertItemLore(player, Integer.parseInt(args[1]) - 1, encode(joinArgs(2, args)));
				break;
			case ("help"):
				for (String line : HELP_MESSAGE) {
					player.sendMessage(line);
				}
				break;
			default:
				return false;
			}
		} catch (NumberFormatException e) {
			return false;
		} catch (IndexOutOfBoundsException e) {
			return false;
		}

		return true;
	}

	private void setItemName(Player holder, String name) {
		ItemStack item = holder.getItemInHand();
		ItemMeta meta = item.getItemMeta();
		meta.setDisplayName(name);
		item.setItemMeta(meta);
	}

	private void addItemLore(Player holder, String line) {
		ItemStack item = holder.getItemInHand();
		ItemMeta meta = item.getItemMeta();
		List<String> lore;
		if (meta.hasLore()) {
			lore = meta.getLore();
		} else {
			lore = new ArrayList<>(1);
		}
		lore.add(line);
		meta.setLore(lore);
		item.setItemMeta(meta);
	}

	private void changeItemLore(Player holder, int lineNumber, String line) {
		ItemStack item = holder.getItemInHand();
		ItemMeta meta = item.getItemMeta();

		List<String> lore;
		if (meta.hasLore()) {
			lore = meta.getLore();
		} else {
			holder.sendMessage("This item has no lore.");
			return;
		}

		lore.set(lineNumber, line);
		meta.setLore(lore);
		item.setItemMeta(meta);
	}

	private void insertItemLore(Player holder, int lineNumber, String line) {
		ItemStack item = holder.getItemInHand();
		ItemMeta meta = item.getItemMeta();

		List<String> lore;
		if (meta.hasLore()) {
			lore = meta.getLore();
		} else {
			holder.sendMessage("This item has no lore.");
			return;
		}

		lore.add(lineNumber, line);
		meta.setLore(lore);
		item.setItemMeta(meta);
	}

	private void removeItemLore(Player holder, int lineNumber) {
		ItemStack item = holder.getItemInHand();
		ItemMeta meta = item.getItemMeta();

		List<String> lore;
		if (meta.hasLore()) {
			lore = meta.getLore();
		} else {
			holder.sendMessage("This item has no lore.");
			return;
		}

		lore.remove(lineNumber);
		meta.setLore(lore);
		item.setItemMeta(meta);
	}

	private String joinArgs(int fromIndex, String[] args) {
		List<String> argList = Arrays.asList(args);
		return String.join(" ", argList.subList(fromIndex, argList.size()));
	}

	private String encode(String easyCode) {
		return easyCode.replaceAll("&([0-9a-fA-Fk-oK-O])", SECTION + "$1");
	}

	private String decode(String hardCode) {
		return hardCode.replaceAll(SECTION + "([0-9a-fA-Fk-oK-O])", "&$1");
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
		if (!(sender instanceof Player)) {
			return null;
		}
		Player player = (Player) sender;
		if (args.length == 0) {
			return Arrays.asList(commands);
		}

		String subCommand = args[0].toLowerCase();

		if (args.length == 1) {
			List<String> matches = new ArrayList<>();
			for (String sc : commands) {
				if (sc.startsWith(subCommand)) {
					matches.add(sc);
				}
			}
			return matches;
		}

		if (args.length == 2
				&& (subCommand.equals("change") || subCommand.equals("remove") || subCommand.equals("insert"))) {
			List<String> lore = player.getItemInHand().getItemMeta().getLore();
			int max = lore == null ? 0 : lore.size();
			List<String> numbers = new ArrayList<>(max);
			for (int i = 1; i <= max; i++) {
				numbers.add(String.valueOf(i));
			}
			return numbers;
		}

		if (args.length == 3 && (subCommand.equals("change") || subCommand.equals("remove"))) {
			try {
				int lineNumber = Integer.parseInt(args[1]);
				String line = player.getItemInHand().getItemMeta().getLore().get(lineNumber - 1);
				return Lists.newArrayList(decode(line));
			} catch (NumberFormatException e) {
				return new ArrayList<>();
			} catch (IndexOutOfBoundsException e) {
				return new ArrayList<>();
			}
		}
		return null;
	}
}
