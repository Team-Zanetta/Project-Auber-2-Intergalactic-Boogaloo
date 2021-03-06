package com.threecubed.auber.entities;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Timer.Task;
import com.threecubed.auber.Utils;
import com.threecubed.auber.World;


/**
 * The infiltrator is the enemy of the game, it will navigate from system to system and sabotage
 * them until caught by the {@link Player}.
 *
 * @author Daniel O'Brien
 * @version 1.0
 * @since 1.0
 * */
public class Infiltrator extends Npc {
  public boolean exposed = false;
  Sprite unexposedSprite;

  /**
   * Initialise an infiltrator at given coordinates.
   *
   * @param x The x position of the infiltrator
   * @param y The y position of the infiltrator
   * @param world The game world
   * */
  public Infiltrator(float x, float y, World world) {
    super(x, y, world);
    setEntityType(2);
    navigateToRandomSystem(world);

  }

  /**
   * Initialise the infiltrator at a random position.
   *
   * @param world The game world
   * */
  public Infiltrator(World world) {
    super(world);
    navigateToRandomSystem(world);
    unexposedSprite = new Sprite(sprite);
    setEntityType(2);
  }

  @Override
  public void update(World world) {
    super.update(world);
    if (exposed && !entityOnScreen(world)) {
      exposed = false;
      sprite = unexposedSprite;
    }
  }

  @Override
  public void handleDestinationReached(World world) {
    States oldState = state;
    state = States.IDLE;

    // Infiltrator won't try and sabotage a system if it has just been fleeing.
    if (oldState != States.FLEEING) {
      if (!playerNearby(world)
          && Utils.randomFloatInRange(world.randomNumberGenerator, 0, 1)
          < World.SYSTEM_SABOTAGE_CHANCE) {
        attackNearbySystem(world);
      } else {
        idleForGivenTime(world, Utils.randomFloatInRange(world.randomNumberGenerator, 5f, 8f));
      }
    }
  }

  @Override
  public void handleTeleporterShot(final World world) {
    if (state == States.ATTACKING_SYSTEM) {
      RectangleMapObject system = getNearbyObjects(World.map);
      if (system != null) {
        Rectangle boundingBox = system.getRectangle();
        world.updateSystemState(boundingBox.x, boundingBox.y, World.SystemStates.WORKING);
      }
    }


    //If the player has the stronger ray power-up, we act as if the infiltrator is already exposed.
    if(world.player.strongerRay){
      world.ui.queueMessage("Stronger Ray used on infiltrator");
      world.player.strongerRay = false;
      exposed = true;
    }

    if(exposed) {
      position.x = Utils.randomFloatInRange(world.randomNumberGenerator,
              World.BRIG_BOUNDS[0][0], World.BRIG_BOUNDS[1][0]);
      position.y = Utils.randomFloatInRange(world.randomNumberGenerator,
              World.BRIG_BOUNDS[0][1], World.BRIG_BOUNDS[1][1]);
      aiEnabled = false;
    } else {
      exposed = true;
      fireProjectileAtPlayer(world);
      sprite = world.atlas.createSprite("infiltrator");
      state = States.FLEEING;
      navigateToFurthestPointFromPlayer(world);
      npcTimer.scheduleTask(new Task() {
        @Override
        public void run() {
          if (exposed) {
            fireProjectileAtPlayer(world);
          } else {
            cancel();
          }
        }
      }, World.INFILTRATOR_FIRING_INTERVAL, World.INFILTRATOR_FIRING_INTERVAL);
    }
  }

  /**
   * Attack a system nearby to the infiltrator.
   * */
  private void attackNearbySystem(final World world) {
    state = States.ATTACKING_SYSTEM;

    final RectangleMapObject system = getNearbyObjects(World.map);
    if (system != null) {
      world.updateSystemState(system.getRectangle().getX(), system.getRectangle().getY(),
          World.SystemStates.ATTACKED);

      npcTimer.scheduleTask(new Task() {
        @Override
        public void run() {
          if (aiEnabled) {
            world.updateSystemState(system.getRectangle().getX(), system.getRectangle().getY(),
                World.SystemStates.DESTROYED);
            navigateToRandomSystem(world);
          }
        }
      }, world.SYSTEM_BREAK_TIME);
    }
  }

  @Override
  public void navigateToNearestFleepoint(final World world) {
    if (aiEnabled) {
      if (state == States.ATTACKING_SYSTEM) {
        RectangleMapObject system = getNearbyObjects(World.map);
        if (system != null) {
          Rectangle boundingBox = system.getRectangle();
          world.updateSystemState(boundingBox.x, boundingBox.y, World.SystemStates.WORKING);  
        }
      }
      super.navigateToNearestFleepoint(world);
    }
  }

  private boolean playerNearby(World world) {
    if (world.demoMode) {
      return false;
    }
    Circle infiltratorSight = new Circle(position, World.INFILTRATOR_SIGHT_RANGE);
    if (infiltratorSight.contains(world.player.position)) {
      return true;
    }
    return false;
  }

  private void fireProjectileAtPlayer(World world) {
    Vector2 projectileVelocity = new Vector2(world.player.position.x - position.x,
                                             world.player.position.y - position.y);
    projectileVelocity.setLength(World.INFILTRATOR_PROJECTILE_SPEED);
    Projectile projectile = new Projectile(getCenterX(), getCenterY(), projectileVelocity, this,
        Projectile.CollisionActions.randomAction(), world);
    world.queueEntityAdd(projectile);
  } 
}
