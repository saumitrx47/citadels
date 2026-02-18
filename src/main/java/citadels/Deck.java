package citadels;

import java.util.*;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

/**
 * The {@code Deck} class represents a deck of cards used in the Citadels game.
 * It manages the draw pile, discard pile, and card definitions,
 * and provides functionality for drawing, shuffling, serializing, and deserializing the deck.
 */
public class Deck {

    private final List<Card> deck = new ArrayList<>();
    private final Map<String, Card> cardDefinitions;
    private final Random random = new Random();
    private List<Card> discardedPile = new ArrayList<>();

    /**
     * Constructs a new {@code Deck} using the provided card definitions.
     *
     * @param cD a map of card names to their corresponding {@code Card} objects
     */
    public Deck(Map<String, Card> cD) {
        this.cardDefinitions = cD;
        buildDeck();
    }

    /**
     * Returns the list of discarded cards.
     *
     * @return the discard pile
     */
    public List<Card> getDiscardedPile() {
        return discardedPile;
    }

    /**
     * Retrieves a card from the discard pile by name.
     *
     * @param s the name of the card to search for
     * @return the matching {@code Card} object, or {@code null} if not found
     */
    public Card getFromDiscardedPile(String s) {
        for (Card c : discardedPile) {
            if (c.getName().equalsIgnoreCase(s)) {
                return c;
            }
        }
        return null;
    }

    /**
     * Adds a card to the discard pile.
     *
     * @param c the {@code Card} to discard
     */
    public void addToDiscardedPile(Card c) {
        discardedPile.add(c);
    }

    /**
     * Checks if a card is present in the discard pile.
     *
     * @param c the {@code Card} to check
     * @return {@code true} if the card is in the discard pile, {@code false} otherwise
     */
    public boolean inDiscardedPile(Card c) {
        return discardedPile.contains(c);
    }

    /**
     * Builds the initial deck based on the provided card definitions.
     */
    private void buildDeck() {
        for (Card c : cardDefinitions.values()) {
            for (int i = 0; i < c.getQuantity(); i++) {
                deck.add(new Card(c.getName(), c.getColor(), c.getAbility(), 1, c.getCost()));
            }
        }
    }

    /**
     * Draws a specified number of cards from the top of the deck.
     *
     * @param count the number of cards to draw
     * @return a list of drawn {@code Card} objects
     */
    public List<Card> drawCards(int count) {
        List<Card> drawnCards = new ArrayList<>();
        for (int i = 0; (i < count && !deck.isEmpty()); i++) {
            drawnCards.add(deck.remove(0));
        }
        return drawnCards;
    }

    /**
     * Shuffles the deck using a random seed.
     */
    public void shuffleDeck() {
        Collections.shuffle(deck, random);
    }

    /**
     * Adds a list of cards to the bottom of the deck.
     *
     * @param c the list of {@code Card} objects to add
     */
    public void addToDeck(List<Card> c) {
        deck.addAll(c);
    }

    /**
     * Returns a card to the deck and shuffles the deck afterward.
     *
     * @param c the {@code Card} to return
     */
    public void returnToDeck(Card c) {
        deck.add(c);
        shuffleDeck();
    }

    /**
     * Serializes the current deck into a {@code JSONObject}.
     *
     * @return a JSON representation of the deck
     */
    @SuppressWarnings("unchecked")
    public JSONObject toJson() {
        JSONObject obj = new JSONObject();

        JSONArray defsArray = new JSONArray();
        for (Map.Entry<String, Card> entry : cardDefinitions.entrySet()) {
            JSONObject cardJson = entry.getValue().toJson();
            cardJson.put("name", entry.getKey());
            defsArray.add(cardJson);
        }
        obj.put("cardDefinitions", defsArray);

        JSONArray drawPileJson = new JSONArray();
        for (Card c : deck) {
            drawPileJson.add(c.toJson());
        }
        obj.put("drawPile", drawPileJson);

        obj.put("discardPile", getDiscardJson());

        return obj;
    }

    /**
     * Deserializes a {@code Deck} object from a {@code JSONObject}.
     *
     * @param obj the JSON object containing deck data
     * @return a reconstructed {@code Deck} object
     */
    public static Deck fromJson(JSONObject obj) {
        JSONArray defsArray = (JSONArray) obj.get("cardDefinitions");
        Map<String, Card> cardDefs = new HashMap<>();
        for (Object o : defsArray) {
            JSONObject cardJson = (JSONObject) o;
            String name = (String) cardJson.get("name");
            cardDefs.put(name, Card.fromJson(cardJson));
        }

        Deck loaded_deck = new Deck(cardDefs);

        JSONArray drawPileJson = (JSONArray) obj.get("drawPile");
        for (Object o : drawPileJson) {
            loaded_deck.deck.add(Card.fromJson((JSONObject) o));
        }

        JSONArray discardJson = (JSONArray) obj.get("discardPile");
        for (Object o : discardJson) {
            loaded_deck.discardedPile.add(Card.fromJson((JSONObject) o));
        }

        return loaded_deck;
    }

    /**
     * Serializes the discard pile into a {@code JSONArray}.
     *
     * @return a JSON array representing the discard pile
     */
    @SuppressWarnings("unchecked")
    public JSONArray getDiscardJson() {
        JSONArray discardArray = new JSONArray();
        for (Card c : discardedPile) {
            discardArray.add(c.toJson());
        }
        return discardArray;
    }
}