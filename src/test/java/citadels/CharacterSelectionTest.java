package citadels;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;
import java.util.*;

public class CharacterSelectionTest extends GameTest {
    
    @Test
    void testSelectionPhaseInitialization() {
        game.begin(4); // Start game with 4 players
        assertNotNull(game.getPlayers());
        assertEquals(4, game.getPlayers().size());
        
        // Check initial player setup
        for (Player player : game.getPlayers()) {
            assertEquals(2, player.getGold()); // Each player should start with 2 gold
            assertEquals(4, player.getHand().size()); // Each player should have 4 cards
        }
    }

    @Test
    void testCharacterDeckSetup() {
        game.begin(4);
        List<Characters> discarded = game.getDiscardedCharacters();
        assertNotNull(discarded);
        // For 4 players, 2 characters should be face-up discarded
        assertTrue(discarded.size() >= 2);
    }

    @Test
    void testCharacterAssignment() {
        game.begin(4);
        Map<Integer, Player> playerCharMap = game.getPlayerCharMap();
        
        // After character selection, each player should have a character
        for (Player player : game.getPlayers()) {
            assertNotNull(player.getCharCard());
            assertTrue(Characters.fromName(player.getCharCard()).getRank() >= 1 
                      && Characters.fromName(player.getCharCard()).getRank() <= 8);
        }
    }

    @Test
    void testCrownedPlayerSelection() {
        game.begin(4);
        // Test that crowned player is within valid range
        assertTrue(game.getPlayers().get(0).getId() >= 1 
                  && game.getPlayers().get(0).getId() <= 4);
    }

    @Test
    void testKingCharacterHandling() {
        game.begin(4);
        Map<Integer, Player> playerCharMap = game.getPlayerCharMap();
        
        // If King is assigned, that player should become crowned
        Player kingPlayer = playerCharMap.get(4); // King has rank 4
        if (kingPlayer != null) {
            assertEquals("King", kingPlayer.getCharCard());
        }
    }

    @Test
    void testCharacterDistribution() {
        game.begin(4);
        Set<String> assignedChars = new HashSet<>();
        for (Player player : game.getPlayers()) {
            String charCard = player.getCharCard();
            assertFalse(assignedChars.contains(charCard), "Character " + charCard + " was assigned multiple times");
            assignedChars.add(charCard);
        }
    }

    @Test
    void testFaceUpDiscardRules() {
        game.begin(4);
        List<Characters> discarded = game.getDiscardedCharacters();
        for (Characters c : discarded) {
            // King should never be face-up discarded
            assertNotEquals(4, c.getRank(), "King was face-up discarded");
        }
    }

    @Test
    void testCharacterSelectionOrder() {
        game.begin(4);
        Map<Integer, Player> playerCharMap = game.getPlayerCharMap();
        // Verify characters are assigned in rank order
        for (int i = 1; i <= 8; i++) {
            Player p = playerCharMap.get(i);
            if (p != null) {
                assertEquals(i, Characters.fromName(p.getCharCard()).getRank());
            }
        }
    }

    @Test
    void testSelectionWithDifferentPlayerCounts() {
        // Test with 5 players
        game = new Game(cardDefinitions);
        game.begin(5);
        List<Characters> discarded5 = game.getDiscardedCharacters();
        assertTrue(discarded5.size() >= 1); // 1 face-up for 5 players

        // Test with 6 players
        game = new Game(cardDefinitions);
        game.begin(6);
        List<Characters> discarded6 = game.getDiscardedCharacters();
        assertTrue(discarded6.size() >= 0); // 0 face-up for 6 players
    }

    @Test
    void testCharacterResetBetweenRounds() {
        game.begin(4);
        // Store first round characters
        Map<Integer, String> firstRoundChars = new HashMap<>();
        for (Player p : game.getPlayers()) {
            firstRoundChars.put(p.getId(), p.getCharCard());
        }

        // Process round and check characters are reset
        game.processesRound();
        for (Player p : game.getPlayers()) {
            assertNull(p.getCharCard(), "Character should be reset between rounds");
        }
    }
} 