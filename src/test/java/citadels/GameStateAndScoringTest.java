package citadels;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;
import java.util.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class GameStateAndScoringTest {
    private Game game;
    private Map<String, Card> cardDefinitions;
    
    @BeforeEach
    void setUp() {
        cardDefinitions = new HashMap<>();
        cardDefinitions.put("Temple", new Card("Temple", "blue", "", 2, 0));
        cardDefinitions.put("Market", new Card("Market", "green", "", 2, 0));
        cardDefinitions.put("Castle", new Card("Castle", "yellow", "", 4, 0));
        cardDefinitions.put("Fortress", new Card("Fortress", "red", "", 5, 0));
        cardDefinitions.put("Laboratory", new Card("Laboratory", "purple", "You may destroy one of your districts and receive one gold more than its cost.", 5, 0));
        
        game = new Game(cardDefinitions);
        game.begin(4);
    }

    @Test
    @Order(1)
    void testRoundProgression() {
        game.setRound(1);
        assertTrue(game.processesRound(), "Round should process successfully");
        
        // Verify round effects
        for (Player p : game.getPlayers()) {
            assertNull(p.getCharCard(), "Characters should be reset between rounds");
            assertFalse(p.getIsCharDead(), "Character death status should be reset");
        }
    }

    @Test
    @Order(2)
    void testGameEndByCompletion() {
        Player player = game.getPlayers().get(0);
        
        // Build 8 districts
        for (int i = 0; i < 8; i++) {
            player.addToCity(new Card("District" + i, "yellow", "", 1, 0));
        }
        
        assertTrue(game.processesRound(), "Round should process");
        assertTrue(game.isGameOver(), "Game should end with 8 districts");
    }

    @Test
    @Order(3)
    void testScoreCalculationAllColors() {
        Player player = game.getPlayers().get(0);
        
        // Add one of each color
        player.addToCity(new Card("Temple", "blue", "", 2, 0));
        player.addToCity(new Card("Market", "green", "", 2, 0));
        player.addToCity(new Card("Castle", "yellow", "", 4, 0));
        player.addToCity(new Card("Fortress", "red", "", 5, 0));
        player.addToCity(new Card("Laboratory", "purple", "", 5, 0));
        
        game.isGameOver();
        
        // Score = 18 (sum of costs) + 3 (all colors bonus)
        assertEquals(21, player.getScore(), "Score should include district costs and color bonus");
    }

    @Test
    @Order(4)
    void testScoreCalculationFirstToComplete() {
        Player player1 = game.getPlayers().get(0);
        Player player2 = game.getPlayers().get(1);
        
        // Player 1 completes first
        for (int i = 0; i < 8; i++) {
            player1.addToCity(new Card("District" + i, "yellow", "", 1, 0));
        }
        
        // Player 2 completes second
        for (int i = 0; i < 8; i++) {
            player2.addToCity(new Card("District" + i, "blue", "", 1, 0));
        }
        
        game.isGameOver();
        
        // Player 1: 8 (districts) + 4 (first to complete)
        assertEquals(12, player1.getScore(), "First player should get 4 bonus points");
        // Player 2: 8 (districts) + 2 (completed but not first)
        assertEquals(10, player2.getScore(), "Second player should get 2 bonus points");
    }

    @Test
    @Order(5)
    void testScoreCalculationTieBreaker() {
        Player player1 = game.getPlayers().get(0);
        Player player2 = game.getPlayers().get(1);
        
        // Both players build same value cities but different characters
        for (int i = 0; i < 5; i++) {
            player1.addToCity(new Card("District" + i, "yellow", "", 2, 0));
            player2.addToCity(new Card("District" + i, "blue", "", 2, 0));
        }
        
        player1.setCharCard("King"); // Rank 4
        player2.setCharCard("Assassin"); // Rank 1
        
        game.isGameOver();
        
        // Both should have same base score
        assertEquals(player1.getScore(), player2.getScore(), "Players should have equal scores");
    }

    @Test
    @Order(6)
    void testSaveAndLoadGameState() {
        // Setup complex game state
        game.setRound(3);
        for (Player p : game.getPlayers()) {
            p.addGold(5);
            p.addToCity(new Card("Temple", "blue", "", 2, 0));
            p.setCharCard("King");
        }
        
        game.saveGame("complexState", game.getCurrentCharacterRank());
        Game loadedGame = game.loadGame("complexState", cardDefinitions);
        
        assertNotNull(loadedGame, "Game should load successfully");
        assertEquals(3, loadedGame.getCurrentCharacterRank(), "Character rank should be preserved");
        assertEquals(4, loadedGame.getPlayers().size(), "Player count should be preserved");
        
        // Verify player states
        for (int i = 0; i < game.getPlayers().size(); i++) {
            Player original = game.getPlayers().get(i);
            Player loaded = loadedGame.getPlayers().get(i);
            
            assertEquals(original.getGold(), loaded.getGold(), "Gold should be preserved");
            assertEquals(original.getCity().size(), loaded.getCity().size(), "City size should be preserved");
            assertEquals(original.getCharCard(), loaded.getCharCard(), "Character should be preserved");
        }
    }

    @Test
    @Order(7)
    void testGameStateAfterCharacterDeath() {
        Player assassin = game.getPlayers().get(0);
        Player victim = game.getPlayers().get(1);
        
        assassin.setCharCard("Assassin");
        victim.setCharCard("King");
        
        Characters assassinChar = Characters.fromName("Assassin");
        game.performCharacterAction(assassinChar, assassin);
        
        assertTrue(game.getDeadCharacters().contains(Characters.fromName("King")), "King should be marked as dead");
        assertTrue(victim.getIsCharDead(), "Victim should be marked as dead");
    }

    @Test
    @Order(8)
    void testGameStateAfterRoundReset() {
        // Setup initial state
        Player player = game.getPlayers().get(0);
        player.setCharCard("King");
        player.setIsCharDead(true);
        
        game.processesRound();
        
        assertNull(player.getCharCard(), "Character should be reset");
        assertFalse(player.getIsCharDead(), "Death status should be reset");
        assertTrue(game.getDeadCharacters().isEmpty(), "Dead characters list should be cleared");
    }

    @Test
    @Order(9)
    void testGameStateWithMultipleRounds() {
        for (int round = 1; round <= 3; round++) {
            game.setRound(round);
            assertTrue(game.processesRound(), "Round " + round + " should process");
            
            // Verify round marker in cities
            for (Player p : game.getPlayers()) {
                boolean hasRoundMarker = false;
                for (Card c : p.getCity()) {
                    if (c.getName().equals("Round")) {
                        hasRoundMarker = true;
                        assertEquals(round, c.getCost(), "Round marker should match current round");
                    }
                }
                assertTrue(hasRoundMarker, "Player should have round marker");
            }
        }
    }

    @Test
    @Order(10)
    void testComplexGameScenario() {
        // Setup multiple players with different states
        Player player1 = game.getPlayers().get(0);
        Player player2 = game.getPlayers().get(1);
        
        // Player 1: All colors but not complete
        player1.addToCity(new Card("Temple", "blue", "", 2, 0));
        player1.addToCity(new Card("Market", "green", "", 2, 0));
        player1.addToCity(new Card("Castle", "yellow", "", 4, 0));
        player1.addToCity(new Card("Fortress", "red", "", 5, 0));
        player1.addToCity(new Card("Laboratory", "purple", "", 5, 0));
        
        // Player 2: Complete city but not all colors
        for (int i = 0; i < 8; i++) {
            player2.addToCity(new Card("District" + i, "yellow", "", 1, 0));
        }
        
        game.isGameOver();
        
        // Player 1: 18 (costs) + 3 (colors)
        assertEquals(21, player1.getScore(), "Score should include district costs and color bonus");
        // Player 2: 8 (costs) + 4 (first to complete)
        assertEquals(12, player2.getScore(), "Score should include district costs and completion bonus");
    }
} 