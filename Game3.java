import java.awt.*;
import javax.imageio.*; // allows image loading
import java.io.*; // allows file access
import javax.swing.*;
import java.awt.event.*;  // Needed for ActionListener
import java.util.ArrayList;
import java.util.*;
import java.awt.image.BufferedImage;
import java.util.Scanner; 

public class Game3 extends Card
{
  
  static Scanner sc; // Initalize Scanner object
  
  private static int playernum = 4; // Default number of players
  private static int numhaswon = 0; // The number of players that have won
  private static int turnNum = 0;   // Which turn we're on (used by modulus to determine which user we're on)
  
  private static ArrayList<Player> players = new ArrayList <Player>(0); // ArrayList containing all the players 
  private static PlayField field = new PlayField();                     // PlayField object that holds all the runs that are played (visible to all)
  private static Deck deck = new Deck();                                // Deck object that the user draws cards from
  
  private static Deck deckb;           // A backup copy of the deck used in case the user messes up
  private static Player playerb;       // A backup copy of a player
  private static ArrayList<Run> runsB; // A backup copy of all the runs in the playField
  
  private static ArrayList <String> winners = new ArrayList<String>(0); // I might just remove this; it's a bit useless in terms of what we want to do
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // This code is Kai's and purely Kai's; don't touch it without explicit permission
  //////////////////////////////////////////////////////////
  
  // Kai's Images
    
  private static gameBoard window = new gameBoard(); // The gameboard object that all of the images will be rendered onto
  private static turn turnDisp;         // Another GUI Object, this is to allow for mouse clicks between turns.
  private static setup setupDisp;
  private static tutorial tut = new tutorial();      // Another GUI Object, this one is purely for the tutorial
  
  private static miscImage bg =          new miscImage(0,    1280, 0,   800, "resources//bg.png");      // The background of the entire UI
  private static miscImage boardbg =     new miscImage(160,  960,  160, 480, "resources//boardbg.png"); // The background of the gameboard
  
  private static miscImage playButton =  new miscImage(50,   120,  690, 60,  "resources//drawbutton.png"); // The play / draw adaptive button
  private static miscImage sortButton =  new miscImage(220,  120,  690, 60,  "resources//sortbutton.png"); // The sort button
  
  private static miscImage winnerImage = new miscImage(0,    1280, 0,   800, "resources//winner//p1.png"); // The Winner Display
  
  private static miscImage handArea =    new miscImage(390,  840,  690, 60,  "resources//handarea.png"); // The area where the user's hand is displayed
  
  // Note: My original intent was for these areas to display the users' names, but making 26*3 images of characters is a bit too much..? I'll see how that goes.
  
  private static ArrayList<miscImage> handImages = new ArrayList<miscImage>();  // An arrayList of all of the images (cards) that will be displayed in the handarea
  private static ArrayList<miscImage> boardImages = new ArrayList<miscImage>(); // An arrayList of all of the images (cards) that will be displayed on the gameboard
  
  ///////////////////////////////////////////////////////////
  
  // Kai's misc. controls
  
  private static boolean isDrawButton = true; // Boolean that controls the state of the play/draw button; true: draw, false: play
  
  private static boolean clickPlayButton = false; // Boolean that allows for interaction with the mouseListener; checks if playButton was clicked
  private static boolean clickSortButton = false; // Boolean that allows for interaction with the mouseListener; checks if sortButton was clicked
  
  // I will probably combine these later
  
  private static boolean madeMove = false; // Boolean that allows for interaction with the mouseListener; checks if user has moved a card around
  private static boolean haveToRedrawHand = false; // technically serves the same purpose as madeMove, but is manipulated by msListener.mouseMoved()
  
  private static int cardDraggedFrom = -1;      // Where was the card dragged from? -1 denotes it's not a card; 0 denotes hand; 1 denotes field
  private static boolean releaseOnRun;          // Was the card released atop a run?
  private static boolean releaseOnField = true; // Was the card even released atop the field?
  
  private static int whichRunFrom;  // Which run was the card moved from
  private static int whichRunTo;    // Which run was the card moved to?
  private static int whichCard;     // In that card (also used for hand), which index is the card
  
  private static boolean tutDone = false; // Checks if tutorial has finished
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  private static int onWhoseTurn = 0;     // A public variable that parallels "player" within the main class; set up so listeners can interact
  private static int currentHandSize = 0; // A public variable that allows listeners to know how many cards current player has on hand
  
