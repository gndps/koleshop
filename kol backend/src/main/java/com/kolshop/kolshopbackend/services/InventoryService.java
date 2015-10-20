package com.kolshop.kolshopbackend.services;

import com.kolshop.kolshopbackend.db.models.InventoryCategory;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Gundeep on 16/10/15.
 */
public class InventoryService {

    public List<InventoryCategory> getCategories() throws Exception {
        List<InventoryCategory> list = new ArrayList<>();
        InventoryCategory inventoryCategory1 = new InventoryCategory("Grocery", "Atta, Rice, Dal etc", "https://cdn4.iconfinder.com/data/icons/48-bubbles/48/03.Office-128.png", "321/537");
        InventoryCategory inventoryCategory2 = new InventoryCategory("Drinks", "Cold drinks, Juices etc", "https://cdn4.iconfinder.com/data/icons/48-bubbles/48/39.Heart-128.png", "342/378");
        InventoryCategory inventoryCategory3 = new InventoryCategory("Namkeen", "Chips, Bhujia, Kurkure etc", "https://cdn4.iconfinder.com/data/icons/48-bubbles/48/10.Folder-128.png", "273/362");
        InventoryCategory inventoryCategory4 = new InventoryCategory("Grocery", "Atta, Rice, Dal etc", "https://cdn4.iconfinder.com/data/icons/48-bubbles/48/17.Brush-128.png", "321/537");
        InventoryCategory inventoryCategory5 = new InventoryCategory("Drinks", "Cold drinks, Juices etc", "https://cdn4.iconfinder.com/data/icons/48-bubbles/48/43.Bell-128.png", "342/378");
        InventoryCategory inventoryCategory6 = new InventoryCategory("Namkeen", "Chips, Bhujia, Kurkure etc", "https://cdn4.iconfinder.com/data/icons/48-bubbles/48/13.Clipboard-128.png", "273/362");
        InventoryCategory inventoryCategory7 = new InventoryCategory("Grocery", "Atta, Rice, Dal etc", "https://cdn4.iconfinder.com/data/icons/48-bubbles/48/42.Badge-128.png", "321/537");
        InventoryCategory inventoryCategory8 = new InventoryCategory("Drinks", "Cold drinks, Juices etc", "https://cdn4.iconfinder.com/data/icons/48-bubbles/48/16.Pen-128.png", "342/378");
        InventoryCategory inventoryCategory9 = new InventoryCategory("Namkeen", "Chips, Bhujia, Kurkure etc", "https://cdn4.iconfinder.com/data/icons/48-bubbles/48/36.Watch-128.png", "273/362");
        InventoryCategory inventoryCategory10 = new InventoryCategory("Namkeen", "Chips, Bhujia, Kurkure etc", "https://cdn4.iconfinder.com/data/icons/48-bubbles/48/09.Handbag-128.png", "273/362");
        InventoryCategory inventoryCategory11 = new InventoryCategory("Drinks", "Cold drinks, Juices etc", "https://cdn4.iconfinder.com/data/icons/48-bubbles/48/40.Cloud-128.png", "342/378");
        list.add(inventoryCategory1);
        list.add(inventoryCategory2);
        list.add(inventoryCategory3);
        list.add(inventoryCategory4);
        list.add(inventoryCategory5);
        list.add(inventoryCategory6);
        list.add(inventoryCategory7);
        list.add(inventoryCategory8);
        list.add(inventoryCategory9);
        list.add(inventoryCategory10);
        list.add(inventoryCategory11);
        return list;
    }

}
