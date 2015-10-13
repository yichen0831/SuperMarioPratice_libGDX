package com.ychstudio.actors.items;

import com.ychstudio.actors.RigidBody;
import com.ychstudio.screens.PlayScreen;

/**
 * Created by yichen on 10/13/15.
 *
 * Item
 */
public abstract class Item extends RigidBody {

    protected String name = "item";

    public Item(PlayScreen playScreen, float x, float y) {
        super(playScreen, x, y);
    }

    public String getName() {
        return name;
    }

    public abstract void use();

}
