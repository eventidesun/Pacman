import javax.swing.Timer;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.*;

public class PacMan extends JPanel implements ActionListener, KeyListener {
    class Block {
        int x, y, width, height;
        Image image;
        int startX, startY;
        char direction = 'U';
        int velocityX = 0, velocityY = 0;

        Block(Image image, int x, int y, int width, int height) {
            this.image = image;
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.startX = x;
            this.startY = y;
        }

        void updateDirection(char direction) {
            char prevDirection = this.direction;
            this.direction = direction;
            updateVelocity();
            this.x += this.velocityX;
            this.y += this.velocityY;
            for (Block wall : walls) {
                if (collision(this, wall)) {
                    this.x -= this.velocityX;
                    this.y -= this.velocityY;
                    this.direction = prevDirection;
                    updateVelocity();
                }
            }
        }

        void updateVelocity() {
            switch (this.direction) {
                case 'U' -> { velocityX = 0; velocityY = -tileSize / 4; }
                case 'D' -> { velocityX = 0; velocityY = tileSize / 4; }
                case 'L' -> { velocityX = -tileSize / 4; velocityY = 0; }
                case 'R' -> { velocityX = tileSize / 4; velocityY = 0; }
            }
        }

        void reset() {
            this.x = this.startX;
            this.y = this.startY;
        }
    }

    private final int rowCount = 21;
    private final int columnCount = 19;
    private final int tileSize = 32;
    private final int boardWidth = columnCount * tileSize;
    private int boardHeight = rowCount * tileSize;

    private Image wallImage, blueGhostImage, pinkGhostImage, redGhostImage;
    private Image pacmanUpImage, pacmanDownImage, pacmanLeftImage, pacmanRightImage;

    HashSet<Block> walls, foods, ghosts;
    Block pacman;
    Timer gameLoop;
    char[] directions = {'U', 'D', 'L', 'R'};
    Random random = new Random();
    int score = 0, lives = 3;
    boolean gameOver = false;

    private List<String> tileMapLines;

    PacMan() {
        setPreferredSize(new Dimension(boardWidth, boardHeight));
        setBackground(Color.BLACK);
        addKeyListener(this);
        setFocusable(true);

        wallImage = new ImageIcon(Objects.requireNonNull(getClass().getResource("/res/wall.png"))).getImage();
        blueGhostImage = new ImageIcon(Objects.requireNonNull(getClass().getResource("/res/blue-ghost.png"))).getImage();
        pinkGhostImage = new ImageIcon(Objects.requireNonNull(getClass().getResource("/res/pink-ghost.png"))).getImage();
        redGhostImage = new ImageIcon(Objects.requireNonNull(getClass().getResource("/res/red-ghost.png"))).getImage();

        pacmanUpImage = new ImageIcon(Objects.requireNonNull(getClass().getResource("/res/pacman-up.png"))).getImage();
        pacmanDownImage = new ImageIcon(Objects.requireNonNull(getClass().getResource("/res/pacman-down.png"))).getImage();
        pacmanLeftImage = new ImageIcon(Objects.requireNonNull(getClass().getResource("/res/pacman-left.png"))).getImage();
        pacmanRightImage = new ImageIcon(Objects.requireNonNull(getClass().getResource("/res/pacman-right.png"))).getImage();

        loadMapFromFile("/res/mazes/maze.txt");
        loadMap();

        for (Block ghost : ghosts) {
            char newDirection = directions[random.nextInt(4)];
            ghost.updateDirection(newDirection);
        }

        System.out.println("blue ghost: " + getClass().getResource("/res/blue-ghost.png"));
        System.out.println("pacman up : " + getClass().getResource("/res/pacman-up.png"));

        gameLoop = new Timer(50, this);
        gameLoop.start();
    }

