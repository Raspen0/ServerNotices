package nl.raspen0.serverannouncements;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;
import nl.raspen0.serverannouncements.events.AnnouncementsSendEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

public class SpigotAnnouncementList extends AnnouncementList{

    private final Map<Integer, TextComponent> map;

    public SpigotAnnouncementList(int pageSize){
        super(pageSize);
        this.map = new HashMap<>(pageSize);
    }

    @Override
    public boolean addAnnouncement(String message, int annCount, ServerAnnouncements plugin, String date) {
        TextComponent component;
        if (message.contains("url:")) {
            component = createUrlMessage(date, message, plugin);
        } else {
            component = new TextComponent((date != null ?
                    (ChatColor.AQUA + "[" + ChatColor.YELLOW + date + ChatColor.AQUA + "]" + ChatColor.RESET) : "") + message);
        }
        map.put(annCount, component);
        return map.size() == pageSize;
    }

    @Override
    public void sendAnnouncements(Player player) {
        System.out.println("Sending announcements");
        AnnouncementsSendEvent event = new AnnouncementsSendEvent(player);
        Bukkit.getServer().getPluginManager().callEvent(event);
        if(!event.isCancelled()) {
            for (int i = 0; i < map.size(); i++) {
                player.spigot().sendMessage(map.get(i));
            }
        }
    }

    private TextComponent createUrlMessage(String date, String message, ServerAnnouncements plugin) {
        //This is a url:(https://stirebuild.com,link to Stirebuild).
        //Becomes: This is a link to Stirebuild.

        TextComponent messageComponent = new TextComponent();
        if (date != null) {
            messageComponent.addExtra(ChatColor.AQUA + "[" + ChatColor.YELLOW + date + ChatColor.AQUA + "]" + ChatColor.RESET);
        }
        messageComponent.setColor(ChatColor.AQUA);

        while (message.contains("url:")){
            int urlStart = message.indexOf("url:");
            int urlEnd = message.indexOf(")", urlStart);
            plugin.getPluginLogger().logDebug("Found URL: Start: " + urlStart + ", End: " + urlEnd);
            messageComponent.addExtra(message.substring(0, urlStart));

            String[] url = message.substring(urlStart + 5, urlEnd).split(",");
            TextComponent linkComponent = new TextComponent(url[1]);
            linkComponent.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, url[0]));
            linkComponent.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(url[0])));
            messageComponent.addExtra(linkComponent);
            message = message.substring(urlEnd + 1);
        }
        plugin.getPluginLogger().logDebug("Processed message: " + message);
        messageComponent.addExtra(message);

        return messageComponent;
    }

    @Override
    public void sendNextPageMessage(Player player, String[] localizedMessage, String nextPage) {
        //String[] message = plugin.getLangHandler().getMessage(player, "announceNextPage").split("\\s.0.\\s");
        TextComponent textComponent = new TextComponent(localizedMessage[0]);
        textComponent.setColor(net.md_5.bungee.api.ChatColor.AQUA);

        TextComponent clickComponent = new TextComponent(" /ann " + nextPage + " ");
        clickComponent.setColor(net.md_5.bungee.api.ChatColor.YELLOW);
        clickComponent.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/ann " + nextPage));
        textComponent.addExtra(clickComponent);

        TextComponent textComponent3 = new TextComponent(localizedMessage[1]);
        textComponent3.setColor(net.md_5.bungee.api.ChatColor.AQUA);
        textComponent.addExtra(textComponent3);
        player.spigot().sendMessage(textComponent);
    }
}
