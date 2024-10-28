
import java.util.ArrayList;
import processing.core.PApplet;

/**
 * The CaboGame class implements the main game logic for the card game CABO. It manages the deck,
 * discard pile, players, game state, and user interactions.
 */
public class CaboGame extends processing.core.PApplet {

  // data fields
  private Deck deck;
  private Deck discard;

  private Player[] players;
  private int currentPlayer;
  private boolean gameOver;
  private int caboPlayer;
  private Button[] buttons;
  private int selectedCardFromCurrentPlayer;
  private BaseCard drawnCard;

  /**
   * Enum representing the different action states in the game (e.g., swapping cards, peeking,
   * spying, switching).
   * 
   * This allows us to easily restrict the possible values of a variable.
   */
  private enum ActionState {
    NONE, SWAPPING, PEEKING, SPYING, SWITCHING
  }

  private ActionState actionState = ActionState.NONE;

  // provided data fields for tracking the players' moves through the game
  private ArrayList<String> gameMessages = new ArrayList<>();

  /**
   * Launch the game window; PROVIDED. Note: the argument to PApplet.main() must match the name of
   * this class, or it won't run!
   * 
   * @param args unused
   */
  public static void main(String[] args) {
    PApplet.main("CaboGame");
  }

  /**
   * Sets up the initial window size for the game; PROVIDED.
   */
  @Override
  public void settings() {
    size(1000, 800);
  }

  /**
   * Sets up the game environment, including the font, game state, and game elements.
   */
  @Override
  public void setup() {
    // setting up the graphical environment for the game
    textFont(createFont("Arial", 16));
    BaseCard.setProcessing(this);
    Deck.setProcessing(this);
    Button.setProcessing(this);
    deckCheck();

    // creating decks
    deck = new Deck(Deck.createDeck());
    discard = new Deck(new ArrayList<BaseCard>());
    drawnCard = null;

    // creating players in the game
    players = new Player[4];
    players[0] = new Player("Cyntra", 0, false);
    players[1] = new AIPlayer("Avalon", 1, true);
    players[2] = new AIPlayer("Balthor", 2, true);
    players[3] = new AIPlayer("Ophira", 3, true);

    caboPlayer = -1;
    currentPlayer = 0;
    selectedCardFromCurrentPlayer = -1;

    // distributing cards to each player
    for (int i = 0; i < 4; i++) {
      for (int j = 0; j < 4; j++) {
        players[j].addCardToHand(deck.drawCard());
      }
    }

    players[0].getHand().setFaceUp(0, true);
    players[0].getHand().setFaceUp(1, true);
    System.out.println("Deck size: " + deck.size());

    // set up buttons
    buttons = new Button[5];

    /*
     * // WARNING: the buttons may exit the screen for certain screens. Use these values to set up
     * buttons in such a case buttons[0] = new Button("Draw from Deck", 750, 50, 150, 40);
     * buttons[1] = new Button("Swap a Card", 750, 150, 150, 40); buttons[2] = new
     * Button("Declare Cabo", 750, 250, 150, 40); buttons[3] = new Button("Use Action", 750, 250 +
     * 100, 150, 40); buttons[4] = new Button("End Turn", 750, 250 + 100 + 100, 150, 40);
     */


    buttons[0] = new Button("Draw from Deck", 50, 700, 150, 40);
    buttons[1] = new Button("Swap a Card", 220, 700, 150, 40);
    buttons[2] = new Button("Declare Cabo", 390, 700, 150, 40);
    buttons[3] = new Button("Use Action", 390 + 170, 700, 150, 40);
    buttons[4] = new Button("End Turn", 390 + 170 + 170, 700, 150, 40);

    // update the states of the buttons
    updateButtonStates();

    // update the gameMessages log: "Turn for "+currentPlayer.name
    setGameStatus("Turn for " + players[currentPlayer].getName());
  }

