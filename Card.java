class Card // This has to exist since Game3 extends it, thus it cannot be in the same file... (We're bad, sorry)
{
    private int rank; // Card has a rank
    private int suit; // Card has a suit (colour, in this case, represented by numbers 1 to 4)
    
    public Card ()     // Default constructor as a failsafe
    {
        this.suit = 1; // Sets suit to 1
        this.rank = 1; // Sets rank to 1
    }

    public Card (int inputr, int inputs) // Constructor that takes in rank and suit
    {
        this.rank = inputr; // Sets rank
        this.suit = inputs; // Sets suit
    }

    public int getrank()  // Getter method for rank
    {
        return this.rank; // Returns rank
    }

    public int getsuit()  // Getter method for suit
    {
        return this.suit; // Returns suit
    }
    
}
