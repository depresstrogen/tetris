/**
 * Tetris By: Emma Power
 * 
 * -<==< CONTROLS >==>-
 * Left Arrow = Move Left
 * Right Arrow = Move Right
 * Up Arrow = Rotate Piece
 * Down Arrow = Drop Piece 1 Block (Hold for 1 second to make it drop even faster)
 * Shift = Hold Piece (Save a piece for later) You can only hold a piece once per new piece, or else you could cheat.
 */


import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.Scanner;
import javax.swing.*;

public class Tetris extends JPanel implements ActionListener{
  // All these are here so its not something like public void method(int a, int b, int c, double d, String e...)
  static Color[] tetrominoColors = new Color[7];
  // Stores the coordinates of the current piece
  static int[][] piece = new int[4][2];
  // Stores the coordinates of the previous piece (To revert to if collision is detected
  static int[][] oldPiece = new int[4][2];
  // Stores the piece positions 1 is placed piece 2 is active piece
  static int[][] board = new int[10][20];
  // Stores the positions of the colors on the board
  static Color[][] coloredBoard = new Color[10][20];
  // Stores The hold graphic
  static int[][] holdGraphic = new int[4][4];
  // Stores the nextPiece Graphic
  static int[][] nextPieceGraphic = new int[4][4];
  // These 5 used so some methods are easier to call
  static int type = 0;
  static int rotation = 0;
  static int oldRotation = rotation;
  static long loopTime = 0;
  static boolean calledByShift = false;
  // Used for the animation when you hold more than once
  static boolean heldOnce = false;
  static long heldAnimationTimer = 0;
  static long heldAnimationBase = 0;
  // Size of blocks
  static int pieceSize = 30;
  // Overall game score
  static int score = 0;
  //Current FPS
  static long fps;
  //Current piece in hold (7 = No Piece)
  static int heldPiece = 7;
  // Stores the next piece to be drawn
  static int nextPiece = 0;
  // Used to see if game is over or not
  static boolean gameOver = false;
  // Outside of method as they are used for JFrame too
  static int boardX = 200;
  static int boardY = 0;
  static int totalLinesCleared = 0;
  // JFrame Variables
  static JFrame frame;
  static JPanel contentPane;
  static JLabel prompt1, prompt2, prompt3, prompt4;
  static JLabel scoreTitle, nameTitle;
  static JLabel name1, name2, name3, name4, name5;
  static JLabel score1, score2, score3, score4, score5;
  static JTextField scoreName;
  static JButton button;
  // Used for java.io (Saving High Scores)
  File savedScores = new File("tetris_scores.txt");
  FileReader in;
  FileWriter out;
  Scanner sc = new Scanner(System.in);
  BufferedReader readFile;
  BufferedWriter writeFile;
  String lineOfText;
  String scoreNameParsed;
  static String top5Names[] = new String[5];
  static int top5Scores[] = new int[5];
  static int lastPosition = -1;
  