  public static void main (String[]args) // The main core of the game
  {
    setupDisp = new setup(); // Initalize the setup window, but don't display it yet
    
    tut.setVisible(true); // Begin by displaying the tutorial sequence; this is governed in its own class
    
    while(!tutDone){ System.out.print(""); } // Wait for tutorial to finish (Loop has to print something otherwise it doesn't run, weird glitch)
    
    int player =  0;           // Which player's turn is it? Defaults to zero. (Can probably merge with onWhoseTurn)
    boolean hasPlayed = false; // Boolean that marks if a player has made a valid move on his turn or not
        
    initialize();             // Initalize method sets up the players and their hands
    
    while (numhaswon < 1) // If a player has not won yet
    {
    
      isDrawButton = true; // In the beginning of the turn, draw button is default selection
      
      onWhoseTurn = -1;     // Default values for these two; -1 denotes that they're not being used
      currentHandSize = -1; // As above
      
      boolean endTurn = false; // Beginning of turn; player has not yet chosen to end his turn
      hasPlayed = false;       // Beginning of turn; player has not yet played a valid move
      
      player = turnNum%playernum; // The player who's active is calculated
      
      window.setVisible(false);     // At this point, make sure the actual game window is hidden (We don't want users peeking at each others' hands)
      turnDisp = new turn(player);  // Create the turn prompt with whatever player is going next
      turnDisp.setVisible(true);    // Then set it to be visible
      
      while (!endTurn) // As long as the player has not decided to end the turn...
      {
        
        redrawImages(player); // redrawImages() is called to draw all of the cards for the current player
        
        if (players.get(player).isDone()) //If user has already won, skip his/her turn and go onto the next one
        {
          turnNum++; // Simply increment the turn number
          break;     // And skip
        }
        
        else // Otherwise, check to see if they've now won
        {
          
          if (userStatus()) // If they have won
          {
            winners.add (players.get(player).getName()); // Add them to the list of winners
            numhaswon++;                                 // Increment the number of people that have won
          }
          
        }
        
        field.orderAll(); // Organize all the runs on the gameBoard (Not working at the moment, have to check this)
        
        if (field.allValid()) // If all of the runs on the gameboard are current valid
        {
          update(); // call the update method to back them up
        }
        
        while(!clickPlayButton && !clickSortButton && !madeMove){ System.out.print(""); } // Keep checking to see if the user has clicked either button or made a move with the cards yet
        
        // NOTE: This loop has a System.out.print("") because it will not function otherwise. It's a strange bug that I can't seem to fix, so I'm circumventing it. 
        
        ///////////////////////////////////////////////////////////////////////////////////////////////////////////////
        // Main DecisionMaking Processes
        ///////////////////////////////////////////////////////////////////////////////////////////////////////////////
        
        // Sorting
        
        if(clickSortButton == true) // If the user clicked the sort button
        {
          madeMove = false;           // Reset the state of the madeMove flag
          clickSortButton = false;    // Reset the state of the clickSortButton flag
          players.get(player).sort(); // Sort the player's hand
        }
        
        // Playing / Drawing
        
        else if (clickPlayButton == true) // If the user clicked the play/draw button
        {
          madeMove = false;        // Reset the state of the madeMove flag
          clickPlayButton = false; // Reset the state of the clickPlayButton flag
          
          field.trimRuns(); // Trim all the empty runs from the board
          
          boolean allValid = field.allValid();// Checks if every run in the field is a valid run
          
          if (allValid) // if all runs on the field are valid
          {
            update(); // Overwrites the backups stored with the current arrays that are deemed to be valid
          }
          
          else // if validity check fails
          {
            hasPlayed = false; // sets hasPlayed to false; user has not made a valid move and thus must draw a card if he wants to end the turn
          }
          
          revert(); // Puts the backups back into their proper positions
          
          if(!hasPlayed && allValid) // If the player has not played anything and yet there have not been illegal edits made to the field
          {
            players.get(player).addCard(deck.deal()); // Deal a card from the hand
            endTurn = true;                           // Allow the user's turn to end
            turnNum++;                                // Increment the turn number to move to the next player
            
            // DrawButton setup
            
            if(hasPlayed == true){ isDrawButton = false; } // If the user has played anything valid, they can be allowed to just end their turn
            else{                  isDrawButton = true;  } // Otherwise, they must draw a card
          }
          
          else if(hasPlayed) // This will only trigger if a player ends the turn having played something out of their hand and all runs are valid
          {
            endTurn = true; // turn will no longer continue looping
            turnNum++;      // and turn number will increment one to allow the next person
            
            // DrawButton setup
            
            if(hasPlayed == true){ isDrawButton = false; } // If the user has played anything valid, they can be allowed to just end their turn
            else{                  isDrawButton = true;  } // Otherwise, they must draw a card
          }
        }
        
        // Manipulating cards
        
        else if(madeMove == true && releaseOnField && cardDraggedFrom != -1) // madeMove must be true for obvious reasons, the card must've been released on the field, and the initial position of the drag must be a card
        {
          madeMove = false;     // Reset the madeMove flag
          
          if(releaseOnRun) // If the card was released on a run
          {
            
            if(cardDraggedFrom == 0) // Was it dragged from the user's hand?
            {
              field.addToRun (whichRunTo, players.get(player).playCard(whichCard)); // Card is added to the playField
              field.orderAll();                                                     // PlayField is ordered (theoretically; doesn't quite work yet)
              window.repaint();
              hasPlayed = true;                                                     // Assume the player has played (it'll be checked when they make the decision to play this setup)
            }
            
            else // Was it dragged from another run?
            {
              Card temp = field.deleteFromRun (whichRunFrom, whichCard); // Delete the card from the original run and store it in a new temporary variable
              field.addToRun (whichRunTo, temp);                         // Add the temporary card into the desired run
              field.orderAll();                                          // PlayField is ordered (theoretically; doesn't quite work yet)
              window.repaint();

              // We don't set hasPlayed to true here, as this is simple manipulation of the field without adding anything new
            }
            
          }
          
          else // If the card was just released randomly into the gameBoard
          {
            
            if(cardDraggedFrom == 0) // If it came from the user's hand
            {
              Card[]temp = new Card [1];                         // Create a temporary array in which to store this single card (runs can only be constructed with Card[] parameters) 
              temp[0] = players.get(player).playCard(whichCard); // Remove card from player's hand and add it to the temporary array
              Run tempa = new Run (temp);                        // Create the new run out of the temporary array
              field.addRun (tempa);                              // Add the new run to the field
              hasPlayed = true;                                  // Assume the player has played a valid run
            }
            
            else
            {
              Card[]temp = new Card[1];                                // Create a temporary array in which to store this single card (runs can only be constructed with Card[] parameters) 
              temp[0] = field.deleteFromRun (whichRunFrom, whichCard); // Remove the card from the original run and add it to the temporary array
              Run tempa = new Run(temp);                               // Create a run from the temporary array 
              field.addRun (tempa);                                    // And add it to the playField
              
              // We don't set hasPlayed to true here, as this is simple manipulation of the field without adding anything new
            }
          }
        }
        
        // Some cleanup
        
        else // If nothing above is fulfilled, it means the user just randomly clicked somewhere on the GUI
        {
          madeMove = false; // Set the madeMove flag to false so our loop will not turn infinite
        }
        
        // DrawButton setup
        
        if(hasPlayed == true){ isDrawButton = false; } // If the user has played anything valid, they can be allowed to just end their turn
        else{                  isDrawButton = true;  } // Otherwise, they must draw a card
        
        field.orderAll(); // Ensure that all the runs on the board are properly ordered
        window.repaint(); // And redraw the image
        
      } 
    }
    
    winnerImage.setImg("resources\\winner\\P" + (player+1) + ".png"); // When the game is won, set the image to display the player's number and that they've won
    
    window.add(winnerImage); // Add that to the window, overlaying right on top
    window.repaint();        // And redraw the window as such
    
  }
  
  public static void redrawImages(int player) // Draws all the cards of a player's hand (and also the playingField)
  {
    
    window.revert();                          // We first set the background of the image to the way it originally was
    handImages = new ArrayList<miscImage>();  // Reinitalize handImages, clearing out all the prior stuff
    boardImages = new ArrayList<miscImage>(); // Reinitalize boardImages, clearing out all the prior stuff
    
    for(int i = 0; i < players.get(player).getHand().size(); i++) // Loop through the player's hand
    {
      handImages.add(new miscImage(395 + (42*i), 42, 693, 55,  "resources\\cards\\" + players.get(player).getHand().get(i).getID() + ".png")); // Add the image to the handImages array                                                                                                            
    }
    
    onWhoseTurn = player;                // Set up onWhoseTurn to duplicate player
    currentHandSize = handImages.size(); // Set up the current hand size
    
    int cardDrawX = 180; // Default X anchor point
    int cardDrawY = 180; // Default Y anchor point
    
    for(int i = 0; i < field.getSize(); i++) // Iterate through the field's runs
    {
      for(int j = 0; j < field.getRun(i).size(); j++) // Iterate through each run's cards
      {
        boardImages.add(new miscImage(cardDrawX + (42*j), 42, cardDrawY, 55, "resources\\cards\\" + field.getRun(i).getCard(j).getID() + ".png")); // Add the image to the boardImages Array
      }
      
      if(field.getSize() - i > 1 && (cardDrawX < 1100 - ((field.getRun(i).size()) * 42) - ((field.getRun(i+1).size()) * 42))) // As long as there is space for the next run horizontally...
      {
        cardDrawX += (42*field.getRun(i).size()) + 20; // Add a spacer in the middle by adjusting the next run's x coordinate
      }
      
      else // If there's no space for the next run horizontally
      {
        cardDrawX = 180; // Reset the X anchor point to the far left again
        cardDrawY += 70; // Move the Y anchor point down a card and a bit
      }
    }
    
    window.add(handImages);  // Add the total handImages arrayList to the GUI
    window.add(boardImages); // Add the total boardImages arrayList to the GUI
    
    window.repaint(); // repaint the GUI
  }
  
  private static void initialize() // Fairly straightforward method to initalize players
  {
    
    for (int i = 0; i < playernum; i++)                  // Iterate through as many players as required
    {
      players.add (new Player ("Player " + (i+1) + "")); // Generate a new player with a randomized deck as per the player object's constructor
    }
    
  }
  
