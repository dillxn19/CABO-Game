
public class ActionCard extends BaseCard {

  // data field
  private String actionType;


  /**
   * Constructs an ActionCard with the specified rank, suit, and action type. You may assume that
   * the provided action type is valid.
   * 
   * @param rank       the rank of the card (e.g., 1 for Ace, 13 for King).
   * @param suit       the suit of the card (e.g., "Hearts", "Diamonds")
   * @param actionType the type of action associated with this card: "peek", "spy", or "switch"
   */
  public ActionCard(int rank, String suit, String actionType) {
    super(rank, suit);
    this.actionType = actionType;
  }

  /**
   * Gets the type of action associated with this card.
   * 
   * @return the action type as a String: "peek", "spy", or "switch".
   */
  public String getActionType() {
    return actionType;

  }
}