  /* Paint Method
   * Runs when repaint(); is called
   * Paints Where Pieces are, in the colour they are supposed to be and important information (Score etc.)
   * This was by far the hardest part, along with making th JFrame in main
   */
  public void paint(Graphics g) {
    // Main Paint Loop
    super.paint(g);
    Graphics2D g2d = (Graphics2D) g;
    // Paints Board
    for (int i = 0; i < 20; i++) {
      for (int j = 0; j < 10; j++) {
        if (coloredBoard[j][i] != Color.WHITE) {
          g2d.setColor(coloredBoard[j][i]);
          g2d.fillRect(j * pieceSize + boardX , i * pieceSize + boardY, pieceSize, pieceSize);
          g2d.setColor(Color.BLACK);
          g2d.drawRect(j * pieceSize + boardX  + 5, i * pieceSize + boardY + 5, 20, 20);
        } else {
          g2d.setColor(Color.WHITE);
          g2d.fillRect(j * pieceSize + boardX , i * pieceSize + boardY, pieceSize, pieceSize);
        }
        g2d.setColor(Color.BLACK);
        g2d.drawRect(j * pieceSize + boardX , i * pieceSize + boardY , pieceSize, pieceSize);
      }
    }
    
    // Paints holdGraphic
    final int holdGraphicX = 25;
    final int holdGraphicY = 50;
    int holdGraphicXOffset = 0;
    Color backgroundColor = Color.WHITE;
    // Makes the graphic background red when you try to hold twice
    if ((heldAnimationTimer - heldAnimationBase) / 50 == 2 || (heldAnimationTimer - heldAnimationBase) / 50 == 4 && System.currentTimeMillis() < heldAnimationTimer) {
    	backgroundColor = Color.RED;
    }
    g2d.setFont(new Font("Lucida Sans Typewriter", Font.BOLD, 24));
    g2d.drawString("Hold", holdGraphicX, holdGraphicY - 5);
    for (int i = 0; i < 4; i++) {
      for (int j = 0; j < 4; j++) {
        if (holdGraphic[j][i] == 1 && heldPiece != 7) {
          g2d.setColor(tetrominoColors[heldPiece]);
          g2d.fillRect(j * pieceSize + holdGraphicX + holdGraphicXOffset, i * pieceSize + holdGraphicY, pieceSize, pieceSize);
          g2d.setColor(Color.BLACK);
          g2d.drawRect(j * pieceSize + holdGraphicX + holdGraphicXOffset + 5, i * pieceSize + holdGraphicY + 5, 20, 20);
        } else {
          g2d.setColor(backgroundColor);
          g2d.fillRect(j * pieceSize + holdGraphicX + holdGraphicXOffset, i * pieceSize+ holdGraphicY, pieceSize, pieceSize);
        }
        g2d.setColor(Color.BLACK);
        g2d.drawRect(j * pieceSize + holdGraphicX + holdGraphicXOffset, i * pieceSize + holdGraphicY, pieceSize, pieceSize);
      }
    }
    // Paints nextPieceGraphic
    final int nextPieceGraphicX = 550;
    final int nextPieceGraphicY = 50;
    g2d.setFont(new Font("Lucida Sans Typewriter", Font.BOLD, 24));
    g2d.drawString("Next Piece", nextPieceGraphicX - 10, nextPieceGraphicY - 5);
    for (int i = 0; i < 4; i++) {
      for (int j = 0; j < 4; j++) {
        if (nextPieceGraphic[j][i] == 1) {
          g2d.setColor(tetrominoColors[nextPiece]);
          g2d.fillRect(j * pieceSize + nextPieceGraphicX, i * pieceSize + nextPieceGraphicY, pieceSize, pieceSize);
          g2d.setColor(Color.BLACK);
          g2d.drawRect(j * pieceSize + nextPieceGraphicX + 5, i * pieceSize + nextPieceGraphicY + 5, 20, 20);
        } else {
          g2d.setColor(Color.WHITE);
          g2d.fillRect(j * pieceSize + nextPieceGraphicX, i * pieceSize+ nextPieceGraphicY, pieceSize, pieceSize);
        }
        g2d.setColor(Color.BLACK);
        g2d.drawRect(j * pieceSize + nextPieceGraphicX, i * pieceSize + nextPieceGraphicY, pieceSize, pieceSize);
      }
    }
    
    // Print Game Over When Done
    if (gameOver == true) {
      g2d.setFont(new Font("Lucida Sans Typewriter", Font.BOLD, 40));
      g2d.drawString("GAME", 530, 310);
      g2d.drawString("OVER", 530, 350);
      g2d.setFont(new Font("Lucida Sans Typewriter", Font.BOLD, 14));
      g2d.drawString("By: Emma Power", 530, 390);
    }
    
    //Paint Score
    g2d.setFont(new Font("Lucida Sans Typewriter", Font.BOLD, 24));
    g2d.drawString("Score", 550, 550);
    g2d.drawString(Integer.toString(score), 550, 580);
  }// Paint
  
