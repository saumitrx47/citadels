/**
 * Represents the main game engine for the Citadels card game.
 * This class manages the game state, player interactions, character abilities,
 * and game progression through rounds until completion.
 */
package citadels;

import java.io.*;
import java.util.*;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

/**
 * The main Game class that controls the flow and state of the Citadels game.
 * It manages players, characters, districts, and all game mechanics.
 */
public class Game{
    private final Map<String, Card> cardDefinitions;
    private final List<Characters> charDefinitions = Arrays.asList(Characters.values());
    private List<Player> players = new ArrayList<>();
    private Deck deckManager;
    private List<Card> discardedPile = new ArrayList<>();
    private List<Characters> CharDeck;
    private List<Characters> DiscardedChar = new ArrayList<>();
    private List<Characters> DeadChar = new ArrayList<>();
    private Map<Characters, String> rob = new HashMap<>(); //<whomTOrob, "T_thiefId">
    private Map<Integer, Player> PlayerChar = new HashMap<>();
    private boolean debugMode = false, KingCard = false;
    private Player currentPlayer;
    private int round, playerCount, CrownedPlayer = 0, completeCity = 8, currentCharacterRank = 0;
    private Random r = new Random();
    private Scanner sc=new Scanner(System.in);
    private boolean gameEnded = false;
    private Deck deck;

    /**
     * Constructs a new Game instance with the specified card definitions.
     * @param cardDefinitions A map containing all available district cards and their properties
     */
    public Game(Map<String, Card> cardDefinitions){
        this.cardDefinitions = cardDefinitions;
        this.deckManager = new Deck(cardDefinitions);
        round = 0;
    }

    /**
     * Initializes and begins the game with the specified number of players.
     * Sets up initial player states, including gold and cards.
     * @param pC The number of players in the game
     */
    public void begin(int pC){
        playerCount = pC;
        deckManager.shuffleDeck();
        if(round==0){
            for(int i=0;i<playerCount;i++){
                Player x = new Player(i+1);
                x.addGold(2);
                x.drawInitialCards(deckManager.drawCards(4));
                players.add(x);
            }
        }
        selectionPhase();
    }

    /**
     * Manages the character selection phase of the game.
     * Handles the removal of face-up and face-down characters and character selection process.
     */
    public void selectionPhase(){
        if(playerCount==0){
            playerCount = players.size();
        }
        CrownedPlayer = (PlayerChar.get(4)==null)?r.nextInt(playerCount)+1:(PlayerChar.get(4).getId());
        CharDeck = Characters.getCharacters();

        System.out.println("Player "+CrownedPlayer+" is the crowned player and goes first.");
        System.out.println("Press t to process turns.");
        System.out.println("================================\nSELECTION PHASE\n================================");
        System.out.print("> ");
        Characters.shuffleDeck();
        int faceUp = playerCount==4?2:(playerCount==5?1:0);
        
        String inp = sc.nextLine();
        while(!inp.equals("t")){
            System.out.print("Please enter \'t\' to continue.\n> ");
            inp = sc.nextLine();
        }

        //removing 1 card facedown
        int remove = r.nextInt(CharDeck.size());
        DiscardedChar.add(CharDeck.get(remove));
        CharDeck.remove(remove); //removes one random character
        System.out.println("A mystery character was removed.");

        //removing 0/1/2 cards face up
        while(faceUp>0){
            remove = r.nextInt(CharDeck.size());
            if(CharDeck.get(remove).getRank()==4){
                System.out.println("King was removed");
                System.out.println("The king cannot be vissibly removed, trying again..");
            }
            else{
                System.out.println(Characters.getName(CharDeck.get(remove))+" was removed.");
                DiscardedChar.add(CharDeck.get(remove));
                CharDeck.remove(remove); //removes one random character
                faceUp--;
            }
        }
        //choosing char card
        chooseCharCard(CrownedPlayer);
        System.out.println("Character choosing is over, action round will begin.");
    }

