import com.almasb.fxgl.app.ApplicationMode;
import com.almasb.fxgl.app.GameApplication;
import com.almasb.fxgl.app.GameSettings;
import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.input.Input;
import com.almasb.fxgl.input.UserAction;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Map;

import static com.almasb.fxgl.dsl.FXGL.*;

public class Game extends GameApplication {

    private Entity tank;
    private Entity enemyTank;
    private Entity bullet;
    private Entity enemyBullet;
    private int count;
    private int inp;
    private boolean bulletShot1 = false;
    private boolean bulletShot2 = false;
    private String playerTurn;
    private int playerTurnNum = FXGL.random(0, 1);
    private int player1Health = 100;
    private int player2Health = 100;
    private int player1Hits = 0;
    private int player2Hits = 0;
    private boolean doubleDamageOn = false;
    private boolean powerUpUsed = false;

    private ArrayList<Double> universalY = new ArrayList<Double>();
    private ArrayList<Double> newYs = new ArrayList<Double>();
    private ArrayList<Entity> rocks = new ArrayList<>();
    private ArrayList<Entity> bullets = new ArrayList<>();
    private ArrayList<Coordinates> bulletCoords = new ArrayList<>();


    public enum Type {
        Tank, EnemyTank, Bullet, EnemyBullet, Barrier, Grass
    }


    @Override
    protected void initSettings(GameSettings settings) {
        //main method that initializes title, width, height, main and gamemenu
        settings.setTitle("Tank Battle");
        settings.setVersion("1.0");
        settings.setWidth(1800);
        settings.setHeight(1000);
        settings.setApplicationMode(ApplicationMode.DEVELOPER);
        settings.setMainMenuEnabled(true);
        settings.setGameMenuEnabled(true);

    }

    @Override
    protected void initGame() {
        //initGame runs at beginning of Game to initializes the entities and other starting methods
        setYCoords();
        spawnTank();
        spawnBarrier();
        spawnGrass();
        chooseTurn();
    }

    private void chooseTurn() {
        //creates a random number from 0 - 1 which is then used later on to decide which player's turn it is
        playerTurnNum = FXGL.random(0, 1);
    }

    @Override
    protected void initUI() {
        //assigns a font, color, and message to a text var and sets its x and y coordinates (player 1's health)
        var health1Text = getUIFactoryService().newText("", Color.BLACK, 24.0);
        health1Text.setTranslateX(60);
        health1Text.setTranslateY(100);
        //tells the variable what text to be assigned to with whatever variable values that are used
        health1Text.textProperty().bind(getip("health1").asString("Player 1 Health: %d"));

        //assigns a font, color, and message to a text var and sets its x and y coordinates (player 2's health)
        var health2Text = getUIFactoryService().newText("", Color.BLACK, 24.0);
        health2Text.setTranslateX(1500);
        health2Text.setTranslateY(100);
        //tells the variable what text to be assigned to with whatever variable values that are used
        health2Text.textProperty().bind(getip("health2").asString("Player 2 Health: %d"));

        //assigns a font, color, and message to a text var and sets its x and y coordinates (displays whoever's turn it is)
        var turnText = getUIFactoryService().newText("", Color.BLACK, 24.0);
        turnText.setTranslateX(815);
        turnText.setTranslateY(100);
        //tells the variable what text to be assigned to with whatever variable values that are used
        turnText.textProperty().bind(getip("turn").asString("Player %d 's turn"));

        //adds these text variables to the actual screen to be displayed
        getGameScene().addUINodes(health1Text, health2Text, turnText);

    }

    @Override
    protected void initGameVars(Map<String, Object> vars) {
        //each of these lines assigns a var name to a value that is associated with it (these var names are used in the above initUI method to display info)
        vars.put("health1", player1Health);
        vars.put("health2", player2Health);
        vars.put("turn", (playerTurnNum % 2 + 1));
    }

    private void setYCoords() {
        bulletCoords.clear();
        //runs a for from 200-1200 and adds y values to an array list based on plugging in the x values into a formula (I experimented with points that I wanted on a website which then generated a formula for me for this parabola)
        //(these are just the universal y coords which are then edited later based on what arc the player wants their bullet to go in
        for (double x = 200; x < 200 + 1010; x += 10) {
            universalY.add((((Math.pow(x, 2) * (29 - inp))) / 25000) - ((203 * x) / 125) + (3567 / 5));
        }

    }