  /*
   * Makes a new, random piece
   * pieceType 0 = Normal
   * pieceType 1 = First Of Game
   * pieceType 2 = Held
   * pieceType 3 = Hold Graphic
   * pieceType 4 = Next PieceGraphic
   */
  public void initPiece(int pieceType) {
    int temp = 0;
    //Makes the held piece the active piece
    if (pieceType == 2 || pieceType == 3) {
      type = heldPiece;
    } 
    // Makes a nextPiece if it it the first piece
    else if (pieceType == 1) {
      type = (int) (Math.random() * 7);
      nextPiece = (int) (Math.random() * 7);
    }
    // Glitch Prevention
    else if (pieceType == 4) {
      temp = type;
      type = nextPiece;
    } else {
      type = nextPiece;
      heldOnce = false;
      nextPiece = (int) (Math.random() * 7);
    }
    if (pieceType == 3 || pieceType == 4) {
      saveOldPiece();
    }
    if (pieceType < 3) {
      rotation = 0;
    }
    // Defines starting point for Tetrominos
    // X Coordinate Y Coordinate
    // I (0)
    if (type == 0) {
      piece[0][0] = 3; piece[0][1] = 0;
      piece[1][0] = 4; piece[1][1] = 0;
      piece[2][0] = 5; piece[2][1] = 0;
      piece[3][0] = 6; piece[3][1] = 0;
    }
    // O (1)
    if (type == 1) {
      piece[0][0] = 4; piece[0][1] = 0;
      piece[1][0] = 5; piece[1][1] = 0;
      piece[2][0] = 4; piece[2][1] = 1;
      piece[3][0] = 5; piece[3][1] = 1;
    }
    // T (2)
    if (type == 2) {
      piece[0][0] = 4; piece[0][1] = 0;
      piece[1][0] = 5; piece[1][1] = 0;
      piece[2][0] = 6; piece[2][1] = 0;
      piece[3][0] = 5; piece[3][1] = 1;
    }
    // S (3)
    if (type == 3) {
      piece[0][0] = 4; piece[0][1] = 1;
      piece[1][0] = 5; piece[1][1] = 1;
      piece[2][0] = 5; piece[2][1] = 0;
      piece[3][0] = 6; piece[3][1] = 0;
    }
    // Z (4)
    if (type == 4) {
      piece[0][0] = 4; piece[0][1] = 0;
      piece[1][0] = 5; piece[1][1] = 0;
      piece[2][0] = 5; piece[2][1] = 1;
      piece[3][0] = 6; piece[3][1] = 1;
    }
    // J (5)
    if (type == 5) {
      piece[0][0] = 4; piece[0][1] = 0;
      piece[1][0] = 4; piece[1][1] = 1;
      piece[2][0] = 5; piece[2][1] = 1;
      piece[3][0] = 6; piece[3][1] = 1;
    }
    // L (6)
    if (type == 6) {
      piece[0][0] = 6; piece[0][1] = 0;
      piece[1][0] = 6; piece[1][1] = 1;
      piece[2][0] = 5; piece[2][1] = 1;
      piece[3][0] = 4; piece[3][1] = 1;
    }
    // If a piece spawns over another GAME OVER
    if (board[piece[0][0]][piece[0][1]] == 1 || board[piece[1][0]][piece[1][1]] == 1
          || board[piece[2][0]][piece[2][1]] == 1 || board[piece[3][0]][piece[3][1]] == 1) {
      gameOver = true;
    }
    if (pieceType == 3) {
      for (int i = 0; i < 4; i++) {
        for (int j = 0; j < 4; j++) {
          holdGraphic[i][j] = 0;
        }
      }
      for (int i = 0; i < 4; i++) {
        holdGraphic[piece[i][0] - 3][piece[i][1] + 1] = 1;
      }
      loadOldPiece();
    }
    else if (pieceType != 4) {
      initPiece(4);
    } else if (pieceType == 4)  {
      for (int i = 0; i < 4; i++) {
        for (int j = 0; j < 4; j++) {
          nextPieceGraphic[i][j] = 0;
        }
      }
      for (int i = 0; i < 4; i++) {
        nextPieceGraphic[piece[i][0] - 3][piece[i][1] + 1] = 1;
      }
      loadOldPiece();
      nextPiece = type;
      type = temp;
    }
    else if (pieceType == 2) {
      
    } else {
      saveOldPiece();
    }
  }// initPiece
  
  /*
   * Removes oldPiece from board, puts piece on board
   * Duplicates that with coloredBoard
   */
  public void boardMaker(int boardType) {
    // Takes last position out
    board[oldPiece[0][0]][oldPiece[0][1]] = 0;
    board[oldPiece[1][0]][oldPiece[1][1]] = 0;
    board[oldPiece[2][0]][oldPiece[2][1]] = 0;
    board[oldPiece[3][0]][oldPiece[3][1]] = 0;

    board[piece[0][0]][piece[0][1]] = boardType;
    board[piece[1][0]][piece[1][1]] = boardType;
    board[piece[2][0]][piece[2][1]] = boardType;
    board[piece[3][0]][piece[3][1]] = boardType;
    // Does the same with the colors
    coloredBoard[oldPiece[0][0]][oldPiece[0][1]] = Color.WHITE;
    coloredBoard[oldPiece[1][0]][oldPiece[1][1]] = Color.WHITE;
    coloredBoard[oldPiece[2][0]][oldPiece[2][1]] = Color.WHITE;
    coloredBoard[oldPiece[3][0]][oldPiece[3][1]] = Color.WHITE;
    
    coloredBoard[piece[0][0]][piece[0][1]] = tetrominoColors[type];
    coloredBoard[piece[1][0]][piece[1][1]] = tetrominoColors[type];
    coloredBoard[piece[2][0]][piece[2][1]] = tetrominoColors[type];
    coloredBoard[piece[3][0]][piece[3][1]] = tetrominoColors[type];
  }// boardMaker
  