  private static void update() // Method used to back the things up
  {
    deckb = deck.duplicate();                             // The deck is backed up using its duplicate() method
    playerb = players.get(turnNum%playernum).duplicate(); // The player is backed up using its duplicate() method
    runsB = field.getRunList();                           // Field is backed up by storing its current list of runs into a backed up variable
  }
  
  private static void revert() // Method used to revert the backups
  {
    deck = deckb.duplicate();                             // The deck is restored using its duplicate() method
    players.set (turnNum%playernum, playerb).duplicate(); // The player is restored using its duplicate() method
    field.setRunList(runsB);                              // Field is restored by setting its run list to the backed up one
  }
  
  private static boolean userStatus() //checks whether the current user has won or not. if they have won, record their info and remove from the arraylist. 
  {
    if (players.get(turnNum%playernum).allEmpty()) // First ensure that the player's hand is completely empty
    {
      if (field.allValid())                        // Then confirm that all the runs on the field are currently valid
      {
        players.get(turnNum%playernum).hasWon();   // Set the player's property to has won
        return true;                               // Flip the boolean here accordingly
      }
    }
    return false;                                  // Return false otherwise
  }
  
  
  
  
  
  
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~//
  //                                                                            Player Class                                                                        //
  //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~//
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  static class Player 
  {
    
    protected ArrayList<Card>hand; // The player's hand
    protected String name;         // The player's name
    boolean won;                   // Whether the player has won yet
    
    public Player (String plrname) // Constructs a player, taking in just the name as a parameter; hand is randomly generated
    {
      
      this.won = false;             // The player has obviously not won yet
      hand = new ArrayList<Card>(); // Initalize the player's hand
      name = plrname;               // Set up the player's name
      
      for (int i = 0; i < 14; i++) 
      {
        hand.add (deck.deal()); //deals 14 cards from the deck, adding it to the player's hand
      }
      
      sort(); // Sorts the player's hand
    }
    
    public Player(String plrname, ArrayList<Card> inHand) // Allows for more flexibility, allows for a player to be created with a defined hand
    {
      won = false;                  // The player has obviously not won yet
      hand = new ArrayList<Card>(); // Initalize the player's hand
      name = plrname;               // Set up the player's name
      
      for(int i = 0; i < inHand.size(); i++) // Similar to above, but iterates through the hand that was sent in
      {
        hand.add(inHand.get(i)); // Copies that hand's contents into the current player's hand
      }
      
      sort(); // Sorts the player's hand
    }
    
    public Player duplicate() // Duplicates the player
    {
      return new Player(name, hand); // Creates a new instance of a player object with the same properties
    }
    
    public String getName()          { return name;        } // Returns player's name
    public ArrayList<Card> getHand() { return hand;        } // Returns player's hand
    public Card getCard (int i)      { return hand.get(i); } // Returns a card from the player's hand
    public int getSize()             { return hand.size(); } // Returns the player's hand's size
    
    public void addCard(Card c)                     { hand.add (c);                            } // Adds a card object to a player's hand
    public void addCard(int cardSuit, int cardRank) { hand.add (new Card(cardSuit, cardRank)); } // Adds a new object created from a suit and rank to a player's hand
    
    public void deal() { hand.add (deck.deal()); } // Deals the next card from the deck into the player's hand
    
    public void hasWon()    { this.won = true; } // Sets the player to have won
    public boolean isDone() { return this.won; } // Checks to see if the player has won
    
    public boolean allEmpty() // Checks if the player's hand is completely empty
    {
      if (hand.size() == 0)   // If the hand has a size of zero, it's empty
      {
        return true; 
      }
      return false;           // Otherwise, it's not
    }
    
    public Card playCard(int i) // Plays a card from the player's hand
    {
      Card temp = hand.get(i);  // Creates a temporary card based on the index of the player's hand
      hand.remove (i);          // Removes the card at that index
      return temp;              // Returns that card to be played somewhere
    }
    
    public void sort() // Method to sort the player's hand
    {
      Card temp = new Card(); // A temporary card that will allow for swapping
      
      for(int i = 0; i < hand.size() - 1; i++) // Iterate through all but the last term of the player's hand (last term inherently sorted)
      {
        
        int lowPos = i; // Assume the lowposition to be whatever index currently at
        
        for(int j = i+1; j < hand.size(); j++) // Iterate through the rest of the arrayList
        {
          if(hand.get(j).getSuit() < hand.get(lowPos).getSuit()) // Compare suits, if something has a lower suit
          {
            lowPos = j; // Set it to be the lowest position
          }
        }
        
        temp = hand.get(i); // Grab the temporary card
        
        hand.set(i, hand.get(lowPos)); // And do the swapping
        hand.set(lowPos, temp);
        
      }
      
      temp = new Card(); // Reinitalize the temporary card
      
      for(int i = 0; i < hand.size() - 1; i++) // Not recommenting this section; same as above, just we're comparing rank this time through
      {
        
        int lowPos = i;
        
        for(int j = i+1; j < hand.size(); j++)
        {
          if(hand.get(j).getRank() < hand.get(lowPos).getRank())
          {
            lowPos = j; 
          }
        }
        
        temp = hand.get(i);
        
        hand.set(i, hand.get(lowPos));
        hand.set(lowPos, temp);
        
      }
    }
  }
  
  
  
  
  
  
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~//
  //                                                                            Run Class                                                                           //
  //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~//
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  static class Run
  {
    private ArrayList <Card> cards; // An arrayList of cards
    
    public Run(Card[]input) // Constructor takes in an array of cards
    {
      cards = new ArrayList<Card>();                                       // Set up the cards arrayList
      for (int i = 0; i < input.length; i++){ this.cards.add (input[i]); } // Copy all of the Cards from the input array into the arrayList
    }
    
    public void add (Card add) { cards.add (add);     } // Adds a new card to the run
    public Card getCard(int i) { return cards.get(i); } // Returns the card at the index specified
    public int size()          { return cards.size(); } // Returns the size of the arrayList 
    
    public Card rmv(int b) // Remove method; returns the card removed
    {
      Card temp = cards.get(b); // Create temporary card from the index that we're removing from
      cards.remove (b);         // Remove the card at that index
      return temp;              // And then return the temporarily stored variable
    }
    
    public Run duplicate() // Duplicate() method
    {
      Card [] cardsToDupe = new Card[cards.size()]; // Create a new array of cards
      cardsToDupe = cards.toArray(cardsToDupe);     // And copy all the data from the cards arrayList
      return (new Run (cardsToDupe));               // Return a new instance of a run object with the same properties
    }
    
    public boolean isValid()
    {
      order(); //order it first
      
      int[]ranks = new int[cards.size()]; // Create an array to store the cards, assume array is a rankedRun
      int[]suits = new int[cards.size()]; // Create an array to store the cards, assume array is a suitedRun
      
      for (int i = 0; i < this.cards.size(); i++){ ranks[i] = this.cards.get(i).getRank(); suits[i] = this.cards.get(i).getSuit(); } // Copy the values from the cards arrayList over to the Arrays
      
      ranks = new QuickSort(ranks).getarr(); // Invoke quicksort on the ranks Array 
      suits = new QuickSort(suits).getarr(); // Invoke quicksort on the suits Array
      
      boolean consecutiveranks  = true; // Assume at this point that these boolean flags are valid
      boolean allsamesuit       = true; // As above
      boolean allsameranks      = true; // As above
      boolean alldifferentsuits = true; // As above
      
      if(cards.size() < 3){ return false; } // First of all, a run must have 3 more cards
      
      for (int y = 0; y < this.cards.size() - 1; y++){ if (!(ranks[y]+1 == ranks[y+1])) { consecutiveranks = false; } if (!(ranks[y] == ranks[y+1])){ allsameranks = false; } } // Iterates, checks if ranks are consecutive and if ranks are all the same
      for (int k = 0; k < this.cards.size() - 1; k++){ if (!(suits[k]   == suits[k+1])) { allsamesuit      = false; } for (int j = k; j < this.cards.size()-1; j++) { if (suits[k] == suits[j+1]) { alldifferentsuits = false; } } } // Iterates, checks if suits are all same or all different
      
      if (consecutiveranks && allsamesuit)   { return true; } // A Suited Run
      if (allsameranks && alldifferentsuits) { return true; } // A Ranked Run
      
      return false; // If nothing triggers, it's false by default
      
    }
    
    public void order () // Ordering the cards in the correct order for when they are displayed
    {
      
      boolean rankrun = false; // By default, assume it is not a ranked run
      
      boolean hasSpade   = false; // By default, assume there is no spade
      boolean hasHeart   = false; // By default, assume there is no heart
      boolean hasDiamond = false; // By default, assume there is no diamond
      boolean hasClub    = false; // By default, assume there is no club
      
      int numSuits = 0; // A counter for the total number of suits in the run
      
      for(int i = 0; i < cards.size(); i++) // Iterate through all the cards in the hand
      {
        
        if(cards.get(i).getSuit() == 1){ hasSpade   = true;  } // If there is a spade
        if(cards.get(i).getSuit() == 2){ hasHeart   = true;  } // If there is a heart
        if(cards.get(i).getSuit() == 3){ hasDiamond = true;  } // If there is a diamond
        if(cards.get(i).getSuit() == 4){ hasClub    = true;  } // If there is a club
        
      }
      
      if (hasSpade   == true){ numSuits++;   } // If there's at least one spade, increment numSuits
      if (hasHeart   == true){ numSuits++;   } // As above; heart
      if (hasDiamond == true){ numSuits++;   } // As above; diamond
      if (hasClub    == true){ numSuits++;   } // As above; club
      
      if(numSuits >= 3){ rankrun = true; } // If there are three or more suits, assume it to be a ranked run (obviously it could just be an invalid run, but we'll get to that later)
      
      if (rankrun) // If it's a rank run
      {
        
        Card temp = new Card(); // Create a new temporary variable
        
        for(int i = 0; i < cards.size() - 1; i++) // Iterate through the hand (apart from the last card, inherently sorted)
        {
          
          int lowPos = i; // Set up the assumed low position to be the current index
          
          for(int j = i+1; j < cards.size(); j++) // Iterate once again from current position to the last card
          {
            
            if(cards.get(j).getSuit() < cards.get(lowPos).getSuit()) // If the card has a lower rank than the one currently set as the lowest
            {
              lowPos = j; // Set the index accordingly
            }
            
          }
          
          temp = cards.get(i);             // Set up the temporary card to hold the card at the initial index
          cards.set(i, cards.get(lowPos)); // Set the initial index's value to be the lower one
          cards.set(lowPos, temp);         // And set the value of where the lower one was to be the card from the initial index
          
        }
      }
      
      else // This section is the exact same selectionSort algorithm as used above, using rank ID's instead of suit ID's this time, comments can be referred to from there
      {
        
        Card temp = new Card();
        
        for(int i = 0; i < cards.size() - 1; i++)
        {
          
          int lowPos = i;
          
          for(int j = i+1; j < cards.size(); j++)
          {
            
            if(cards.get(j).getRank() < cards.get(lowPos).getRank())
            {
              lowPos = j; 
            }
            
          }
          
          temp = cards.get(i);
          cards.set(i, cards.get(lowPos));
          cards.set(lowPos, temp);
          
        }
        
      }
    }
  }
  
  
  
  
  
  
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~//
  //                                                                       PlayField Class                                                                          //
  //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~//
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  static class PlayField
  {
    
    private ArrayList <Run> runs; // A list of runs, containing all the runs on the boad
    
    public PlayField(){ runs = new ArrayList<Run>(); } //Initalizes the arrayList
    
    public ArrayList<Run> getRunList() // Creates a copy of the runs from the PlayField (used in backing them up)
    {
      
      ArrayList<Run> res = new ArrayList<Run>(); // Creates the new ArrayList of runs that will be returned
      
      for(int i = 0; i < runs.size(); i++) // loop through all the elements in the ArrayList of runs that are in this current Playfield object
      {
        res.add(runs.get(i)); // And adds each of them to the new ArrayList of runs that will be returned
      }
      
      return res; // Return the new copied ArrayList
      
    }
    
    public void setRunList(ArrayList<Run> someList) // The partner method to getRunList; this one takes in an ArrayList of runs as a parameter and copies them into the current PlayField object
    {
      
      runs = new ArrayList<Run>(); // Reinitalize runs just to ensure that there isn't any conflicting information
      
      for(int i = 0; i < someList.size(); i++) // Iterate through the ArrayList of runs that was passed in
      {
        runs.add(someList.get(i)); // And add each element to the current PlayField
      }
      
    }
    
    public int getSize ()       { return runs.size(); } // Returns the ArrayList.size() for the ArrayList of runs
    public Run getRun  (int i)  { return runs.get(i); } // Returns the run at position i
    
    public void addRun (Run add){ this.runs.add (add); } // Takes in a Run object and adds it to the ArrayList of runs
    
    public void addToRun (int runIndex, Card toAdd){ runs.get(runIndex).add(toAdd); } // Takes in runIndex and a card, adds the card to the run specified by the index
    
    public Card deleteFromRun (int runIndex, int cardIndex) // Takes in runIndex and cardIndex; removes cardIndex of the run at runIndex 
    {
      Card temp = runs.get(runIndex).rmv(cardIndex); 
      return temp; // Returns the card object
    }
    
    public void orderAll() // Orders all of the runs in the PlayField
    {
      //System.out.println("orderAll invoked");
      if (this.runs.size() != 0) // As long as the size of the PlayField is not zero
      {
        for (int i = 0; i < runs.size(); i++)
        {
          runs.get(i).order(); // Simply invoke the Run.order() method 
        }
      }
    }
    
    public boolean allValid()
    {
      boolean res = true; // Set up a boolean as the result; assume it to be true until proven otherwise
      
      if (this.runs.size() != 0) // As long as the size isn't zero
      {
        for (int i = 0; i < this.runs.size(); i++) // Iterate through the ArrayList of runs
        {
          if (!this.runs.get(i).isValid()) // Check each of them in turn for validity using the Run.isValid() method
          {
            res = false; // If any is false; the result is false
          }
        }
      }
      return res; // Return the result
    }
    
    public void trimRuns()
    {
      int counter = 0; // Set up a counter as we can't use a for loop in this case due to ArrayList's dynamic resizing
      
      while(counter < runs.size()) // While the counter isn't at the maximum index
      {
        if(runs.get(counter).size() == 0){ runs.remove(counter); } // if a run's size is zero, remove the run but do not increment counter
        else{ counter++;} // otherwise, push the counter up one
      }
    }
    
  }
  
  
  
  
  
  
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~//
  //                                                                           Deck Class                                                                           //
  //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~//
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  static class Deck // Not highly involved class; doesn't play a prominent role in RummiKub
  {
    
    private ArrayList <Card> cards; // Deck is essentially just an arrayList of cards 
    
    public Deck () // Constructor generates a random deck
    {
      cards = new ArrayList <Card>();         // Initalizes the card ArrayList
      
      for (int h = 0; h < 2; h++)             // In Rummikub, two full decks are used
      {
        for (int i = 1; i < 5; i++)           // Loops through the five suits
        {
          for (int j = 1; j < 14; j++)        // Loops through the thirteen ranks
          {
            this.cards.add (new Card (i, j)); // Adds the designated card
          }
        }
      }
      
      shuffle(); // shuffle method invoked to randomize the deck
      
    }
    
    public Deck(ArrayList<Card> inCards) // Alternative constructor (only used for duplication)
    {
      
      cards = new ArrayList<Card>(); // Resets the cards ArrayList
      
      for(int i = 0; i < inCards.size(); i++) // Loops through the input
      {
        cards.add(inCards.get(i)); // And copies each value over
      }
      
    }
    
    public void shuffle () // method to shuffle the cards
    {
      
      ArrayList<Card> tempArrayList = new ArrayList (cards.size()); // sets up a temporary arrayList to store the shuffled deck
      
      for(int i = 0; i < cards.size(); i++) // looping through the elements in the arrayList
      {
        
        int ranIndex; // the randomly generated index
        
        do // using a doWhile loop because this must be done at least once regardless of circumstances
        {
          
          ranIndex = (int)(Math.random()*cards.size()); // Generate a random index
          
        } while(tempArrayList.contains(cards.get(ranIndex))); // Ensures that the temporary array list does not already contain the element that was located at this index; i.e. prevents duplicates
        
        tempArrayList.add(cards.get(ranIndex)); // Adds the element to the temporary arraylist
        
      }
      
      cards = new ArrayList(tempArrayList.size()); // reinitalize the deck arraylist
      
      for(int i = 0; i < tempArrayList.size(); i++) // loop through the temporary arraylist
      {
        cards.add(tempArrayList.get(i)); // and copy all of the items back into the original deck
      }
      
    }
    
    public Card deal()                        // Deals the top card out of the deck
    {
      Card temp =  cards.get(cards.size()-1); // Sets up a temporary card for the return
      cards.remove (cards.size()-1);          // Removes that value from the deck
      return temp;                            // Temporary card is returned
    }
    
    public Deck duplicate()   // Returns a duplicated object for the hand
    {
      return new Deck(cards); // Does not simply copy pointers, but creates a new instance using the second constructor above
    }
    
  }
  
  
  
  
  
  
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~//
  //                                                                        QuickSort Class                                                                         //
  //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~//
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  static class QuickSort // Kai's note: Evan decided to make this a class that could be invoked from anywhere in the code, I suppose it works..?
  {
    
    private int[] x; // An array that will be used for the sorting
    
    public QuickSort (int[]input) // Constructor takes in an array as the input
    {
      x = input;                  // Does not duplicate; irrelevant at this point because we're sorting that array anyway
      int low = 0;                // Low value set to 0
      int high = x.length-1;      // High value set to final index
       
      quickSort (x, low, high);   // Recursively calls QuickSort.quickSort() (Kai's note: Evan's naming conventions, not mine.)
    }
    
    public static void quickSort(int[] arr, int low, int high) // Recursive component 
    {
      
      if (arr == null || arr.length == 0) // If the array is nonexistent or has a length of zero
        return;                           // Deem it to be sorted and return it
      
      if (low >= high)                    // If the two counters have passed each other, we're done
        return;                           // Deem it to be sorted and return it
      
      // pick the pivot
      int middle = low + (high - low) / 2;
      int pivot = arr[middle];
      
      // make left < pivot and right > pivot
      int i = low, j = high;
      
      while (i <= j)
      {
        
        while (arr[i] < pivot) // If the value at the left side counter is less than the pivot 
        {
          i++;                 // Increment the left side counter
        }
        
        while (arr[j] > pivot) // If the value at the right side counter is greater than the pivot
        {
          j--;                 // Decrement (for all intents and purposes, it's moving in a direction we want it go, so I really want to say increment) the right side counter
        }
        
        if (i <= j)            // As long as these conditions still hold
        {
          int temp = arr[i];   // The value at index i is thrown into the temporary value
          arr[i] = arr[j];     // Set the value at index i to be the one from index j
          arr[j] = temp;       // And do the same in the other way
          i++;                 // Increment i
          j--;                 // Decrement (Increment, technically) j
        }
      }
      
      // recursively sort two sub parts
      
      if (low < j)
        quickSort(arr, low, j);
      
      if (high > i)
        quickSort(arr, i, high);
      
    }
    
    public int[] getarr() // Simply returns the sort array
    {
      return this.x;      // Returns the array
    }
    
  }
  
  
  
  
  
  
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~//
  //                                                                           Card Class                                                                           //
  //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~//
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  static class Card // This is the actual card class that is used for this program; (Kai's note: I still don't understand why Evan wants Game3 to extend Card, but that's how it currently is. I might look into it further in due time.)
  {
    
    private int suit;   // Card has a suit
    private int rank;   // Card has a rank
    private int cardID; // Card has an ID (derived from the suit and rank, used for drawing images)
    
    public Card() // Default constructor
    {
      suit = 1; // Suit defaults to 1
      rank = 1; // Rank defaults to 1
    }
    
    public Card (int a, int b) // Useful constructor with actual parameters
    {
      this.suit = a;               // Sets the suit to the desired one
      this.rank = b;               // Sets the rank to the desired one
      cardID = (suit-1)*13 + rank; // Generates the card's ID (copied right over from my old Card ArrayList program)
    }
    
    public int getID()   { return cardID;    } // Getter method for ID; returns ID
    public int getRank() { return this.rank; } // Getter method for Rank; returns rank
    public int getSuit() { return this.suit; } // Getter method for Suit; returns suit
  }
  
  
  
  
  
  
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~//
  //                                                             A Big Clump of JFrame-Derived Classes                                                              //
  //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~//
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  static class tutorial extends JFrame // Tutorial class, responsible for the opening tutorial
  {
    
    protected drawArea tutArea = new drawArea(600,600);                                         // Tutorial drawArea is 600px by 600px
    protected miscImage tutImage = new miscImage(0, 600, 0, 600, "resources//tutorial//1.png"); // Initial image is labelled 1.png, and they continue sequentially
    
    protected int counter = 1; // The counter for the images is 1 (since the inital image is 1.png) and this is later incremented
    
    public tutorial()
    { 
      
      msListen mouse = new msListen();                 // Add a mouseListener (we really only need mouseClicked, but whatever)
      
      tutArea.setLayout(null);                         // Don't give it a layout since it's all based on images and absolute coordinates anyway
      tutArea.addMouseListener(mouse);                 // Add the mouseListener
      
      tutArea.add(tutImage);                           // Only component here is the tutorial image
      
      setContentPane(tutArea);                         // set the content pane to be whatever content pane contains all the others
      pack ();                                         // this is apparently required
      setTitle ("Rummi-Kyubey Tutorial");              // set the title of the window
      setSize (600,600);                               // set the size of the window (in pixels)
      setDefaultCloseOperation (JFrame.EXIT_ON_CLOSE); // set the close operation (just use EXIT_ON_CLOSE, we're not one of those bastards who minimizes windows when the user hits close)
      setLocationRelativeTo (null);                    // Center window.
      
    }
    
    class msListen implements MouseListener // The Mouse Listener
    {
      
      // These ones are only here because I must extend all abstract methods in the MouseListener interface
      
      public void mouseEntered(MouseEvent e){}
      public void mouseExited(MouseEvent e){}
      public void mouseReleased(MouseEvent e){}
      public void mousePressed(MouseEvent e){}
      
      public void mouseClicked(MouseEvent e) // Watches if the user has clicked
      {
        
        if(counter != 20) // As long as we're not at the last image in the counter (Kai: It's funny how OCD I am with these things. The tutorial was originally 19 images, but I had to make it a perfect number, so I split stuff)
        {
          counter++;                                                   // Increment the counter
          tutImage.setImg("resources//tutorial//" + counter + ".png"); // And set up the next image in the line
          repaint();                                                   // Draw the frame
        }
        
        else // If the counter has hit the last image
        {
          setVisible(false);          // We remove this window
          setupDisp.setVisible(true); // Kick in the setup window
          dispose();                  // And because we're nice, we clean up after ourselves
        }
        
      }
    }
  }
  