  /**
   * Console-only output for verifying the setup of the card objects and the deck containing them
   */
  public void deckCheck() {
    Deck tempDeck = new Deck(Deck.createDeck());
    // checks if deck size is 52
    System.out.println("Deck size is 52: " + (tempDeck.size() == 52));

    // checks if 8 of each action card
    int peekCount = 0;
    int spyCount = 0;
    int switchCount = 0;
    int heartsCount = 0;
    int spadesCount = 0;
    int diamondsCount = 0;
    int clubsCount = 0;
    for (int i = 0; i < tempDeck.size(); i++) {
      // checks the number of each suit
      if (tempDeck.cardList.get(i).suit.equals("Hearts")) {
        heartsCount += 1;
      } else if (tempDeck.cardList.get(i).suit.equals("Spades")) {
        spadesCount += 1;
      } else if (tempDeck.cardList.get(i).suit.equals("Diamonds")) {
        diamondsCount += 1;
      } else if (tempDeck.cardList.get(i).suit.equals("Clubs")) {
        clubsCount += 1;
      }

      // checks if 8 of each action card
      if (tempDeck.cardList.get(i) instanceof ActionCard) {
        ActionCard action = (ActionCard) tempDeck.cardList.get(i);
        if (action.getActionType().equals("peek")) {
          peekCount += 1;
        } else if (action.getActionType().equals("spy")) {
          spyCount += 1;
        } else if (action.getActionType().equals("switch")) {
          switchCount += 1;
        }
      }

      if (tempDeck.cardList.get(i).getRank() == -1 && tempDeck.cardList.get(i).rank == 13
          && tempDeck.cardList.get(i).suit == "Diamonds") {
        System.out.println("King of Diamonds found and returns -1");
      }
    }


    if (peekCount == 8 && spyCount == 8 && switchCount == 8) {
      System.out.println("There is 8 of each action card");
    } else {
      System.out.print("There is not 8 of each action card");
    }

    if (heartsCount == 13 && spadesCount == 13 && diamondsCount == 13 && clubsCount == 13) {
      System.out.println("There is 13 of each suit");
    } else {
      System.out.println("There is not 13 of each suit");
    }


  }

  /**
   * Updates the state of the action buttons based on the current game state. Activates or
   * deactivates buttons depending on whether it's the start of a player's turn, a card has been
   * drawn, or the player is an AI.
   */
  public void updateButtonStates() {
    for (int i = 0; i < 5; i++) {
      buttons[i].setActive(false);
    }

    // if the current player is a computer, do not activate any button
    if (!players[currentPlayer].isComputer()) {
      if (drawnCard == null) { // no card has been drawn yet by the current player if drawnCard is
                               // null.
        buttons[0].setActive(true);
        if (caboPlayer == -1) // cabo has not been called
          buttons[2].setActive(true);
      } else {
        buttons[1].setActive(true); // swap a card
        buttons[4].setActive(true); // end turn
        if (drawnCard.getRank() >= 7 && drawnCard.getRank() <= 12) // for action cards
          buttons[3].setActive(true);
        if (drawnCard.getRank() == 7 || drawnCard.getRank() == 8) // peek
          buttons[3].setLabel("PEEK");
        else if (drawnCard.getRank() == 9 || drawnCard.getRank() == 10) // spy
          buttons[3].setLabel("SPY");
        else if (drawnCard.getRank() == 11 || drawnCard.getRank() == 12) // switch
          buttons[3].setLabel("SWITCH");


      }
    }
  }