    /**
     * Handles the character selection process for each player.
     * @param turn The ID of the player whose turn it is to choose a character
     */
    public void chooseCharCard(int turn){
        String inp;
        int chosen, i,count=0;
        
        while(count<playerCount){ 
            if(turn==7 && CharDeck.size()==1){
                CharDeck.addAll(DiscardedChar);
            }

            if(turn==1){
                System.out.println("Choose your character. Available characters: ");
                i=0;
                for(Characters c:CharDeck){
                    System.out.print(Characters.getName(c)+(i==(CharDeck.size()-1)?"":", "));
                    i++;
                }
                System.out.print("\n> ");
                inp = sc.nextLine().trim();
                while(!CharDeck.contains(Characters.fromName(inp))){
                    System.out.print("Please choose a valid character from the above mentioned characters.\n> ");
                    inp = sc.nextLine().trim();
                }
                players.get(0).setCharCard(Characters.getName(Characters.fromName(inp)));
                CharDeck.remove(Characters.fromName(inp));

                PlayerChar.put(Characters.fromName(inp).getRank(), players.get(0));

                System.out.println("Player "+turn+" chose a character.");
                System.out.print("> ");
                inp = sc.nextLine().trim();
                while(!(inp.equals("t")) && (turn!=1)){
                    System.out.println("It is not your turn. Press t to continue with other players turns.");
                    System.out.print("> ");
                    inp = sc.nextLine().trim();
                }    
            }
            else{
                chosen = r.nextInt(CharDeck.size());
                players.get(turn-1).setCharCard(Characters.getName(CharDeck.get(chosen)));
                PlayerChar.put(CharDeck.get(chosen).getRank(), players.get(turn-1));
                CharDeck.remove(chosen);

                System.out.println("Player "+turn+" chose a character.");
                System.out.print("> ");
                inp = sc.nextLine().trim();
                while(!(inp.equals("t")) && (turn!=1)){
                    System.out.println("It is not your turn. Press t to continue with other players turns.");
                    System.out.print("> ");
                    inp = sc.nextLine().trim();
                }    
            }
            count++;
            turn = (turn==playerCount)?1:turn+1;
        }

        if(!CharDeck.isEmpty()){
            DiscardedChar.addAll(CharDeck);
            CharDeck.clear();
        }
    }