  static class turn extends JFrame // Turn class, responsible for preceeding turns
  {
    
    protected drawArea turnArea = new drawArea(1280, 800);                                     // Size of 1280px * 800px for that synergy with the gameboard
    protected miscImage turnImage = new miscImage(0, 1280, 0, 800, "resources//turn//p1.png"); // Set it by default to show Player 1's turn
    
    public turn(int player) // Constructor relies on the player that needs to click through
    {
      
      msListen mouse = new msListen(); // Add a mouseListener (we really only need MouseClicked)
      
      turnArea.setLayout(null);         // Refer to my comment from the tutorial class
      turnArea.addMouseListener(mouse); // As above
      
      turnImage.setImg("resources//turn//p" + (player+1) + ".png"); // Since the player ID begins on zero, we must increment one to load up the proper image here
      
      turnArea.add(turnImage);                             // Add the image to the board
      
      setContentPane(turnArea);                            // set the content pane to be whatever content pane contains all the others
      pack ();                                             // this is apparently required
      setTitle ("Player " + (player + 1) + "'s turn~!");   // set the title of the window
      setSize (1280,800);                                  // set the size of the window (in pixels)
      setDefaultCloseOperation (JFrame.EXIT_ON_CLOSE);     // set the close operation (just use EXIT_ON_CLOSE, we're not one of those shitty developers who minimizes windows when the user hits close)
      setLocationRelativeTo (null);                        // Center window.
      
    }
    
