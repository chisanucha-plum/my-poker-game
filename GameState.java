import java.util.*;

public class GameState {
    public ArrayList<Card> dealerHand = new ArrayList<>();
    public ArrayList<Card> playerHand = new ArrayList<>();
    public ArrayList<Card> communityCards = new ArrayList<>();
    public int revealedCommunity = 0;
    public int playerSum = 0;
    public int dealerSum = 0;
    public int playerAceCount = 0;
    public int dealerAceCount = 0;
    public int playerCoin = 10000;
    public int dealerCoin = 10000;
    public int currentBet = 0;
    public boolean canRaise = true;
}