    protected void initInput() {
        Input input = getInput();

        input.addAction(new UserAction("Player 1 Tank Move Left") {
            @Override
            //(even is player 1 turn, odd is player 2 turn)
            //(A key)
            protected void onAction() {
                //checks if it is player 1s turn (the player can only move on their turn)
                if (playerTurnNum % 2 == 0) {
                    //subtracts 5 from the tanks x position every time key is pressed (left)
                    tank.setX(tank.getX() - 5);
                    //this if makes sure the tank does not go out of bounds
                    if (tank.getX() < -260) {
                        tank.setX(tank.getX() + 5);
                    }
                }

            }
        }, KeyCode.A);
        input.addAction(new UserAction("Player 1 Tank Move Right") {
            @Override
            //(D key)
            protected void onAction() {
                //checks if it is player 1s turn (the player can only move on their turn)
                if (playerTurnNum % 2 == 0) {
                    //adds 5 from the tanks x position every time key is pressed (right)
                    tank.setX(tank.getX() + 5);
                    //this if makes sure the tank does not go out of bounds
                    if (tank.getX() > 565) {
                        tank.setX(tank.getX() - 5);
                    }
                }
            }
        }, KeyCode.D);


        input.addAction(new UserAction("Player 2 Tank Move Left") {
            @Override
            //(left arrow key)

            protected void onAction() {
                //checks if it is player 2s turn (the player can only move on their turn)
                if (playerTurnNum % 2 == 1) {
                    //subtracts 5 from the tanks x position every time key is pressed (left)
                    enemyTank.setX(enemyTank.getX() - 5);
                    //this if makes sure the tank does not go out of bounds
                    if (enemyTank.getX() < 370) {
                        enemyTank.setX(enemyTank.getX() + 5);
                    }
                }
            }
        }, KeyCode.LEFT);
        input.addAction(new UserAction("Player 2 Tank Move Right") {
            @Override
            //(right arrow key)
            protected void onAction() {
                //checks if it is player 2s turn (the player can only move on their turn)
                if (playerTurnNum % 2 == 1) {
                    //adds 5 from the tanks x position every time key is pressed (right)
                    enemyTank.setX(enemyTank.getX() + 5);
                    //this if makes sure the tank does not go out of bounds
                    if (enemyTank.getX() > 1205) {
                        enemyTank.setX(enemyTank.getX() - 5);
                    }
                }
            }
        }, KeyCode.RIGHT);

        input.addAction(new UserAction("Shoot Bullet") {
            @Override
            //(left mouse click)
            protected void onAction() {

                    spawnBullet();
                    //gives player a prompt until their input is between 1 and 5
                    while (inp < 1 || inp > 5) {
                        inp = Integer.parseInt(JOptionPane.showInputDialog("Enter Bullet Arc (1-5) (1 is a lower arc and as the number gets higher so does the arc)"));
                    }
                    //based on which arc the player chooses (1-5), the universal y coords are subtracted or added from to make the arc bigger or smaller (1 small arc - 5 big arc)
                    //these new y coords are then added to a newYs array so that each time it can take the universal y coords, add them to the newYs array and then use them on each of the moves of the players
                    if (inp == 1) {
                        for (int i = 0; i < universalY.size(); i++) {
                            newYs.add(universalY.get(i) + 10);
                        }
                    }
                    if (inp == 2) {
                        newYs.addAll(universalY);
                    }
                    if (inp == 3) {
                        for (int i = 0; i < universalY.size(); i++) {
                            newYs.add(universalY.get(i) - 50);
                        }
                    }
                    if (inp == 4) {
                        for (int i = 0; i < universalY.size(); i++) {
                            newYs.add(universalY.get(i) - 100);
                        }
                    }
                    if (inp == 5) {
                        for (int i = 0; i < universalY.size(); i++) {
                            newYs.add(universalY.get(i) - 125);
                        }
                    }
                    //checks if it is player 1s turn
                    if (playerTurnNum % 2 == 0) {
                        double o = bullet.getX();
                        int count = 0;
                        bulletCoords.clear();
                        //runs a loop from wherever the bullet is to 1010 x values away in 10 increments
                        for (double x = o; x < o + 1010; x += 10) {
                            //these Xs and the Ys from the pregenerated Ys array newYs are taken and added to a bulletCoords arraylist of Coordinate objects (gives the bullet path a parabola shape)
                            bulletCoords.add(new Coordinates(x, newYs.get(count)));
                            count++;
                        }
                        bulletShot1 = true;
                        //checks if it is player 2s turn
                    } else if (playerTurnNum % 2 == 1) {
                        double o = enemyBullet.getX();
                        int count = 0;
                        bulletCoords.clear();
                        //runs a loop from wherever the bullet is to 1010 x values away in 10 increments
                        for (double x = o; x > o - 1010; x -= 10) {
                            //these Xs and the Ys from the pregenerated Ys array newYs are taken and added to a bulletCoords arraylist of Coordinate objects (gives the bullet path a parabola shape)
                            bulletCoords.add(new Coordinates(x, newYs.get(count)));
                            count++;

                        }
                        bulletShot2 = true;
                }
            }
        }, MouseButton.PRIMARY);
        //input.clearAll();
        input.addAction(new UserAction("Double Damage Power Up") {
            @Override
            //(Z key)
            protected void onAction() {
                //this boolean ensures the powerup is only used once (after the powerup is clicked on the powerUpUsed is set to true not allowing it to be pressed again)
                if (!powerUpUsed) {
                    doubleDamageOn = true;
                    powerUpUsed = true;
                }
            }
        }, KeyCode.Z);
    }