  /**
   * Renders the graphical user interface; also handles some game logic for the computer players.
   */
  @Override
  public void draw() {
    background(0, 128, 0);
    // draws deck and discard
    deck.draw(500, 80, false);
    discard.draw(600, 80, true);

    // labels
    textSize(16);
    fill(255);
    text("Deck:", 520, 60);
    text("Discard Pile:", 644, 60);

    // draws players' hands
    text("Cyntra", 50, 45 + 150 * 0);
    text("Avalon", 50, 45 + 150 * 1);
    text("Balthor", 50, 45 + 150 * 2);
    text("Ophira", 50, 45 + 150 * 3);
    players[0].getHand().draw(60 + 150 * 0);
    players[1].getHand().draw(60 + 150 * 1);
    players[2].getHand().draw(60 + 150 * 2);
    players[3].getHand().draw(60 + 150 * 3);

    // draws buttons
    if (!players[currentPlayer].isComputer()) {
      buttons[0].draw();
      buttons[1].draw();
      buttons[2].draw();
      buttons[3].draw();
      buttons[4].draw();
    }

    // show the drawn card, if there is one
    if (drawnCard != null) {
      drawnCard.setFaceUp(true);
      drawnCard.draw(500, 500);
    }
    // Display game messages with different colors based on the content
    int y = 200; // Starting y-position for messages
    for (String message : gameMessages) {
      textSize(16);
      if (message.contains("CABO")) {
        fill(255, 128, 0);
      } else if (message.contains("switched")) {
        fill(255, 204, 153);
      } else if (message.contains("spied")) {
        fill(255, 229, 204);
      } else {
        fill(255);
      }
      text(message, width - 300, y); // Adjust x-position as needed
      y += 20; // Spacing between messages
    }
    // if the game is over, display the game over status
    if (gameOver)
      displayGameOver();

    // handles the computer players' turns
    if (players[currentPlayer].isComputer() && !gameOver) {
      performAITurn();
    }


  }


  /**
   * Handles mouse press events during the game. It manages user interactions with buttons (that is,
   * drawing a card, declaring CABO, swapping cards, using action cards) and updates the game state
   * accordingly.
   */
  @Override
  public void mousePressed() {

    // if game is over or it's the computer's turn, do nothing
    if (!gameOver && !(players[currentPlayer].isComputer())) {
      // handles button clicks
      for (int i = 0; i < buttons.length; i++) {
        Button button = buttons[i];
        if (button.isMouseOver() && button.isActive()) {
          switch (i) {
            case 0 -> { // draw from deck
              drawFromDeck();
              break;
            }
            case 1 -> { // swap cards

              setGameStatus("Click a card in your hand to swap it with the drawn card.");
              actionState = ActionState.SWAPPING;
            }
            case 2 -> { // declare Cabo
              declareCabo();
              break;
            }
            case 3 -> { // Use Action
              if (drawnCard instanceof ActionCard) {
                ActionCard newCard = (ActionCard) drawnCard;
                String action = newCard.getActionType();

                if (action.toLowerCase().equals("peek")) {
                  actionState = ActionState.PEEKING;
                  setGameStatus("Click a card in your hand to peek at it.");
                } else if (action.toLowerCase().equals("spy")) {
                  actionState = ActionState.SPYING;
                  setGameStatus("Click a card in another player's hand to spy on it.");
                } else {
                  actionState = ActionState.SWITCHING;
                  setGameStatus(
                      "Click a card from your hand, then a card from another Kingdom's hand to switch.");
                }
              }
              break;
            }
            case 4 -> { // end turn
              nextTurn();
            }
          }
          break;
        }
      }

      // handle additional action states
      switch (actionState) {
        case SWAPPING -> handleCardSwap();
        case PEEKING -> handlePeek();
        case SPYING -> handleSpy();
        case SWITCHING -> handleSwitch();
        default -> {
          /* No action to be taken */
        }
      }
    }
  }

  ///////////////////////////////////// BUTTON CLICK HANDLERS /////////////////////////////////////

  /**
   * Handles the action of drawing a card from the deck. If the deck is empty, the game ends.
   * Otherwise, the drawn card is displayed in the middle of the table. The game status and button
   * states are updated accordingly.
   */
  public void drawFromDeck() { // if the deck is empty, game over
    if (deck.size() == 0) {
      gameOver = true;
    } else { // otherwise, draw the next card from the deck
      drawnCard = deck.drawCard();

      setGameStatus(players[currentPlayer].getName() + " drew a card."); // update the gameMessages
                                                                         // log: player.name+" drew
                                                                         // a card."

      updateButtonStates(); // update the button states
    }

  }

