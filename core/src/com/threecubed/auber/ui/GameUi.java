package com.threecubed.auber.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.threecubed.auber.AuberGame;
import com.threecubed.auber.World;
import com.threecubed.auber.entities.Player;

import java.util.ArrayList;


public class GameUi {
  private static final int CHARGE_METER_WIDTH = 20;
  private static final int CHARGE_METER_MAX_HEIGHT = 100;
  private static final Vector2 CHARGE_METER_POSITION = new Vector2(50f, 50f);

  private static final Vector2 HEALTHBAR_POSITION = new Vector2(250f, 50f);
  private static final int HEALTHBAR_WIDTH = 20;
  private static final int HEALTHBAR_MAX_HEIGHT = 100;

  private static final Vector2 HEALTH_WARNINGS_POSITION = new Vector2(350f, 70f);

  private static final Vector2 SYSTEM_WARNINGS_POSITION = new Vector2(1750f, 50f);

  private static final Vector2 MESSAGES_POSITION = new Vector2(Gdx.graphics.getWidth()/2, 50f);
  //private static final Vector2 SPEED_BOOST_DURATION_POSITION = new Vector2(Gdx.graphics.getWidth()/2, 100f);
  private static final int MESSAGE_DURATION = 200;  //Measured in calls of update, not seconds.

  private ShapeRenderer shapeRenderer = new ShapeRenderer();

  private Sprite arrowSprite;
  private Color blindedColor = new Color(0f, 0f, 0f, 1f);

  private BitmapFont uiFont = new BitmapFont();

  private ArrayList<Message> messages = new ArrayList<>();

  public GameUi(AuberGame game) {
    arrowSprite = game.atlas.createSprite("arrow2");
  }