    /**
     * Executes the special ability of a character.
     * @param c The character whose ability is being used
     * @param bot Whether the action is being performed by an AI player
     * @param p The player performing the action
     */
    public void charAction(Characters c, boolean bot, Player p){
        int inp;
        char inp1;
        switch(c.getRank()){
            case 1: //assassin
                if(!bot){
                    System.out.print("Who do you want to kill? Choose a  character from 2-8:\n> ");
                }
                inp = bot?(r.nextInt(7)+2):Integer.parseInt(sc.nextLine());
                while(inp==p.getId() && bot){
                    inp = r.nextInt(7)+2;   
                }
                if(inp>1 && inp<9){
                    System.out.println((bot?"Player "+p.getId():"You")+" chose to kill the "+Characters.getName(Characters.fromRank(inp)));
                }
                else{
                    System.out.println("Invalid character chosen.");
                }
                DeadChar.add(Characters.fromRank(inp)); 
                p.setIsCharDead(true);
                break;

            case 2: //thief
                if(!bot){
                    System.out.print("Who do you want to steal from? Choose a character from 3-8:\n> ");
                }
                inp = bot?(r.nextInt(6)+3):Integer.parseInt(sc.nextLine());
                while(inp==p.getId() && bot){
                    inp = r.nextInt(6)+3;   
                }
                if(inp>2 && inp<9){
                    System.out.println((bot?"Player "+p.getId():"You")+" chose to steal from the "+Characters.getName(Characters.fromRank(inp)));
                }
                else{
                    System.out.println("Invalid character chosen.");
                }
                //take gold when that chars turn comes
                if(DeadChar.contains(Characters.fromRank(inp))){
                    System.out.println("You cannot steal from killed character.");
                }
                else{
                    rob.put(Characters.fromRank(inp), "T_"+p.getId()); //whome to steal from
                }
                break;

            case 3: //magician
                if(rob.containsKey(c)){
                    if(rob.get(c).charAt(0)==('T')){
                        System.out.println("The Thief steals "+p.getGold()+" gold from the Magician (Player "+p.getId()+")");
                        players.get(Character.getNumericValue(rob.get(c).charAt(2))).addGold(p.getGold());
                        p.addGold(p.getGold()*-1);
                    }
                }
                if(!bot){
                    System.out.println("Choose:\n(a)Exchange your hand from another player?");
                    System.out.print("(b)Discard any number of cards and redraw equal number of cards?\nChoose option[a/b]\n> ");
                }
                inp1 = bot?(r.nextBoolean()?'a':'b'):sc.nextLine().charAt(0);
                if(inp1=='a'){ //swapping hands
                    if(!bot){
                        System.out.print("Who do you want to exchange your hand from? Choose a player from 2-"+playerCount+":\n> ");
                    }
                    inp = bot?(r.nextInt(playerCount-1)+2):Integer.parseInt(sc.nextLine());
                    while(bot && inp==p.getId()){
                        inp = r.nextInt(playerCount)+1;
                    }
                    System.out.println((bot?"Player "+p.getId():"You")+" chose to swap hands with Player "+inp);
                    List <Card> temp = p.getHand(); //swapping hands
                    p.setHand(players.get(inp-1).getHand(), true);
                    players.get(inp-1).setHand(temp, true);
                }
                else if(inp1=='b'){ //redraw cards
                    if(!bot){
                        System.out.print("How many district cards do you to redraw? Choose a number between 1-"+p.getHand().size()+":\n> ");
                    }
                    inp = bot?(r.nextInt(p.getHand().size())+1):Integer.parseInt(sc.nextLine());
                    System.out.println((bot?"Player "+p.getId():"You")+" chose to redraw "+inp+" district cards");
                    List <Card> temp = p.getHand().subList(0, inp); //drawn from hand
                    deckManager.addToDeck(temp);
                    temp.clear();
                    p.setHand(deckManager.drawCards(inp), false);
                }
                else{
                    System.out.println("Invalid option.");
                }
                break;

            case 4: //king
                if(rob.containsKey(c)){
                    if(rob.get(c).charAt(0)==('T')){
                        System.out.println("The Thief steals "+p.getGold()+" gold from the King (Player "+p.getId()+")");
                        players.get(Character.getNumericValue(rob.get(c).charAt(2))).addGold(p.getGold());
                        p.addGold((p.getGold()*-1));
                    }
                }
                CrownedPlayer = p.getId();
                KingCard = true;
                break;
        
            case 5: //bishop
                if(rob.containsKey(c)){
                    if(rob.get(c).charAt(0)==('T')){
                        System.out.println("The Thief steals "+p.getGold()+" gold from the Bishop (Player "+p.getId()+")");
                        players.get(Character.getNumericValue(rob.get(c).charAt(2))).addGold(p.getGold());
                        p.addGold((p.getGold()*-1));
                    }
                }
                break;
            
            case 6: //merchant
                if(rob.containsKey(c)){
                    if(rob.get(c).charAt(0)==('T')){
                        System.out.println("The Thief steals "+p.getGold()+" gold from the Merchant (Player "+p.getId()+")");
                        players.get(Character.getNumericValue(rob.get(c).charAt(2))).addGold(p.getGold());
                        p.addGold((p.getGold()*-1));
                    }
                }
                break;

            case 7: //architect
                if(rob.containsKey(c)){
                    if(rob.get(c).charAt(0)==('T')){
                        System.out.println("The Thief steals "+p.getGold()+" gold from the Architect (Player "+p.getId()+")");
                        players.get(Character.getNumericValue(rob.get(c).charAt(2))).addGold(p.getGold());
                        p.addGold((p.getGold()*-1));
                    }
                }
                break;

            case 8: //warlord 
                if(rob.containsKey(c)){
                    if(rob.get(c).charAt(0)==('T')){
                        System.out.println("The Thief steals "+p.getGold()+" gold from the Warlord (Player "+p.getId()+")");
                        players.get(Character.getNumericValue(rob.get(c).charAt(2))).addGold(p.getGold());
                        p.addGold((p.getGold()*-1));
                    }
                }
                if(!bot){
                    System.out.print("Do you want to destroy a district? (y/n)\n> ");
                }
                inp = bot?(r.nextBoolean()?'y':'n'):sc.nextLine().charAt(0);
                if(inp=='n'){
                    return;
                }
                if(!bot){
                    System.out.println("You can destroy one district of your choice by paying one fewer gold than its building cost. You cannot destroy a district in city with 8 or more districts");
                    System.out.print("Choose a player (from 2-"+playerCount+") to destroy one of their district:\n> ");
                }
                inp = bot?(r.nextInt(playerCount)+1):Integer.parseInt(sc.nextLine()); //player
                while(inp==p.getId() && bot){
                    System.out.print(inp+", ");
                    inp = r.nextInt(playerCount)+1;
                }
                Player p1 = players.get(inp-1);
                if(p1.getCharCard().equalsIgnoreCase("Bishop") && p1.getIsCharDead()==false){
                    System.out.println("Buildings of Bishop cannot be destroyed while Bishop is alive.");
                }
                else if(p1.getCity().isEmpty()){
                    System.out.println("Player "+p.getId()+" has no city to destroy.");
                }
                else{
                    int i=1;
                    System.out.println("Player "+p1.getId()+" has built:");
                    for(Card cc:p1.getCity()){
                        System.out.println((i++)+". "+cc.getName()+" ("+cc.getColor()+"), cost to destroy: "+(cc.getCost()-1));
                    }

                    if(!bot){
                        System.out.print("Choose index of the district (from 1-"+(p1.getCity().size()-round)+") you want to destroy:\n> ");
                    }
                    inp = bot?(r.nextInt(p1.getCity().size())+1):Integer.parseInt(sc.nextLine()); //district index
                    while(p1.getCity().get(inp-1).getName().equals("Round")){
                        inp = r.nextInt(p1.getCity().size())+1; //card index 1, 2, ...
                    }
                    if(p.getGold()<(p1.getCity().get(inp-1).getCost()-1)){
                        System.out.println((bot?"Player "+p.getId()+" does ":"You do ")+"not have enough gold to destroy the district.");
                    }
                    else{
                        System.out.println((bot?"Player "+p.getId():"You")+" destroyed "+p1.getCity().get(inp-1)+" of Player "+p1.getId());
                        p.addGold(-1*(p1.getCity().get(inp-1).getCost()-1)); //subtract gold
                        p1.getCity().remove(inp-1);
                    }
                }
                break;
        }
    }