  /**
   * Handles the action of declaring CABO. Updates the game status to show that the player has
   * declared CABO.
   */
  public void declareCabo() {
    caboPlayer = currentPlayer;
    setGameStatus(players[currentPlayer].getName() + " declares CABO!"); // update the gameMessages
                                                                         // log: player.name+"
                                                                         // declares CABO!"
    nextTurn(); // end this player's turn


  }

  ///////////////////////////////////// ACTION STATE HANDLERS /////////////////////////////////////


  /**
   * This method runs when the human player has chosen to SWAP the drawn card with one from their
   * hand. Detect if the mouse is over a card from the currentPlayer's hand and, if it is, swap the
   * drawn card with that card.
   * 
   * If the mouse is not currently over a card from the currentPlayer's hand, this method does
   * nothing.
   */
  public void handleCardSwap() {


    actionState = ActionState.SWAPPING;

    // find the index of the card from the current player's hand that the mouse is currently over
    int index = -1;
    for (int i = 0; i < players[currentPlayer].getHand().size(); i++) {
      if (players[currentPlayer].getHand().cardList.get(i).isMouseOver()) {
        index = i;
        break;
      }
    }

    if (index >= 0) {
      // swap the card at the selected index with the drawnCard and add it to the discard pile
      discard.addCard(players[currentPlayer].getHand().swap(drawnCard, index));

      // update the gameMessages log: "Swapped the drawn card with card "+(index+1)+" in the hand."
      setGameStatus("Swapped the drawn card with card " + (index + 1) + " in the hand.");

      // for COmputer Players
      AIPlayer AI;
      for (int j = 1; j < players.length; ++j) {
        AI = (AIPlayer) players[j];
        AI.setCardKnowledge(currentPlayer, index, false); // erase all knowledge of the card at that
                                                          // index from the AI
      }
      // set all buttons except End Turn to inactive
      for (int i = 0; i < 5; i++) {
        buttons[i].setActive(false);
      }
      buttons[4].setActive(true);

      // set the drawnCard to null and the actionState to NONE
      actionState = ActionState.NONE;
      drawnCard = null;
    }



  }

  /**
   * Handles the action of peeking at one of your cards. The player selects a card from their own
   * hand, which is then revealed (set face-up).
   * 
   * If the mouse is not currently over a card from the currentPlayer's hand, this method does
   * nothing.
   */
  public void handlePeek() {
    // find index of a card from the current player's hand that the mouse is currently over
    int index = players[currentPlayer].getHand().indexOfMouseOver();
    if (index >= 0) {
      players[currentPlayer].getHand().setFaceUp(index, true); // set that card at that index to be
                                                               // face-up

      setGameStatus("Revealed card " + (index + 1) + " in the hand."); // update the gameMessages
                                                                       // log: "Revealed card
                                                                       // "+(index+1)+" in the
                                                                       // hand."

      discard.addCard(drawnCard); // add the drawnCard to the discard pile

      // set all buttons except End Turn to inactive
      for (int i = 0; i < 5; i++) {
        buttons[i].setActive(false);
      }
      buttons[4].setActive(true);

      // set the drawnCard to null and the actionState to NONE
      actionState = ActionState.NONE;
      drawnCard = null;

    }
  }