    class msListen implements MouseListener // Here we go again
    {
      
      // I'm not writing this comment again
      
      public void mouseEntered(MouseEvent e){}
      public void mouseExited(MouseEvent e){}
      public void mouseReleased(MouseEvent e){}
      public void mousePressed(MouseEvent e){}
      
      public void mouseClicked(MouseEvent e) // If the mouse is clicked
      {
        setVisible(false);       // We remove this window
        window.setVisible(true); // And allow the gameboard to be displayed
        dispose();               // And again, since we're polite people, we clean up after ourselves
        
      }
      
    }
  }
  
  static class setup extends JFrame // The setup method that asks how many players are playing
  {
    
    protected drawArea setupArea = new drawArea(600, 600);                                          // Size of 600px * 600px for that synergy with the tutorial 
    protected miscImage setupImage = new miscImage(0, 600, 0, 600, "resources//players//base.png"); // Set the image to be the default image (no choices highlighted)
    
    public setup() // Constructor without parameters, just creates the GUI
    {
      
      msListen mouse = new msListen();                    // As commented in the above classes
      
      setupArea.setLayout(null);                          // As above
      setupArea.addMouseListener(mouse);                  // As above
      setupArea.addMouseMotionListener(mouse);            // We need this now because we have hover effects
      
      setupArea.add(setupImage);                          // Add the image to the board
      
      setContentPane(setupArea);                          // set the content pane to be whatever content pane contains all the others
      pack ();                                            // this is apparently required
      setTitle ("Game Setup~!");                          // set the title of the window
      setSize (600,600);                                  // set the size of the window (in pixels)
      setDefaultCloseOperation (JFrame.EXIT_ON_CLOSE);    // set the close operation (just use EXIT_ON_CLOSE, we're not one of those annoying devs who minimizes windows when the user hits close)
      setLocationRelativeTo (null);                       // Center window.
     
    }
    
