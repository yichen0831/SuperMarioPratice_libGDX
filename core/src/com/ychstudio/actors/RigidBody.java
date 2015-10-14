package com.ychstudio.actors;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.World;
import com.ychstudio.screens.PlayScreen;

/**
 * Created by yichen on 10/11/15.
 *
 * RigidBody
 */
public abstract class RigidBody extends Sprite {
    protected PlayScreen playScreen;
    protected World world;
    protected Body body;

    protected boolean toBeDestroyed;
    protected boolean destroyed;

    public RigidBody(PlayScreen playScreen, float x, float y) {
        this.playScreen = playScreen;
        this.world = playScreen.world;

        toBeDestroyed = false;
        destroyed = false;

        setPosition(x, y);
        defBody();
    }

    protected abstract void defBody();
    public abstract void update(float delta);

    public void onCollide(Collider other) {

    }

    public void onTrigger(Collider other) {

    }

    public void queueDestroy() {
        toBeDestroyed = true;
    }

    public boolean isDestroyed() {
        return destroyed;
    }
}
