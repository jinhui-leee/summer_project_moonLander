package moon_lander;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;

import static moon_lander.Framework.gameTime;

/**
 * Actual game.
 *
 * @author www.gametutorial.net
 */

public class Game {

    /**
     * The space rocket with which player will have to land.
     */
    private PlayerRocket playerRocket;

    /**
     * Landing area on which rocket will have to land.
     */
    private LandingArea landingArea;

    /**
     * Enemy destroys a rocket when touched
     */
    private Enemy []enemy;


    /**
     * Game background image.
     */
    private BufferedImage backgroundImg;

    /**
     * Red border of the frame. It is used when player crash the rocket.
     */
    private BufferedImage redBorderImg;

    private MP3Player failMusic;

    private MP3Player successMusic;

    private long minimum;



    public Game()
    {
        Framework.gameState = Framework.GameState.GAME_CONTENT_LOADING;

        Thread threadForInitGame = new Thread() {
            @Override
            public void run(){
                // Sets variables and objects for the game.
                Initialize();
                // Load game files (images, sounds, ...)
                LoadContent();

                Framework.gameState = Framework.GameState.PLAYING;
            }
        };
        threadForInitGame.start();
    }

    public void timeCompare() {

        if (minimum == 0) {
            minimum = gameTime;
        }
        else
        {
            if (0 < gameTime && minimum > gameTime) {
                minimum = gameTime;
            }
        }
    }

    /**
     * Set variables and objects for the game.
     */
    private void Initialize()
    {
        playerRocket = new PlayerRocket();
        landingArea  = new LandingArea();
        enemy = new Enemy[5];
        for (int i=0; i<enemy.length; i++) {
            enemy[i] = new Enemy(playerRocket.rocketImgHeight, playerRocket.y, landingArea.y);
        }

    }

    /**
     * Load game files - images, sounds, ...
     */
    private void LoadContent()
    {

        try
        {
            URL backgroundImgUrl = this.getClass().getResource("/resources/images/background.jpg");
            backgroundImg = ImageIO.read(backgroundImgUrl);

            URL redBorderImgUrl = this.getClass().getResource("/resources/images/red_border.png");
            redBorderImg = ImageIO.read(redBorderImgUrl);
        }
        catch (IOException ex) {
            Logger.getLogger(Game.class.getName()).log(Level.SEVERE, null, ex);
        }
    }


    /**
     * Restart game - reset some variables.
     */
    public void RestartGame()
    {
        playerRocket.ResetPlayer();

        for (int i=0; i<enemy.length; i++) {
            enemy[i].resetXY(playerRocket.rocketImgHeight, playerRocket.y, landingArea.y);

        }

    }


    /**
     * Update game logic.
     *
     * @param gameTime gameTime of the game.
     * @param mousePosition current mouse position.
     */
    public void UpdateGame(long gameTime, Point mousePosition)
    {
        // Move the rocket
        playerRocket.Update();

        // Checks where the player rocket is. Is it still in the space or is it landed or crashed?
        // First we check bottom y coordinate of the rocket if is it near the landing area.
        if(playerRocket.y + playerRocket.rocketImgHeight - 10 > landingArea.y)
        {
            // Here we check if the rocket is over landing area.
            if((playerRocket.x > landingArea.x) && (playerRocket.x < landingArea.x + landingArea.landingAreaImgWidth - playerRocket.rocketImgWidth))
            {
                // Here we check if the rocket speed isn't too high.
                if(playerRocket.speedY <= playerRocket.topLandingSpeed)
                    playerRocket.landed = true;
                else
                    playerRocket.crashed = true;
            }
            else
                playerRocket.crashed = true;

            Framework.gameState = Framework.GameState.GAMEOVER;
        }

        //장애물과 닿으면 crashed
        for (int i=0; i<enemy.length; i++) {
            if (enemy[i].isCrashed(playerRocket.x, playerRocket.y, playerRocket.rocketImgWidth, playerRocket.rocketImgHeight))
            {
                playerRocket.crashed = true;
                Framework.gameState = Framework.GameState.GAMEOVER;

            }
        }
    }

    /**
     * Draw the game to the screen.
     *
     * @param g2d Graphics2D
     * @param mousePosition current mouse position.
     */
    public void Draw(Graphics2D g2d, Point mousePosition)
    {
        g2d.drawImage(backgroundImg, 0, 0, Framework.frameWidth, Framework.frameHeight, null);

        landingArea.Draw(g2d);

        //장애물 랜덤 생성
        playerRocket.Draw(g2d);
        for (int i=0; i<enemy.length; i++) {
            enemy[i].Draw(g2d);

        }
    }


    /**
     * Draw the game over screen.
     *
     * @param g2d Graphics2D
     * @param mousePosition Current mouse position.
     * @param gameTime Game time in nanoseconds.
     */
    public void DrawGameOver(Graphics2D g2d, Point mousePosition, long gameTime)
    {

        Draw(g2d, mousePosition);

        g2d.drawString("Press space or enter to restart.", Framework.frameWidth / 2 - 100, Framework.frameHeight / 3 + 70);

        if(playerRocket.landed)
        {
            g2d.drawString("You have successfully landed!", Framework.frameWidth / 2 - 100, Framework.frameHeight / 3);
            g2d.drawString("You have landed in " + gameTime / Framework.secInNanosec + " seconds.", Framework.frameWidth / 2 - 100, Framework.frameHeight / 3 + 20);
            g2d.drawString("Your shortest time is  " + minimum / Framework.secInNanosec + " seconds.", Framework.frameWidth / 2 - 105, Framework.frameHeight / 3 + 40);

            timeCompare();

            successMusic = new MP3Player("C:\\summerProject-master\\summerProject-master\\src\\resources\\mp3\\success.wav",false);
            successMusic.start();

        }
        else
        {
            g2d.setColor(Color.red);
            g2d.drawString("You have crashed the rocket!", Framework.frameWidth / 2 - 95, Framework.frameHeight / 3);
            g2d.drawImage(redBorderImg, 0, 0, Framework.frameWidth, Framework.frameHeight, null);

            //fail music play
            failMusic = new MP3Player("C:\\summerProject-master\\summerProject-master\\src\\resources\\mp3\\fail.wav", false);
            failMusic.start();


        }

    }
}
