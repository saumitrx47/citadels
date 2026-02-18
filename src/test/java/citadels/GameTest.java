package citadels;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class GameTest {
    private Game game;
    private Map<String, Card> cardDefinitions;
    
    @BeforeEach
    void setUp() {
        cardDefinitions = new HashMap<>();
        // Add test cards of each color
        cardDefinitions.put("Temple", new Card("Temple", "blue", "", 2, 0));
        cardDefinitions.put("Market", new Card("Market", "green", "", 2, 0));
        cardDefinitions.put("Castle", new Card("Castle", "yellow", "", 4, 0));
        cardDefinitions.put("Fortress", new Card("Fortress", "red", "", 5, 0));
        cardDefinitions.put("Laboratory", new Card("Laboratory", "purple", "You may destroy one of your districts and receive one gold more than its cost.", 5, 0));
        
        game = new Game(cardDefinitions);
    }

    @Test
    @Order(1)
    void testGameInitialization() {
        assertNotNull(game);
        assertEquals(0, game.getPlayers().size());
    }

    @Test
    @Order(2)
    void testGameStartWithFourPlayers() {
        game.begin(4);
        assertEquals(4, game.getPlayers().size());
        
        // Check each player's initial state
        for (Player player : game.getPlayers()) {
            assertEquals(2, player.getGold(), "Each player should start with 2 gold");
            assertEquals(4, player.getHand().size(), "Each player should start with 4 cards");
            assertTrue(player.getCity().isEmpty(), "City should be empty at start");
        }
    }

    @Test
    @Order(3)
    void testCharacterSelection() {
        game.begin(4);
        Map<Integer, Player> playerCharMap = game.getPlayerCharMap();
        
        // Verify character assignments
        assertFalse(playerCharMap.isEmpty(), "Characters should be assigned");
        
        // Check for unique character assignments
        Set<String> assignedChars = new HashSet<>();
        for (Player player : game.getPlayers()) {
            String charCard = player.getCharCard();
            assertNotNull(charCard, "Each player should have a character");
            assertFalse(assignedChars.contains(charCard), "Each character should be unique");
            assignedChars.add(charCard);
        }
    }

    @Test
    @Order(4)
    void testPlayerTurnActions() {
        game.begin(4);
        Player player = game.getPlayers().get(0);
        int initialGold = player.getGold();
        int initialHandSize = player.getHand().size();

        // Test gold collection
        game.playerTurn(1, "gold");
        assertEquals(initialGold + 2, player.getGold(), "Player should receive 2 gold");

        // Test card drawing
        game.playerTurn(1, "cards");
        assertTrue(player.getHand().size() > initialHandSize, "Player should receive at least one card");
    }

    @Test
    @Order(5)
    void testBuildingDistrict() {
        game.begin(4);
        Player player = game.getPlayers().get(0);
        
        // Add a cheap district card and enough gold
        Card cheapDistrict = new Card("TestDistrict", "yellow", "", 1, 0);
        player.addToHand(cheapDistrict);
        player.addGold(5);
        
        int initialCitySize = player.getCity().size();
        game.buildDistrict("1");
        
        assertEquals(initialCitySize + 1, player.getCity().size(), "District should be built");
    }

    @Test
    @Order(6)
    void testGameEndCondition() {
        game.begin(4);
        Player player = game.getPlayers().get(0);
        
        // Build 8 districts to trigger game end
        for (int i = 0; i < 8; i++) {
            player.addToCity(new Card("District" + i, "yellow", "", 1, 0));
        }
        
        assertTrue(game.processesRound(), "Game should process round");
        assertTrue(game.isGameOver(), "Game should end when a player has 8 districts");
    }

    @Test
    @Order(7)
    void testScoreCalculation() {
        game.begin(4);
        Player player = game.getPlayers().get(0);
        
        // Add districts of all colors
        player.addToCity(new Card("Temple", "blue", "", 2, 0));
        player.addToCity(new Card("Market", "green", "", 2, 0));
        player.addToCity(new Card("Castle", "yellow", "", 4, 0));
        player.addToCity(new Card("Fortress", "red", "", 5, 0));
        player.addToCity(new Card("Laboratory", "purple", "", 5, 0));
        
        game.isGameOver(); // Trigger score calculation
        
        // Expected score: 18 (district costs) + 3 (all colors bonus)
        assertEquals(21, player.getScore(), "Score should include district costs and color bonus");
    }

    @Test
    @Order(8)
    void testSaveAndLoadGame() {
        game.begin(4);
        Player player = game.getPlayers().get(0);
        player.addGold(5);
        player.addToCity(new Card("Castle", "yellow", "", 4, 0));
        
        game.saveGame("testSave", game.getCurrentCharacterRank());
        Game loadedGame = game.loadGame("testSave", cardDefinitions);
        
        assertNotNull(loadedGame, "Game should be loaded successfully");
        assertEquals(7, loadedGame.getPlayers().get(0).getGold(), "Gold should be preserved");
        assertEquals(1, loadedGame.getPlayers().get(0).getCity().size(), "City should be preserved");
    }

    @Test
    @Order(9)
    void testCharacterAbilities() {
        game.begin(4);
        Player player = game.getPlayers().get(0);
        player.setCharCard("King");
        
        // Add yellow district for King's ability
        player.addToCity(new Card("Castle", "yellow", "", 4, 0));
        int initialGold = player.getGold();
        
        Characters king = Characters.fromName("King");
        game.performCharacterAction(king, player);
        
        assertTrue(player.getGold() > initialGold, "King should receive gold for yellow districts");
    }

    @Test
    @Order(10)
    void testErrorHandling() {
        game.begin(4);
        
        // Test invalid district building
        assertDoesNotThrow(() -> game.buildDistrict("100"));
        
        // Test invalid character selection
        Player player = game.getPlayers().get(0);
        assertThrows(IllegalArgumentException.class, () -> player.setCharCard("InvalidCharacter"));
        
        // Test invalid city display
        assertDoesNotThrow(() -> game.displayCity("invalid"));
    }
} 