  /**
   * Render the different elements of the UI to the screen.
   *
   * @param world The game world.
   * @param screenBatch The batch to draw the UI to
   * */
  public void render(World world, SpriteBatch screenBatch) {
    if (world.player.blinded) {
      shapeRenderer.begin(ShapeType.Filled);
      shapeRenderer.setColor(blindedColor);
      shapeRenderer.rect(0f, 0f, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
      shapeRenderer.setColor(Color.WHITE);
      shapeRenderer.end();
    }

    drawChargeMeter(world, screenBatch);
    drawHealthbar(world, screenBatch);
    drawHealthWarnings(world, screenBatch);
    drawSystemWarnings(world, screenBatch);
    drawMessages(world, screenBatch);
    //displaySpeedBoostDuration(world, screenBatch);
  }

  /**
   * Draw the teleporter charge meter.
   *
   * @param world The game world
   * @param screenBatch The batch to draw to
   * */
  private void drawChargeMeter(World world, SpriteBatch screenBatch) {
    screenBatch.begin();
    uiFont.draw(screenBatch, "Teleporter Ray Charge",
        CHARGE_METER_POSITION.x,
        CHARGE_METER_POSITION.y + (CHARGE_METER_MAX_HEIGHT / 2));
    screenBatch.end();

    float chargeMeterHeight = world.auberTeleporterCharge * CHARGE_METER_MAX_HEIGHT;
    
    // Make the charge meter green if weapon is charged
    if (chargeMeterHeight > CHARGE_METER_MAX_HEIGHT * 0.95f) {
      shapeRenderer.setColor(Color.GREEN);
    } else {
      shapeRenderer.setColor(Color.RED);
    }

    shapeRenderer.begin(ShapeType.Filled);
    shapeRenderer.rect(CHARGE_METER_POSITION.x + 160f, CHARGE_METER_POSITION.y, CHARGE_METER_WIDTH,
        chargeMeterHeight);
    // Draw background (fill remaining space)
    shapeRenderer.setColor(Color.DARK_GRAY);
    shapeRenderer.rect(CHARGE_METER_POSITION.x + 160f, CHARGE_METER_POSITION.y+chargeMeterHeight, CHARGE_METER_WIDTH,
            CHARGE_METER_MAX_HEIGHT-chargeMeterHeight);
    shapeRenderer.end();
  }

  private void drawHealthbar(World world, SpriteBatch screenBatch) {
    screenBatch.begin();
    uiFont.draw(screenBatch, "Health",
        HEALTHBAR_POSITION.x,
        HEALTHBAR_POSITION.y + (HEALTHBAR_MAX_HEIGHT / 2));
    screenBatch.end();

    float healthbarHeight = world.player.health * HEALTHBAR_MAX_HEIGHT;

    // Set the healthbar colour based on amount of health
    if (healthbarHeight > HEALTHBAR_MAX_HEIGHT * 0.8f) {
      shapeRenderer.setColor(Color.GREEN);
    } else if (healthbarHeight > HEALTHBAR_MAX_HEIGHT * 0.5) {
      shapeRenderer.setColor(Color.ORANGE);
    } else {
      shapeRenderer.setColor(Color.RED);
    }

    shapeRenderer.begin(ShapeType.Filled);
    // Draw the bar
    shapeRenderer.rect(HEALTHBAR_POSITION.x + 60f, HEALTHBAR_POSITION.y, HEALTHBAR_WIDTH,
        healthbarHeight);
    // Draw background (fill remaining space)
    shapeRenderer.setColor(Color.DARK_GRAY);
    shapeRenderer.rect(HEALTHBAR_POSITION.x + 60f, HEALTHBAR_POSITION.y+healthbarHeight, HEALTHBAR_WIDTH,
        HEALTHBAR_MAX_HEIGHT-healthbarHeight);
    shapeRenderer.end();
  }

  /**
   * Draw any health status warnings for Auber to the screen.
   *
   * @param world The game world
   * @param screenBatch The batch to draw to
   * */
  private void drawHealthWarnings(World world, SpriteBatch screenBatch) {
    screenBatch.begin();
    uiFont.setColor(Color.RED);
    if (world.player.confused) {
      uiFont.draw(screenBatch, "CONFUSED", HEALTH_WARNINGS_POSITION.x, HEALTH_WARNINGS_POSITION.y);
    }
    if (world.player.slowed) {
      uiFont.draw(screenBatch, "SLOWED", HEALTH_WARNINGS_POSITION.x,
          HEALTH_WARNINGS_POSITION.y + 20f);
    }
    if (world.player.blinded) {
      uiFont.draw(screenBatch, "BLINDED", HEALTH_WARNINGS_POSITION.x,
          HEALTH_WARNINGS_POSITION.y + 40f);
    }
    uiFont.setColor(Color.WHITE);
    screenBatch.end();
  }

  /**
   * Draw the state of each system to the screen.
   *
   * @param world The world object
   * @param screenBatch The batch to draw to
   * */
  private void drawSystemWarnings(World world, SpriteBatch screenBatch) {
    screenBatch.begin();
    int offset = 0;
    for (RectangleMapObject system : world.systems) {
      Rectangle systemRectangle = system.getRectangle();
      Vector2 systemAngleVector = new Vector2(systemRectangle.getX() - world.player.position.x,
                                              systemRectangle.getY() - world.player.position.y);

      arrowSprite.setPosition(SYSTEM_WARNINGS_POSITION.x - 20f, SYSTEM_WARNINGS_POSITION.y
          + offset - 10f);
      arrowSprite.setRotation(systemAngleVector.angleDeg() - 90f);
      arrowSprite.draw(screenBatch);

      String systemName = system.getName();
      // No need for DESTROYED case as when system destroyed, removed from systems list
      switch (world.getSystemState(system)) {
        case WORKING:
          uiFont.setColor(Color.GREEN);
          break;
        case ATTACKED:
          uiFont.setColor(Color.RED);
          break;
        default:
          break;
      }

      uiFont.draw(screenBatch, systemName, SYSTEM_WARNINGS_POSITION.x,
          SYSTEM_WARNINGS_POSITION.y + offset);
      offset += 25f;

    }
    uiFont.setColor(Color.WHITE);
    screenBatch.end();
  }


  /**
   * Represents a message to be displayed in the message system
   */
  private class Message {
    String text;
    int timeToDisplay;
    int remainingTime;

    public Message(String text, int timeToDisplay){
      this.text = text;
      this.timeToDisplay = timeToDisplay;
      this.remainingTime = timeToDisplay;
    }
  }

  /**
   * When called, removes any expired messages from the list, and draws all others.
   *
   * @param world The world object
   * @param screenBatch The batch to draw to
   */
  private void drawMessages(World world, SpriteBatch screenBatch){
    screenBatch.begin();
    uiFont.setColor(Color.WHITE);
    int offset = 0;

    int i = 0;
    while (i < messages.size()){
      Message message = messages.get(i);
      if (message.remainingTime < 0){
        messages.remove(i);
      } else {
        message.remainingTime -= 1;
        uiFont.draw(screenBatch, message.text, MESSAGES_POSITION.x,
                MESSAGES_POSITION.y + offset);
        offset += 25f;
        i++;
      }
    }
    screenBatch.end();
  }

  /**
   * Adds a given message to the list of messages to display, with the default duration.
   *
   * @param text String containing message to be displayed
   */
  public void queueMessage(String text){
    Message message = new Message(text, MESSAGE_DURATION);
    messages.add(message);
  }
}
