package citadels;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;
import java.util.*;

public class GameStateTest extends GameTest {
    
    @BeforeEach
    void setUpGameState() {
        game.begin(4);
    }

    @Test
    void testGameRoundProgression() {
        assertEquals(0, game.getCurrentCharacterRank());
        game.setRound(1);
        assertTrue(game.processesRound());
        assertEquals(2, game.getCurrentCharacterRank());
    }

    @Test
    void testMultipleRoundProgression() {
        assertEquals(0, game.getCurrentCharacterRank());
        
        // Process multiple rounds
        for (int i = 1; i <= 3; i++) {
            game.setRound(i);
            assertTrue(game.processesRound());
            assertEquals(i + 1, game.getCurrentCharacterRank());
        }
    }

    @Test
    void testGameEndCondition() {
        Player player = game.getPlayers().get(0);
        
        // Add 8 districts to trigger game end
        for (int i = 0; i < 8; i++) {
            Card district = new Card("Test" + i, "yellow", "", i + 1, 0);
            player.addToCity(district);
        }
        
        assertTrue(game.processesRound());
        assertTrue(game.isGameOver());
    }

    @Test
    void testGameEndWithMultiplePlayers() {
        // Give multiple players enough districts to end game
        for (Player p : game.getPlayers()) {
            for (int i = 0; i < 8; i++) {
                p.addToCity(new Card("Test" + i, "yellow", "", i + 1, 0));
            }
        }
        
        assertTrue(game.processesRound());
        assertTrue(game.isGameOver());
    }

    @Test
    void testScoreCalculation() {
        Player player = game.getPlayers().get(0);
        
        // Add districts of different colors
        player.addToCity(new Card("Test1", "yellow", "", 2, 0));
        player.addToCity(new Card("Test2", "blue", "", 3, 0));
        player.addToCity(new Card("Test3", "green", "", 4, 0));
        player.addToCity(new Card("Test4", "red", "", 5, 0));
        player.addToCity(new Card("Test5", "purple", "", 6, 0));
        
        game.isGameOver(); // This will calculate scores
        
        // Score should be sum of district costs (20) + 3 for all colors
        assertEquals(23, player.getScore());
    }

    @Test
    void testScoreCalculationWithMultiplePlayers() {
        // Setup multiple players with different district combinations
        Player player1 = game.getPlayers().get(0);
        Player player2 = game.getPlayers().get(1);
        
        // Player 1: All colors (20 points + 3 bonus)
        player1.addToCity(new Card("Test1", "yellow", "", 2, 0));
        player1.addToCity(new Card("Test2", "blue", "", 3, 0));
        player1.addToCity(new Card("Test3", "green", "", 4, 0));
        player1.addToCity(new Card("Test4", "red", "", 5, 0));
        player1.addToCity(new Card("Test5", "purple", "", 6, 0));
        
        // Player 2: Same cost, different colors (20 points, no bonus)
        player2.addToCity(new Card("Test6", "yellow", "", 2, 0));
        player2.addToCity(new Card("Test7", "yellow", "", 3, 0));
        player2.addToCity(new Card("Test8", "yellow", "", 4, 0));
        player2.addToCity(new Card("Test9", "yellow", "", 5, 0));
        player2.addToCity(new Card("Test10", "yellow", "", 6, 0));
        
        game.isGameOver();
        
        assertEquals(23, player1.getScore()); // 20 + 3 bonus
        assertEquals(20, player2.getScore()); // 20, no bonus
    }

    @Test
    void testSaveAndLoadGame() {
        // Setup initial game state
        game.setRound(2);
        Player player = game.getPlayers().get(0);
        player.addGold(5);
        player.addToCity(new Card("TestDistrict", "yellow", "", 3, 0));
        
        // Save game
        game.saveGame("testSave", game.getCurrentCharacterRank());
        
        // Load game
        Game loadedGame = game.loadGame("testSave", cardDefinitions);
        
        // Verify state was preserved
        assertNotNull(loadedGame);
        assertEquals(2, loadedGame.getCurrentCharacterRank());
        assertEquals(7, loadedGame.getPlayers().get(0).getGold()); // 2 initial + 5 added
        assertEquals(1, loadedGame.getPlayers().get(0).getCity().size());
    }