  /**
   * Handles the spy action, allowing the current player to reveal one of another player's cards.
   * The current player selects a card from another player's hand, which is temporarily revealed.
   * 
   * If the mouse is not currently over a card from another player's hand, this method does nothing.
   */
  public void handleSpy() {
    for (int i = 0; i < players.length; i++) {
      // find a card from any player's hand that the mouse is currently over
      int index = players[i].getHand().indexOfMouseOver();
      if (index >= 0 && i != currentPlayer) {
        players[i].getHand().setFaceUp(index, true); // if it is not one of their own cards, set it
                                                     // to be face-up
        setGameStatus("Spied on " + players[i].getName() + "'s card."); // update the gameMessages
                                                                        // log: "Spied on
                                                                        // "+player.name+"'s card."
        discard.addCard(drawnCard);// add the drawnCard to the discard,

        // set all buttons except End Turn to inactive
        for (int j = 0; j < 5; j++) {
          buttons[j].setActive(false);
        }
        buttons[4].setActive(true);

        // set the drawnCard to null and the actionState to NONE
        actionState = ActionState.NONE;
        drawnCard = null;

        break;
      }
    }
  }


  /**
   * Handles the switch action, allowing the current player to switch one of their cards with a card
   * from another player's hand.
   * 
   * This action is performed in 2 steps, in this order: (1) select a card from the current player's
   * hand (2) select a card from another player's hand
   * 
   * If the mouse is not currently over a card, this method does nothing.
   */
  public void handleSwitch() {
    // check if the player has selected a card from their own hand yet
    if (selectedCardFromCurrentPlayer == -1) {
      // if they haven't: determine which card in their own hand the mouse is over & store it
      // and do nothing else
      selectedCardFromCurrentPlayer = players[currentPlayer].getHand().indexOfMouseOver();
    } else { // if they have selected a card from their own hand already:

      // find a card from any OTHER player's hand that the mouse is currently over
      for (int i = 0; i < players.length; i++) {
        int index = players[i].getHand().indexOfMouseOver();
        if (index >= 0 && i != currentPlayer) {
          players[currentPlayer].getHand().switchCards(selectedCardFromCurrentPlayer,
              players[i].getHand(), index); // swap the selected card with the card from the
                                            // currentPlayer's hand

          setGameStatus("Switched a card with " + players[i].getName()); // update the gameMessages
                                                                         // log: "Switched a card
                                                                         // with "+player.name

          discard.addCard(drawnCard); // add the drawnCard to the discard pile

          // set all buttons except End Turn to inactive
          for (int j = 0; j < 5; j++) {
            buttons[j].setActive(false);
          }
          buttons[4].setActive(true);

          // set the drawnCard to null and the actionState to NONE
          actionState = ActionState.NONE;
          drawnCard = null;

          // update the knowledge of the swapped card for the other player
          if (players[i] instanceof AIPlayer) {
            boolean knowledge = ((AIPlayer) players[i]).getCardKnowledge(currentPlayer,
                selectedCardFromCurrentPlayer);
            ((AIPlayer) players[i]).setCardKnowledge(currentPlayer, selectedCardFromCurrentPlayer,
                ((AIPlayer) players[i]).getCardKnowledge(i, index));
            ((AIPlayer) players[i]).setCardKnowledge(i, index, knowledge);
          }
          selectedCardFromCurrentPlayer = -1; // reset the selected card instance variable to -1

          break;
        }
      }
    }
  }

  /////////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * Advances the game to the next player's turn. Hides all players' cards, updates the current
   * player, checks for game-over conditions, resets action states, and updates the UI button states
   * for the new player's turn.
   */
  public void nextTurn() {
    // hide all players' cards
    for (int i = 0; i < players.length; i++) {
      for (int j = 0; j < 4; j++) {
        players[i].getHand().setFaceUp(j, false);
      }
    }
    buttons[3].setLabel("Use Action");

    // if there is still an active drawnCard, discard it and set drawnCard to null
    if (drawnCard != null) {
      discard.addCard(drawnCard);
      drawnCard = null;
    }
    // advance the current player to the next one in the list
    if (currentPlayer == players.length - 1) {
      currentPlayer = 0;
    } else {
      currentPlayer++;
    }

    // check if the new player is the one who declared CABO (and end the game if so)
    if (currentPlayer == caboPlayer) {
      gameOver = true;
    } else { // set up the conditions for a new turn
      setGameStatus("Turn for " + players[currentPlayer].getName()); // update the gameMessages log:
                                                                     // "Turn for "+player.name
      actionState = ActionState.NONE; // reset the action state to NONE
      updateButtonStates(); // update the button states

    }
  }

