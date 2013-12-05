/*
 * @author     ucchy
 * @license    LGPLv3
 * @copyright  Copyright ucchy 2013
 */
package org.bitbucket.ucchy.nd;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * プレイヤーが倒された時に、インベントリからランダムで2、3個アイテムを落とすようにするプラグイン。
 * @author ucchy
 */
public class NisancoDrop extends JavaPlugin implements Listener {

    private static final int ARMOR_OFFSET = 50;
    
    /**
     * プラグインが有効化された時に呼び出されるメソッド
     * @see org.bukkit.plugin.java.JavaPlugin#onEnable()
     */
    @Override
    public void onEnable() {
        // イベントリスナーとして登録する
        getServer().getPluginManager().registerEvents(this, this);
    }

    /**
     * プレイヤーが死亡した時に呼び出されるメソッド
     * @param event 
     */
    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        
        Player player = event.getEntity();
        
        // keep inventory 設定を取得する
        boolean isKeepInventory = false;
        String value = player.getWorld().getGameRuleValue("keepInventory");
        if ( value.equalsIgnoreCase("true") ) {
            isKeepInventory = true;
        }
        
        // ドロップする個数（2個か3個）を決定する
        int num = 2 + (int)(Math.random() * 2);
        
        if ( !isKeepInventory ) {
            // keep inventory がオフの場合の処理
            
            // イベントからドロップを取得する
            List<ItemStack> items = event.getDrops();
            if ( items.size() <= num ) {
                return;
            }
            
            // シャッフルしたあと、最初の２、３個を残してすべて消去する
            Collections.shuffle(items); // シャッフル
            int size = items.size();
            for ( int i=(size-1); i>=num; i-- ) {
                items.remove(i);
            }
            
        } else {
            // keep inventory がオンの場合の処理
            
            // インベントリからアイテムを取得する
            ItemStack[] items = player.getInventory().getContents();
            ItemStack[] armors = player.getInventory().getArmorContents();
            
            // インデックスの数値を配列として作成する
            ArrayList<Integer> indexes = getItemIndexes(items, armors);
            
            // シャッフルしたあと、最初の２、３個をドロップさせて消去する
            Collections.shuffle(indexes);
            for ( int j=0; j<num; j++ ) {
                if ( indexes.size() <= j ) {
                    break;
                }
                int index = indexes.get(j);
                
                if ( index < ARMOR_OFFSET ) {
                    player.getWorld().dropItemNaturally(player.getLocation(), items[index]);
                    player.getInventory().clear(index);
                } else {
                    ItemStack item;
                    if ( index == ARMOR_OFFSET + 3 ) {
                        item = player.getInventory().getHelmet();
                        player.getInventory().setHelmet(null);
                    } else if ( index == ARMOR_OFFSET + 2 ) {
                        item = player.getInventory().getChestplate();
                        player.getInventory().setChestplate(null);
                    } else if ( index == ARMOR_OFFSET + 1 ) {
                        item = player.getInventory().getLeggings();
                        player.getInventory().setLeggings(null);
                    } else {
                        item = player.getInventory().getBoots();
                        player.getInventory().setBoots(null);
                    }
                    player.getWorld().dropItemNaturally(player.getLocation(), item);
                }
            }
        }
    }
    
    /**
     * インベントリから取得したContentsについて、
     * アイテムが入っている欄のインデックスを調べて返す
     * @param items 
     * @return
     */
    private static ArrayList<Integer> getItemIndexes(
            ItemStack[] items, ItemStack[] armors) {
        
        ArrayList<Integer> indexes = new ArrayList<Integer>();
        for ( int i=0; i<items.length; i++ ) {
            if ( items[i] != null ) {
                indexes.add(i);
            }
        }
        for ( int i=0; i<armors.length; i++ ) {
            if ( armors[i] != null && armors[i].getType() != Material.AIR ) {
                indexes.add(i + ARMOR_OFFSET);
            }
        }
        
        return indexes;
    }
}