    /**
     * Processes the end of a round and checks for game completion.
     * @return true if the game is over, false otherwise
     */
    public boolean processesRound(){ 
        round++;
        for(Player p:players){
            p.addToCity(new Card("Round", "black", "", round,round));
        }

        //reset for each round
        PlayerChar.clear();
        DiscardedChar.clear();
        DeadChar.clear();
        rob.clear();
        for(Player p:players){
            p.setCharCard(null);
            p.setIsCharDead(false);
        }
        System.out.println("Everyone is done, new round!");
        for(Player p:players){
            if((p.getCity().size()-round)>=completeCity){
                return isGameOver();
            }
        }
        return false;
    }

    /**
     * Calculates final scores and determines the winner.
     * Considers district points, color bonuses, and completion bonuses.
     * @return true when scoring is complete
     */
    public boolean isGameOver(){
        List<String> colors = Arrays.asList("red", "yellow", "blue", "green", "purple");
        List<String> all_type;
        List<Player> result = new ArrayList<>();
        int score;
        for(Player p:players){   
            score = 0;
            all_type = new ArrayList<>();
            System.out.println("Calculating score of Player "+p.getId()+(p.getId()==1?" (You)":"")+":");

            for(Card c:p.getCity()){
                if(!c.getName().equalsIgnoreCase("round")){
                    score += c.getCost();
                    all_type.add(c.getColor());
                }
            }
            System.out.println("Total cost of each building -------- "+score+" points");
            if(all_type.containsAll(colors)){
                score += 3;                                     
                System.out.println("City contains atleast one district of each type -------- 3 points");
            }
            if((p.getCity().size()-round)>=8)
            {    
                if(result.size()==1){
                    score += 4;
                    System.out.println("First player to completed their city -------- 4 points");
                }
                else{
                    score += 2;
                    System.out.println("Player completed their city -------- +2");
                }    
            }
            p.setScore(score);
            result.add(p);
            System.out.println("Total score = "+p.getScore());
            System.out.println("----------------------------------------------------------------");
        }

        result.sort((p1, p2) -> Integer.compare(p2.getScore(), p1.getScore()));
        for(int i=0;i<result.size();i++){ //sorting score if there is a tie
            if(i!=result.size()-1){
                if(result.get(i).getScore()==result.get(i+1).getScore()){
                    if(result.get(i).getCharRank()>result.get(i+1).getCharRank()){
                        continue;
                    }
                    else{
                        Collections.swap(result, i, i+1);
                    }
                }
            }
        }
        System.out.println("\n=========== Final Rankings ===========");
        int rank = 1;
        for(Player p : players){
            System.out.println(rank + ". Player " + p.getId() + (p.getId() == 1 ? " (You)" : "") + " - Score: " + p.getScore());    
            rank++;
        }
        return true;
    }

