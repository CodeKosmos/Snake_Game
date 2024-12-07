package game;
import javax.swing.*;
import java.util.Locale;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Scanner;
import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.Timer;
import javax.swing.JOptionPane;

public class Board extends JPanel implements ActionListener {

    // Board dimensions
    private final int B_WIDTH = 600; // Width of the game board
    private final int B_HEIGHT = 600; // Height of the game board
    private final int DOT_SIZE = 10; // Size of each snake segment
    private final int ALL_DOTS = 3600; // Total possible positions on the board
    private final int RAND_POS = 59; // Random positioning for the apple

    private int DELAY; // Game speed (adjustable based on difficulty)
    private final int[] SPEEDS = {240, 180, 120}; // Speeds for easy, medium, and hard modes

    // Arrays to store the snake's coordinates
    private final int x[] = new int[ALL_DOTS];
    private final int y[] = new int[ALL_DOTS];

    private int dots; // Number of segments in the snake
    private int apple_x; // X-coordinate of the apple
    private int apple_y; // Y-coordinate of the apple

    // Directions for snake movement
    private boolean leftDirection = false;
    private boolean rightDirection = true;
    private boolean upDirection = false;
    private boolean downDirection = false;
    private boolean inGame = true; // Whether the game is active

    private Timer timer; // Timer to control game updates

    private int score = 0; // Current score
    private int bestScore = 0; // Best score
    private String bestPlayer = "Unknown"; // Name of the best player
    private final String SCORE_FILE = "best_score.txt"; // File to save high scores
  
    private String playerName; // Current player's name
    
    public Board() {
        initBoard();
    }

    // Initializes the game board
    private void initBoard() {
        playerName = askPlayerName(); // Ask for player's name
        addKeyListener(new TAdapter()); // Add key listener for controls
        setBackground(new Color(30, 30, 30)); // Dark background color
        setFocusable(true);
        setPreferredSize(new Dimension(B_WIDTH, B_HEIGHT)); // Set board size
        loadBestScore(); // Load the best score from the file
        selectDifficulty(); // Select game difficulty
        initGame(); // Initialize game state
    }


 // Prompts the player to enter their name
    private String askPlayerName() {
        String playerName;
        do {
            // Create a custom input dialog with "Cancel" instead of "Abbrechen"
            JTextField textField = new JTextField();
            Object[] message = {
                "Enter your name:", textField // English prompt
            };
            Object[] options = {"OK", "Cancel"}; // Custom button labels
            
            int option = JOptionPane.showOptionDialog(
                null,
                message,
                "Player Name", // English title
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                options,
                options[0] // Default button
            );

            if (option == 0) { // "OK" clicked
                playerName = textField.getText();
                if (playerName.trim().isEmpty()) {
                    JOptionPane.showMessageDialog(
                        null,
                        "The name cannot be empty. Please enter a valid name.", //  error message
                        "Invalid Name", // title
                        JOptionPane.WARNING_MESSAGE
                    );
                } else {
                    return playerName; // Return the entered name
                }
            } else { // "Cancel" clicked
                int choice = JOptionPane.showOptionDialog(
                    null,
                    "Are you sure you want to cancel?", //  confirmation message
                    "Confirmation", // title
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE,
                    null,
                    new String[]{"Yes", "No"}, // Custom buttons
                    "No"
                );

                if (choice == JOptionPane.YES_OPTION) {
                    System.exit(0); // Exit the program
                }
            }
        } while (true);
    }


    // Allows the player to select the game difficulty
    private void selectDifficulty() {
        String[] options = {"Easy", "Medium", "Hard"};
        int choice = JOptionPane.showOptionDialog(
            null,
            "Choose the difficulty level",
            "Game Mode",
            JOptionPane.DEFAULT_OPTION,
            JOptionPane.INFORMATION_MESSAGE,
            null,
            options,
            options[1]
        );
        DELAY = SPEEDS[choice >= 0 ? choice : 1]; // Default to Medium
    }

    // Loads the best score from a file
    private void loadBestScore() {
        File file = new File(SCORE_FILE);
        if (file.exists()) {
            try (Scanner scanner = new Scanner(file)) {
                if (scanner.hasNext()) {
                    bestPlayer = scanner.next();
                    if (scanner.hasNextInt()) {
                        bestScore = scanner.nextInt();
                    } else {
                        bestScore = 0;
                    }
                }
            } catch (Exception e) {
                System.out.println("Warning: Error loading the best score file. Using default values.");
                bestPlayer = "Unknown";
                bestScore = 0;
            }
        } else {
            System.out.println("No best score file found. Using default values.");
            bestPlayer = "Unknown";
            bestScore = 0;
        }
    }