    class msListen implements MouseListener, MouseMotionListener // This one now relies on both MouseListener and MouseMotionListener
    {
      
      // Here's all those abstract methods we must extend
      
      public void mouseEntered(MouseEvent e){}
      public void mouseExited(MouseEvent e){}
      public void mouseReleased(MouseEvent e){}
      public void mousePressed(MouseEvent e){}
      public void mouseDragged(MouseEvent e){}
      
      public void mouseClicked(MouseEvent e) // If the mouse is clicked
      {
        
        if(e.getX() >= 50 && e.getX() <= 550 && e.getY() >= 45 && e.getY() <= 200) // Check if it's clicked on the 2 players option
        {
          setupImage.setImg("resources//players//clicked2.png"); // Set the image accordingly
          repaint();                                             // Redraw the GUI for responsive feedback
          playernum = 2;                                         // And set playerNum properly
        }
        
        else if(e.getX() >= 50 && e.getX() <= 550 && e.getY() >= 225 && e.getY() <= 375) // As above
        {
          setupImage.setImg("resources//players//clicked3.png");
          repaint();
          playernum = 3;
        }
        else if(e.getX() >= 50 && e.getX() <= 550 && e.getY() >= 405 && e.getY() <= 555) // As above
        {
          setupImage.setImg("resources//players//clicked4.png");
          repaint();
          playernum = 4;
        }
        
        setVisible(false); // This class is now done, close it
        tutDone = true;    // Flip this boolean flag to true so main knows it can proceed 
        dispose();         // Clean up
      }
      
      public void mouseMoved(MouseEvent e) // Same concept as mouseClicked above
      {
        
        if(e.getX() >= 50 && e.getX() <= 550 && e.getY() >= 45 && e.getY() <= 200) // If the mouse is within the region of the 2 players button
        {
          setupImage.setImg("resources//players//selected2.png"); // Set the image accordingly
          repaint();                                              // And repaint
        }
        
        else if(e.getX() >= 50 && e.getX() <= 550 && e.getY() >= 225 && e.getY() <= 375) // As above
        {
          setupImage.setImg("resources//players//selected3.png");
          repaint();
        }
        
        else if(e.getX() >= 50 && e.getX() <= 550 && e.getY() >= 405 && e.getY() <= 555) // As above
        {
          setupImage.setImg("resources//players//selected4.png");
          repaint();
        }
        
      }
    }
  }
  
  static class gameBoard extends JFrame // This class alone probably took me longer than all the other ones combined
  {
    
    protected drawArea main; // Set up the drawArea public to subclasses so the listeners can use it
    