  /*
   * Drops the piece down by 1 block, and resets the loopTimer
   */
  public void drop() {
    saveOldPiece();
    piece[0][1] = piece[0][1] + 1;
    piece[1][1] = piece[1][1] + 1;
    piece[2][1] = piece[2][1] + 1;
    piece[3][1] = piece[3][1] + 1;
  }// Drop
  
  /*
   * Makes oldPiece = piece
   * i didn't want to write this loop every time
   */
  public void saveOldPiece() {
    for (int i = 0; i < 4; i++) {
      for (int j = 0; j < 2; j++) {
        oldPiece[i][j] = piece[i][j];
      }
    }
    oldRotation = rotation;
  }// saveOldPiece
  
  /*
   * Makes piece = oldPiece
   * i didn't want to write this loop every time
   */
  public void loadOldPiece() {
    for (int i = 0; i < 4; i++) {
      for (int j = 0; j < 2; j++) {
        piece[i][j] = oldPiece[i][j];
      }
    }
    rotation = oldRotation;
  }// loadOldPiece
  
  /*
   *  Checks if a move is valid by:
   *   - Not going out of array bounds
   *   - Not intersecting other pieces
   */
  public void collisionCheck() {
    // Top Condition
    if (piece[0][1] < 0 || piece[1][1] < 0 || piece[2][1] < 0 || piece[3][1] < 0) {
      loadOldPiece();
      boardMaker(2);
    }
    
    // Bottom Condition
    else if (piece[0][1] >= 20 || piece[1][1] >= 20 || piece[2][1] >= 20 || piece[3][1] >= 20 && calledByShift == false) {
      loadOldPiece();
      boardMaker(1);
      initPiece(0);
    }
    
    // Left Wall Condition
    else if (piece[0][0] < 0 || piece[1][0] < 0 || piece[2][0] < 0 || piece[3][0] < 0) {
      loadOldPiece();
      boardMaker(2);
    }
    
    // Right Wall Condition
    else if (piece[0][0] + 1 > 10 || piece[1][0] + 1 > 10 || piece[2][0] + 1 > 10 || piece[3][0] + 1 > 10) {
      loadOldPiece();
      boardMaker(2);
    }
    
    // Piece To Piece Collision
    else if (board[piece[0][0]][piece[0][1]] == 1 || board[piece[1][0]][piece[1][1]] == 1
               || board[piece[2][0]][piece[2][1]] == 1 || board[piece[3][0]][piece[3][1]] == 1) {
      // So shift does not mess up the rest of the collision
      if (calledByShift == true) {
        loadOldPiece();
        boardMaker(2);
      }
      // Bottom Collision
      else if (board[oldPiece[0][0]][oldPiece[0][1] + 1] == 1 || board[oldPiece[1][0]][oldPiece[1][1] + 1] == 1
                 || board[oldPiece[2][0]][oldPiece[2][1] + 1] == 1 || board[oldPiece[3][0]][oldPiece[3][1] + 1] == 1) {
        loadOldPiece();
        boardMaker(1);
        initPiece(0);
      } else {
        loadOldPiece();
        boardMaker(2);
      }
    }
    
    // Downwards Condition
    else if (board[piece[0][0]][piece[0][1]] == 1 || board[piece[1][0]][piece[1][1]] == 1
               || board[piece[2][0]][piece[2][1]] == 1 || board[piece[3][0]][piece[3][1]] == 1) {
      boardMaker(1);
      initPiece(0);
    }
  }// Collision Check
  
  /*
   * Shifts the piece left or right, and applies this to the board
   */
  public void shift(int direction) {
    saveOldPiece();
    
    if (direction == -1) {
      piece[0][0] = piece[0][0] - 1;
      piece[1][0] = piece[1][0] - 1;
      piece[2][0] = piece[2][0] - 1;
      piece[3][0] = piece[3][0] - 1;
    }
    if (direction == 1) {
      piece[0][0] = piece[0][0] + 1;
      piece[1][0] = piece[1][0] + 1;
      piece[2][0] = piece[2][0] + 1;
      piece[3][0] = piece[3][0] + 1;
    }
    calledByShift = true;
    collisionCheck();
    calledByShift = false;
    boardMaker(2);
  }// Shift
  