  /**
   * Displays the game-over screen and reveals all players' cards. The method calculates each
   * player's score, identifies the winner, and displays a message about the game's result,
   * including cases where there is no winner.
   * 
   * We've provided the code for the GUI parts, but the logic behind this method is still TODO
   */
  public void displayGameOver() {
    // Create a dimmed background overlay
    fill(0, 0, 0, 200);
    rect(0, 0, width, height);
    fill(255);
    textSize(32);
    textAlign(CENTER, CENTER);
    text("Game Over!", (float) width / 2, (float) height / 2 - 150);

    // setting up variables to calculate the winner
    int score;
    int yPosition = height / 2 - 100;
    boolean tie = false;
    int highScore = players[0].getHand().calcHand();
    String winner = players[0].getName();

    // finding out the winner
    for (int i = 0; i < players.length; i++) {
      score = players[i].getHand().calcHand();
      if (score < highScore) {
        highScore = score;
        winner = players[i].getName();
        if (tie) { // in case of 2 or more winners
          tie = false;
        }
      } else if (score == highScore) {
        tie = true;
      }

      // display each player's score
      textSize(24);
      text(players[i].getName() + "'s score: " + score, (float) width / 2, yPosition);
      yPosition += 30;

      // reveal all cards of each player
      for (int j = 0; j < 4; j++) {
        players[i].getHand().setFaceUp(j, true);
      }
    }



    // check if there is a tie or a specific CABO winner (lowest score wins)
    if (tie)
      text("No Winner. The war starts.", (float) width / 2, yPosition + 30); // there is no winner

    else
      text("Winner: " + winner, (float) width / 2, yPosition + 30); // there is a winner
  }

  /**
   * PROVIDED: Sets the current game status message and updates the message log. If the message log
   * exceeds a maximum number of messages, the oldest message is removed.
   *
   * @param message the message to set as the current game status.
   */
  private void setGameStatus(String message) {
    gameMessages.add(message);
    int MAX_MESSAGES = 15;
    if (gameMessages.size() > MAX_MESSAGES) {
      gameMessages.remove(0); // Remove the oldest message
    }
  }

  /////////////////////////////////////////////////////////////////////////////////////////////////
  // The 2 methods below this line are PROVIDED in their entirety to run the AIPlayer interactions
  // with the CABO game. Uncomment them once you are ready to add AIPlayer actions to your game!
  /////////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * Performs the AI player's turn by drawing a card and deciding whether to swap, discard, or use
   * an action card. If the AI player draws a card that is better than their highest card, they swap
   * it; otherwise, they discard it. If the drawn card is an action card, the AI player performs the
   * corresponding action. If the AI player's hand value is low enough, they may declare CABO.
   */