    @Test
    void testSaveAndLoadComplexGameState() {
        // Setup complex game state
        game.setRound(3);
        
        // Setup multiple players with different states
        for (int i = 0; i < game.getPlayers().size(); i++) {
            Player p = game.getPlayers().get(i);
            p.addGold(i * 2); // Different gold amounts
            p.addToCity(new Card("Test" + i, "yellow", "", i + 1, 0));
            p.setCharCard(Characters.getName(Characters.fromRank(i + 1)));
        }
        
        // Save and load
        game.saveGame("complexSave", game.getCurrentCharacterRank());
        Game loadedGame = game.loadGame("complexSave", cardDefinitions);
        
        // Verify complex state was preserved
        assertNotNull(loadedGame);
        assertEquals(3, loadedGame.getCurrentCharacterRank());
        
        for (int i = 0; i < loadedGame.getPlayers().size(); i++) {
            Player originalPlayer = game.getPlayers().get(i);
            Player loadedPlayer = loadedGame.getPlayers().get(i);
            
            assertEquals(originalPlayer.getGold(), loadedPlayer.getGold());
            assertEquals(originalPlayer.getCity().size(), loadedPlayer.getCity().size());
            assertEquals(originalPlayer.getCharCard(), loadedPlayer.getCharCard());
        }
    }

    @Test
    void testPlayerTurnActions() {
        Player player = game.getPlayers().get(0);
        int initialGold = player.getGold();
        
        // Test taking gold action
        game.playerTurn(1, "gold");
        assertEquals(initialGold + 2, player.getGold());
        
        // Test drawing cards action
        int initialHandSize = player.getHand().size();
        game.playerTurn(1, "cards");
        assertTrue(player.getHand().size() >= initialHandSize);
    }

    @Test
    void testPlayerTurnWithDifferentCharacters() {
        Player player = game.getPlayers().get(0);
        
        // Test Architect (can build 3 districts)
        player.setCharCard("Architect");
        for (int i = 0; i < 3; i++) {
            player.addToHand(new Card("Test" + i, "yellow", "", 1, 0));
        }
        player.addGold(10);
        
        game.playerTurn(1, "gold");
        assertTrue(player.getCity().size() <= 3);
        
        // Test normal character (can build 1 district)
        game = new Game(cardDefinitions);
        game.begin(4);
        player = game.getPlayers().get(0);
        player.setCharCard("King");
        player.addToHand(new Card("Test", "yellow", "", 1, 0));
        player.addGold(10);
        
        game.playerTurn(1, "gold");
        assertTrue(player.getCity().size() <= 1);
    }

    @Test
    void testDisplayCommands() {
        // Test display methods don't throw exceptions
        assertDoesNotThrow(() -> {
            game.displayHand();
            game.displayGold();
            game.displayCity("1");
            game.displayAll();
        });
    }

    @Test
    void testBuildDistrict() {
        Player player = game.getPlayers().get(0);
        int initialCitySize = player.getCity().size();
        
        // Add a card to hand that we can build
        Card buildableCard = new Card("TestBuild", "yellow", "", 2, 0);
        player.addToHand(buildableCard);
        player.addGold(5); // Ensure enough gold
        
        game.buildDistrict("1"); // Try to build first card
        
        assertTrue(player.getCity().size() > initialCitySize);
    }

    @Test
    void testBuildMultipleDistricts() {
        Player player = game.getPlayers().get(0);
        player.addGold(20); // Ensure enough gold
        
        // Add multiple buildable cards
        for (int i = 0; i < 3; i++) {
            player.addToHand(new Card("Test" + i, "yellow", "", 2, 0));
        }
        
        int initialCitySize = player.getCity().size();
        
        // Try to build multiple districts
        for (int i = 0; i < 3; i++) {
            game.buildDistrict("1");
        }
        
        assertEquals(initialCitySize + 3, player.getCity().size());
    }
} 