  /*
   * Rotates the piece, based on where the piece already is
   * Hard coded because i could not find/figure out an algorithm to do the same task
   * Then it applies them to board
   */
  public void rotate() {
    saveOldPiece();
    // I, S & Z pieces have 2 rotations
    if (type == 0 || type == 3 || type == 4) {
      rotation = (rotation + 1) % 2;
    }
    // T, J & L pieces have 4 rotations
    if (type == 2 || type == 5 || type == 6){
      rotation = (rotation + 1) % 4;
    }
    // *
    // * I Piece
    // *
    // *
    if (type == 0) {
      switch(rotation) {
        case 0:
          piece[0][0] -= 1; piece[0][1] += 1;
          piece[2][0] += 1; piece[2][1] -= 1;
          piece[3][0] += 2; piece[3][1] -= 2;
          break;
        case 1:
          piece[0][0] += 1; piece[0][1] -= 1;
          piece[2][0] -= 1; piece[2][1] += 1;
          piece[3][0] -= 2; piece[3][1] += 2;
          break;
      }
    }
    
    // ** O's Can't rotate lol
    // **
    
    // *** T Piece
    //  *
    if (type == 2) {
      switch(rotation) {
        case 0:
          piece[0][0] -= 1; piece[0][1] += 1;
          piece[2][0] += 1; piece[2][1] -= 1;
          piece[3][0] += 1; piece[3][1] += 1;
          break;
        case 1:
          piece[0][0] += 1; piece[0][1] += 1;
          piece[2][0] -= 1; piece[2][1] -= 1;
          piece[3][0] += 1; piece[3][1] -= 1;
          break;
        case 2:
          piece[0][0] += 1; piece[0][1] -= 1;
          piece[2][0] -= 1; piece[2][1] += 1;
          piece[3][0] -= 1; piece[3][1] -= 1;
          break;
        case 3:
          piece[0][0] -= 1; piece[0][1] -= 1;
          piece[2][0] += 1; piece[2][1] += 1;
          piece[3][0] -= 1; piece[3][1] += 1;
          break;
      }
    }
    //  **  S Piece
    // ** 
    if (type == 3) {
      switch(rotation) {
        case 0:
          piece[0][0] -= 1; piece[0][1] -= 1;
          piece[2][0] += 1; piece[2][1] -= 1;
          piece[3][0] += 2; piece[3][1] += 0;
          break;
        case 1:
          piece[0][0] += 1; piece[0][1] += 1;
          piece[2][0] -= 1; piece[2][1] += 1;
          piece[3][0] -= 2; piece[3][1] += 0;
          break;
      }
    }
    // **  Z Piece
    //  ** 
    if (type == 4) {
      switch(rotation) {
        case 0:
          piece[0][0] -= 1; piece[0][1] -= 1;
          piece[2][0] -= 1; piece[2][1] += 1;
          piece[3][0] += 0; piece[3][1] += 2;
          break;
        case 1:
          piece[0][0] += 1; piece[0][1] += 1;
          piece[2][0] += 1; piece[2][1] -= 1;
          piece[3][0] += 0; piece[3][1] -= 2;
          break;
      }
    }
    if (type == 5) {
      switch(rotation) {
        case 0:
          piece[0][0] += 1; piece[0][1] -= 1;
          piece[2][0] += 1; piece[2][1] += 1;
          piece[3][0] += 2; piece[3][1] += 2;
          break;
        case 1:
          piece[0][0] += 1; piece[0][1] += 1;
          piece[2][0] -= 1; piece[2][1] += 1;
          piece[3][0] -= 2; piece[3][1] += 2;
          break;
        case 2:
          piece[0][0] -= 1; piece[0][1] += 1;
          piece[2][0] -= 1; piece[2][1] -= 1;
          piece[3][0] -= 2; piece[3][1] -= 2;
          break;
        case 3:
          piece[0][0] -= 1; piece[0][1] -= 1;
          piece[2][0] += 1; piece[2][1] -= 1;
          piece[3][0] += 2; piece[3][1] -= 2;
          break;
          
      }
    }
    if (type == 6) {
      switch(rotation) {
        case 0:
          piece[0][0] += 1; piece[0][1] -= 1;
          piece[2][0] -= 1; piece[2][1] -= 1;
          piece[3][0] -= 2; piece[3][1] -= 2;
          break;
        case 1:
          piece[0][0] += 1; piece[0][1] += 1;
          piece[2][0] += 1; piece[2][1] -= 1;
          piece[3][0] += 2; piece[3][1] -= 2;
          break;
        case 2:
          piece[0][0] -= 1; piece[0][1] += 1;
          piece[2][0] += 1; piece[2][1] += 1;
          piece[3][0] += 2; piece[3][1] += 2;
          break;
        case 3:
          piece[0][0] -= 1; piece[0][1] -= 1;
          piece[2][0] -= 1; piece[2][1] += 1;
          piece[3][0] -= 2; piece[3][1] += 2;
          break;
          
      }
    }
    collisionCheck();
    boardMaker(2);
  }// Rotate
  
