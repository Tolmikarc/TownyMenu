package net.tolmikarc.townymenu.town.prompt;

import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.TownySettings;
import com.palmergames.bukkit.towny.exceptions.AlreadyRegisteredException;
import com.palmergames.bukkit.towny.exceptions.EconomyException;
import com.palmergames.bukkit.towny.exceptions.NotRegisteredException;
import com.palmergames.bukkit.towny.object.Town;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.Prompt;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mineacademy.fo.conversation.SimplePrompt;
import org.mineacademy.fo.debug.LagCatcher;

import java.util.ArrayList;
import java.util.List;

public class TownNamePrompt extends SimplePrompt {

	Town town;

	public TownNamePrompt(Town town) {
		super(false);

		this.town = town;

	}

	@Override
	protected String getPrompt(ConversationContext ctx) {
		return "&3Type in the name you would like to give to your town: (under 10 characters) &bCurrent Name: " + town.getName() + " &cType cancel to exit.";
	}

	@Override
	protected boolean isInputValid(ConversationContext context, String input) {
		LagCatcher.start("load-all-town-names");
		List<String> allTownNames = new ArrayList<>();
		for (Town town : TownyAPI.getInstance().getDataSource().getTowns())
			allTownNames.add(town.getName());
		LagCatcher.end("load-all-town-names");
		return (input.length() < 10 && !allTownNames.contains(input));
	}

	@Override
	protected String getFailedValidationText(ConversationContext context, String invalidInput) {
		return "&cName must be unique and less than 10 characters.";
	}

	@Override
	protected @Nullable Prompt acceptValidatedInput(@NotNull ConversationContext context, @NotNull String input) {
		if (!getPlayer(context).hasPermission("towny.command.town.set.name"))
			return null;


		try {
			if (town.getAccount().canPayFromHoldings(TownySettings.getTownRenameCost())) {
				TownyAPI.getInstance().getDataSource().renameTown(town, input);
				town.getAccount().pay(TownySettings.getTownRenameCost(), "Renaming town.");

				tell("&3Successfully set town name to " + input);
				TownyAPI.getInstance().getDataSource().saveTown(town);
			} else
				tell("&cNot enough money in town bank to change town name.");
		} catch (EconomyException | NotRegisteredException | AlreadyRegisteredException e) {
			e.printStackTrace();
		}


		return null;
	}
}
