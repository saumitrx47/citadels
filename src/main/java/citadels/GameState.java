package citadels;

import java.util.*;

/**
 * The {@code GameState} class represents the current state of the Citadels game.
 * It contains information about players, the deck, round number, crown holder,
 * and whether the game has ended.
 */
public class GameState {
    private List<Player> players;
    private Deck deck;
    private int currentCharacterRank;
    private int crownHolder;
    private boolean gameEnded;
    private int round;

    /**
     * Constructs a {@code GameState} with the given number of players, card data,
     * crown position, round number, and current character rank.
     *
     * @param playerCount the number of players in the game
     * @param cD a mapping of card names to {@code Card} objects (used to initialize the deck)
     * @param cP the player ID of the current crown holder
     * @param round the current round number
     * @param currentCharacterRank the character rank currently in play
     */
    public GameState(int playerCount, Map<String, Card> cD, int cP, int round, int currentCharacterRank) {
        this.players = new ArrayList<>();
        for (int i = 1; i <= playerCount; i++) {
            this.players.add(new Player(i));
        }
        this.deck = new Deck(cD);
        this.deck.shuffleDeck();
        this.crownHolder = cP;
        this.currentCharacterRank = currentCharacterRank;
        this.gameEnded = false;
        this.round = round;
    }

    /**
     * @return the list of players in the game
     */
    public List<Player> getPlayers() {
        return this.players;
    }

    /**
     * @return the rank of the current character in play
     */
    public int getCurrentCharacterRank() {
        return this.currentCharacterRank;
    }

    /**
     * @return the total number of players in the game
     */
    public int getPlayerCount() {
        return this.players.size();
    }

    /**
     * @return the player ID of the current crown holder
     */
    public int getCrownHolder() {
        return this.crownHolder;
    }

    /**
     * @return the deck of district cards used in the game
     */
    public Deck getDeck() {
        return this.deck;
    }

    /**
     * @return the current round number
     */
    public int getRound() {
        return this.round;
    }

    /**
     * @return {@code true} if the game has ended, otherwise {@code false}
     */
    public boolean isGameEnded() {
        return gameEnded;
    }

    /**
     * Marks the game as ended.
     */
    public void endGame() {
        this.gameEnded = true;
    }
}