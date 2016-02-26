package com.ychstudio.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.DelayAction;
import com.badlogic.gdx.scenes.scene2d.actions.MoveToAction;
import com.badlogic.gdx.scenes.scene2d.actions.RunnableAction;
import com.badlogic.gdx.scenes.scene2d.actions.SequenceAction;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.ychstudio.SuperMario;
import com.ychstudio.actors.Mario;
import com.ychstudio.actors.effects.BrickDebris;
import com.ychstudio.actors.effects.Effect;
import com.ychstudio.actors.effects.FlippingCoin;
import com.ychstudio.actors.effects.SpawningEffect;
import com.ychstudio.actors.enemies.Enemy;
import com.ychstudio.actors.items.*;
import com.ychstudio.actors.maptiles.MapTileObject;
import com.ychstudio.actors.stageitems.Flag;
import com.ychstudio.actors.weapons.Fireball;
import com.ychstudio.actors.weapons.SpawningFireball;
import com.ychstudio.gamesys.GameManager;
import com.ychstudio.hud.Hud;
import com.ychstudio.hud.ScoreIndicator;
import com.ychstudio.utils.WorldContactListener;
import com.ychstudio.utils.WorldCreator;

import java.util.LinkedList;

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

    private float cameraLeftLimit;
    private float cameraRightLimit;

    private TiledMap tiledMap;
    private OrthogonalTiledMapRenderer mapRenderer;

    private float mapWidth;
