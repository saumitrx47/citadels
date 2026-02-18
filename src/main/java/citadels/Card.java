package citadels;

import org.json.simple.JSONObject;

/**
 * The {@code Card} class represents a single card in the Citadels game.
 * Each card has a name, color, ability, quantity (number of copies), and cost.
 * This class also provides JSON serialization and deserialization support.
 */
public class Card {

    private final String name, color, ability;
    private final int quantity, cost;

    /**
     * Constructs a {@code Card} with the specified properties.
     *
     * @param name     the name of the card
     * @param color    the color of the card (e.g., district type)
     * @param ability  the special ability or description of the card
     * @param quantity the number of copies of this card in the deck
     * @param cost     the cost to build the card
     */
    public Card(String name, String color, String ability, int quantity, int cost) {
        this.name = name;
        this.color = color;
        this.ability = ability;
        this.quantity = quantity;
        this.cost = cost;
    }

    /**
     * Returns the name of the card.
     *
     * @return the card name
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the color of the card.
     *
     * @return the card color
     */
    public String getColor() {
        return color;
    }

    /**
     * Returns the cost of the card.
     *
     * @return the card cost
     */
    public int getCost() {
        return cost;
    }

    /**
     * Returns the special ability text of the card.
     *
     * @return the card's ability
     */
    public String getAbility() {
        return ability;
    }

    /**
     * Returns the number of copies of this card in the deck.
     *
     * @return the quantity
     */
    public int getQuantity() {
        return quantity;
    }

    /**
     * Returns a short string representation of the card.
     *
     * @return a string with the name, color, and cost
     */
    public String getCard() {
        return this.getName() + " [" + this.getColor() + "], cost: " + this.getCost();
    }

    /**
     * Returns a concise string representation of the card,
     * typically used for debugging or display purposes.
     *
     * @return a formatted string representing the card
     */
    @Override
    public String toString() {
        return name + " [" + color + cost + "]";
    }

    /**
     * Serializes this card into a {@code JSONObject}.
     *
     * @return a JSON object representing the card
     */
    @SuppressWarnings("unchecked")
    public JSONObject toJson() {
        JSONObject obj = new JSONObject();
        obj.put("name", this.name);
        obj.put("color", this.color);
        obj.put("ability", this.ability);
        obj.put("quantity", this.quantity);
        obj.put("cost", this.cost);
        return obj;
    }

    /**
     * Creates a {@code Card} object from a JSON representation.
     *
     * @param obj the JSON object representing the card
     * @return a new {@code Card} instance with data from the JSON
     */
    public static Card fromJson(JSONObject obj) {
        String name = (String) obj.get("name");
        String color = (String) obj.get("color");
        String ability = (String) obj.get("ability");
        int quantity = ((Long) obj.get("quantity")).intValue();
        int cost = ((Long) obj.get("cost")).intValue();
        return new Card(name, color, ability, quantity, cost);
    }

}