  private void performAITurn() {
    AIPlayer aiPlayer = (AIPlayer) players[currentPlayer];
    String gameStatus = aiPlayer.getName() + " is taking their turn.";
    setGameStatus(gameStatus);

    // Draw a card from the deck
    drawnCard = deck.drawCard();
    if (drawnCard == null) {
      gameOver = true;
      return;
    }

    gameStatus = aiPlayer.getName() + " drew a card.";
    setGameStatus(gameStatus);

    // Determine if AI should swap or discard
    int drawnCardValue = drawnCard.getRank();
    int highestCardIndex = aiPlayer.getHighestIndex();
    if (highestCardIndex == -1) {
      highestCardIndex = 0;
    }
    int highestCardValue = aiPlayer.getHand().getRankAtIndex(highestCardIndex);

    // Swap if the drawn card has a lower value than the highest card in hand
    if (drawnCardValue < highestCardValue) {
      BaseCard cardInHand = aiPlayer.getHand().swap(drawnCard, highestCardIndex);
      aiPlayer.setCardKnowledge(aiPlayer.getLabel(), highestCardIndex, true);
      discard.addCard(cardInHand);
      gameStatus = aiPlayer.getName() + " swapped the drawn card with card "
          + (highestCardIndex + 1) + " in their hand.";
      setGameStatus(gameStatus);
    } else if (drawnCard instanceof ActionCard) { // Use the action card
      String actionType = ((ActionCard) drawnCard).getActionType();
      gameStatus = aiPlayer.getName() + " uses an action card: " + actionType;
      setGameStatus(gameStatus);
      performAIAction(aiPlayer, actionType);
      discard.addCard(drawnCard);
    } else { // Discard the drawn card
      discard.addCard(drawnCard);
      gameStatus = aiPlayer.getName() + " discarded the drawn card: " + drawnCard;
      setGameStatus(gameStatus);
    }

    // AI may declare Cabo if hand value is low enough
    int handValue = aiPlayer.calcHandBlind();
    if (handValue <= random(13, 21) && caboPlayer == -1) {
      declareCabo();
    }

    // Prepare for the next turn
    drawnCard = null;
    nextTurn();
  }//


  /**
   * Performs the specified action for the AI player based on the drawn action card. Actions include
   * peeking at their own cards, spying on another player's card, or switching cards with another
   * player.
   *
   * @param aiPlayer   the AI player performing the action.
   * @param actionType the type of action to perform ("peek", "spy", or "switch").
   */

  private void performAIAction(AIPlayer aiPlayer, String actionType) {
    Player otherPlayer = players[0]; // Assuming Player 1 is the human player
    String gameStatus = "";
    switch (actionType) {
      case "peek" -> { // AI peeks at one of its own cards
        int unknownCardIndex = aiPlayer.getUnknownCardIndex();
        if (unknownCardIndex != -1) {
          aiPlayer.setCardKnowledge(aiPlayer.getLabel(), unknownCardIndex, true);
          gameStatus = aiPlayer.getName() + " peeked at their card " + (unknownCardIndex + 1);
          setGameStatus(gameStatus);
        }
      }
      case "spy" -> { // AI spies on one of the human player's cards
        int spyIndex = aiPlayer.getSpyIndex();
        if (spyIndex != -1) {
          aiPlayer.setCardKnowledge(0, spyIndex, true);
          gameStatus = aiPlayer.getName() + " spied on Player 1's card " + (spyIndex + 1);
          setGameStatus(gameStatus);
        }
      }
      case "switch" -> { // AI switches one of its cards with one of the human player's cards
        int aiCardIndex = aiPlayer.getHighestIndex();
        if (aiCardIndex == -1) {
          aiCardIndex = (int) random(aiPlayer.getHand().size());
        }
        int otherCardIndex = aiPlayer.getLowestIndex(otherPlayer);
        if (otherCardIndex == -1)
          otherCardIndex = (int) random(otherPlayer.getHand().size());

        // Swap the cards between AI and the human player
        aiPlayer.getHand().switchCards(aiCardIndex, otherPlayer.getHand(), otherCardIndex);
        boolean preCardKnowledge = aiPlayer.getCardKnowledge(aiPlayer.getLabel(), aiCardIndex);
        aiPlayer.setCardKnowledge(aiPlayer.getLabel(), aiCardIndex,
            aiPlayer.getCardKnowledge(0, otherCardIndex));
        aiPlayer.setCardKnowledge(0, otherCardIndex, preCardKnowledge);

        gameStatus = aiPlayer.getName() + " switched card " + (aiCardIndex + 1) + " with "
            + otherPlayer.getName() + "'s " + (otherCardIndex + 1) + ".";
        setGameStatus(gameStatus);
      }
    }
  }


}