    protected void onUpdate(double tpf) {
        //listener that is constantly checking the variable values of the var health1 to see if player 1s health goes below 0
        getWorldProperties().<Integer>addListener("health1", (prev, now) -> {
            if (now <= 0)
                gameOver();

        });
        //listener that is constantly checking the variable values of the var health3 to see if player 2s health goes below 0
        getWorldProperties().<Integer>addListener("health2", (prev, now) -> {
            if (now <= 0)
                gameOver();
        });
        //checks to see if left mouse has been clicked (player 1's bullet)
        if (bulletShot1) {
            //count is a variable that represents the indexes of the bulletCoords array and the if checks to see if count is greater than or equal to 101 which is the size of bulletCoords array
            if (count >= 101) {
                //if count is equal greater than or equal to 101 all the coords in the array have been used and it removes the bullet, clears the bulletCoords array, the newY coords array, and resets and count and input variables
                getGameWorld().getEntitiesByType(Type.Bullet).forEach(Entity::removeFromWorld);
                bulletCoords.clear();
                newYs.clear();
                inp = 0;
                count = 0;
                bulletShot1 = false;
                //checks to see if doubleDamageOn boolean is true (means powerup was on but since the bullet didn't hit anything this will do nothing except make it false)
                if (doubleDamageOn){
                    doubleDamageOn = false;
                }
                //this is used to keep track of the player's turn and the turn text var is set to the playerTurn mod 2 + 1 which represents the player
                playerTurnNum++;
                set("turn", playerTurnNum % 2 + 1);
            } else {
                //if count is less than 101 (means that coords in bulletCoords have not been used yet)
                //then it sets the bullets x and y to the x and y coords from the array and does this until all coords are used
                getGameWorld().getEntitiesByType(Type.Bullet).forEach(bullet -> bullet.setX(bulletCoords.get(count).getX()));
                getGameWorld().getEntitiesByType(Type.Bullet).forEach(bullet -> bullet.setY(bulletCoords.get(count).getY()));
                count++;
            }
        }
        //checks to see if left mouse has been clicked (player 2's bullet is called enemyBullet)
        if (bulletShot2) {
            //count is a variable that represents the indexes of the bulletCoords array and the if checks to see if count is greater than or equal to 101 which is the size of bulletCoords array
            if (count >= 101) {
                //if count is equal greater than or equal to 101 all the coords in the array have been used and it removes the bullet, clears the bulletCoords array, the newY coords array, and resets and count and input variables
                getGameWorld().getEntitiesByType(Type.EnemyBullet).forEach(Entity::removeFromWorld);
                bulletCoords.clear();
                newYs.clear();
                inp = 0;
                count = 0;
                bulletShot2 = false;
                //checks to see if doubleDamageOn boolean is true (means powerup was on but since the bullet didn't hit anything this will do nothing except make it false)
                if (doubleDamageOn){
                    doubleDamageOn = false;
                }
                //this is used to keep track of the player's turn and the turn text var is set to the playerTurn mod 2 + 1 which represents the player
                playerTurnNum++;
                set("turn", playerTurnNum % 2 + 1);
            } else {
                //if count is less than 101 (means that coords in bulletCoords have not been used yet)
                //then it sets the bullets x and y to the x and y coords from the array and does this until all coords are used
                getGameWorld().getEntitiesByType(Type.EnemyBullet).forEach(enemyBullet -> enemyBullet.setX(bulletCoords.get(count).getX()));
                getGameWorld().getEntitiesByType(Type.EnemyBullet).forEach(enemyBullet -> enemyBullet.setY(bulletCoords.get(count).getY()));
                count++;
            }
        }
    }