    public gameBoard ()      // Constructor without parameters; all controlled by listeners and main program
    {
      
      main = new drawArea(1280, 800); // The resolution is 1280px * 800px because who cares about standard resolutions anyway
      
      // Create / initialize components such as buttons 
      
      // Begin by first initalizing the listeners for all the components required
      
      msListen mouse = new msListen();    // Create the msListen object
      
      main.setLayout(null);               // We don't have any layout required since we're using absolute coordinates
      main.addMouseListener(mouse);       // Add mouse in terms of a MouseListener
      main.addMouseMotionListener(mouse); // And also add mouse in terms of a MouseMotionListener
      
      main.add(bg);                       // Add the background image
      main.add(playButton);               // Add the playButton image
      main.add(sortButton);               // Add the sortButton image
      main.add(handArea);                 // Add the handArea image
      main.add(boardbg);                  // Add the board background image
      
      drawArea cardArea = new drawArea(960, 480); // Create the drawArea of size 960px * 480px (Kai's note: This was actually decided through several different mockups in PhotoShop after which I chose the most visually appealing one to me)
      
      cardArea.add(boardbg);              // This is actually left over from legacy code; there's no reason why I can't draw this onto main, but it's working fine
      
      main.add(cardArea);                 // Add the cardArea onto main
      
      
      // Set the Window attributes 
      
      setContentPane(main);                                // set the content pane to be whatever content pane contains all the others
      pack ();                                             // this is apparently required
      setTitle ("Rummi-Kyubey");                           // set the title of the window
      setSize (1280, 800);                                 // set the size of the window (in pixels)
      setDefaultCloseOperation (JFrame.EXIT_ON_CLOSE);     // set the close operation (just use EXIT_ON_CLOSE, we're not one of those irritating programmers who minimizes windows when the user hits close)
      setLocationRelativeTo (null);                        // Center window.
      
      
    }
    
    public void add(miscImage anImage) // Since only the gameBoard class is public (drawArea is a subclass of it), the rest of the program cannot directly access drawArea
    {
      main.add(anImage); // Thus, use this method to pass an image to add
    }
    
    public void add(ArrayList<miscImage> anImageList) // If there are a lot of images?
    {
      main.add(anImageList); // Pass an arrayList and the drawArea will deal with it
    }
    
    public void revert() // Reverts the GUI to its default blank state
    {
      main.revert();        // Again, simply invoke it in the drawArea
      
      main.add(bg);         // Add the background again
      main.add(handArea);   // Add the handArea again
      main.add(boardbg);    // Add the boardBackground (Kai's note: I love my consistency; this is thrown onto cardArea initially and look where it is now)
      main.add(playButton); // Add the PlayButton again
      main.add(sortButton); // Add the SortButton again
    }
    
    class msListen implements MouseListener, MouseMotionListener // Welcome to hell
    {
      
      // These two are used throughout this class to allow for interaction between methods
      private int pressedX = 0; // Where the mouse was pressed (X)
      private int pressedY = 0; // Where the mouse was pressed (Y)
      
      // These are abstract methods that I am forced into extending
      public void mouseEntered(MouseEvent e){}
      public void mouseExited(MouseEvent e){}
      public void mouseDragged(MouseEvent e){}
      
      public void mousePressed(MouseEvent e) // If the mouse is pressed...
      {
        
        pressedX = e.getX(); // Set pressedX
        pressedY = e.getY(); // Set presesdY
        
        if(!boardbg.checkBounds(pressedX, pressedY))    // Check to see if the click was within the boardBackground image (i.e. the playField) If it's not, then continue
        {
          if(!handArea.checkBounds(pressedX, pressedY)) // Check to see if the click was within the handArea image, if it's not...
          {
            cardDraggedFrom = -1; // Set this ID to -1, meaning that the user's click was not an interaction with a card
          }
        }
        
      }
      
      public void mouseReleased(MouseEvent e) // If the mouse is released (mainly just used with dropping cards onto the board)
      {
        
        releaseOnRun = false; // By default, we assume the card was not released atop a run
        
        int boardCounter = 0; // Allows me to simply go up the boardImages list
        
        for(int i = 0; i < field.getSize(); i++) // loop through every run in field
        {
          
          for(int j = 0; j < field.getRun(i).size(); j++) // Loop through every card in each run
          {
            
            if(boardImages.get(boardCounter).checkBounds(e.getX(), e.getY())) // If the released location was on top of a run
            {
              whichRunTo = i;      // Record which run it was dropped atop
              releaseOnRun = true; // And flip this flag to true
            }
            
            boardCounter++; // Increment the counter
            
          }
          
        }
        
        if(releaseOnRun)         // If the card was released on a run
        {
          releaseOnField = true; // If must therefore have been released on the field
        }
        
        else if(!boardbg.checkBounds(e.getX(), e.getY())) // If the card was released out of bounds of the field
        {
          releaseOnField = false;                         // Set releaseOnField to be false; card doesn't go anywhere
        }
        
        else                     // Otherwise...
        {
          releaseOnField = true; // We know the card must've been dropped on the field but not atop a run
        }
        
        madeMove = true;         // The player has now made a move, flip this flag over
        haveToRedrawHand = true; // We know that therefore the hand must be redrawn, flip this flag over
        
      }
      
      public void mouseMoved(MouseEvent e) // This was a lot of work...
      {
        
        // For the current hand
         
        int handCounter = 0; // Use a counter to track the iteration
        
        while(handCounter < currentHandSize) // As long as the counter hasn't hit the end yet
        {
          
          if(handImages.get(handCounter).checkBounds(e.getX(), e.getY())) // If we are hovering this card at the moment
          {
            whichCard = handCounter;                                      // Note down that we dragged this card (we technically have not yet, but this is always going to occur before a card is dragged)
            cardDraggedFrom = 0;                                          // Mark down that the card was dragged from the user's hand
            
            handImages.get(handCounter).setImg("resources\\cards\\" + players.get(onWhoseTurn).getCard(handCounter).getID() + "selected.png"); // Set the card's image to be the selected version
          }
          
          else // If the mouse is not currently hovering over this card
          {
            handImages.get(handCounter).setImg("resources\\cards\\" + players.get(onWhoseTurn).getCard(handCounter).getID() + ".png"); // Just for security, set the card's image to be the nonselected version
          }
        
          handCounter++; // Increment our counter
          
        }
        
        // For the playField
        
        int boardCounter = 0; // Allows me to simply go up the boardImages list
        
        for(int i = 0; i < field.getSize(); i++) // loop through every run in field
        {
          
          for(int j = 0; j < field.getRun(i).size(); j++) // Loop through every card in each run
          {
            
            if(boardImages.get(boardCounter).checkBounds(e.getX(), e.getY())) // If we are hovering over this card
            {
              whichCard = j;       // Note down that we dragged this card
              whichRunFrom = i;    // Note down that we dragged this card from this run
              cardDraggedFrom = 1; // note down that in this case, it's over one of the field's cards, not a hand's card
              boardImages.get(boardCounter).setImg("resources\\cards\\" + field.getRun(i).getCard(j).getID() + "selected.png"); // If the mouse is currently hovered over that card, we can set it to the selected image
            }
            
            else
            {
              boardImages.get(boardCounter).setImg("resources\\cards\\" + field.getRun(i).getCard(j).getID() + ".png"); // If the mouse is not currently hovering over the card, set it to the unselected image
            }
                        
            boardCounter ++; // Increment counter
            
          }
          
        }
        
        // For the play button
        
        if(playButton.checkBounds(e.getX(), e.getY())) // If the mouse is hovering over the play Button
        {
          if(!isDrawButton){ playButton.setImg("resources\\playbuttonselected.png"); } // If it's currently being used as the play Button, then set it to be the selected playButton
          else{ playButton.setImg("resources\\drawbuttonselected.png"); }              // If it's currently being used as the draw Button, then set it to be the selected drawButton
        }
        
        else // Otherwise
        {
          if(!isDrawButton){ if(!playButton.getImg().equals("resources\\playbutton.png")){ playButton.setImg("resources\\playbutton.png"); } } // If it's currently being used as the playButton, set it to be the unselected playButton
          else{ if(!playButton.getImg().equals("resources\\drawbutton.png")){ playButton.setImg("resources\\drawbutton.png"); } }              // If it's currently being used as the drawButton, set it to be the unselected drawButton
        }
        
        // For the sort button
        
        if(sortButton.checkBounds(e.getX(), e.getY()))              // Check the boundaries
        { sortButton.setImg("resources\\sortbuttonselected.png"); } // If it's within the range, set it to be the selected drawButton image
        else
        { if(!sortButton.getImg().equals("resources\\sortbutton.png")){ sortButton.setImg("resources\\sortbutton.png"); } } // Otherwise, set it to be the unselected drawButton
        
        //////////////////////////////////////////////////////////////////////////////////////
        // Controls Card Movement (Kai's notes: This is my masterpiece and I am quite proud)//
        //////////////////////////////////////////////////////////////////////////////////////
        
        for(int i = 0; i < handImages.size(); i++) // Iterate through the handImages
        {
          
          if(haveToRedrawHand) // If the hand has to be redrawn
          {
            // These two are reset to prevent the next stage from executing, as it checks the boundaries based on pressedX and pressedY
            pressedX = -1;     // Reset pressedX
            pressedY = -1;     // Reset pressedY
            
            haveToRedrawHand = !haveToRedrawHand; // We've confirmed that the hand will be redrawn properly, this can be flipped back
          }
          
          else // If there wasn't a move made from the hand
          {
            
            if(handImages.get(i).checkBounds(pressedX, pressedY)) // And if the pressed coordinates are atop one of the cards in the hand
            {
              handImages.get(i).setX(e.getX()); // Set the X coordinate based on movement
              handImages.get(i).setY(e.getY()); // Set the Y coordinate based on movement
            }
            
          }
        }
        
        boardCounter = 0; // A counter used for the board to allow us to iterate through two loops with one counter to work for the boardImages
        
        for(int i = 0; i < field.getSize(); i++)          // Looping through all the runs in the field
        {
          
          for(int j = 0; j < field.getRun(i).size(); j++) // Looping through all the cards in the run
          {
            
            if(boardImages.get(boardCounter).checkBounds(pressedX, pressedY)) // If the pressed coordinates are atop one of the hands in the field
            {
              boardImages.get(boardCounter).setX(e.getX()); // Set the X Coordinate based on movement
              boardImages.get(boardCounter).setY(e.getY()); // Set the Y Coordinate based on movement
            }
            
            boardCounter++; // Increment the counter
            
          }
          
        }
        
        repaint(); // When all is said and done, repaint the GUI
        
      }
      