  /*
   * Totals up the score, and applies changes to the board
   */
  public void score() {
    int piecesFilled = 0;
    int lineClears = 0;
    int baseScore = 100;
    
    // Totals up lines cleared
    for (int i = 0; i < 20; i++) {
      for (int j = 0; j < 10; j++) {
        if (board[j][i] == 1) {
          piecesFilled += 1;
        }
      }
      if (piecesFilled == 10) {
        lineClears += 1;
      }
      piecesFilled = 0;
    }
    
    // Removes pieces from board, and  shifts everything down
    //Do it for every row
    for (int i = 0; i < 20; i++) {
    //Count up filled Pieces
      for (int j = 0; j < 10; j++) {
        if (board[j][i] == 1) {
          piecesFilled += 1;
        }
      }
      if (piecesFilled == 10) {
    	// For Every Line Above
        for (int x = i; x >= 0; x--) {
        	// For Every Piece Above
          for (int y = 0; y < 10; y++) {
        	  if (x == 0) {
              }
        	  else if (board[y][x] != 2) {
              board[y][x] = board [y][x - 1];
              coloredBoard[y][x] = coloredBoard[y][x - 1];
            }
          }
        }
      }
      piecesFilled = 0;
    }
    for (int i = 0; i < 20; i++) {
        //Count up filled Pieces
          for (int j = 0; j < 10; j++) {
            if (board[j][i] == 2) {
            	board[j][i] = 0;
           }
        }
    }
    boardMaker(2);
    // Adds To Score
    switch(lineClears) {
      case 1:
        score += baseScore * 1;
        break;
      case 2:
        score += baseScore * 2;
        break;
      case 3:
        score += baseScore * 4;
        break;
      case 4:
        score += baseScore * 16;
        break;
    }
  }// Score
  
  /*
   * Puts active piece into  hold, and put held piece into board.
   */
  public void hold() {
	  if (!heldOnce) {
    int temp = type;
    int temp2 = heldPiece;
    heldPiece = temp;
    initPiece(3);
    coloredBoard[piece[0][0]][piece[0][1]] = Color.WHITE;
    coloredBoard[piece[1][0]][piece[1][1]] = Color.WHITE;
    coloredBoard[piece[2][0]][piece[2][1]] = Color.WHITE;
    coloredBoard[piece[3][0]][piece[3][1]] = Color.WHITE;
    heldPiece = temp2;
    if (heldPiece == 7) {
      initPiece(0);
    } else {
      initPiece(2);
    }
    boardMaker(2);
    heldPiece = temp;
    heldOnce = true;
	  } else {
		  heldAnimationTimer = System.currentTimeMillis() + 200;
		  heldAnimationBase = System.currentTimeMillis();
	  }
  }// Hold
  /*
   * Nested Class For JFrame
   * Makes Grid Layout, With all the prompts, buttons and other items when submitting score.
   */
  public Tetris() {
	  if (gameOver) {
	  frame = new JFrame("Save Your Score");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		contentPane = new JPanel();
		contentPane.setLayout(new GridLayout(0, 2, 10, 5));
		contentPane.setBorder(BorderFactory.createEmptyBorder(20, 50, 20, 50));

		prompt1 = new JLabel("Enter Your Name:");
		contentPane.add(prompt1);
		
		scoreName = new JTextField(10);
		contentPane.add(scoreName);
		
		prompt2 = new JLabel("Score: " + score);
		contentPane.add(prompt2);
		
		button = new JButton("              Save Score              ");
		button.setAlignmentX(JButton.CENTER_ALIGNMENT);
		button.setActionCommand("Submit Score");
		button.addActionListener(this);
		contentPane.add(button);
		
		contentPane.add(new JSeparator()
		,
        BorderLayout.LINE_START);
		contentPane.add(new JSeparator());
		
		prompt3 = new JLabel("");
		contentPane.add(prompt3);
		
		prompt4 = new JLabel("");
		contentPane.add(prompt4);
		
		contentPane.add(new JSeparator(JSeparator.HORIZONTAL));
		contentPane.add(new JSeparator(JSeparator.HORIZONTAL));
		
		nameTitle = new JLabel("");
		contentPane.add(nameTitle);
		
		scoreTitle = new JLabel("");
		contentPane.add(scoreTitle);
		
		contentPane.add(new JSeparator(JSeparator.HORIZONTAL));
		contentPane.add(new JSeparator(JSeparator.HORIZONTAL));
		
		name1 = new JLabel("");
		contentPane.add(name1);
		
		score1 = new JLabel("");
		contentPane.add(score1);
		
		name2 = new JLabel("");
		contentPane.add(name2);
		
		score2 = new JLabel("");
		contentPane.add(score2);
		
		name3 = new JLabel("");
		contentPane.add(name3);
		
		score3 = new JLabel("");
		contentPane.add(score3);
		
		name4 = new JLabel("");
		contentPane.add(name4);
		
		score4 = new JLabel("");
		contentPane.add(score4);
		
		name5 = new JLabel("");
		contentPane.add(name5);
		
		score5 = new JLabel("");
		contentPane.add(score5);
		
		frame.setContentPane(contentPane);
		frame.pack();
		frame.setVisible(true);
	  }
  }//Tetris
  