    /**
     * Executes a player's turn action (collecting gold or drawing cards).
     * @param playerID The ID of the player taking the turn
     * @param opt The chosen action ("gold" or "cards")
     */
    public void playerTurn(int playerID, String opt){
        Player p = players.get(playerID-1);
        if(opt.equals("gold")){
            p.addGold(2); 
            System.out.println("Player "+p.getId()+(p.getId()==1?" (You)":"")+" recieved 2 gold."); 
        }
        else if(opt.equals("cards")){
            List<Card> drawnCards = deckManager.drawCards(2);   
            if(playerID==1){
                System.out.println("Pick one of the following cards: \'collect card <option>\'.");
                System.out.print("1. "+drawnCards.get(0).getCard()+"\n2. "+drawnCards.get(1).getCard()+"\n> ");
            }
            String inp = p.getId()==1?sc.nextLine():Integer.toString(r.nextInt(2)+1);        
            System.out.println((p.getId()==1?"You":"Player "+p.getId())+" chose "+drawnCards.get(Character.getNumericValue(inp.charAt(inp.length()-1))-1)+(p.getId()!=1?"from the two drawn cards.":""));

            p.addToHand(drawnCards.get(Character.getNumericValue(inp.charAt(inp.length()-1))-1));
        }

        //bot code below
        if(this.debugMode && p.getId()!=1){ 
            System.out.print("Debug: ");
            for(int i=0;i<p.getHand().size();i++){
                System.out.print(p.getHand().get(i));
                if(i<(p.getHand().size()-1)){
                    System.out.print(", ");
                }
            }
            System.out.println();
        }
        //auto build
        if(p.getId()!=1){
            if(p.getCharCard().equalsIgnoreCase("Architect")){
                p.tryAutoBuild(p.getId(), 3);
            }
            else{
                p.tryAutoBuild(p.getId(), 1);
            }
            if(p.getCharRank()>3 && p.getCharRank()<7){
                performAction(null, p);
            }
        }
    }

    /**
     * Displays the current cards in the first player's hand.
     */
    public void displayHand(){
        players.get(0).printHand();
    }

    /**
     * Displays the amount of gold the first player has.
     */
    public void displayGold(){
        players.get(0).printGold();
    }

