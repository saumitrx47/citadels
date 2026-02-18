package citadels;

import java.util.*;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

/**
 * The {@code Player} class represents a player in the Citadels game.
 * It tracks the player's gold, hand of cards, city (built districts), character role, score,
 * and whether their character has been eliminated for the round.
 * The class also includes methods for game actions like building districts, drawing cards,
 * and serializing to/from JSON.
 */
public class Player {

    private int id;
    private int gold;
    private List<Card> hand = new ArrayList<>();
    private List<Card> city = new ArrayList<>();
    private String charCard;
    private boolean IsChardDead = false;
    private int score;

    /**
     * Constructs a {@code Player} with a given player ID.
     *
     * @param id the unique identifier for the player
     */
    public Player(int id) {
        this.id = id;
        this.gold = 0;
        this.score = 0;
    }

    /** @return the player's current score */
    public int getScore() {
        return score;
    }

    /** @return the rank of the player's assigned character */
    public int getCharRank() {
        return Characters.fromName(charCard).getRank();
    }

    /**
     * Increments the player's score by a given amount.
     *
     * @param n the value to add to the score
     */
    public void addtoScore(int n) {
        score += n;
    }

    /**
     * Sets the player's score to a specific value.
     *
     * @param x the score to set
     */
    public void setScore(int x) {
        score = x;
    }

    /** @return the player's unique ID */
    public int getId() {
        return id;
    }

    /** @return the list of cards currently in the player's hand */
    public List<Card> getHand() {
        return hand;
    }

    /**
     * Adds a card to the player's hand.
     *
     * @param c the card to add
     */
    public void addToHand(Card c) {
        hand.add(c);
    }

    /**
     * Sets the player's hand to a new set of cards.
     *
     * @param swapped_hand the new list of cards
     * @param clear whether to clear the existing hand before adding the new cards
     */
    public void setHand(List<Card> swapped_hand, boolean clear) {
        if (clear)
            hand.clear();
        hand.addAll(swapped_hand);
    }

    /** @return the list of cards the player has built (city) */
    public List<Card> getCity() {
        return city;
    }

    /**
     * Adds a card to the player's city.
     *
     * @param c the card to add
     */
    public void addToCity(Card c) {
        city.add(c);
    }

    /** @return the name of the character card assigned to the player */
    public String getCharCard() {
        return charCard;
    }

    /** @return whether the player's character is marked as dead */
    public boolean getIsCharDead() {
        return IsChardDead;
    }

    /**
     * Sets whether the player's character is dead for the round.
     *
     * @param t true if the character is dead, false otherwise
     */
    public void setIsCharDead(boolean t) {
        IsChardDead = t;
    }

    /**
     * Sets the character card name for the player.
     *
     * @param c the character card name
     */
    public void setCharCard(String c) {
        this.charCard = c;
    }

    /** @return the player's current amount of gold */
    public int getGold() {
        return gold;
    }

    /**
     * Increases the player's gold by a specified amount.
     *
     * @param amt the amount of gold to add
     */
    public void addGold(int amt) {
        this.gold += amt;
    }

    /**
     * Decreases the player's gold by a specified amount.
     *
     * @param amt the amount of gold to subtract
     */
    public void subtractGold(int amt) {
        this.gold -= amt;
    }

    /**
     * Adds a list of cards to the player's initial hand.
     *
     * @param cards the list of cards to add
     */
    public void drawInitialCards(List<Card> cards) {
        hand.addAll(cards);
    }

    /** Prints the player's current gold to the console. */
    public void printGold() {
        System.out.println("You have " + this.gold + " gold.");
    }

    /** Prints the player's hand, including each card's name, color, and cost. */
    public void printHand() {
        System.out.println("You have " + gold + " gold. Cards in hand:");
        int i = 1;
        for (Card c : hand) {
            System.out.println(i + ". " + c.getName() + " (" + c.getColor() + "), cost: " + c.getCost());
            i++;
        }
    }

    /** Prints the player's built city, excluding any "Round" cards. */
    public void printCity() {
        System.out.println("Player " + id + " has built:");
        for (Card c : city) {
            if (!c.getName().equalsIgnoreCase("Round")) {
                System.out.println(c.getName() + " (" + c.getColor() + "), points: " + c.getCost());
            }
        }
    }

