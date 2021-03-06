package ru.frostdelta.bungeereports;


import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import ru.frostdelta.bungeereports.gui.PunishUI;
import ru.frostdelta.bungeereports.gui.ReasonsUI;
import ru.frostdelta.bungeereports.hash.HashedLists;
import ru.frostdelta.bungeereports.holders.GetReportsHolder;
import ru.frostdelta.bungeereports.holders.PunishHolder;
import ru.frostdelta.bungeereports.holders.ReasonHolder;
import ru.frostdelta.bungeereports.holders.UserHolder;
import ru.frostdelta.bungeereports.spectate.SpectateManager;

import java.util.HashMap;
import java.util.Map;

public class EventHandler extends SpectateManager implements Listener {

    private Loader plugin;
    private int index;

    public EventHandler(Loader instance){

        plugin = instance;

    }

    private final Network Network = new Network();
    private final UpdateReport update = new UpdateReport();

    private Map<String, String> map = new HashMap<>();
    private Map<String, String> ban = new HashMap<>();
    private Map<String, String> comment = new HashMap<>();
    private static Map<Integer, String> send = new HashMap<>();

    public EventHandler(HashMap<Integer, String> sender) {
        EventHandler.send = sender;
    }

    @org.bukkit.event.EventHandler(priority = EventPriority.LOW)
    public void playerDisconnect(PlayerQuitEvent e){
        if(isTarget(e.getPlayer())){
            Player player = (Player) getTarget().get(e.getPlayer());
            spectateOff(player);
            getTarget().remove(e.getPlayer());
            player.sendMessage(ChatColor.RED + "Игрок вышел из игры!");
        }
    }

    @org.bukkit.event.EventHandler
    public void asyncChatEvent(AsyncPlayerChatEvent e){
        String player = e.getPlayer().getName();
       if(comment.containsKey(player)){
           String reason = comment.get(player);
           Network.addReport(player, map.get(player),reason, e.getMessage());
           map.remove(player);
           comment.remove(player);

           HashedLists.loadReports();
           e.getPlayer().sendMessage(ChatColor.GREEN + "Репорт успешно отправлен!");

           e.setCancelled(true);
       }
    }

    public Map<String, String> getBan(){
        return ban;
    }

    @org.bukkit.event.EventHandler
    public void onInventoryClick(InventoryClickEvent e){
        Player p = (Player) e.getWhoClicked();
        if(e.getSlotType() != InventoryType.SlotType.OUTSIDE && e.getSlotType() == InventoryType.SlotType.CONTAINER) {

            if (e.getInventory().getHolder() instanceof PunishHolder && !e.getCurrentItem().getType().equals(Material.AIR)) {
                String s = p.getOpenInventory().getItem(4).getItemMeta().getDisplayName();
               
                switch(e.getCurrentItem().getItemMeta().getDisplayName()){
                    case "Принять":
                        update.updateReport(ban.get(p.getName()), s, "accept");
                        ban.remove(p.getName());
                        p.getOpenInventory().close();
                        p.sendMessage(ChatColor.GREEN + "Репорт принят");
                        HashedLists.changeCount(index);
                        break;
                    case "Отклонить":
                        update.updateReport(ban.get(p.getName()), s, "reject");
                        ban.remove(p.getName());
                        p.getOpenInventory().close();
                        p.sendMessage(ChatColor.RED + "Репорт отклонён!");
                        HashedLists.changeCount(index);
                        break;
                    case "Наблюдать":
                        if(Bukkit.getServer().getPlayer(ban.get(p.getName())) != null && !isSpectate(p)){
                            Player target = Bukkit.getServer().getPlayer(ban.get(p.getName()));
                            setSpectate(p,target);
                            p.getOpenInventory().close();
                        }
                        break;
                }

                e.setCancelled(true);
            }

            if (e.getInventory().getHolder() instanceof GetReportsHolder && !e.getCurrentItem().getType().equals(Material.AIR)) {

                PunishUI PunishUI = new PunishUI(plugin);

                String s = send.get(e.getSlot());

                ban.put(e.getWhoClicked().getName(), e.getCurrentItem().getItemMeta().getDisplayName());
                PunishUI.openGUI(e.getCurrentItem().getItemMeta().getDisplayName(), p, s);
                index = e.getSlot();

                e.setCancelled(true);
            }

            if (e.getInventory().getHolder() instanceof UserHolder && !e.getCurrentItem().getType().equals(Material.AIR)) {

                if (!plugin.getWhitelist().contains(e.getCurrentItem().getItemMeta().getDisplayName())) {

                    ReasonsUI ReasonsUI = new ReasonsUI(plugin);
                    map.put(e.getWhoClicked().getName(), e.getCurrentItem().getItemMeta().getDisplayName());
                    p.getOpenInventory().close();
                    ReasonsUI.openGUI(p);

                    e.setCancelled(true);
                } else {
                    p.getOpenInventory().close();
                    e.setCancelled(true);
                    p.sendMessage(ChatColor.RED + "На данного игрока невозможно отправить жалобу!");
                }
            }

            if (e.getInventory().getHolder() instanceof ReasonHolder && !e.getCurrentItem().getType().equals(Material.AIR)) {

                if (!plugin.getConfig().getBoolean("comments")) {

                    Network.addReport(e.getWhoClicked().getName(), map.get(e.getWhoClicked().getName()), e.getCurrentItem().getItemMeta().getDisplayName(), "");
                    HashedLists.addReport(e.getWhoClicked().getName(), map.get(e.getWhoClicked().getName()), e.getCurrentItem().getItemMeta().getDisplayName(), "");
                    map.remove(e.getWhoClicked().getName());
                    e.getWhoClicked().getOpenInventory().close();
                    p.sendMessage(ChatColor.GREEN + "Репорт успешно отправлен!");

                } else {
                    comment.put(e.getWhoClicked().getName(), e.getCurrentItem().getItemMeta().getDisplayName());
                    e.getWhoClicked().getOpenInventory().close();
                    e.getWhoClicked().sendMessage(ChatColor.RED + "Введите комментарий в чат!");
                }

                e.setCancelled(true);
            }
        }else return;
    }
}
