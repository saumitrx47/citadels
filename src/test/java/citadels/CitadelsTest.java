package citadels;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.*;

public class CitadelsTest {

    @Test
    public void testCardCreation() {
        Card card = new Card("Castle", "Blue", "No ability", 2, 4);
        assertEquals("Castle", card.getName());
        assertEquals("Blue", card.getColor());
        assertEquals("No ability", card.getAbility());
        assertEquals(2, card.getQuantity());
        assertEquals(4, card.getCost());
    }

    @Test
    public void testPlayerGoldAndHand() {
        Player player = new Player(0);
        player.addGold(5);
        assertEquals(5, player.getGold());

        Card card = new Card("Temple", "Blue", "No ability", 1, 2);
        player.addToHand(card);
        assertEquals(1, player.getHand().size());
    }

    @Test
    public void testDeckDiscardedPile() {
        Map<String, Card> cardMap = new HashMap<>();
        Card card = new Card("Temple", "Blue", "No ability", 1, 2);
        cardMap.put("Temple", card);
        Deck deck = new Deck(cardMap);

        deck.addToDiscardedPile(card);
        assertTrue(deck.inDiscardedPile(card));
        assertEquals(card, deck.getFromDiscardedPile("Temple"));
    }

    @Test
    public void testCharactersEnum() {
        assertEquals(1, Characters.ASSASSIN.getRank());
        assertTrue(Characters.THIEF.getAbility().contains("rob"));
    }

    @Test
    public void testGameStateInitialization() {
        Map<String, Card> cardMap = new HashMap<>();
        cardMap.put("Temple", new Card("Temple", "Blue", "No ability", 1, 2));
        GameState state = new GameState(2, cardMap, 0, 1, 3);

        assertEquals(2, state.getPlayers().size());
        assertEquals(0, state.getCrownHolder());
        assertEquals(1, state.getRound());
        assertEquals(3, state.getCurrentCharacterRank());
    }
}