    /**
     * Displays the districts built in a player's city.
     * @param id The ID of the player whose city to display
     */
    public void displayCity(String id){
        try{
            int ID = Integer.parseInt(id);
            if(ID<1 || ID>players.size()){        
                if(players.get(ID-1).getCity().isEmpty()){
                    System.out.println("Player "+id+(id.equals("1")?" (You) ":"")+" have no district built in their city.");
                }
                else{
                    players.get(ID-1).printCity();
                }
            }
            else{
                System.out.println("Error: invalid player ID.");
            }
        } catch (NumberFormatException e) {
            System.out.println("Error: invalid player ID.");
        }
    }
    
    /**
     * Attempts to build a district from the player's hand.
     * @param index The index of the card in the player's hand to build
     */
    public void buildDistrict(String index){
        try{
            int i = Integer.parseInt(index)-1;
            players.get(0).buildDistrict(i,1);
        }
        catch(Exception e){
            System.out.println("Error: invalid card index.");
        }
    }

    /**
     * Executes a character's special action.
     * @param inp Command parameters for the action
     * @param p The player performing the action
     */
    public void performAction(String[] inp, Player p){
        Characters c = Characters.fromName(p.getCharCard());
        int g=0;
        boolean bot = p.getId()!=1;
        switch(c.getRank()){
            case 1: //assassin
                charAction(c, bot, p);
                break;
            case 2: //thief
                charAction(c, bot, p);
                break;
            case 3: //magician
                charAction(c, bot, p);
                break;
            case 4: //king
                for(Card cc:p.getCity()){
                    if(cc.getColor().equals("yellow"))
                        g++;
                }
                if(g>0){
                    System.out.println("Player "+p.getId()+" collected "+g+" from king action.");
                }
                break;

            case 5: //bishop
                for(Card cc:p.getCity()){
                    if(cc.getColor().equals("blue"))
                        g++;
                }
                if(g>0){
                    System.out.println("Player "+p.getId()+" collected "+g+" from bishop action.");
                }
                break;

            case 6: //merchant
                for(Card cc:p.getCity()){
                    if(cc.getColor().equals("green"))
                        g++;
                }
                g++;
                if(g>0){
                    System.out.println("Player "+p.getId()+" collected "+g+" from merchant action.");
                }
                break;
            case 8: //warlord
                charAction(c, bot, p);
                break;

            default:
                System.out.println("Invalid action.");
                break;
        }
    }

    /**
     * Displays available game commands and their descriptions.
     */
    public void help(){
        System.out.println("Available commands:");
        System.out.println("info : show information about a character or building");
        System.out.println("t : processes turns");
        System.out.println("all : shows all current game info");
        System.out.println("citadel/list/city : shows districts built by a player");
        System.out.println("hand : shows cards in hand");
        System.out.println("gold: shows amount of gold you have");
        System.out.println("build <place in hand> : Builds a building into your city");
        System.out.println("action : Gives info about your special action and how to perform it");
        System.out.println("end : Ends your turn");
    }

    /**
     * Displays information about a character or district card.
     * @param inp The name or index of the character/card to show info about
     */
    public void showInfo(String inp){
        try{  //for purple cards
            int index = Integer.parseInt(inp);
            if(index<=players.get(0).getHand().size()){
                Card c = players.get(0).getHand().get(index-1);
                if(cardDefinitions.containsKey(c.getName()) && c.getColor().equalsIgnoreCase("purple")){
                    System.out.println(c.getName()+": "+c.getAbility());
                }
                else{
                    System.out.println("No purple card found at index "+index+".");
                }
            }
            else{
                System.out.println("Invalid card index.");
            }
        }
        catch (NumberFormatException e){ //for characters
            if(charDefinitions.contains(Characters.fromName(inp))){
                for(Characters c:charDefinitions){
                    if(Characters.getName(c).equalsIgnoreCase(inp)){
                        System.out.println(c);
                        return;
                    }
                }
            }
            else{
                System.out.println("Invalid character name.");
            }
        }
    }

    /**
     * Ends the current player's turn.
     */
    public void endTurn(){
        System.out.println("You ended your turn.");
    }

    /**
     * Displays a summary of all players' current game states.
     */
    public void displayAll(){
        for(Player p:players){
            p.printSummary();
        }
    }

