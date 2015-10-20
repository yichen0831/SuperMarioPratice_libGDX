package com.ychstudio.hud;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.ychstudio.gamesys.GameManager;

/**
 * Created by yichen on 10/12/15.
 *
 * Hud
 */
public class Hud implements Disposable {

    class CoinHUD extends Actor {

        private Animation anim;
        private float stateTime;

        public CoinHUD(TextureAtlas textureAtlas) {

            Array<TextureRegion> keyFrames = new Array<TextureRegion>();
            for (int i = 0; i < 3; i++) {
                keyFrames.add(new TextureRegion(textureAtlas.findRegion("CoinHUD"), i * 8, 0, 8, 8));
            }
            anim = new Animation(0.3f, keyFrames);

            setSize(16, 16);
            stateTime = 0;
        }

        @Override
        protected void positionChanged() {
            super.positionChanged();

        }

        @Override
        public void draw(Batch batch, float parentAlpha) {
            batch.draw(anim.getKeyFrame(stateTime, true), getX(), getY());
        }

        @Override
        public void act(float delta) {
            stateTime += delta;
        }
    }


    private Stage stage;

    private int timeLeft;

    private Label scoreLabel;
    private Label timeLabel;
    private Label levelLabel;

    private Label coinCountLabel;

    private boolean showFPS;
    private Label fpsLabel;

    private float fpsTimeAccumulator;
    private float accumulator;

    private BitmapFont font;

    private TextureAtlas textureAtlas;

    public Hud(SpriteBatch batch) {

        Viewport viewport = new FitViewport(GameManager.WINDOW_WIDTH / 1.5f, GameManager.WINDOW_HEIGHT / 1.5f, new OrthographicCamera());
        stage = new Stage(viewport, batch);

        timeLeft = 300;

        font = new BitmapFont(Gdx.files.internal("fonts/Fixedsys500c.fnt"));
        Label.LabelStyle style = new Label.LabelStyle(font, Color.WHITE);

        Label scoreTextLabel = new Label("SCORE", style);
        Label timeTextLabel = new Label("TIME", style);
        Label levelTextLabel = new Label("WORLD", style);

        scoreLabel = new Label("", style);
        timeLabel = new Label(intToString(timeLeft, 3), style);
        levelLabel = new Label("1-1", style);

        textureAtlas = new TextureAtlas("imgs/actors.atlas");
        CoinHUD coin = new CoinHUD(textureAtlas);


        Table table = new Table();
        table.top();
        table.setFillParent(true);

        table.add(scoreTextLabel).expandX().padTop(6.0f);
        table.add();
        table.add(levelTextLabel).expandX().padTop(6.0f);
        table.add(timeTextLabel).expandX().padTop(6.0f);

        table.row();

        table.add(scoreLabel).expandX();

        // coin count
        Table table1 = new Table();
        coinCountLabel = new Label("x00", style);
        table1.add(coin);
        table1.add(coinCountLabel);
        table.add(table1).expandX();

        table.add(levelLabel).expandX();
        table.add(timeLabel).expandX();

        table.row();

        // FPS
        fpsLabel = new Label("FPS:    ", style);
        Table fpsTable = new Table();
        fpsTable.add(fpsLabel);
        table.add(fpsTable).expand().bottom();

        stage.addActor(table);

        accumulator = 0;
        fpsTimeAccumulator = 0;
        showFPS = false;
    }

    public void setLevel(String level) {
        levelLabel.setText(level);
    }

    public boolean isShowFPS() {
        return showFPS;
    }

    public int getTimeLeft() {
        return timeLeft;
    }

    public void setShowFPS(boolean value) {
        showFPS = value;
    }

    public void draw() {
        scoreLabel.setText(intToString(GameManager.instance.getScore(), 6));
        stage.draw();

    }

    public void update(float delta) {
        accumulator += delta;

        fpsLabel.setVisible(showFPS);

        if (showFPS) {
            fpsTimeAccumulator += delta;
            if (fpsTimeAccumulator > 0.2) {
                fpsLabel.setText("FPS: " + intToString((int) (1 / delta * GameManager.timeScale), 3));
                fpsTimeAccumulator = 0;
            }
        }

        if (accumulator > 1.0f) {
            if (timeLeft > 0)
                timeLeft -= 1;
            accumulator -= 1.0f;
            timeLabel.setText(intToString(timeLeft, 3));
        }

        coinCountLabel.setText("x" + intToString(GameManager.instance.getCoins(), 2));

        stage.act(delta);

    }

    private String intToString(int value, int length) {
        String valueStr = Integer.toString(value);
        StringBuilder result = new StringBuilder();
        if (valueStr.length() < length) {
            for (int i = 0; i < length - valueStr.length(); i++) {
                result.append(0);
            }
        }
        result.append(valueStr);
        return result.toString();
    }

    @Override
    public void dispose() {
        font.dispose();
        stage.dispose();
        textureAtlas.dispose();
    }
}