  /*
   * Activates when button is pressed
   * Writes To file, Reads Top 5, And Displays Those on JFrame
   */
  public void actionPerformed(ActionEvent event) {
		String eventName = event.getActionCommand();
		if (eventName.contentEquals("Submit Score")) {
			scoreNameParsed = scoreName.getText();
			writeScore();
			readScore();
			
			prompt3.setText("High Scores");
			
			nameTitle.setText("Name");
			scoreTitle.setText("Score");
			
			name1.setText(top5Names[0]);
			name2.setText(top5Names[1]);
			name3.setText(top5Names[2]);
			name4.setText(top5Names[3]);
			name5.setText(top5Names[4]);
			
			score1.setText(Integer.toString(top5Scores[0]));
			score2.setText(Integer.toString(top5Scores[1]));
			score3.setText(Integer.toString(top5Scores[2]));
			score4.setText(Integer.toString(top5Scores[3]));
			score5.setText(Integer.toString(top5Scores[4]));
			
			button.setText("Score Submitted");
			button.setActionCommand("Not Again");
		} else {
			button.setText("Score Already Submitted");
		}
	}//actionPerformed

  /*
   * Used For JFrame
   */
  private static void runGUI() {
		Tetris tetris = new Tetris();
	}//RunGUI

  /*
   * Appends score, name entered and system time to file
   */
  public void writeScore() {
	  if (savedScores.exists()) {
		} else {
			try {
				savedScores.createNewFile();
			} catch (IOException e) {
				System.err.println("IOExeption: " + e.getMessage());
			}
		}
		//Writes
		try {
			out = new FileWriter(savedScores,true);
			writeFile = new BufferedWriter(out);
			writeFile.write(Integer.toString(score));
			writeFile.newLine();
			writeFile.write(scoreNameParsed);
			writeFile.newLine();
			writeFile.write(Long.toString(System.currentTimeMillis()));
			writeFile.newLine();
			writeFile.close();
			out.close();
		} catch (IOException e) { 
			System.err.println("IOExeption: " + e.getMessage());
		}
  }//Write Score
  