//    private float mapHeight; // currently not used

    private TextureAtlas textureAtlas;

    private Box2DDebugRenderer box2DDebugRenderer;
    private boolean renderB2DDebug;

    private Array<MapTileObject> mapTileObjects;
    private Array<Enemy> enemies;

    private Array<Item> items;
    private LinkedList<SpawningItem> itemSpawnQueue;

    private Array<Effect> effects;
    private LinkedList<SpawningEffect> effectSpawnQueue;

    private Array<Fireball> fireballs;
    private LinkedList<SpawningFireball> fireballSpawnQueue;

    private Mario mario;

    private Hud hud;
    private ScoreIndicator scoreIndicator;

    private boolean playingHurryMusic;
    private boolean playMusic;

    private float countDown;

    private Stage levelCompletedStage;
    private boolean levelCompleted = false;
    private boolean flagpoleMusicPlay = false;
    private boolean levelCompletedMusicPlay = false;

    public PlayScreen(SuperMario game) {
        this.game = game;
    }

    @Override
    public void show() {

        camera = new OrthographicCamera();

        viewport = new FitViewport(GameManager.V_WIDTH, GameManager.V_HEIGHT);
        viewport.setCamera(camera);

        camera.position.set(GameManager.V_WIDTH / 2, GameManager.V_HEIGHT / 2, 0);

        textureAtlas = new TextureAtlas("imgs/actors.atlas");

        // create Box2D world
        world = new World(GameManager.GRAVITY, true);
        world.setContactListener(new WorldContactListener());

        // load tmx tiled map
        TmxMapLoader tmxMapLoader = new TmxMapLoader();
        tiledMap = tmxMapLoader.load("maps/Level_1-1.tmx");
        mapRenderer = new OrthogonalTiledMapRenderer(tiledMap, 1 / GameManager.PPM);

        mapWidth = ((TiledMapTileLayer) tiledMap.getLayers().get(0)).getWidth();
//        mapHeight = ((TiledMapTileLayer) tiledMap.getLayers().get(0)).getHeight(); // currently not used

        // create world from TmxTiledMap
        WorldCreator worldCreator = new WorldCreator(this, tiledMap);
        mapTileObjects = worldCreator.getMapTileObject();
        enemies = worldCreator.getEnemies();
        mario = new Mario(this, (worldCreator.getStartPosition().x + 8) / GameManager.PPM, (worldCreator.getStartPosition().y + 8) / GameManager.PPM);


        // for spawning item
        items = new Array<Item>();
        itemSpawnQueue = new LinkedList<SpawningItem>();

        // for spawning effect
        effects = new Array<Effect>();
        effectSpawnQueue = new LinkedList<SpawningEffect>();

        // for spawning fireball
        fireballs = new Array<Fireball>();
        fireballSpawnQueue = new LinkedList<SpawningFireball>();


        hud = new Hud(game.batch);
        hud.setLevel("1-1");

        scoreIndicator = new ScoreIndicator(this, game.batch);

        accumulator = 0;

        cameraLeftLimit = GameManager.V_WIDTH / 2;
        cameraRightLimit =  mapWidth - GameManager.V_WIDTH / 2;

        box2DDebugRenderer = new Box2DDebugRenderer();
        renderB2DDebug = false;

        countDown = 3.0f;

        playingHurryMusic = false;
        playMusic = true;

        // flag and levelCompletedStage
        Flag flag = new Flag(this, (worldCreator.getFlagPosition().x - 9)/ GameManager.PPM, worldCreator.getFlagPosition().y / GameManager.PPM);
        MoveToAction flagSlide = new MoveToAction();
        flagSlide.setPosition((worldCreator.getFlagPosition().x - 9) / GameManager.PPM, 3);
        flagSlide.setDuration(1.0f);
        flag.addAction(flagSlide);
        levelCompletedStage = new Stage(viewport, game.batch);
        levelCompletedStage.addActor(flag);
        RunnableAction setLevelCompletedScreen = new RunnableAction();
        setLevelCompletedScreen.setRunnable(new Runnable() {
            @Override
            public void run() {
                game.setScreen(new GameOverScreen(game));
                dispose();
            }
        });
        levelCompletedStage.addAction(new SequenceAction(new DelayAction(8.0f), setLevelCompletedScreen));

    }

    public TextureAtlas getTextureAtlas() {
        return textureAtlas;
    }

    public TiledMap getTiledMap() {
        return tiledMap;
    }

    public float getMapWidth() {
        return mapWidth;
    }

    /* currently not used
    public float getMapHeight() {
        return mapHeight;
    }
    */

    public OrthographicCamera getCamera() {
        return camera;
    }

    public ScoreIndicator getScoreIndicator() {
        return scoreIndicator;
    }

    public void levelCompleted() {
        if (levelCompleted) {
            return;
        }
        levelCompleted = true;
    }

    public void addSpawnItem(float x, float y, Class<? extends Item> type) {
        itemSpawnQueue.add(new SpawningItem(x, y, type));
    }

    private void handleSpawningItem() {
        if (itemSpawnQueue.size() > 0) {
            SpawningItem spawningItem = itemSpawnQueue.poll();

            if (spawningItem.type == Mushroom.class) {
                items.add(new Mushroom(this, spawningItem.x, spawningItem.y));
            }
            else if (spawningItem.type == Flower.class) {
                items.add(new Flower(this, spawningItem.x, spawningItem.y));
            }
            else if (spawningItem.type == Star.class) {
                items.add(new Star(this, spawningItem.x, spawningItem.y));
            }

        }
    }

    public void addSpawnEffect(float x, float y, Class<? extends Effect> type) {
        effectSpawnQueue.add(new SpawningEffect(x, y, type));
    }

    public void handleSpawningEffect() {
        if (effectSpawnQueue.size() > 0) {
            SpawningEffect spawningEffect = effectSpawnQueue.poll();

            if (spawningEffect.type == FlippingCoin.class) {
                effects.add(new FlippingCoin(this, spawningEffect.x, spawningEffect.y));
            }
            else if (spawningEffect.type == BrickDebris.class) {
                effects.add(new BrickDebris(this, spawningEffect.x, spawningEffect.y));
            }
        }
    }

    public void addSpawnFireball(float x, float y, boolean movingRight) {
        fireballSpawnQueue.add(new SpawningFireball(x, y, movingRight));
    }

    public void handleSpawningFireball() {
        if (fireballSpawnQueue.size() > 0) {
            SpawningFireball spawningFireball = fireballSpawnQueue.poll();
            fireballs.add(new Fireball(this, spawningFireball.x, spawningFireball.y, spawningFireball.movingRight));
        }
    }

    public void handleInput() {

        // press M to pause / play music
        if (Gdx.input.isKeyJustPressed(Input.Keys.M)) {
            if (GameManager.instance.isPlayingMusic()) {
                GameManager.instance.pauseMusic();
                playMusic = false;
            }
            else {
                GameManager.instance.resumeMusic();
                playMusic = true;
            }
        }

        // Press B to toggle Box2DDebuggerRenderer
        if (Gdx.input.isKeyJustPressed(Input.Keys.B)) {
            renderB2DDebug = !renderB2DDebug;
        }

        // Press F to toggle show FPS
        if (Gdx.input.isKeyJustPressed(Input.Keys.F)) {
            hud.setShowFPS(!hud.isShowFPS());
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.LEFT_BRACKET)) {
            float timeScale = GameManager.timeScale;
            GameManager.setTimeScale(timeScale - 0.2f);
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.RIGHT_BRACKET)) {
            float timeScale = GameManager.timeScale;
            GameManager.setTimeScale(timeScale + 0.2f);
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.EQUALS)) {
            GameManager.setTimeScale(1.0f);
        }
    }

    public void handleMusic() {
        if (!playMusic) {
            return;
        }

        if (mario.isDead()) {
            GameManager.instance.stopMusic();
        }
        else if (levelCompleted) {
            if (!flagpoleMusicPlay) {
                GameManager.instance.playMusic("flagpole.ogg", false);
                flagpoleMusicPlay = true;
            }
            else if (!GameManager.instance.isPlayingMusic("flagpole.ogg")) {
                if (!levelCompletedMusicPlay) {
                    GameManager.instance.playMusic("stage_clear.ogg", false);
                    levelCompletedMusicPlay = true;
                }
            }
        }
        else {
            if (mario.isInvincible()) {
                GameManager.instance.playMusic("invincible.ogg", true);
            }
            else if (hud.getTimeLeft() < 60) {
                if (!playingHurryMusic) {
                    GameManager.instance.playMusic("out_of_time.ogg", false);
                    playingHurryMusic = true;
                }
                else {
                    if (!GameManager.instance.isPlayingMusic("out_of_time.ogg")) {
                        GameManager.instance.playMusic("mario_music_hurry.ogg");
                    }
                }
            }
            else {
                GameManager.instance.playMusic("mario_music.ogg");
            }
        }
    }

    public void update(float delta) {
        delta *= GameManager.timeScale;
        float step = GameManager.STEP * GameManager.timeScale;

        handleInput();
        handleSpawningItem();
        handleSpawningEffect();
        handleSpawningFireball();
        handleMusic();

        if (hud.getTimeLeft() == 0) {
            mario.suddenDeath();
        }

        // Box2D world step
        accumulator += delta;
        if (accumulator > step) {
            world.step(step, 8, 3);
            accumulator -= step;
        }

        // update map tile objects
        for (MapTileObject mapTileObject : mapTileObjects) {
            mapTileObject.update(delta);
        }

        // update enemies
        for (Enemy enemy : enemies) {
            enemy.update(delta);
        }

        // update items
        for (Item item : items) {
            item.update(delta);
        }

        // update effects
        for (Effect effect : effects) {
            effect.update(delta);
        }

        // update fireballs
        for (Fireball fireball : fireballs) {
            fireball.update(delta);
        }

        // update Mario
        mario.update(delta);

        // camera control
        float targetX = camera.position.x;
        if (!mario.isDead()) {
            targetX = MathUtils.clamp(mario.getPosition().x, cameraLeftLimit, cameraRightLimit);
        }

        camera.position.x = MathUtils.lerp(camera.position.x, targetX, 0.1f);
        if (Math.abs(camera.position.x - targetX) < 0.1f) {
            camera.position.x = targetX;
        }
        camera.update();

        // update map renderer
        mapRenderer.setView(camera);

        // update ScoreIndicator
        scoreIndicator.update(delta);

        // update HUD
        hud.update(delta);

        // update levelCompletedStage
        if (levelCompleted) {
            levelCompletedStage.act(delta);
        }

        cleanUpDestroyedObjects();


        // check if Mario is dead
        if (mario.isDead()) {
            countDown -= delta;

            if (countDown < 0) {
                GameManager.instance.gameOver();
                game.setScreen(new GameOverScreen(game));
                dispose();
            }
        }
    }

    private void cleanUpDestroyedObjects() {
        /*
        for (int i = 0; i < mapTileObjects.size; i++) {
            if (mapTileObjects.get(i).isDestroyed()) {
                mapTileObjects.removeIndex(i);
            }
        }
        */

        for (int i = 0; i < items.size; i++) {
            if (items.get(i).isDestroyed()) {
                items.removeIndex(i);
            }
        }

        for (int i = 0; i < effects.size; i++) {
            if (effects.get(i).isDestroyed()) {
                effects.removeIndex(i);
            }
        }

        for (int i = 0; i < fireballs.size; i++) {
            if (fireballs.get(i).isDestroyed()) {
                fireballs.removeIndex(i);
            }
        }
    }

    public Vector2 getMarioPosition() {
        return mario.getPosition();
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0.2f, 0.2f, 0.2f, 1.0f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // draw map
        mapRenderer.render(new int[] {0, 1});

        // draw ScoreIndicator
        scoreIndicator.draw();

        game.batch.setProjectionMatrix(camera.combined);
        game.batch.begin();

        // draw map tile objects
        for (MapTileObject mapTileObject : mapTileObjects) {
            mapTileObject.draw(game.batch);
        }

        // draw effects
        for (Effect effect : effects) {
            effect.draw(game.batch);
        }

        // draw items
        for (Item item : items) {
            item.draw(game.batch);
        }

        // draw enemies
        for (Enemy enemy : enemies) {
            enemy.draw(game.batch);
        }

        // draw fireballs
        for (Fireball fireball : fireballs) {
            fireball.draw(game.batch);
        }

        // draw Mario
        mario.draw(game.batch);

        game.batch.end();

        // draw levelCompletedStage
        levelCompletedStage.draw();


        // draw HUD
        hud.draw();

        if (renderB2DDebug) {
            box2DDebugRenderer.render(world, camera.combined);
        }

        update(delta);

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
        hud.dispose();
        scoreIndicator.dispose();
        tiledMap.dispose();
        world.dispose();
        textureAtlas.dispose();
        box2DDebugRenderer.dispose();
        levelCompletedStage.dispose();
    }
}
