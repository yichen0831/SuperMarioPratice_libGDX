package com.ychstudio.actors.enemies;

import com.ychstudio.actors.RigidBody;
import com.ychstudio.screens.PlayScreen;

/**
 * Created by yichen on 10/13/15.
 *
 * Enemy
 */
public abstract class Enemy extends RigidBody {

    protected int hp = 1;
    protected boolean active = false;

    public Enemy(PlayScreen playScreen, float x, float y) {
        super(playScreen, x, y);
    }

    public abstract void getDamage(int damage);

}
