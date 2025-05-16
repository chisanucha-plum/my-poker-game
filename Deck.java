import java.util.*;

public class Deck {
    private ArrayList<Card> cards;
    private Random random = new Random();

    public Deck() {
        buildDeck();
        shuffle();
    }

    public void buildDeck() {
        cards = new ArrayList<>();
        String[] values = {"A", "2", "3", "4", "5", "6", "7", "8", "9", "10", "J", "Q", "K"};
        String[] types = {"C", "D", "H", "S"};
        for (String type : types) {
            for (String value : values) {
                cards.add(new Card(value, type));
            }
        }
    }

    public void shuffle() {
        Collections.shuffle(cards, random);
    }

    public Card draw() {
        return cards.remove(cards.size() - 1);
    }
}