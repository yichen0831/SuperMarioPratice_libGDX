package com.ychstudio.actors.items;

/**
 * Created by yichen on 10/13/15.
 *
 * SpawningItem
 */
public class SpawningItem {
    public float x;
    public float y;
    public Class<? extends Item> type;

    public SpawningItem(float x, float y, Class<? extends Item> type) {
        this.x = x;
        this.y = y;
        this.type = type;
    }
}
