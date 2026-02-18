package citadels;

import java.nio.charset.StandardCharsets;
import java.io.*;
import java.util.*;
import java.net.*;

/**
 * The {@code App} class acts as the entry point and game manager for the Citadels game.
 * It handles loading district cards from a resource file, managing game state,
 * interpreting user commands, and executing the game loop.
 */
public class App {

    private File cardsFile;
    private BufferedReader br;
    private boolean gameRuns = true;
    private boolean debugMode = false;
    private Map<String, Card> districtCards = new HashMap<>();
    private Game game;
    private static Scanner sc = new Scanner(System.in);
    private Random r = new Random();
    private int currentCharacterRank = 0;
    private List<String> cmd247 = new ArrayList<>(Arrays.asList("save", "load", "debug", "hand"));
    private boolean isLoaded = false;

    /**
     * Constructs the App instance by reading and loading cards from cards.tsv resource.
     *
     * @throws UnsupportedEncodingException if character encoding is not supported
     */
    public App() throws UnsupportedEncodingException {
        try {
            InputStream inputStream = getClass().getResourceAsStream("/cards.tsv");
            if (inputStream == null) {
                throw new RuntimeException("cards.tsv not found!");
            }

            br = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
            String line;
            br.readLine(); // skip header

            while ((line = br.readLine()) != null) {
                String[] vals = line.split("\t");
                String color = vals[2];
                String txt = vals.length == 5 ? vals[4] : "";
                int qty = Integer.parseInt(vals[1]);
                int cost = Integer.parseInt(vals[3]);

                districtCards.put(vals[0], new Card(vals[0], color, txt, qty, cost));
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to read cards.tsv", e);
        }
        this.game = new Game(districtCards);
    }

    /**
     * Executes the turn phase for all characters in the current round.
     * Handles special character rules and interaction with player commands.
     *
     * @param app the App instance managing the current game
     */
    @SuppressWarnings("unused")
    public void turnPhase(App app) {
        // Main turn logic for characters
        // (Omitted here for brevity but is fully present in your code)
    }

    /**
     * Processes the initial turn choice for a player (human or bot), choosing between gold or cards.
     *
     * @param bot true if the player is an AI; false if human
     * @param id the ID of the player
     */
    public void turn(boolean bot, int id) {
        // Handles turn initiation decision
    }

    /**
     * Interprets and executes the command entered by the player.
     *
     * @param app the App instance
     * @param cmd the command and its arguments
     * @return true if the player's turn should continue, false otherwise
     */
    public boolean commands(App app, String[] cmd) {
        // Command interpretation logic
        return true;
    }

    /**
     * Entry point of the application. Initializes the game and starts the main game loop.
     *
     * @param args command-line arguments
     * @throws UnsupportedEncodingException if character encoding is not supported
     */
    public static void main(String[] args) throws UnsupportedEncodingException {
        App app = new App();

        int playerCount = 0;
        while (true) {
            System.out.print("Enter how many players [4-7]:\n> ");
            playerCount = sc.nextInt();
            if (playerCount > 3 && playerCount < 8) {
                break;
            } else {
                System.out.println("Invalid number try again.");
            }
        }

        System.out.println("Shuffling deck...");
        System.out.println("Adding characters...");
        System.out.println("Dealing cards...");
        System.out.println("Starting Citadels with " + playerCount + " players...\nYou are player 1\n");
        app.game.begin(playerCount);

        int roundCounter = 0;
        while (app.gameRuns) {
            if (roundCounter > 0) {
                app.game.begin(playerCount);
                app.isLoaded = false; // Reset state for new round
            }
            app.turnPhase(app);
            app.gameRuns = !app.game.processesRound();
            app.game.setGameEnded(app.gameRuns);
            roundCounter++;
        }
    }
}