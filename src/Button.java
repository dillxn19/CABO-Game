
/**
 * CLASS DESCRIPTION
 * 
 * The Button class represents a simple interactive button in the Processing environment. It
 * displays a label and can change its appearance when active or inactive. The button's appearance
 * and behavior are managed through the Processing library.
 */
public class Button {
  private boolean active; // The active state of the button (true if active)
  private int height; // The height of the button
  private String label; // The text label displayed on the button
  protected static processing.core.PApplet processing; // The Processing environment used for
                                                       // drawing the button
  private int width; // The width of the button
  private int x; // The x-coordinate of the top-left corner of the button
  private int y; // The y-coordinate of the top-left corner of the button


  /**
   * CONSTRUCTOR Constructs a Button with the specified label and position, which is inactive by
   * default.
   * 
   * @param label - the text label displayed on the button.
   * @paramx - the x-coordinate of the top-left corner of the button.
   * @param y      - the y-coordinate of the top-left corner of the button.
   * @param width  - the width of the button.
   * @param height - the height of the button.
   * @throws IllegalStateException if the Processing environment has not been initialized.
   */
  public Button(String label, int x, int y, int width, int height) {
    if (processing == null)
      throw new IllegalStateException();
    this.label = label;
    this.x = x;
    this.y = y;
    this.width = width;
    this.height = height;
    active = false;

  }

  /**
   * Renders the button on the Processing canvas. The button changes color based on its isActive
   * parameter and whether the mouse is currently over it
   */
  public void draw() {

    if (isActive()) {

      if (isMouseOver()) {
        processing.fill(150); // mouse hovering over button
      } else {
        processing.fill(200); // mouse is not over button
      }
    } else {
      processing.fill(255, 51, 51); // non-active button
    }

    processing.rect(x, y, width, height, 5);

    // text inside button
    processing.fill(0);
    processing.textSize(14);
    processing.textAlign(processing.CENTER, processing.CENTER);
    processing.text(label, x + width / 2, y + height / 2);
  }

  /**
   * Returns the label of this button
   * 
   * @return this button's current label
   */
  public String getLabel() {
    return label;
  }

  /**
   * Returns whether the button is currently active.
   * 
   * @return true if the button is active, false otherwise.
   */
  public boolean isActive() {
    return active;
  }

  /**
   * Checks if the mouse is currently over this button.
   * 
   * @return true if the button is under the mouse's current position, false otherwise.
   */
  public boolean isMouseOver() {
    if ((x <= processing.mouseX && processing.mouseX <= x + width)
        && (y <= processing.mouseY && processing.mouseY <= y + height)) {
      return true;
    }
    return false;
  }

  /**
   * Sets the active state of the button. If true, the button will be rendered as active. If false,
   * it will be rendered as inactive.
   * 
   * @param active - the new active state of the button.
   */
  public void setActive(boolean active) {
    this.active = active;
  }

  /**
   * Changes the label of this button
   * 
   * @param label - the new label for this button
   */
  public void setLabel(String label) {
    this.label = label;
  }

  /**
   * Sets the Processing environment to be used by the Button class. Must be called before creating
   * any buttons
   * 
   * @param processing - the Processing environment to be used for drawing and interaction.
   */
  public static void setProcessing(processing.core.PApplet processing) {
    Button.processing = processing;
  }
}