      public void mouseClicked(MouseEvent e)
      {
        
        // For the Play Button
        
        if(playButton.checkBounds(e.getX(), e.getY())) // If the mouse is clicked atop the playButton
        { 
          if(!isDrawButton){ playButton.setImg("resources\\playbuttonclicked.png"); } // If it's the playButton, set the clicked image for playButton
          else { playButton.setImg("resources\\drawbuttonclicked.png"); }             // If it's the drawButton, set the clicked image for sortButton
          
          clickPlayButton = true; // Flip this boolean flag
          
        }
        
        else // Otherwise, if it's not clicked atop it
        { 
          if(!isDrawButton){ if(!playButton.getImg().equals("resources\\playbutton.png")){ playButton.setImg("resources\\playbutton.png"); } } // If it's playButton, set the unclicked image for playButton
          else{ if(!playButton.getImg().equals("resources\\drawbutton.png")){ playButton.setImg("resources\\drawbutton.png"); } }              // If it's drawButton, set the unclicked image for sortButton
        }
        
        // For the sort button
        
        if(sortButton.checkBounds(e.getX(), e.getY()))                                     // If clicked atop the SortButton
        { sortButton.setImg("resources\\sortbuttonclicked.png"); clickSortButton = true; } // Set the clicked image and flip the boolean flag
        else
        { if(!sortButton.getImg().equals("resources\\sortbutton.png")){ sortButton.setImg("resources\\sortbutton.png"); } } // Otherwise, just set it as the unclicked image
        
        repaint(); // Redraw the GUI
        
      }
      
    }
  }
  
  static class miscImage // Very modular class used to manipulate images (Kai's Note: I made this as flexible as possible and it certainly made this program a lot easier to work with than it could've been)
  {
    
    protected BufferedImage img; // The image itself
    protected int x;             // The X Coordinate
    protected int xSize;         // The X-Size (length) of the image
    protected int y;             // The Y Coordinate
    protected int ySize;         // The Y-Size (height) of the image
    protected String imgPath;    // This variable is really only used to be returned; constructor and setImg() all use the parameters passed in anyway
    
    public miscImage(int xIn, int xSizeIn, int yIn, int ySizeIn, String path) // Very convoluted constructor
    {
      
      try // Attempts to read the image from the given path
      {
        img = ImageIO.read(new File(path)); 
      }
      
      catch(IOException ex) // This will occur if the resources folder is not found
      {
      }
      
      x = xIn;         // Set up the X coordinate
      xSize = xSizeIn; // Set up the X - Size
      y = yIn;         // Set up the Y coordinate
      ySize = ySizeIn; // Set up the Y - Size
      imgPath = path;  // Store the path
      
    }
    
    public int getX(){ return x; } // Getter; returns x
    public int getY(){ return y; } // Getter; returns y
    
    public int getXSize(){ return xSize; } // Getter; returns XSize
    public int getYSize(){ return ySize; } // Getter; returns YSize
    
    public String getImg(){ return imgPath; } // Getter; returns image Path
    
    public void setX(int newX){ x = newX; } // Setter, sets X
    public void setY(int newY){ y = newY; } // Setter, sets Y
    
    public void draw(Graphics g){ g.drawImage(img, x, y, null); } // Draw Method, core of image display, simply uses data fields in class to execute
    
    public boolean checkBounds(int xChk, int yChk) // Checks if a point is within the image
    {
      boolean res = false; // At this point, we assume it to be false
      
      if(xChk <= (x + xSize) && xChk >= x && yChk <= (y + ySize) && yChk >= y) // However, if it's within all of the bounds
      {
        res = true; // Flip the flag over to true
      }
      
      return res; // Return this boolean result
    }
    
    public void setImg(String path) // Probably the most important method, allows the image to be replaced
    {
      
      try
      {
        img = ImageIO.read(new File(path)); // Loads a new image based off the new path that is entered
        imgPath = path;                     // Stores the path
      }
       
      catch(IOException ex) // This will be thrown if the resources folder is absent
      {
      }
      
    }
  }
  
  static class drawArea extends JPanel // This was in Mr. Jay's demo for the ArrayList and Cards program, I just manipulated it to serve my needs
  {
    
    protected ArrayList<miscImage> images = new ArrayList<miscImage>(); // Contains all the images that will be drawn onto it 
    
    public drawArea(int width, int height){ this.setPreferredSize(new Dimension(width, height)); } // Constructor simply creates the area with dimensions
    
    public void add(miscImage img){ images.add(img); }                                                                        // Simply adds the image that is passed in
    
    public void add(ArrayList<miscImage> imgList) { for(int i = 0; i < imgList.size(); i++) { images.add(imgList.get(i)); } } // Iterates through list passed in, adds every image
    
    public void paintComponent(Graphics g){ for(int i = 0; i < images.size(); i++){ images.get(i).draw(g); } } // In essence, is only here in name, it invokes each image's simple draw() method
    
    public void revert(){ images = new ArrayList<miscImage>(); } // Revert just defaults it and clears everything off
    
  }
}

