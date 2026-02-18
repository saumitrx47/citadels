package citadels;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;
import java.util.*;

public class ErrorHandlingTest extends GameTest {
    
    @BeforeEach
    void setUpErrorTest() {
        game.begin(4);
    }

    @Test
    void testInvalidPlayerCount() {
        Game newGame = new Game(cardDefinitions);
        assertThrows(IndexOutOfBoundsException.class, () -> {
            newGame.begin(1); // Game should not start with less than 2 players
        });
    }

    @Test
    void testInvalidCharacterSelection() {
        Player player = game.getPlayers().get(0);
        assertThrows(IllegalArgumentException.class, () -> {
            player.setCharCard("InvalidCharacter");
        });
    }

    @Test
    void testInvalidCityDisplay() {
        // Test with invalid player ID
        assertDoesNotThrow(() -> {
            game.displayCity("10"); // Should handle gracefully
            game.displayCity("invalid"); // Should handle gracefully
        });
    }

    @Test
    void testInvalidBuildDistrict() {
        // Test with invalid card index
        assertDoesNotThrow(() -> {
            game.buildDistrict("100"); // Should handle gracefully
            game.buildDistrict("invalid"); // Should handle gracefully
        });
    }

    @Test
    void testInvalidGameSaveLoad() {
        assertDoesNotThrow(() -> {
            game.loadGame("nonexistent_save", cardDefinitions); // Should return null or handle gracefully
        });
    }

    @Test
    void testDeadCharacterInteractions() {
        Player player = game.getPlayers().get(0);
        player.setCharCard("King");
        player.setIsCharDead(true);
        
        // Dead character should not be able to perform actions
        Characters king = Characters.fromName("King");
        game.performCharacterAction(king, player);
        
        // Verify no changes occurred
        assertEquals(2, player.getGold()); // Should still have initial gold
    }

    @Test
    void testInsufficientGoldForBuilding() {
        Player player = game.getPlayers().get(0);
        player.addGold(-2); // Remove all gold
        
        Card expensiveCard = new Card("Expensive", "yellow", "", 10, 0);
        player.addToHand(expensiveCard);
        
        int initialCitySize = player.getCity().size();
        game.buildDistrict("1");
        
        // City size should not change due to insufficient gold
        assertEquals(initialCitySize, player.getCity().size());
    }

    @Test
    void testWarlordTargetingBishop() {
        Player warlord = game.getPlayers().get(0);
        Player bishop = game.getPlayers().get(1);
        
        warlord.setCharCard("Warlord");
        bishop.setCharCard("Bishop");
        
        // Add district to bishop's city
        Card district = new Card("TestDistrict", "red", "", 3, 0);
        bishop.addToCity(district);
        
        Characters warlordChar = Characters.fromName("Warlord");
        game.performCharacterAction(warlordChar, warlord);
        
        // Bishop's buildings should be protected
        assertEquals(1, bishop.getCity().size());
    }

    @Test
    void testThiefTargetingDeadCharacter() {
        Player thief = game.getPlayers().get(0);
        Player target = game.getPlayers().get(1);
        
        thief.setCharCard("Thief");
        target.setCharCard("Merchant");
        target.setIsCharDead(true);
        
        Characters thiefChar = Characters.fromName("Thief");
        game.performCharacterAction(thiefChar, thief);
        
        // Verify thief didn't steal from dead character
        assertEquals(2, target.getGold()); // Should still have initial gold
    }
} 