    /** Prints a summary of the player's status, including hand size, gold, and city cards. */
    public void printSummary() {
        System.out.print("Player " + id + (id == 1 ? " (you)" : "") + ": cards=" + hand.size() + " gold=" + gold + " city=");
        if (city.isEmpty()) {
            System.out.println("\n");
        } else {
            for (int i = 0; i < city.size(); i++) {
                if (!city.get(i).getName().equalsIgnoreCase("Round")) {
                    System.out.print(city.get(i));
                    if (i < city.size() - 1) {
                        System.out.print(", ");
                    }
                }
            }
            System.out.println("\n");
        }
    }

    /**
     * Attempts to build a district from the player's hand.
     *
     * @param index    the index of the card in hand to build
     * @param playerID the ID of the player performing the action
     */
    public void buildDistrict(int index, int playerID) {
        if (index < 0 || index >= hand.size()) {
            System.out.println("Invalid card index.");
            return;
        }

        Card c = hand.get(index);
        if (gold >= c.getCost()) {
            if (hasBuilt(c.getName())) {
                System.out.println("You cannot build duplicate buildings.");
                return;
            }
            subtractGold(c.getCost());
            hand.remove(index);
            city.add(c);
            if (playerID == 1) {
                System.out.println("Built " + c);
            } else {
                System.out.println("Player " + playerID + " built a " + c + " in their city.");
            }

        } else {
            System.out.println("You cannot afford to build this building.");
        }
    }

    /**
     * Checks if a card with the given name has already been built in the city.
     *
     * @param name the name of the card
     * @return true if the card has already been built, false otherwise
     */
    private boolean hasBuilt(String name) {
        for (Card c : city) {
            if (name.equalsIgnoreCase(c.getName())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Attempts to automatically build up to a limit of districts from hand.
     * Only builds affordable and non-duplicate cards.
     *
     * @param playerID the ID of the player
     * @param limit    the number of builds to attempt
     */
    public void tryAutoBuild(int playerID, int limit) {
        for (int i = 0; i < hand.size(); i++) {
            if ((gold >= hand.get(i).getCost()) && !(hasBuilt(hand.get(i).getName()))) {
                buildDistrict(i, playerID);
                limit--;
            }
            if (limit == 0) {
                return;
            }
        }
    }

    /**
     * Serializes the player data into a {@code JSONObject}.
     *
     * @return the JSON representation of the player
     */
    @SuppressWarnings("unchecked")
    public JSONObject toJson() {
        JSONObject obj = new JSONObject();
        obj.put("id", this.id);
        obj.put("gold", this.gold);

        JSONArray handArray = new JSONArray();
        for (Card c : hand) {
            handArray.add(c.toJson());
        }
        obj.put("hand", handArray);

        JSONArray cityArray = new JSONArray();
        for (Card c : city) {
            cityArray.add(c.toJson());
        }
        obj.put("city", cityArray);
        obj.put("character", this.charCard);

        return obj;
    }

    /**
     * Constructs a {@code Player} object from its JSON representation.
     *
     * @param obj the JSON object representing the player
     * @return the reconstructed {@code Player} object
     */
    public static Player fromJson(JSONObject obj) {
        int x = ((Long) obj.get("id")).intValue();
        Player p = new Player(x);
        p.id = ((Long) obj.get("id")).intValue();
        p.gold = ((Long) obj.get("gold")).intValue();

        JSONArray handArray = (JSONArray) obj.get("hand");
        for (Object o : handArray) {
            p.hand.add(Card.fromJson((JSONObject) o));
        }

        JSONArray cityArray = (JSONArray) obj.get("city");
        for (Object o : cityArray) {
            p.city.add(Card.fromJson((JSONObject) o));
        }

        p.charCard = (String) obj.get("character");
        return p;
    }

    /**
     * Returns a debug string with internal player state.
     *
     * @return a string summarizing the player's state
     */
    public String debug() {
        return id + " " + charCard + " gold: " + gold + " hand: " + hand.size() + " city: " + city.size() + " score: " + score + " dead: " + IsChardDead;
    }
}