package com.ychstudio.utils;

import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.objects.TiledMapTileMapObject;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.ychstudio.actors.Brick;
import com.ychstudio.actors.MapTileObject;
import com.ychstudio.actors.Rock;
import com.ychstudio.gamesys.GameManager;
import com.ychstudio.screens.PlayScreen;

/**
 * Created by yichen on 10/11/15.
 *
 * WorldCreator
 */
public class WorldCreator {

    private Array<MapTileObject> mapTileObjects;

    private Vector2 startPosition;

    public WorldCreator(PlayScreen playScreen, TiledMap tiledMap) {

        mapTileObjects = new Array<MapTileObject>();

        MapLayer mapLayer = tiledMap.getLayers().get("Rocks");
        for (MapObject mapObject : mapLayer.getObjects()) {
            float x = ((TiledMapTileMapObject) mapObject).getX();
            float y = ((TiledMapTileMapObject) mapObject).getY();

            mapTileObjects.add(new Rock(playScreen, (x + 8) / GameManager.PPM, (y + 8) / GameManager.PPM, ((TiledMapTileMapObject) mapObject).getTextureRegion()));

        }

        mapLayer = tiledMap.getLayers().get("Bricks");
        for (MapObject mapObject : mapLayer.getObjects()) {
            float x = ((TiledMapTileMapObject) mapObject).getX();
            float y = ((TiledMapTileMapObject) mapObject).getY();

            mapTileObjects.add(new Brick(playScreen, (x + 8) / GameManager.PPM, (y + 8) / GameManager.PPM, ((TiledMapTileMapObject) mapObject).getTextureRegion()));

        }

        startPosition = new Vector2(2.0f, 2.0f);

        mapLayer = tiledMap.getLayers().get("Start");
        if (mapLayer.getObjects().getCount() > 0) {
            float x = ((TiledMapTileMapObject) mapLayer.getObjects().get(0)).getX();
            float y = ((TiledMapTileMapObject) mapLayer.getObjects().get(0)).getY();

            startPosition = new Vector2(x, y);
        }

    }

    public Vector2 getStartPosition() {
        return startPosition;
    }

    public Array<MapTileObject> getMapTileObject() {
        return mapTileObjects;
    }
}