    // Saves the current best score to a file
    private void saveBestScore() {
        if (score > bestScore) {
            bestScore = score;
            bestPlayer = playerName;
            LocalDateTime now = LocalDateTime.now();
            String timestamp = now.format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"));
            try (FileWriter writer = new FileWriter(SCORE_FILE)) {
                writer.write(playerName + " " + bestScore + " " + timestamp + "\n");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // Initializes the game state
    private void initGame() {
        dots = 3; // Initial snake length
        score = 0;

        // Initialize the snake's starting position
        for (int z = 0; z < dots; z++) {
            x[z] = 50 - z * 10;
            y[z] = 50;
        }

        locateApple(); // Place the first apple
        if (timer != null) {
            timer.stop();
        }
        timer = new Timer(DELAY, this);
        timer.start();
        inGame = true;
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        doDrawing(g);
    }

    // Draws the game elements

    private void doDrawing(Graphics g) {
        if (inGame) {
            g.setColor(Color.RED);
            g.fillOval(apple_x, apple_y, DOT_SIZE, DOT_SIZE);

            for (int z = 0; z < dots; z++) {
                if (z == 0) {
                    g.setColor(Color.GREEN);
                } else {
                    g.setColor(Color.YELLOW);
                }
                g.fillRect(x[z], y[z], DOT_SIZE, DOT_SIZE);
            }

            drawScore(g);
        } else {
            gameOver(g);
        }
    }

    // Draws the score on the screen
    private void drawScore(Graphics g) {
        g.setColor(Color.white);
        g.setFont(new Font("Helvetica", Font.BOLD, 14)); // Font for the score
        g.drawString("Score: " + score, 10, 20);
        g.drawString("Best: " + bestScore + " (" + bestPlayer + ")", 450, 20);
    }

    // Handles the game over state
    private void gameOver(Graphics g) {
        String msg = "Game Over";
        Font small = new Font("Helvetica", Font.BOLD, 20);
        FontMetrics metr = getFontMetrics(small);

        g.setColor(Color.white);
        g.setFont(small);
        g.drawString(msg, (B_WIDTH - metr.stringWidth(msg)) / 2, B_HEIGHT / 2);

        saveBestScore();

        String[] options = {"Yes", "No", "Change difficulty"};
        int retry = JOptionPane.showOptionDialog(
            null,
            "Play again?",
            "Game Over",
            JOptionPane.DEFAULT_OPTION,
            JOptionPane.QUESTION_MESSAGE,
            null,
            options,
            options[0]
        );


        if (retry == JOptionPane.YES_OPTION) {
            initGame();
        } else if (retry == JOptionPane.CANCEL_OPTION) {
            selectDifficulty();
            initGame();
        } else {
            System.exit(0);
        }
    }

    // Checks if the snake eats the apple
    private void checkApple() {
        if ((x[0] == apple_x) && (y[0] == apple_y)) {
            dots++;
            score++;
            locateApple();
        }
    }

    // Moves the snake
    private void move() {
        for (int z = dots; z > 0; z--) {
            x[z] = x[(z - 1)];
            y[z] = y[(z - 1)];
        }

        if (leftDirection) {
            x[0] -= DOT_SIZE;
        }
        if (rightDirection) {
            x[0] += DOT_SIZE;
        }
        if (upDirection) {
            y[0] -= DOT_SIZE;
        }
        if (downDirection) {
            y[0] += DOT_SIZE;
        }
    }

    // Checks for collisions with walls or the snake itself
    private void checkCollision() {
        for (int z = dots; z > 0; z--) {
            if ((z > 4) && (x[0] == x[z]) && (y[0] == y[z])) {
                inGame = false;
            }
        }

        if (y[0] >= B_HEIGHT || y[0] < 0 || x[0] >= B_WIDTH || x[0] < 0) {
            inGame = false;
        }

        if (!inGame) {
            timer.stop();
        }
    }

    // Places the apple at a random position
    private void locateApple() {
        int r = (int) (Math.random() * RAND_POS);
        apple_x = ((r * DOT_SIZE));

        r = (int) (Math.random() * RAND_POS);
        apple_y = ((r * DOT_SIZE));
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (inGame) {
            checkApple();
            checkCollision();
            move();
        }
        repaint();
    }

    // Key listener for snake controls
    private class TAdapter extends KeyAdapter {
        @Override
        public void keyPressed(KeyEvent e) {
            int key = e.getKeyCode();

            if ((key == KeyEvent.VK_LEFT) && (!rightDirection)) {
                leftDirection = true;
                upDirection = false;
                downDirection = false;
            }

            if ((key == KeyEvent.VK_RIGHT) && (!leftDirection)) {
                rightDirection = true;
                upDirection = false;
                downDirection = false;
            }

            if ((key == KeyEvent.VK_UP) && (!downDirection)) {
                upDirection = true;
                rightDirection = false;
                leftDirection = false;
            }

            if ((key == KeyEvent.VK_DOWN) && (!upDirection)) {
                downDirection = true;
                rightDirection = false;
                leftDirection = false;
            }
        }
    }
}