    /**
     * Saves the current game state to a file.
     * @param filename The name of the file to save to
     * @param currentCharacacterRank The current character's rank being played
     */
    @SuppressWarnings("unchecked")
    public void saveGame(String filename, int currentCharacacterRank) {
        filename = "saves/" + filename.trim() + ".json";
        File file = new File(filename);

        try (FileWriter writer = new FileWriter(file)) {
            GameState state = new GameState(playerCount, cardDefinitions, round, CrownedPlayer, currentCharacacterRank);
            JSONObject json = new JSONObject();

            JSONArray array_players = new JSONArray();
            for(Player p : players){
                array_players.add(p.toJson());
            }
            json.put("players", array_players);

            if(!rob.isEmpty()){
                JSONArray array_rob = new JSONArray();
                for(Characters c : rob.keySet()){
                    JSONObject obj = new JSONObject();
                    obj.put("whomToRobRank", c.getRank());
                    obj.put("thief", rob.get(c));
                    array_rob.add(obj);
                }
                json.put("robMap", array_rob);
            }

            if(!DeadChar.isEmpty()){
                JSONArray array_dead = new JSONArray();
                for(Characters c : DeadChar){
                    JSONObject obj = new JSONObject();
                    obj.put("deadCharRank", c.getRank());
                    array_dead.add(obj);
                }
                json.put("deadCharList", array_dead);
            }

            if(!DiscardedChar.isEmpty()){
                JSONArray array_discarded = new JSONArray();
                for(Characters c : DiscardedChar){
                    JSONObject obj = new JSONObject();
                    obj.put("discardedCharRank", c.getRank());
                    array_discarded.add(obj);
                }
                json.put("discardedCharList", array_discarded);
            }
            
            json.put("kingCard", KingCard);

            // adding things to json from gamestate
            json.put("currentCharacterRank", state.getCurrentCharacterRank());
            json.put("deck", state.getDeck().toJson());
            json.put("round", state.getRound());
            json.put("crownedPlayer", state.getCrownHolder());
            json.put("playerCount", state.getPlayerCount());
            json.put("debugMode", debugMode);
            writer.write(json.toJSONString());

        } catch (IOException e) {
            System.out.println("Failed to save game: " + e.getMessage());
        }
    }

