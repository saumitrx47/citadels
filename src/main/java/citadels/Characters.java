package citadels;

import java.util.*;

/**
 * The {@code Characters} enum represents the roles available in the Citadels game.
 * Each character has a unique rank and a special ability that affects gameplay.
 */
public enum Characters {
    ASSASSIN(1, "Select another character to kill. The killed character loses their turn."),
    THIEF(2, "Select another character to rob. When that character reveals themselves, you immediately steal all of their gold. You cannot rob the Assassin or the killed character."),
    MAGICIAN(3, "Either exchange your hand with another player, or discard any number of district cards and draw the same number from the deck (once per turn)."),
    KING(4, "Gain 1 gold for each yellow (noble) district in your city. Receive the crown token and choose characters first next round."),
    BISHOP(5, "Gain 1 gold for each blue (religious) district in your city. Your buildings cannot be destroyed by the Warlord, unless you are killed."),
    MERCHANT(6, "Gain 1 gold for each green (trade) district in your city. Also gain 1 extra gold."),
    ARCHITECT(7, "Draw 2 extra district cards. You may build up to 3 districts this turn."),
    WARLORD(8, "Gain 1 gold for each red (military) district in your city. You may destroy one district by paying 1 less gold than its cost, unless the city has 8 or more districts.");

    private int rank;
    private String ability;

    /**
     * Constructs a {@code Characters} enum instance with a given rank and ability.
     *
     * @param rank    the turn order rank of the character
     * @param ability a description of the character's special ability
     */
    Characters(int rank, String ability) {
        this.rank = rank;
        this.ability = ability;
    }

    /**
     * @return the turn order rank of the character
     */
    public int getRank() {
        return rank;
    }

    /**
     * @return the description of the character's special ability
     */
    public String getAbility() {
        return ability;
    }

    /**
     * @return a list containing all {@code Characters} in their declared order
     */
    public static List<Characters> getCharacters() {
        return new ArrayList<>(Arrays.asList(values()));
    }

    /**
     * Randomly shuffles the list of characters.
     * <p><b>Note:</b> This method creates a shuffled list but does not store or return it.
     */
    public static void shuffleDeck() {
        List<Characters> characters = getCharacters();
        Collections.shuffle(characters, new Random());
    }

    /**
     * Returns the {@code Characters} enum constant with the given name.
     *
     * @param name the name of the character (case-insensitive)
     * @return the {@code Characters} constant, or {@code null} if not found
     */
    public static Characters fromName(String name) {
        for (Characters c : values()) {
            if (c.name().equalsIgnoreCase(name)) {
                return c;
            }
        }
        return null;
    }

    /**
     * Returns the {@code Characters} constant with the specified rank.
     *
     * @param i the rank to look for
     * @return the {@code Characters} constant with the given rank, or {@code null} if not found
     */
    public static Characters fromRank(int i) {
        for (Characters c : values()) {
            if (c.getRank() == i) {
                return c;
            }
        }
        return null;
    }

    /**
     * Returns the formatted name of the given {@code Characters} constant.
     * For example, "ASSASSIN" becomes "Assassin".
     *
     * @param c the character enum constant
     * @return the formatted name
     */
    public static String getName(Characters c) {
        return Character.toUpperCase(c.name().charAt(0)) + c.name().substring(1).toLowerCase();
    }

    /**
     * Returns a string representation of the character including its name, rank, and ability.
     *
     * @return a formatted string describing the character
     */
    @Override
    public String toString() {
        return getName(this) + " (" + rank + "): " + ability;
    }
}