    public void loadMapFromFile(String resourcePath) {
        tileMapLines = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                Objects.requireNonNull(getClass().getResourceAsStream(resourcePath))))) {
            String line;
            while ((line = reader.readLine()) != null) {
                tileMapLines.add(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void loadMap() {
        walls = new HashSet<>();
        foods = new HashSet<>();
        ghosts = new HashSet<>();

        for (int r = 0; r < rowCount; r++) {
            String row = tileMapLines.get(r);
            for (int c = 0; c < columnCount; c++) {
                char tile = row.charAt(c);
                int x = c * tileSize;
                int y = r * tileSize;

                switch (tile) {
                    case 'X' -> walls.add(new Block(wallImage, x, y, tileSize, tileSize));
                    case 'b' -> ghosts.add(new Block(blueGhostImage, x, y, tileSize, tileSize));
                    case 'o' -> ghosts.add(new Block(pinkGhostImage, x, y, tileSize, tileSize));
                    case 'p' -> ghosts.add(new Block(redGhostImage, x, y, tileSize, tileSize));
                    case 'P' -> pacman = new Block(pacmanRightImage, x, y, tileSize, tileSize);
                    case ' ' -> foods.add(new Block(null, x + 14, y + 14, 4, 4));
                }
            }
        }
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        draw(g);
    }

    public void draw(Graphics g) {
        g.drawImage(pacman.image, pacman.x, pacman.y, pacman.width, pacman.height, null);

        for (Block ghost : ghosts)
            g.drawImage(ghost.image, ghost.x, ghost.y, ghost.width, ghost.height, null);
        for (Block wall : walls)
            g.drawImage(wall.image, wall.x, wall.y, wall.width, wall.height, null);

        g.setColor(Color.WHITE);
        for (Block food : foods)
            g.fillRect(food.x, food.y, food.width, food.height);

        g.setFont(new Font("Arial", Font.PLAIN, 18));
        if (gameOver)
            g.drawString("Game Over: " + score, tileSize / 2, tileSize / 2);
        else
            g.drawString("x" + lives + " Score: " + score, tileSize / 2, tileSize / 2);
    }

    public void move() {
        pacman.x += pacman.velocityX;
        pacman.y += pacman.velocityY;

        for (Block wall : walls) {
            if (collision(pacman, wall)) {
                pacman.x -= pacman.velocityX;
                pacman.y -= pacman.velocityY;
                break;
            }
        }

        for (Block ghost : ghosts) {
            if (collision(ghost, pacman)) {
                lives--;
                if (lives == 0) {
                    gameOver = true;
                    return;
                }
                resetPositions();
            }

            if (ghost.y == tileSize * 9 && ghost.direction != 'U' && ghost.direction != 'D') {
                ghost.updateDirection('U');
            }

            ghost.x += ghost.velocityX;
            ghost.y += ghost.velocityY;

            for (Block wall : walls) {
                if (collision(ghost, wall) || ghost.x <= 0 || ghost.x + ghost.width >= boardWidth) {
                    ghost.x -= ghost.velocityX;
                    ghost.y -= ghost.velocityY;
                    char newDirection = directions[random.nextInt(4)];
                    ghost.updateDirection(newDirection);
                }
            }
        }

        Block foodEaten = null;
        for (Block food : foods) {
            if (collision(pacman, food)) {
                foodEaten = food;
                score += 10;
            }
        }
        foods.remove(foodEaten);

        if (foods.isEmpty()) {
            loadMap();
            resetPositions();
        }
    }

    public boolean collision(Block a, Block b) {
        return a.x < b.x + b.width && a.x + a.width > b.x &&
                a.y < b.y + b.height && a.y + a.height > b.y;
    }

    public void resetPositions() {
        pacman.reset();
        pacman.velocityX = 0;
        pacman.velocityY = 0;
        for (Block ghost : ghosts) {
            ghost.reset();
            char newDirection = directions[random.nextInt(4)];
            ghost.updateDirection(newDirection);
        }
    }

    @Override public void actionPerformed(ActionEvent e) {
        move();
        repaint();
        if (gameOver) gameLoop.stop();
    }

    @Override public void keyTyped(KeyEvent e) {}
    @Override public void keyPressed(KeyEvent e) {}
    @Override public void keyReleased(KeyEvent e) {
        if (gameOver) {
            loadMap();
            resetPositions();
            lives = 3;
            score = 0;
            gameOver = false;
            gameLoop.start();
        }

        switch (e.getKeyCode()) {
            case KeyEvent.VK_UP -> pacman.updateDirection('U');
            case KeyEvent.VK_DOWN -> pacman.updateDirection('D');
            case KeyEvent.VK_LEFT -> pacman.updateDirection('L');
            case KeyEvent.VK_RIGHT -> pacman.updateDirection('R');
        }

        switch (pacman.direction) {
            case 'U' -> pacman.image = pacmanUpImage;
            case 'D' -> pacman.image = pacmanDownImage;
            case 'L' -> pacman.image = pacmanLeftImage;
            case 'R' -> pacman.image = pacmanRightImage;
        }
    }
}