    /**
     * Loads a game state from a file.
     * @param filename The name of the file to load from
     * @param cardDefinitions The card definitions to use for the loaded game
     * @return The loaded Game instance
     */
    public Game loadGame(String filename, Map<String, Card> cardDefinitions ) {
        filename = "saves/" + filename.trim()+".json";
        Game game = new Game(cardDefinitions);
        try {

            JSONParser parser = new JSONParser();
            JSONObject json = (JSONObject) parser.parse(new FileReader(filename));
            
            // loading currentCharacterRank
            game.currentCharacterRank = ((Long) json.get("currentCharacterRank")).intValue();
            currentCharacterRank = game.currentCharacterRank; // Set the current character rank

            //loading crowned player
            game.CrownedPlayer = ((Long) json.get("crownedPlayer")).intValue();
            CrownedPlayer = game.CrownedPlayer; // Set the crowned player

            // Check players
            JSONArray jsonPlayers = (JSONArray) json.get("players");
            game.players.clear();
            for (Object o : jsonPlayers) {
                game.players.add(Player.fromJson((JSONObject) o));
            }
            players = game.players; // Set the players list
            
            //loading kingCard
            game.KingCard = (Boolean) json.get("kingCard");
            KingCard = game.KingCard; // Set the king card status
            
            //loading playerChar map
            game.PlayerChar.clear();
            for(Player p:game.players){
                Characters c = Characters.fromName(p.getCharCard());
                game.PlayerChar.put(c.getRank(), p);
            }
            PlayerChar = game.PlayerChar; // Set the player-character map

            // loading dead characters
            JSONArray dead = (JSONArray) json.get("deadCharList");
            game.DeadChar.clear();
            if(dead != null) {
                for(Object o : dead) {
                    JSONObject obj = (JSONObject) o;
                    int deadCharRank = ((Long) obj.get("deadCharRank")).intValue();
                    game.DeadChar.add(Characters.fromRank(deadCharRank));
                }
                DeadChar = game.DeadChar; // Set the dead characters list
            }

            // loading discarded characters
            JSONArray discarded = (JSONArray) json.get("discardedCharList");
            game.DiscardedChar.clear();
            if(discarded!=null){    
                for(Object o : discarded) {
                    JSONObject obj = (JSONObject) o;
                    int discardedCharRank = ((Long) obj.get("discardedCharRank")).intValue();
                    game.DiscardedChar.add(Characters.fromRank(discardedCharRank));
                }
                DiscardedChar = game.DiscardedChar; // Set the discarded characters list
            }

            // loading deck
            game.deckManager = Deck.fromJson((JSONObject) json.get("deck"));
            deckManager = game.deckManager; // Set the deck manager
            
            // loading round number
            game.round = ((Long) json.get("round")).intValue();
            round = game.round; // Set the round number

            // Check player count   
            game.playerCount = ((Long) json.get("playerCount")).intValue();
            playerCount = game.playerCount; // Set the player count

            //loading rob map
            JSONArray robInfo = (JSONArray) json.get("robMap");
            if(robInfo != null) {        
                for(Object s:robInfo){
                    JSONObject obj = (JSONObject) s;
                    int whomToRobRank = ((Long) obj.get("whomToRobRank")).intValue();
                    String thief = (String) obj.get("thief");
                    game.rob.put(Characters.fromRank(whomToRobRank), thief);
                }
                rob = game.rob; // Set the rob map
            }

            // Check debug mode
            game.debugMode = (Boolean) json.get("debugMode");
            debugMode = game.debugMode; // Set the debug mode

            return game;
        } catch (Exception e) {
            System.err.println("Error loading game: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Converts a JSON array to a map of player characters.
     * @param array The JSON array to convert
     * @return A map of character ranks to players
     */
    private Map<Integer, Player> jsonToMap(JSONArray array) {
        Map<Integer, Player> map = new HashMap<>();
        for (Object o : array) {
            JSONObject obj = (JSONObject) o;
            Integer key = ((Long) obj.get("key")).intValue();  // JSON uses Long
            JSONObject playerJson = (JSONObject) obj.get("player");
            Player player = Player.fromJson(playerJson);
            map.put(key, player);
        }
        return map;
    }

    /**
     * Sets the debug mode for the game.
     * @param enabled Whether debug mode should be enabled
     */
    public void setDebugMode(boolean enabled){
        debugMode = enabled;
    }

    /**
     * Gets the list of discarded characters.
     * @return List of discarded characters
     */
    public List<Characters> getDiscardedCharacters(){
        return DiscardedChar;
    }
    
    /**
     * Gets the mapping of character ranks to players.
     * @return Map of character ranks to players
     */
    public Map<Integer, Player> getPlayerCharMap(){
        return PlayerChar;
    }

    /**
     * Executes a character's ability for a specific player.
     * @param c The character whose ability to execute
     * @param p The player performing the ability
     */
    public void performCharacterAction(Characters c, Player p){
        charAction(c, p.getId()!=1,p); // bot = false if player 1
    }

    /**
     * Gets the list of all players in the game.
     * @return List of players
     */
    public List<Player> getPlayers(){
        return this.players; 
    }

    /**
     * Sets the game's ended state.
     * @param b Whether the game has ended
     */
    public void setGameEnded(boolean b){
        gameEnded = b;
    }

    /**
     * Sets the current round number.
     * @param i The round number to set
     */
    public void setRound(int i) {
        round = i;
    }

    /**
     * Gets the rank of the current character being played.
     * @return The current character's rank
     */
    public int getCurrentCharacterRank() {
        return currentCharacterRank;
    }

    /**
     * Gets the list of characters that have been killed.
     * @return List of dead characters
     */
    public List<Characters> getDeadCharacters() {
        return DeadChar;
    }
}