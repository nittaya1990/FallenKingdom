package fr.devsylone.fallenkingdom.listeners.entity.player;

import java.time.LocalDate;
import java.time.Month;
import java.util.ArrayList;
import java.util.List;

import fr.devsylone.fallenkingdom.Fk;
import fr.devsylone.fallenkingdom.players.FkPlayer;
import fr.devsylone.fallenkingdom.players.FkPlayer.PlayerState;
import fr.devsylone.fallenkingdom.utils.Messages;
import fr.devsylone.fallenkingdom.utils.NMSUtils;
import fr.devsylone.fkpi.FkPI;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent.Result;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import com.cryptomorin.xseries.SkullUtils;
import com.cryptomorin.xseries.XMaterial;

public class JoinListener implements Listener
{
	private final boolean NON_TESTING_ENV = NMSUtils.obcOptionalClass("inventory.CraftMetaSkull").isPresent();

	@EventHandler
	public void prelogin(final AsyncPlayerPreLoginEvent e)
	{
		if(!Fk.getInstance().getPluginError().isEmpty())
		{
			e.setLoginResult(Result.KICK_OTHER);
			e.setKickMessage(kickMessage());
		}
	}

	@EventHandler
	public void join(final PlayerJoinEvent e)
	{
		if(!Fk.getInstance().getPluginError().isEmpty()) // Bukkit n'a pas l'air d'invoquer l'AsyncPlayerPreLoginEvent
			e.getPlayer().kickPlayer(kickMessage());

		if (!Fk.getInstance().getWorldManager().isAffected(e.getPlayer().getWorld()))
			return;

		FkPlayer player = Fk.getInstance().getPlayerManager().getPlayer(e.getPlayer());

		if(e.getPlayer().isOp())
			for(String s : Fk.getInstance().getOnConnectWarnings())
				e.getPlayer().sendMessage(s);

		player.refreshScoreboard();
		if(NON_TESTING_ENV)
			FkPI.getInstance().getTeamManager().nametag().addEntry(e.getPlayer());

		e.setJoinMessage(null);
		Fk.broadcast(Messages.CHAT_JOIN.getMessage().replace("%player%", e.getPlayer().getDisplayName()));

		if(player.getState() == PlayerState.EDITING_SCOREBOARD)
			player.getSbDisplayer().display();

		if(NON_TESTING_ENV)
		{
			LocalDate currentDate = LocalDate.now();
			if(Fk.getInstance().getGame().isPreStart() && e.getPlayer().getInventory().getHelmet() == null && (currentDate.getDayOfMonth() == 12) && (currentDate.getMonth() == Month.JUNE))
				e.getPlayer().getInventory().setHelmet(head(currentDate));
		}
	}

	@EventHandler
	public void quit(PlayerQuitEvent e)
	{
		final FkPlayer fkPlayer = Fk.getInstance().getPlayerManager().getPlayerIfExist(e.getPlayer());
		if (fkPlayer != null) {
			Fk.getInstance().getDisplayService().hide(e.getPlayer(), fkPlayer);
			if(fkPlayer.getState() == PlayerState.EDITING_SCOREBOARD)
				fkPlayer.getSbDisplayer().exit();
		}

		if (!Fk.getInstance().getWorldManager().isAffected(e.getPlayer().getWorld()))
			return;

		e.setQuitMessage(null);
		Fk.broadcast(Messages.CHAT_QUIT.getMessage().replace("%player%", e.getPlayer().getDisplayName()));
	}

	private String kickMessage()
	{
		return Messages.CONSOLE_KICK_MESSAGE.getMessage() + Fk.getInstance().getPluginError();
	}
	
	private ItemStack head(LocalDate date)
	{
		ItemStack skull = new ItemStack(XMaterial.PLAYER_HEAD.parseItem());
		SkullMeta skullMeta = SkullUtils.applySkin(skull.getItemMeta(), "ewogICJ0aW1lc3RhbXAiIDogMTYwNDUxNzcwODc5OSwKICAicHJvZmlsZUlkIiA6ICJhZmI0ODljNDlmYzg0OGE0OThmMmRkN2JlYTQxNGM5YSIsCiAgInByb2ZpbGVOYW1lIiA6ICJNSEZfQ2FrZSIsCiAgInNpZ25hdHVyZVJlcXVpcmVkIiA6IHRydWUsCiAgInRleHR1cmVzIiA6IHsKICAgICJTS0lOIiA6IHsKICAgICAgInVybCIgOiAiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS9lYzI0MWE1OTdjMjg1ZTEwNGMyNzExOTZkNzg1ZGI0Y2QwMTEwYTQwYzhmOGU1ZDM1NGM1NjQ0MTU5NTY3YzlkIgogICAgfQogIH0KfQ=="); // MHF_Cake's skin
		skullMeta.setDisplayName(Messages.EASTER_EGG_ANNIVERSARY_NAME.getMessage());
		List<String> lore = new ArrayList<>();
		lore.add(Messages.EASTER_EGG_ANNIVERSARY_LORE_1.getMessage());
		lore.add(Messages.EASTER_EGG_ANNIVERSARY_LORE_2.getMessage());
		lore.add(Messages.EASTER_EGG_ANNIVERSARY_LORE_3.getMessage().replace("%age%", Integer.toString(date.getYear() - 2016)));
		skullMeta.setLore(lore);
		skull.setItemMeta(skullMeta);
		return skull;
	}
}