  /*
   * Reads File, and sorts 5 highest scores into the arrays
   */
  public void readScore() {
	  int j = 0;
	  try {
			in = new FileReader(savedScores);
			readFile = new BufferedReader(in);
			while ((lineOfText = readFile.readLine()) != null) {
				if (j % 3 == 0) {
					if ((top5Scores[0]) < Integer.parseInt(lineOfText)) {
						top5Scores[4] = top5Scores[3];
						top5Scores[3] = top5Scores[2];
						top5Scores[2] = top5Scores[1];
						top5Scores[1] = top5Scores[0];
						top5Scores[0] = Integer.parseInt(lineOfText);
						lastPosition = 0;
					} else if ((top5Scores[1]) < Integer.parseInt(lineOfText)) {
						top5Scores[4] = top5Scores[3];
						top5Scores[3] = top5Scores[2];
						top5Scores[2] = top5Scores[1];
						top5Scores[1] = Integer.parseInt(lineOfText);
						lastPosition = 1;
					} else if ((top5Scores[2]) < Integer.parseInt(lineOfText)) {
						top5Scores[4] = top5Scores[3];
						top5Scores[3] = top5Scores[2];
						top5Scores[2] = Integer.parseInt(lineOfText);
						lastPosition = 2;
					} else if ((top5Scores[3]) < Integer.parseInt(lineOfText)) {
						top5Scores[4] = top5Scores[3];
						top5Scores[3] = Integer.parseInt(lineOfText);
						lastPosition = 3;
					} else if ((top5Scores[4]) < Integer.parseInt(lineOfText)) {
						top5Scores[4] = Integer.parseInt(lineOfText);
						lastPosition = 4;
					} else {
						lastPosition = -1;
					}
				}
				if (j % 3 == 1) {
					if (lastPosition == 0) {
						top5Names[4] = top5Names[3];
						top5Names[3] = top5Names[2];
						top5Names[2] = top5Names[1];
						top5Names[1] = top5Names[0];
						top5Names[0] = lineOfText;
					} else if (lastPosition == 1) {
						top5Names[4] = top5Names[3];
						top5Names[3] = top5Names[2];
						top5Names[2] = top5Names[1];
						top5Names[1] = lineOfText;
					} else if (lastPosition == 2) {
						top5Names[4] = top5Names[3];
						top5Names[3] = top5Names[2];
						top5Names[2] = lineOfText;
					} else if (lastPosition == 3) {
						top5Names[4] = top5Names[3];
						top5Names[3] = lineOfText;
					} else if (lastPosition == 4) {
						top5Names[4] = lineOfText;
					}
				}
				j++;
			}
			readFile.close();
			in.close();
		} catch (IOException e) {
			System.out.println("Problem reading file");
			System.err.println("IOExeption: " + e.getMessage());
		}
  }//Read File

  public static void main(String[] args) {
    // Variables
    long loopTime = System.currentTimeMillis() + 300;
    for (int i = 0; i < 10; i++) {
      for (int j = 0; j < 20; j++) {
        coloredBoard[i][j] = Color.WHITE;
      }
    }
    
    // Defines Colors For Tetris Pieces
    tetrominoColors[0] = Color.CYAN;
    tetrominoColors[1] = Color.YELLOW;
    tetrominoColors[2] = Color.MAGENTA;
    tetrominoColors[3] = Color.GREEN;
    tetrominoColors[4] = Color.RED;
    tetrominoColors[5] = Color.BLUE;
    tetrominoColors[6] = Color.ORANGE;
    
    // Constructs JFrame
    JFrame frame = new JFrame("Tetris | By: Emma Power");
    final Tetris tetris = new Tetris();
    frame.add(tetris);
    frame.setSize(boardX + 317 + boardX, 640);
    
    // Key Listener Activates Shift/Rotate/Drop Methods on key press
    frame.addKeyListener(new KeyListener() {
      public void keyTyped(KeyEvent e) {
      }
      public void keyPressed(KeyEvent e) {
        if(e.getKeyCode() == KeyEvent.VK_LEFT){
          tetris.shift(-1);
        }
        if (e.getKeyCode() == KeyEvent.VK_RIGHT){
          tetris.shift(1);
        }
        if(e.getKeyCode() == KeyEvent.VK_UP){
          tetris.rotate();
        }
        if(e.getKeyCode() == KeyEvent.VK_DOWN){  
          tetris.drop();
          tetris.collisionCheck();
          tetris.boardMaker(2);
          tetris.score();
        }
        if(e.getKeyCode() == KeyEvent.VK_SHIFT){
          tetris.hold();
        }
      }
      public void keyReleased(KeyEvent e) {
      }
    });
    
    // Start Game
    frame.setVisible(true);
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    tetris.initPiece(1);
    tetris.boardMaker(2);
    
    //Main Game Loop
    while (!gameOver) {
      loopTime = System.currentTimeMillis() + 300;
      tetris.repaint();
      // Loops until loopTime is over, and forces a drop in piece
      while (System.currentTimeMillis() < loopTime) {
        tetris.repaint();
      }
      tetris.drop();
      tetris.collisionCheck();
      tetris.score();
      tetris.boardMaker(2);
    }
    tetris.repaint();
    javax.swing.SwingUtilities.invokeLater(new Runnable() {
		public void run() {
			runGUI();
		}	
	});
  }// Main
}// Tetris