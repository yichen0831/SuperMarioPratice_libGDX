package com.ychstudio.hud;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.Pool;
import com.ychstudio.gamesys.GameManager;
import com.ychstudio.screens.PlayScreen;

/**
 * Created by yichen on 10/19/15.
 *
 * ScoreIndicator
 */
public class ScoreIndicator implements Disposable {

    class ScoreItem implements Pool.Poolable {
        float x;
        float y;
        float life;
        String score;

        public ScoreItem() {
            x = 0;
            y = 0;
            life = 0;
            score = "";
        }

        public void init(float x, float y, int score) {
            this.x = x;
            this.y = y;
            this.score = Integer.toString(score);
            life = 0.8f;
        }

        @Override
        public void reset() {
            life = 0;
        }
    }

    private BitmapFont font; // TODO: use TTF
    private PlayScreen playScreen;
    private SpriteBatch batch;

    private OrthographicCamera camera;

    private Pool<ScoreItem> scoreItemPool;
    private Array<ScoreItem> scoreItems;

    private float RATIO;

    public ScoreIndicator(PlayScreen playScreen, SpriteBatch batch) {
        this.playScreen = playScreen;
        this.batch = batch;

        RATIO = GameManager.WINDOW_WIDTH / GameManager.V_WIDTH;

        camera = new OrthographicCamera(GameManager.WINDOW_WIDTH, GameManager.WINDOW_HEIGHT);
        camera.position.set(playScreen.getCamera().position.x * RATIO, GameManager.WINDOW_HEIGHT / 2, 0);


        font = new BitmapFont(Gdx.files.internal("fonts/Fixedsys500c.fnt"));
        font.getData().setScale(1.2f);

        scoreItemPool = new Pool<ScoreItem>() {
            @Override
            protected ScoreItem newObject() {
                return new ScoreItem();
            }
        };

        scoreItems = new Array<ScoreItem>();

    }

    public void addScoreItem(float x, float y, int score) {
        ScoreItem scoreItem = scoreItemPool.obtain();
        scoreItem.init(x * RATIO, (y + 1.5f) * RATIO, score);
        scoreItems.add(scoreItem);

    }

    public void update(float delta) {
        for (int i = 0; i < scoreItems.size; i++) {
            scoreItems.get(i).life -= delta;
            scoreItems.get(i).y += 1.2;
            if (scoreItems.get(i).life < 0) {
                scoreItemPool.free(scoreItems.get(i));
                scoreItems.removeIndex(i);
            }
        }
    }

    public void draw() {
        camera.position.x = playScreen.getCamera().position.x * RATIO;
        camera.update();

        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        for (ScoreItem scoreItem : scoreItems) {
            font.draw(batch, scoreItem.score, scoreItem.x, scoreItem.y);
        }
        batch.end();

    }


    @Override
    public void dispose() {
        font.dispose();
    }
}
