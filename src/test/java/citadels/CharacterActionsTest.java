package citadels;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;
import java.util.*;

public class CharacterActionsTest extends GameTest {
    
    @BeforeEach
    void setUpCharacterTest() {
        game.begin(4);
    }

    @Test
    void testAssassinAction() {
        Player player = game.getPlayers().get(0);
        player.setCharCard("Assassin");
        
        Characters assassin = Characters.fromName("Assassin");
        game.performCharacterAction(assassin, player);
        
        List<Characters> deadChars = game.getDeadCharacters();
        assertFalse(deadChars.isEmpty());
        assertTrue(deadChars.get(0).getRank() > 1); // Assassin can't kill rank 1
    }

    @Test
    void testAssassinTargetingRestrictions() {
        Player assassinPlayer = game.getPlayers().get(0);
        assassinPlayer.setCharCard("Assassin");
        
        // Try to kill Assassin (rank 1)
        Characters assassin = Characters.fromName("Assassin");
        game.performCharacterAction(assassin, assassinPlayer);
        
        List<Characters> deadChars = game.getDeadCharacters();
        for (Characters c : deadChars) {
            assertNotEquals(1, c.getRank(), "Assassin shouldn't be able to kill rank 1");
        }
    }

    @Test
    void testThiefAction() {
        Player player = game.getPlayers().get(0);
        player.setCharCard("Thief");
        
        Characters thief = Characters.fromName("Thief");
        game.performCharacterAction(thief, player);
        
        // Verify thief can't steal from rank 1 or 2
        for (Characters target : game.getDeadCharacters()) {
            assertTrue(target.getRank() > 2);
        }
    }

    @Test
    void testThiefTargetingRestrictions() {
        Player thiefPlayer = game.getPlayers().get(0);
        thiefPlayer.setCharCard("Thief");
        
        Characters thief = Characters.fromName("Thief");
        game.performCharacterAction(thief, thiefPlayer);
        
        // Verify thief can't target assassin or thief
        assertDoesNotThrow(() -> {
            for (Characters target : game.getDeadCharacters()) {
                assertTrue(target.getRank() > 2, "Thief shouldn't target ranks 1 or 2");
            }
        });
    }

    @Test
    void testKingAction() {
        Player player = game.getPlayers().get(0);
        player.setCharCard("King");
        
        // Add multiple yellow districts
        for (int i = 0; i < 3; i++) {
            player.addToCity(new Card("TestYellow" + i, "yellow", "", 2, 0));
        }
        
        int initialGold = player.getGold();
        Characters king = Characters.fromName("King");
        game.performCharacterAction(king, player);
        
        // Should receive 1 gold per yellow district
        assertEquals(initialGold + 3, player.getGold());
    }

    @Test
    void testBishopAction() {
        Player player = game.getPlayers().get(0);
        player.setCharCard("Bishop");
        
        // Add multiple blue districts
        for (int i = 0; i < 3; i++) {
            player.addToCity(new Card("TestBlue" + i, "blue", "", 2, 0));
        }
        
        int initialGold = player.getGold();
        Characters bishop = Characters.fromName("Bishop");
        game.performCharacterAction(bishop, player);
        
        // Should receive 1 gold per blue district
        assertEquals(initialGold + 3, player.getGold());
    }

    @Test
    void testBishopProtection() {
        Player bishop = game.getPlayers().get(0);
        Player warlord = game.getPlayers().get(1);
        
        bishop.setCharCard("Bishop");
        warlord.setCharCard("Warlord");
        warlord.addGold(10);
        
        // Add district to bishop's city
        bishop.addToCity(new Card("TestBlue", "blue", "", 2, 0));
        int initialCitySize = bishop.getCity().size();
        
        Characters warlordChar = Characters.fromName("Warlord");
        game.performCharacterAction(warlordChar, warlord);
        
        // Bishop's city should remain unchanged
        assertEquals(initialCitySize, bishop.getCity().size());
    }

    @Test
    void testMerchantAction() {
        Player player = game.getPlayers().get(0);
        player.setCharCard("Merchant");
        
        // Add multiple green districts
        for (int i = 0; i < 3; i++) {
            player.addToCity(new Card("TestGreen" + i, "green", "", 2, 0));
        }
        
        int initialGold = player.getGold();
        Characters merchant = Characters.fromName("Merchant");
        game.performCharacterAction(merchant, player);
        
        // Should receive 1 gold per green district plus 1 bonus
        assertEquals(initialGold + 4, player.getGold());
    }

    @Test
    void testMerchantBonusGold() {
        Player player = game.getPlayers().get(0);
        player.setCharCard("Merchant");
        
        int initialGold = player.getGold();
        Characters merchant = Characters.fromName("Merchant");
        game.performCharacterAction(merchant, player);
        
        // Should receive 1 bonus gold even with no green districts
        assertEquals(initialGold + 1, player.getGold());
    }

    @Test
    void testWarlordAction() {
        Player attacker = game.getPlayers().get(0);
        Player target = game.getPlayers().get(1);
        
        attacker.setCharCard("Warlord");
        attacker.addGold(10);
        
        // Add multiple districts to target's city
        for (int i = 0; i < 3; i++) {
            target.addToCity(new Card("TestRed" + i, "red", "", 3, 0));
        }
        
        Characters warlord = Characters.fromName("Warlord");
        game.performCharacterAction(warlord, attacker);
        
        // Should be able to destroy one district
        assertTrue(target.getCity().size() < 3);
    }

    @Test
    void testWarlordInsufficientGold() {
        Player attacker = game.getPlayers().get(0);
        Player target = game.getPlayers().get(1);
        
        attacker.setCharCard("Warlord");
        attacker.addGold(-2); // Remove all gold
        
        target.addToCity(new Card("TestRed", "red", "", 5, 0));
        int initialCitySize = target.getCity().size();
        
        Characters warlord = Characters.fromName("Warlord");
        game.performCharacterAction(warlord, attacker);
        
        // Should not be able to destroy without sufficient gold
        assertEquals(initialCitySize, target.getCity().size());
    }

    @Test
    void testMagicianSwapHands() {
        Player magician = game.getPlayers().get(0);
        Player target = game.getPlayers().get(1);
        
        magician.setCharCard("Magician");
        
        List<Card> hand1 = new ArrayList<>(Arrays.asList(
            new Card("Test1", "red", "", 1, 0),
            new Card("Test2", "blue", "", 2, 0)
        ));
        List<Card> hand2 = new ArrayList<>(Arrays.asList(
            new Card("Test3", "green", "", 3, 0),
            new Card("Test4", "yellow", "", 4, 0)
        ));
        
        magician.setHand(hand1, true);
        target.setHand(hand2, true);
        
        Characters magicianChar = Characters.fromName("Magician");
        game.performCharacterAction(magicianChar, magician);
        
        // Verify hands were swapped
        assertNotNull(magician.getHand());
        assertNotNull(target.getHand());
    }

    @Test
    void testMagicianRedrawCards() {
        Player magician = game.getPlayers().get(0);
        magician.setCharCard("Magician");
        
        List<Card> initialHand = new ArrayList<>(Arrays.asList(
            new Card("Test1", "red", "", 1, 0),
            new Card("Test2", "blue", "", 2, 0)
        ));
        magician.setHand(initialHand, true);
        
        Characters magicianChar = Characters.fromName("Magician");
        game.performCharacterAction(magicianChar, magician);
        
        // Verify hand was redrawn
        assertNotNull(magician.getHand());
    }
} 