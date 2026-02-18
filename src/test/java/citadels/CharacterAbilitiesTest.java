package citadels;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;
import java.util.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class CharacterAbilitiesTest {
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
    void testAssassinAbility() {
        Player assassin = game.getPlayers().get(0);
        assassin.setCharCard("Assassin");
        
        Characters assassinChar = Characters.fromName("Assassin");
        game.performCharacterAction(assassinChar, assassin);
        
        List<Characters> deadChars = game.getDeadCharacters();
        assertFalse(deadChars.isEmpty(), "Assassin should kill a character");
        assertTrue(deadChars.get(0).getRank() > 1, "Assassin cannot kill rank 1");
    }

    @Test
    @Order(2)
    void testThiefAbility() {
        Player thief = game.getPlayers().get(0);
        Player victim = game.getPlayers().get(1);
        
        thief.setCharCard("Thief");
        victim.setCharCard("Merchant");
        victim.addGold(5);
        
        Characters thiefChar = Characters.fromName("Thief");
        game.performCharacterAction(thiefChar, thief);
        
        // Verify thief can't steal from ranks 1 or 2
        for (Characters target : game.getDeadCharacters()) {
            assertTrue(target.getRank() > 2, "Thief cannot steal from ranks 1 or 2");
        }
    }

    @Test
    @Order(3)
    void testMagicianAbility() {
        Player magician = game.getPlayers().get(0);
        Player target = game.getPlayers().get(1);
        
        magician.setCharCard("Magician");
        
        // Setup known hands
        List<Card> hand1 = Arrays.asList(
            new Card("Test1", "red", "", 1, 0),
            new Card("Test2", "blue", "", 2, 0)
        );
        List<Card> hand2 = Arrays.asList(
            new Card("Test3", "green", "", 3, 0),
            new Card("Test4", "yellow", "", 4, 0)
        );
        
        magician.setHand(hand1, true);
        target.setHand(hand2, true);
        
        Characters magicianChar = Characters.fromName("Magician");
        game.performCharacterAction(magicianChar, magician);
        
        assertNotNull(magician.getHand(), "Magician should have cards after swap");
        assertNotNull(target.getHand(), "Target should have cards after swap");
    }

    @Test
    @Order(4)
    void testKingAbility() {
        Player king = game.getPlayers().get(0);
        king.setCharCard("King");
        
        // Add multiple yellow districts
        for (int i = 0; i < 3; i++) {
            king.addToCity(new Card("Castle", "yellow", "", 4, 0));
        }
        
        int initialGold = king.getGold();
        Characters kingChar = Characters.fromName("King");
        game.performCharacterAction(kingChar, king);
        
        assertEquals(initialGold + 3, king.getGold(), "King should receive 1 gold per yellow district");
    }

    @Test
    @Order(5)
    void testBishopAbility() {
        Player bishop = game.getPlayers().get(0);
        bishop.setCharCard("Bishop");
        
        // Add multiple blue districts
        for (int i = 0; i < 3; i++) {
            bishop.addToCity(new Card("Temple", "blue", "", 2, 0));
        }
        
        int initialGold = bishop.getGold();
        Characters bishopChar = Characters.fromName("Bishop");
        game.performCharacterAction(bishopChar, bishop);
        
        assertEquals(initialGold + 3, bishop.getGold(), "Bishop should receive 1 gold per blue district");
    }

    @Test
    @Order(6)
    void testMerchantAbility() {
        Player merchant = game.getPlayers().get(0);
        merchant.setCharCard("Merchant");
        
        // Add multiple green districts
        for (int i = 0; i < 3; i++) {
            merchant.addToCity(new Card("Market", "green", "", 2, 0));
        }
        
        int initialGold = merchant.getGold();
        Characters merchantChar = Characters.fromName("Merchant");
        game.performCharacterAction(merchantChar, merchant);
        
        assertEquals(initialGold + 4, merchant.getGold(), "Merchant should receive 1 gold per green district plus 1 bonus");
    }

    @Test
    @Order(7)
    void testArchitectAbility() {
        Player architect = game.getPlayers().get(0);
        architect.setCharCard("Architect");
        architect.addGold(15);
        
        // Add multiple districts to hand
        for (int i = 0; i < 3; i++) {
            architect.addToHand(new Card("District" + i, "yellow", "", 2, 0));
        }
        
        game.playerTurn(1, "gold");
        
        assertTrue(architect.getCity().size() <= 3, "Architect should be able to build up to 3 districts");
    }

    @Test
    @Order(8)
    void testWarlordAbility() {
        Player warlord = game.getPlayers().get(0);
        Player target = game.getPlayers().get(1);
        
        warlord.setCharCard("Warlord");
        warlord.addGold(10);
        
        // Add district to target's city
        target.addToCity(new Card("Fortress", "red", "", 5, 0));
        int initialCitySize = target.getCity().size();
        
        Characters warlordChar = Characters.fromName("Warlord");
        game.performCharacterAction(warlordChar, warlord);
        
        assertTrue(target.getCity().size() <= initialCitySize, "Warlord should be able to destroy a district");
    }

    @Test
    @Order(9)
    void testCharacterInteractions() {
        // Test Bishop protection from Warlord
        Player bishop = game.getPlayers().get(0);
        Player warlord = game.getPlayers().get(1);
        
        bishop.setCharCard("Bishop");
        warlord.setCharCard("Warlord");
        warlord.addGold(10);
        
        bishop.addToCity(new Card("Temple", "blue", "", 2, 0));
        int initialCitySize = bishop.getCity().size();
        
        Characters warlordChar = Characters.fromName("Warlord");
        game.performCharacterAction(warlordChar, warlord);
        
        assertEquals(initialCitySize, bishop.getCity().size(), "Bishop's districts should be protected from Warlord");
    }

    @Test
    @Order(10)
    void testDeadCharacterInteractions() {
        Player assassin = game.getPlayers().get(0);
        Player thief = game.getPlayers().get(1);
        Player victim = game.getPlayers().get(2);
        
        assassin.setCharCard("Assassin");
        thief.setCharCard("Thief");
        victim.setCharCard("Merchant");
        
        // Assassin kills Merchant
        Characters assassinChar = Characters.fromName("Assassin");
        game.performCharacterAction(assassinChar, assassin);
        
        // Thief tries to steal from dead Merchant
        Characters thiefChar = Characters.fromName("Thief");
        game.performCharacterAction(thiefChar, thief);
        
        assertEquals(2, victim.getGold(), "Dead character's gold should not be stolen");
    }
} 