package citadels;

import org.junit.Before;
import org.junit.jupiter.api.BeforeEach;
// import processing.core.PApplet;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.HashMap;
import java.util.Map;

public class SampleTest {
    private Game game;
    private Map<String, Card> cardDefs;

    @BeforeEach
    void setup() {
        cardDefs = new HashMap<>();
        cardDefs.put("TestCard", new Card("TestCard", "blue", "test ability", 1, 5));
        game = new Game(cardDefs);
    }

    @Test
    void testBeginInitializesPlayers() {
        // Test that begin creates the correct number of players
        //game.setRound(0);
        game.begin(4);
        System.out.println(game.getPlayers().size());
        assertEquals(4, game.getPlayers().size(), "Should initialize 4 players");
    }

    @Test
    public void testPlayerGold() { //this test is to check whether player can get gold or not
        Player p = new Player(1);
        p.addGold(5);
        assertEquals(7, p.getGold(), "Player should have 5 gold after adding 5.");
    }

    @Test
    public void testPlayerInitialGold() {
        // Test that a new player starts with 0 gold
        Player p = new Player(1);
        assertEquals(0, p.getGold(), "New player should have 0 gold.");
    }

    @Test
    public void testPlayerAddGold() {
        // Test that addGold correctly adds gold
        Player p = new Player(1);
        p.addGold(5);
        assertEquals(5, p.getGold(), "Player should have 5 gold after adding 5.");
    }

    @Test
    public void testPlayerSubtractGold() {
        // Test that subtractGold correctly removes gold
        Player p = new Player(1);
        p.addGold(10);
        p.subtractGold(3);
        assertEquals(7, p.getGold(), "Player should have 7 gold after subtracting 3.");
    }

    @Test
    public void testPlayerScoreUpdates() {
        // Test setting and updating the player's score
        Player p = new Player(1);
        p.setScore(10);
        p.addtoScore(5);
        assertEquals(15, p.getScore(), "Score should be 15 after adding 5 to 10.");
    }

    @Test
    public void testPlayerBuildInvalidCard() {
        // Ensure buildDistrict handles an invalid index
        Player p = new Player(1);
        p.buildDistrict(-1, 1); // should not throw exception
        p.buildDistrict(99, 1); // should not throw exception
    }

    @Test
    void testBuildDistrictInvalidIndex() {
        game.begin(4);
        // Should not throw, but print error
        assertDoesNotThrow(() -> game.buildDistrict("100"), "Should handle invalid index gracefully");
    }





    

}

// gradle jar						Generate the jar file
// gradle test						Run the testcases

// Please ensure you leave comments in your testcases explaining what the testcase is testing.
// Your mark will be based off the average of branches and instructions code coverage.
// To run the testcases and generate the jacoco code coverage report: 
// gradle test jacocoTestReport