    protected void initPhysics() {
        //in the event of collision between bullet and grass, bullet is removed, bullet coord and new Y arrays are cleared and the count and input vars a reset to 0
        onCollision(Type.Bullet, Type.Grass, (bullet, grass) -> {
            getGameWorld().getEntitiesByType(Type.Bullet).forEach(Entity::removeFromWorld);
            bulletCoords.clear();
            newYs.clear();
            inp = 0;
            count = 0;
            bulletShot1 = false;
            //checks to see if doubleDamageOn boolean is true (means powerup was on but since the bullet hit grass and not a tank this does nothing except make it false)
            if (doubleDamageOn){
                doubleDamageOn = false;
            }
            //this is used to keep track of the player's turn and the turn text var is set to the playerTurn mod 2 + 1 which represents the player
            playerTurnNum++;
            set("turn", playerTurnNum % 2 + 1);
        });
        //in the event of collision between enemybullet and grass, bullet is removed, bullet coord and new Y arrays are cleared and the count and input vars a reset to 0
        onCollision(Type.EnemyBullet, Type.Grass, (enemyBullet, grass) -> {
            getGameWorld().getEntitiesByType(Type.EnemyBullet).forEach(Entity::removeFromWorld);
            bulletCoords.clear();
            newYs.clear();
            inp = 0;
            count = 0;
            bulletShot2 = false;
            //checks to see if doubleDamageOn boolean is true (means powerup was on but since the bullet hit grass and not a tank this does nothing except make it false)
            if (doubleDamageOn){
                doubleDamageOn = false;
            }
            //this is used to keep track of the player's turn and the turn text var is set to the playerTurn mod 2 + 1 which represents the player
            playerTurnNum++;
            set("turn", playerTurnNum % 2 + 1);
        });
        //in the event of collision between bullet (player 1 bullet) and enemyTank (player 2 tank), bullet is removed, bullet coord and new Y arrays are cleared and the count and input vars a reset to 0
        onCollision(Type.Bullet, Type.EnemyTank, (bullet, enemyTank) -> {
            getGameWorld().getEntitiesByType(Type.Bullet).forEach(Entity::removeFromWorld);
            bulletCoords.clear();
            newYs.clear();
            inp = 0;
            count = 0;
            bulletShot1 = false;
            //checks to see if doubleDamageOn boolean is true (means powerup was on and will subtract a double random damage value than if the power was not on)
            if (doubleDamageOn){
                player2Health = player2Health - FXGL.random(20,40);
                doubleDamageOn = false;
            } else {
                //when powerup is not on a regular random damage value is generated to be subtracted from player 2 health
                player2Health = player2Health - FXGL.random(10, 20);
            }
            //this is used to keep track of the player's health and the health2 text var is set to player 2s updated health
            set("health2", player2Health);
            player1Hits++;
            //this is used to keep track of the player's turn and the turn text var is set to the playerTurn mod 2 + 1 which represents the player
            playerTurnNum++;
            set("turn", playerTurnNum % 2 + 1);
        });
        //in the event of collision between enemybullet (player 2 bullet) and tank (player 1 tank), bullet is removed, bullet coord and new Y arrays are cleared and the count and input vars a reset to 0
        onCollision(Type.EnemyBullet, Type.Tank, (enemyBullet, tank) -> {
            getGameWorld().getEntitiesByType(Type.EnemyBullet).forEach(Entity::removeFromWorld);
            bulletCoords.clear();
            newYs.clear();
            inp = 0;
            count = 0;
            bulletShot2 = false;
            //checks to see if doubleDamageOn boolean is true (means powerup was on and will subtract a double random damage value than if the power was not on)
            if (doubleDamageOn){
                player1Health = player1Health - FXGL.random(20,40);
                doubleDamageOn = false;
            } else {
                //when powerup is not on a regular random damage value is generated to be subtracted from player 1 health
                player1Health = player1Health - FXGL.random(10, 20);
            }
            //this is used to keep track of the player's health and the health1 text var is set to player 1s updated health
            set("health1", player1Health);
            player2Hits++;
            //this is used to keep track of the player's turn and the turn text var is set to the playerTurn mod 2 + 1 which represents the player
            playerTurnNum++;
            set("turn", playerTurnNum % 2 + 1);
        });
        //in the event of collision between bullet (player 1 bullet) and middle barrier, bullet is removed, bullet coord and new Y arrays are cleared and the count and input vars a reset to 0
        onCollision(Type.Bullet, Type.Barrier, (bullet, barrier) -> {
            getGameWorld().getEntitiesByType(Type.Bullet).forEach(Entity::removeFromWorld);
            bulletCoords.clear();
            newYs.clear();
            inp = 0;
            count = 0;
            bulletShot1 = false;
            //checks to see if doubleDamageOn boolean is true (means powerup was on but since the bullet hit barrier and not a tank this does nothing except make it false)
            if (doubleDamageOn){
                doubleDamageOn = false;
            }
            //this is used to keep track of the player's turn and the turn text var is set to the playerTurn mod 2 + 1 which represents the player
            playerTurnNum++;
            set("turn", playerTurnNum % 2 + 1);
        });
        //in the event of collision between enemybullet (player 2 bullet) and middle barrier, bullet is removed, bullet coord and new Y arrays are cleared and the count and input vars a reset to 0
        onCollision(Type.EnemyBullet, Type.Barrier, (enemyBullet, barrier) -> {
            getGameWorld().getEntitiesByType(Type.EnemyBullet).forEach(Entity::removeFromWorld);
            bulletCoords.clear();
            newYs.clear();
            inp = 0;
            count = 0;
            bulletShot2 = false;
            //checks to see if doubleDamageOn boolean is true (means powerup was on but since the bullet hit barrier and not a tank this does nothing except make it false)
            if (doubleDamageOn) {
                doubleDamageOn = false;
            }
            //this is used to keep track of the player's turn and the turn text var is set to the playerTurn mod 2 + 1 which represents the player
            playerTurnNum++;
            set("turn", playerTurnNum % 2 + 1);
        });

    }

