



import java.io.File;


public class BaseCard {
  // data fields
  private static processing.core.PImage cardBack;
  private processing.core.PImage cardImage;
  protected boolean faceUp;
  private final int HEIGHT = 70;
  protected static processing.core.PApplet processing;
  protected int rank;
  protected String suit;
  private final int WIDTH = 50;
  private int x;
  private int y;



  /**
   * Constructs a new BaseCard with the specified rank and suit. The card is initialized to be face
   * down by default. You may assume that the provided rank and suit are valid. This method should
   * also initialize the cardImage, and initialize cardBack if that has not yet been done by any
   * other constructor call.
   * 
   * @param rank the rank of the card (e.g., 1 for Ace, 13 for King).
   * @param suit the suit of the card (e.g., "Hearts", "Diamonds").
   */
  public BaseCard(int rank, String suit) {
    if (processing == null) {
      throw new IllegalStateException("Processing not set");
    }
    faceUp = false;
    this.rank = rank;
    this.suit = suit;
    cardImage = processing
        .loadImage("images" + File.separator + rank + "_of_" + suit.toLowerCase() + ".png");
    cardBack = processing.loadImage("images" + File.separator + "back.png");

  }

  /**
   * Sets the Processing environment to be used for drawing and interacting with cards. This method
   * must be called before creating any BaseCard objects.
   * 
   * @param processing the Processing PApplet environment.
   */
  public static void setProcessing(processing.core.PApplet processing) {
    BaseCard.processing = processing;
  }

  /**
   * Returns the rank of the card directly, or -1 if the card is the King of Diamonds
   * 
   * @return the rank of the card, or -1 for the King of Diamonds
   */
  public int getRank() {
    if (rank == 13 && suit.toLowerCase().equals("diamonds")) {
      return -1;
    }

    return rank;
  }

  /**
   * Sets the face-up status of the card.
   * 
   * @param faceUp if true, set the card face-up; if false, set it face-down.
   */
  public void setFaceUp(boolean faceUp) {
    this.faceUp = faceUp;
  }

  /**
   * Returns a string representation of the card, showing its suit and rank.
   * 
   * @return a string in the format "Suit Rank" (e.g., "Hearts 10").
   */
  @Override
  public String toString() {
    return suit + " " + rank;
  }

  /**
   * Draws the card on the PApplet at the specified position. Before drawing a card's image, be sure
   * to draw a white rectangle for it to sit on:
   * 
   * processing.fill(255); processing.rect(xPosition, yPosition, WIDTH, HEIGHT);
   * 
   * @param xPosition the x-coordinate to draw the card.
   * @param yPosition the y-coordinate to draw the card.
   */
  public void draw(int xPosition, int yPosition) {
    x = xPosition;
    y = yPosition;
    processing.fill(255);
    processing.rect(xPosition, yPosition, WIDTH, HEIGHT);
    if (this.faceUp) {
      processing.image(cardImage, x, y, WIDTH, HEIGHT); // face up
    } else {
      processing.image(cardBack, x, y, WIDTH, HEIGHT); // face down

    }

  }

  /**
   * Checks if the mouse is currently over this card. Use PApplet's mouseX and mouseY fields to
   * determine where the mouse is; the (x,y) coordinates of this card's upper left corner were set
   * when it was last drawn.
   * 
   * @return true if the card is under the mouse's current position, false otherwise.
   */
  public boolean isMouseOver() {
    if ((x <= processing.mouseX && processing.mouseX <= x + WIDTH)
        && (y <= processing.mouseY && processing.mouseY <= y + HEIGHT)) {
      return true;
    }
    return false;
  }


}
