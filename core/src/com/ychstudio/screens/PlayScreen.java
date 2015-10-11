package com.ychstudio.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.ychstudio.SuperMario;
import com.ychstudio.actors.MapTileObject;
import com.ychstudio.actors.Mario;
import com.ychstudio.gamesys.GameManager;
import com.ychstudio.utils.WorldContactListener;
import com.ychstudio.utils.WorldCreator;

/**
 * Created by yichen on 10/11/15.
 *
 * PlayScreen
 */
public class PlayScreen implements Screen {

    private SuperMario game;

    public World world;

    private float accumulator;

    private OrthographicCamera camera;
    private Viewport viewport;

    private TiledMap tiledMap;
    private OrthogonalTiledMapRenderer mapRenderer;

    private Box2DDebugRenderer box2DDebugRenderer;

    private Array<MapTileObject> mapTileObjects;

    private Mario mario;

    public PlayScreen(SuperMario game) {
        this.game = game;
    }


    @Override
    public void show() {

        camera = new OrthographicCamera();

        viewport = new FitViewport(GameManager.V_WIDTH, GameManager.V_HEIGHT);
        viewport.setCamera(camera);

        camera.position.set(GameManager.V_WIDTH / 2, GameManager.V_HEIGHT / 2, 0);

        TmxMapLoader tmxMapLoader = new TmxMapLoader();
        tiledMap = tmxMapLoader.load("maps/Level_01.tmx");
        mapRenderer = new OrthogonalTiledMapRenderer(tiledMap, 1 / GameManager.PPM);

        world = new World(GameManager.GRAVITY, true);
        world.setContactListener(new WorldContactListener());

        box2DDebugRenderer = new Box2DDebugRenderer();

        WorldCreator worldCreator = new WorldCreator(this, tiledMap);
        mapTileObjects = worldCreator.getMapTileObject();
        mario = new Mario(this, (worldCreator.getStartPosition().x + 8) / GameManager.PPM, (worldCreator.getStartPosition().y + 8) / GameManager.PPM);


        accumulator = 0;
    }

    public void update(float delta) {
        accumulator += delta;
        if (accumulator > GameManager.STEP) {
            world.step(GameManager.STEP, 6, 2);
            accumulator -= GameManager.STEP;
        }


        mario.update(delta);
        camera.position.x = mario.getPosition().x;

        camera.update();
        mapRenderer.setView(camera);

    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0.2f, 0.2f, 0.2f, 1.0f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        update(delta);

        mapRenderer.render(new int[] {0, 1});

        game.batch.setProjectionMatrix(camera.combined);
        game.batch.begin();

        for (MapTileObject mapTileObject : mapTileObjects) {
            mapTileObject.draw(game.batch);
        }

        game.batch.end();

        box2DDebugRenderer.render(world, camera.combined);

    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height);
    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void hide() {

    }

    @Override
    public void dispose() {
        tiledMap.dispose();
        world.dispose();
        box2DDebugRenderer.dispose();
    }
}