    private void gameOver(){
        //checks to see which player's tank died and a message shown based on that to show who wins and points are generated based on a random num times the number each player hit the other tank
        if (player1Health <= 0){
            showMessage("Game Over. Winner: Player 2 with a score of " + (player2Hits * FXGL.random(10,20)) + " defeated Player 1 with a score of " + (player1Hits * FXGL.random(5,10)) + ".");
        } else if (player2Health <= 0){
            showMessage("Game Over. Winner: Player 1 with a score of " + (player1Hits * FXGL.random(10,20)) + " defeated Player 2 with a score of " + (player2Hits * FXGL.random(5,10)) + ".");
        }


    }
    private void spawnTank() {
        //spawns both enemyTank and tank entities with their types EnemyTank and Tank respectively, at a random location on their respective sides, with an image, they are set to collidable, and are scaled down
        enemyTank = entityBuilder()
                .type(Type.EnemyTank)
                .at(FXGL.random(420, 1100), 240)
                .viewWithBBox("cartoonTankLeft.png")
                .collidable()
                .scale(0.2, 0.2)
                .buildAndAttach();
        tank = entityBuilder()
                .type(Type.Tank)
                .at(FXGL.random(-200, 500), 165)
                .viewWithBBox("cartoonTank.png")
                .collidable()
                .scale(0.2, 0.2)
                .buildAndAttach();


    }


    private void spawnBullet() {
        //spawns both enemyBullet and bullet entities with their types EnemyBullet and Bullet respectively, at a location based on their respective tanks, with an image, they are set to collidable, and are scaled down
        //the bullets spawn based on whoevers turn it is
        if (playerTurnNum % 2 == 0) {
            bullet = entityBuilder()
                    .type(Type.Bullet)
                    .at(tank.getX() + 50, tank.getY() + 150)
                    .viewWithBBox("bullet.png")
                    .collidable()
                    .scale(0.125, 0.125)
                    .buildAndAttach();
        } else if (playerTurnNum % 2 == 1) {
            enemyBullet = entityBuilder()
                    .type(Type.EnemyBullet)
                    .at(enemyTank.getX() + 175, enemyTank.getY() + 125)
                    .viewWithBBox("bulletLeft.png")
                    .collidable()
                    .scale(0.125, 0.125)
                    .buildAndAttach();
        }

    }

    private void spawnBarrier() {
        //spawns Barrier type entity, at the middle of the screen, as a black rectangle with a pre-generated width and height , they are set to collidable
        entityBuilder()
                .type(Type.Barrier)
                .at(875, 400)
                .viewWithBBox(new Rectangle(40, 200, Color.BLACK))
                .collidable()
                .buildAndAttach();
    }

    private void spawnGrass() {
        //spawns Grass type entity, at the bottom of the screen, as a green rectangle with a pre-generated width and height , they are set to collidable
        entityBuilder()
                .type(Type.Grass)
                .at(0, 600)
                .viewWithBBox(new Rectangle(2000, 700, Color.GREEN))
                .collidable()
                .buildAndAttach();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
