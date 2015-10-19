package com.ychstudio.utils;

import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.objects.TiledMapTileMapObject;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.ychstudio.actors.enemies.Enemy;
import com.ychstudio.actors.enemies.Goomba;
import com.ychstudio.actors.enemies.Koopa;
import com.ychstudio.actors.maptiles.*;
import com.ychstudio.gamesys.GameManager;
import com.ychstudio.screens.PlayScreen;

/**
 * Created by yichen on 10/11/15.
 *
 * WorldCreator
 */
public class WorldCreator {

    private Array<MapTileObject> mapTileObjects;
    private Array<Enemy> enemies;

    private Vector2 startPosition;
    private Vector2 flagPosition;

    public WorldCreator(PlayScreen playScreen, TiledMap tiledMap) {

        mapTileObjects = new Array<MapTileObject>();
        enemies = new Array<Enemy>();

        MapLayer mapLayer = tiledMap.getLayers().get("Rocks");
        if (mapLayer != null) {
            for (MapObject mapObject : mapLayer.getObjects()) {
                float x = ((TiledMapTileMapObject) mapObject).getX();
                float y = ((TiledMapTileMapObject) mapObject).getY();

                mapTileObjects.add(new Rock(playScreen, (x + 8) / GameManager.PPM, (y + 8) / GameManager.PPM, (TiledMapTileMapObject) mapObject));
            }
        }

        mapLayer = tiledMap.getLayers().get("MetalBlocks");
        if (mapLayer != null) {
            for (MapObject mapObject : mapLayer.getObjects()) {
                float x = ((TiledMapTileMapObject) mapObject).getX();
                float y = ((TiledMapTileMapObject) mapObject).getY();

                mapTileObjects.add(new Pipe(playScreen, (x + 8) / GameManager.PPM, (y + 8) / GameManager.PPM, (TiledMapTileMapObject) mapObject));
            }
        }

        mapLayer = tiledMap.getLayers().get("Pipes");
        if (mapLayer != null) {
            for (MapObject mapObject : mapLayer.getObjects()) {
                float x = ((TiledMapTileMapObject) mapObject).getX();
                float y = ((TiledMapTileMapObject) mapObject).getY();

                mapTileObjects.add(new Pipe(playScreen, (x + 8) / GameManager.PPM, (y + 8) / GameManager.PPM, (TiledMapTileMapObject) mapObject));
            }
        }

        mapLayer = tiledMap.getLayers().get("Bricks");
        if (mapLayer != null) {
            for (MapObject mapObject : mapLayer.getObjects()) {
                float x = ((TiledMapTileMapObject) mapObject).getX();
                float y = ((TiledMapTileMapObject) mapObject).getY();

                mapTileObjects.add(new Brick(playScreen, (x + 8) / GameManager.PPM, (y + 8) / GameManager.PPM, (TiledMapTileMapObject) mapObject));
            }
        }

        mapLayer = tiledMap.getLayers().get("CoinBlocks");
        if (mapLayer != null) {
            for (MapObject mapObject : mapLayer.getObjects()) {
                float x = ((TiledMapTileMapObject) mapObject).getX();
                float y = ((TiledMapTileMapObject) mapObject).getY();

                mapTileObjects.add(new CoinBlock(playScreen, (x + 8) / GameManager.PPM, (y + 8) / GameManager.PPM, (TiledMapTileMapObject) mapObject));
            }
        }

        mapLayer = tiledMap.getLayers().get("Goombas");
        if (mapLayer != null) {
            for (MapObject mapObject : mapLayer.getObjects()) {
                float x = ((TiledMapTileMapObject) mapObject).getX();
                float y = ((TiledMapTileMapObject) mapObject).getY();

                enemies.add(new Goomba(playScreen, (x + 8) / GameManager.PPM, (y + 8) / GameManager.PPM));
            }
        }

        mapLayer = tiledMap.getLayers().get("Koopas");
        if (mapLayer != null) {
            for (MapObject mapObject : mapLayer.getObjects()) {
                float x = ((TiledMapTileMapObject) mapObject).getX();
                float y = ((TiledMapTileMapObject) mapObject).getY();

                enemies.add(new Koopa(playScreen, (x + 8) / GameManager.PPM, (y + 8) / GameManager.PPM));
            }
        }

        mapLayer = tiledMap.getLayers().get("Flagpole");
        if (mapLayer != null) {
            for (MapObject mapObject : mapLayer.getObjects()) {
                float x = ((TiledMapTileMapObject) mapObject).getX();
                float y = ((TiledMapTileMapObject) mapObject).getY();

                mapTileObjects.add(new Flagpole(playScreen, (x + 8) / GameManager.PPM, (y + 8) / GameManager.PPM, (TiledMapTileMapObject) mapObject));
            }
        }

        flagPosition = new Vector2();

        mapLayer = tiledMap.getLayers().get("Flag");
        if (mapLayer != null) {
            if (mapLayer.getObjects().getCount() > 0) {
                float x = ((TiledMapTileMapObject) mapLayer.getObjects().get(0)).getX();
                float y = ((TiledMapTileMapObject) mapLayer.getObjects().get(0)).getY();

                flagPosition = new Vector2(x, y);
            }
        }


        startPosition = new Vector2(64.0f, 64.0f);

        mapLayer = tiledMap.getLayers().get("Start");
        if (mapLayer != null) {
            if (mapLayer.getObjects().getCount() > 0) {
                float x = ((TiledMapTileMapObject) mapLayer.getObjects().get(0)).getX();
                float y = ((TiledMapTileMapObject) mapLayer.getObjects().get(0)).getY();

                startPosition = new Vector2(x, y);
            }
        }

    }

    public Vector2 getStartPosition() {
        return startPosition;
    }

    public Vector2 getFlagPosition() {
        return flagPosition;
    }

    public Array<MapTileObject> getMapTileObject() {
        return mapTileObjects;
    }

    public Array<Enemy> getEnemies() {
        return enemies;
    }
}
