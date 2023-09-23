import java.io.*;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.lang.SecurityException;

public class Main {

    private static int currentPlayerID = -1;
    private static int currentPartyID = -1;
    private static String pathToLog;
    private static String EnvironmentPath;

    private static int GamesPlayed = 0;

    private static ArrayList<String> MapPlayedSoFarInOrder = new ArrayList<String>();

    private static long lastKnownLength = 0;
    private static long LineCount = 0;

    public static void main(String[] args) throws IOException {

        //Attempt to get the %AppData%
        try {
            EnvironmentPath = System.getenv("AppData");
        } catch (SecurityException | NullPointerException e) {
            System.out.println("The environment path cannot be found");
        }

        //This goes back one folder, as %Appdata% brings you to roaming first,
        //so we go back one folder before finishing the path
        pathToLog = EnvironmentPath + "\\..\\LocalLow\\Mediatonic\\FallGuys_client\\Player.log";

        //Find the file and begin the file reader.
        File file = new File(pathToLog);
        FileReader fr = null;
        try {
            fr = new FileReader(file);
        } catch (FileNotFoundException e) {
            System.out.println("The file has not been found!");
        }

        //Open buffered reader to read the information inside the file.
        BufferedReader br = new BufferedReader(fr);

        String currentLine;

        int roundInfoCounter = 19;
        Boolean printRoundInfo = false;


        AtomicBoolean keepRunning = new AtomicBoolean(true);

        Scanner scanner = new Scanner(System.in);
        // Create a single-threaded executor service
        ExecutorService executorService = Executors.newSingleThreadExecutor();

        while (keepRunning.get()) {
            // Execute the input checking task in the background
            executorService.execute(() -> {

                if (scanner.hasNextLine()) {
                    String input = scanner.nextLine();

                    if (input.equalsIgnoreCase("exit")) {
                        keepRunning.set(false);
                        System.out.println("No longer will be running");
                    }

                    System.out.println("You entered: " + input);
                }
            });


            long fileLength = new File(pathToLog).length();

            if (fileLength > lastKnownLength) {

                //Go over each line in the file and print out everything.
                while ((currentLine = br.readLine()) != null) {
                    ++LineCount;
                    if (printRoundInfo && roundInfoCounter > 0) {
                        System.out.println(currentLine);
                        roundInfoCounter--;
                    } else if (printRoundInfo && roundInfoCounter <= 0) {
                        printRoundInfo = false;
                        roundInfoCounter = 19;

                    }

                    if (currentLine.contains("== [CompletedEpisodeDto] ==")) {
                        GamesPlayed++;
                    }

                    if (currentLine.contains("[CreateLocalPlayerInstances] Added new player as Participant, player ID = ")) {
                        String[] splitParts = currentLine.split("player ID =");
                        System.out.println(splitParts.length);
                        currentPlayerID = Integer.parseInt(splitParts[1].trim());
                    }


                    //We need to parse the data to find the important information:
                    if (currentLine.contains("[Round")) {
                        --roundInfoCounter;
                        printRoundInfo = true;
                        System.out.println(currentLine);
                    }


                    // System.out.println(currentLine);
                }
                lastKnownLength = fileLength;
            }

            try {
                //Sleep for 2 second so we check for if the file updated every 2 seconds
                Thread.sleep(2000);
            } catch (IllegalArgumentException | InterruptedException e) {
                System.out.println(e.getMessage());
            }
            System.out.println("Rechecking the file now!");
        }

        System.out.println("The file size in mb: " + String.format("%.2f", new File(pathToLog).length() / (1.049 * Math.pow(10, 6))));
        System.out.println("The total amount of lines we read: " + LineCount);
        System.out.println("The total amount of games played this session is: " + GamesPlayed);
        System.out.println("The most recent playerid is : " + currentPlayerID);


        //Close the buffered reader and file reader after you finish using it.
        br.close();
        fr.close();
        executorService.shutdown();

        //idk why it doesnt exit without this line loll and im to tired to debug.
        System.exit(0);
    }

    public static void OpenFile(String pathToFile) {


    }

    public static void UpdatePlayerId() {

    }


}

class Match {
    ArrayList<Round> rounds = new ArrayList<>();

    boolean matchFinished = false;
    boolean matchWon = false;

    int numOfRounds;

    public Match() {

    }


}

class Round {

    boolean FinalRound = false;
    boolean earlyFinalRound = false;
    boolean qualified = false;


    int kudosEarnedThisRound = 0;
    int famedEarnedThisRound = 0;
    int bonusFameEarned = 0;
    int bonusKudosEarned = 0;
    int bonusTier = 0;
    int positionPlaced = 0;
    int teamScore = 0;


    //this should be represented as time but for now do this.
    long DurationOfRound;

    public Round() {

    }

}

class GameSessionStats {

    int wins = 0;
    int losses = 0;
    List<List<Match>> matchStreaks;
    private int currentStreak;
    private int highestStreak;
    private long priorStreakSize;

    //Again this should be a time value but will change this later.
    private long durationOfGameSession;

    public GameSessionStats() {

    }

}

class LevelStats {

    public enum LevelType {
        Unknown,
        CreativeRace,
        CreativeSurvival,
        CreativeHunt,
        CreativeLogic,
        CreativeTeam,
        Race,
        Survival,
        Hunt,
        Logic,
        Team,
        Invisibeans,
        Final
    }

    String id;
    String name;
    int qualified;
    int gold;
    int silver;
    int bronze;
    int played;
    int kudos;
    boolean isCreative;
    boolean isFinal;
    int timeLimitInSeconds;
    int timeLimitInSecondsForSquad;

    int season;
    int finishedCount;

    //Need to track the durations and times here.

    ArrayList<Round> stats;

    LevelType type;

    public static Map<String, LevelStats> ALLMAPS = new HashMap<>();

    static {
        ALLMAPS.put("user_creative_race_round", new LevelStats("user_creative_race_round", "User Creative Race Round", LevelType.CreativeRace, true, false, 10, 0, 0));
        ALLMAPS.put("creative_race_round", new LevelStats("creative_race_round", "Creative Race Round", LevelType.Race, true, false, 10, 0, 0));
        ALLMAPS.put("creative_race_final_round", new LevelStats("creative_race_final_round", "Creative Race Final Round", LevelType.Race, true, true, 10, 0, 0));
        ALLMAPS.put("wle_s10_orig_round_001", new LevelStats("wle_s10_orig_round_001", "Beans Ahoy!", LevelType.Race, true, false, 10, 0, 0));
        ALLMAPS.put("wle_s10_orig_round_002", new LevelStats("wle_s10_orig_round_002", "Airborne Antics", LevelType.Race, true, false, 10, 0, 0));
        ALLMAPS.put("wle_s10_orig_round_003", new LevelStats("wle_s10_orig_round_003", "Scythes & Roundabouts", LevelType.Race, true, false, 10, 0, 0));
        ALLMAPS.put("wle_s10_orig_round_004", new LevelStats("wle_s10_orig_round_004", "Cardio Runners", LevelType.Race, true, false, 10, 0, 0));
        ALLMAPS.put("wle_s10_orig_round_005", new LevelStats("wle_s10_orig_round_005", "Fan Flingers", LevelType.Race, true, false, 10, 0, 0));
        ALLMAPS.put("wle_s10_orig_round_006", new LevelStats("wle_s10_orig_round_006", "Uphill Struggle", LevelType.Race, true, false, 10, 0, 0));
        ALLMAPS.put("wle_s10_orig_round_007", new LevelStats("wle_s10_orig_round_007", "Spinner Sprint", LevelType.Race, true, false, 10, 0, 0));
        ALLMAPS.put("wle_s10_orig_round_008", new LevelStats("wle_s10_orig_round_008", "Lane Changers", LevelType.Race, true, false, 10, 0, 0));
        ALLMAPS.put("wle_s10_orig_round_009", new LevelStats("wle_s10_orig_round_009", "Gentle Gauntlet", LevelType.Race, true, false, 10, 0, 0));
        ALLMAPS.put("wle_s10_orig_round_012", new LevelStats("wle_s10_orig_round_012", "Up & Down", LevelType.Race, true, false, 10, 0, 0));
        ALLMAPS.put("wle_s10_orig_round_013", new LevelStats("wle_s10_orig_round_013", "Choo Choo Challenge", LevelType.Race, true, false, 10, 0, 0));
        ALLMAPS.put("wle_s10_orig_round_014", new LevelStats("wle_s10_orig_round_014", "Runner Beans", LevelType.Race, true, false, 10, 0, 0));
        ALLMAPS.put("wle_s10_orig_round_015", new LevelStats("wle_s10_orig_round_015", "Disc Dashers", LevelType.Race, true, false, 10, 0, 0));
        ALLMAPS.put("wle_s10_orig_round_016", new LevelStats("wle_s10_orig_round_016", "Two Faced", LevelType.Race, true, false, 10, 0, 0));
        ALLMAPS.put("wle_s10_orig_round_019", new LevelStats("wle_s10_orig_round_019", "Blueberry Bombardment", LevelType.Race, true, false, 10, 0, 0));
        ALLMAPS.put("wle_s10_orig_round_020", new LevelStats("wle_s10_orig_round_020", "Chuting Stars", LevelType.Race, true, false, 10, 0, 0));
        ALLMAPS.put("wle_s10_orig_round_021", new LevelStats("wle_s10_orig_round_021", "Slimy Slopes", LevelType.Race, true, false, 10, 0, 0));
        ALLMAPS.put("wle_s10_orig_round_022", new LevelStats("wle_s10_orig_round_022", "Circuit Breakers", LevelType.Race, true, false, 10, 0, 0));
        ALLMAPS.put("wle_s10_orig_round_023", new LevelStats("wle_s10_orig_round_023", "Winding Walkways", LevelType.Race, true, false, 10, 0, 0));
        ALLMAPS.put("wle_s10_orig_round_026", new LevelStats("wle_s10_orig_round_026", "Hyperlink Hijinks", LevelType.Race, true, false, 10, 0, 0));
        ALLMAPS.put("wle_s10_orig_round_027", new LevelStats("wle_s10_orig_round_027", "Fan Frolics", LevelType.Race, true, false, 10, 0, 0));
        ALLMAPS.put("wle_s10_orig_round_028", new LevelStats("wle_s10_orig_round_028", "Windmill Road", LevelType.Race, true, false, 10, 0, 0));
        ALLMAPS.put("wle_s10_orig_round_029", new LevelStats("wle_s10_orig_round_029", "Conveyor Clash", LevelType.Race, true, false, 10, 0, 0));
        ALLMAPS.put("wle_s10_orig_round_032", new LevelStats("wle_s10_orig_round_032", "Fortress Frolics", LevelType.Race, true, false, 10, 0, 0));
        ALLMAPS.put("wle_s10_orig_round_033", new LevelStats("wle_s10_orig_round_033", "Super Door Dash", LevelType.Race, true, false, 10, 0, 0));
        ALLMAPS.put("wle_s10_orig_round_034", new LevelStats("wle_s10_orig_round_034", "Spiral Of Woo", LevelType.Race, true, false, 10, 0, 0));
        ALLMAPS.put("wle_s10_orig_round_035", new LevelStats("wle_s10_orig_round_035", "Tornado Trial", LevelType.Race, true, false, 10, 0, 0));
        ALLMAPS.put("wle_s10_orig_round_036", new LevelStats("wle_s10_orig_round_036", "Hopscotch Havoc", LevelType.Race, true, false, 10, 0, 0));
        ALLMAPS.put("wle_s10_orig_round_037", new LevelStats("wle_s10_orig_round_037", "Beat Bouncers", LevelType.Race, true, false, 10, 0, 0));
        ALLMAPS.put("wle_s10_orig_round_038", new LevelStats("wle_s10_orig_round_038", "Blunder Bridges", LevelType.Race, true, false, 10, 0, 0));
        ALLMAPS.put("wle_s10_orig_round_039", new LevelStats("wle_s10_orig_round_039", "Incline Rewind", LevelType.Race, true, false, 10, 0, 0));
        ALLMAPS.put("wle_s10_orig_round_040", new LevelStats("wle_s10_orig_round_040", "Prismatic Parade", LevelType.Race, true, false, 10, 0, 0));
        ALLMAPS.put("wle_s10_orig_round_041", new LevelStats("wle_s10_orig_round_041", "Swept Away", LevelType.Race, true, false, 10, 0, 0));
        ALLMAPS.put("wle_s10_orig_round_042", new LevelStats("wle_s10_orig_round_042", "Balancing Act", LevelType.Race, true, false, 10, 0, 0));
        ALLMAPS.put("wle_s10_orig_round_043", new LevelStats("wle_s10_orig_round_043", "Trouble Tower", LevelType.Race, true, false, 10, 0, 0));
        ALLMAPS.put("wle_s10_orig_round_044", new LevelStats("wle_s10_orig_round_044", "Serpent Slalom", LevelType.Race, true, false, 10, 0, 0));

        // ALLMAPS.put("wle_s10_orig_round_045", new LevelStats("wle_s10_orig_round_045", "Floorless", LevelType.Race, true, false, 10, 0, 0));
        // ALLMAPS.put("wle_s10_orig_round_046", new LevelStats("wle_s10_orig_round_046", "In The Cloud", LevelType.Race, true, false, 10, 0, 0));
        // ALLMAPS.put("wle_s10_orig_round_047", new LevelStats("wle_s10_orig_round_047", "Downstream Duel", LevelType.Race, true, false, 10, 0, 0));
        // ALLMAPS.put("wle_s10_orig_round_048", new LevelStats("wle_s10_orig_round_048", "Lost Palace", LevelType.Race, true, false, 10, 0, 0));

        ALLMAPS.put("wle_s10_orig_round_045_long", new LevelStats("wle_s10_orig_round_045_long", "Floorless", LevelType.Race, true, false, 10, 0, 0));
        ALLMAPS.put("wle_s10_long_round_003", new LevelStats("wle_s10_long_round_003", "Fall Speedway", LevelType.Race, true, false, 10, 0, 0));
        ALLMAPS.put("wle_s10_long_round_004", new LevelStats("wle_s10_long_round_004", "Zig Zag Zoomies", LevelType.Race, true, false, 10, 0, 0));
        ALLMAPS.put("wle_s10_long_round_005", new LevelStats("wle_s10_long_round_005", "Terrabyte Trial", LevelType.Race, true, false, 10, 0, 0));

        ALLMAPS.put("wle_s10_round_001", new LevelStats("wle_s10_round_001", "Digi Trek", LevelType.Race, true, false, 10, 0, 0));
        ALLMAPS.put("wle_s10_round_002", new LevelStats("wle_s10_round_002", "Shortcut Links", LevelType.Race, true, false, 10, 0, 0));
        ALLMAPS.put("wle_s10_round_003", new LevelStats("wle_s10_round_003", "Upload Heights", LevelType.Race, true, false, 10, 0, 0));
        ALLMAPS.put("wle_s10_round_005", new LevelStats("wle_s10_round_005", "Data Streams", LevelType.Race, true, false, 10, 0, 0));
        ALLMAPS.put("wle_s10_round_006", new LevelStats("wle_s10_round_006", "Gigabyte Gauntlet", LevelType.Race, true, false, 10, 0, 0));
        ALLMAPS.put("wle_s10_round_007", new LevelStats("wle_s10_round_007", "Cube Corruption", LevelType.Race, true, false, 10, 0, 0));
        ALLMAPS.put("wle_s10_round_008", new LevelStats("wle_s10_round_008", "Wham Bam Boom", LevelType.Race, true, false, 10, 0, 0));
        ALLMAPS.put("wle_s10_round_010", new LevelStats("wle_s10_round_010", "Pixel Hearts", LevelType.Race, true, false, 10, 0, 0));
        ALLMAPS.put("wle_s10_round_011", new LevelStats("wle_s10_round_011", "Cyber Circuit", LevelType.Race, true, false, 10, 0, 0));
        ALLMAPS.put("wle_s10_round_012", new LevelStats("wle_s10_round_012", "Boom Blaster Trial", LevelType.Race, true, false, 10, 0, 0));
        ALLMAPS.put("wle_s10_player_round_wk3_01", new LevelStats("wle_s10_player_round_wk3_01", "Cloudy Chaos", LevelType.Race, true, false, 10, 0, 0));
        ALLMAPS.put("wle_s10_player_round_wk3_02", new LevelStats("wle_s10_player_round_wk3_02", "Door Game", LevelType.Race, true, false, 10, 0, 0));
        ALLMAPS.put("wle_s10_player_round_wk3_03", new LevelStats("wle_s10_player_round_wk3_03", "Full Speed Sliding (FSS) - Jelly Road", LevelType.Race, true, false, 10, 0, 0));
        ALLMAPS.put("wle_s10_player_round_wk3_04", new LevelStats("wle_s10_player_round_wk3_04", "Sky High Run", LevelType.Race, true, false, 10, 0, 0));
        ALLMAPS.put("wle_s10_player_round_wk3_06", new LevelStats("wle_s10_player_round_wk3_06", "Spiral Upheaval", LevelType.Race, true, false, 10, 0, 0));
        ALLMAPS.put("wle_s10_player_round_wk3_07", new LevelStats("wle_s10_player_round_wk3_07", "Creative Descent", LevelType.Race, true, false, 10, 0, 0));
        ALLMAPS.put("wle_s10_player_round_wk3_08", new LevelStats("wle_s10_player_round_wk3_08", "Rainbow Slide", LevelType.Race, true, false, 10, 0, 0));
        ALLMAPS.put("wle_s10_player_round_wk3_09", new LevelStats("wle_s10_player_round_wk3_09", "Fragrant Trumpet", LevelType.Race, true, false, 10, 0, 0));
        ALLMAPS.put("wle_s10_player_round_wk3_10", new LevelStats("wle_s10_player_round_wk3_10", "Bridges That Don't Like You", LevelType.Race, true, false, 10, 0, 0));
        ALLMAPS.put("wle_s10_player_round_wk3_11", new LevelStats("wle_s10_player_round_wk3_11", "Rainbow Dash", LevelType.Race, true, false, 10, 0, 0));
        ALLMAPS.put("wle_s10_player_round_wk3_12", new LevelStats("wle_s10_player_round_wk3_12", "Variable Valley", LevelType.Race, true, false, 10, 0, 0));
        ALLMAPS.put("wle_fp2_wk6_01", new LevelStats("wle_fp2_wk6_01", "Broken Course", LevelType.Race, true, false, 10, 0, 0));
        ALLMAPS.put("wle_s10_player_round_wk3_14", new LevelStats("wle_s10_player_round_wk3_14", "Tower of Fall", LevelType.Race, true, false, 10, 0, 0));
        ALLMAPS.put("wle_s10_player_round_wk3_15", new LevelStats("wle_s10_player_round_wk3_15", "Parkour Party", LevelType.Race, true, false, 10, 0, 0));
        ALLMAPS.put("wle_s10_player_round_wk3_16", new LevelStats("wle_s10_player_round_wk3_16", "Catastrophe Climb", LevelType.Race, true, false, 10, 0, 0));
        ALLMAPS.put("wle_s10_player_round_wk3_17", new LevelStats("wle_s10_player_round_wk3_17", "Yeet Golf", LevelType.Race, true, false, 10, 0, 0));
        ALLMAPS.put("wle_s10_player_round_wk3_18", new LevelStats("wle_s10_player_round_wk3_18", "Hill of Fear", LevelType.Race, true, false, 10, 0, 0));
        ALLMAPS.put("wle_s10_player_round_wk3_19", new LevelStats("wle_s10_player_round_wk3_19", "Sky Time", LevelType.Race, true, false, 10, 0, 0));
        ALLMAPS.put("wle_s10_player_round_wk3_20", new LevelStats("wle_s10_player_round_wk3_20", "Ezz Map", LevelType.Race, true, false, 10, 0, 0));

        ALLMAPS.put("wle_s10_player_round_wk4_01", new LevelStats("wle_s10_player_round_wk4_01", "Slippery Stretch", LevelType.Race, true, false, 10, 0, 0));
        ALLMAPS.put("wle_s10_player_round_wk4_02", new LevelStats("wle_s10_player_round_wk4_02", "Ball 'N Fall", LevelType.Race, true, false, 10, 0, 0));
        ALLMAPS.put("wle_s10_player_round_wk4_03", new LevelStats("wle_s10_player_round_wk4_03", "Rowdy Cloudy", LevelType.Race, true, false, 10, 0, 0));
        ALLMAPS.put("wle_s10_player_round_wk4_05", new LevelStats("wle_s10_player_round_wk4_05", "Vertiginous Steps", LevelType.Race, true, false, 10, 0, 0));
        ALLMAPS.put("wle_s10_player_round_wk4_06", new LevelStats("wle_s10_player_round_wk4_06", "Topsie Tursie", LevelType.Race, true, false, 10, 0, 0));
        ALLMAPS.put("wle_s10_player_round_wk4_07", new LevelStats("wle_s10_player_round_wk4_07", "Arcade Assault", LevelType.Race, true, false, 10, 0, 0));
        ALLMAPS.put("wle_s10_player_round_wk4_08", new LevelStats("wle_s10_player_round_wk4_08", "The Eight Pit Trials", LevelType.Race, true, false, 10, 0, 0));
        ALLMAPS.put("wle_s10_player_round_wk4_09", new LevelStats("wle_s10_player_round_wk4_09", "Green Beans", LevelType.Race, true, false, 10, 0, 0));
        ALLMAPS.put("wle_s10_player_round_wk4_10", new LevelStats("wle_s10_player_round_wk4_10", "Hop Hill", LevelType.Race, true, false, 10, 0, 0));
        ALLMAPS.put("wle_s10_player_round_wk4_11", new LevelStats("wle_s10_player_round_wk4_11", "Quick Sliders", LevelType.Race, true, false, 10, 0, 0));
        ALLMAPS.put("wle_s10_player_round_wk4_12", new LevelStats("wle_s10_player_round_wk4_12", "Split Path", LevelType.Race, true, false, 10, 0, 0));
        ALLMAPS.put("wle_s10_player_round_wk4_13", new LevelStats("wle_s10_player_round_wk4_13", "Piso Resbaloso", LevelType.Race, true, false, 10, 0, 0));
        ALLMAPS.put("wle_s10_player_round_wk4_15", new LevelStats("wle_s10_player_round_wk4_15", "Snowboard Street", LevelType.Race, true, false, 10, 0, 0));
        ALLMAPS.put("wle_s10_player_round_wk4_18", new LevelStats("wle_s10_player_round_wk4_18", "House Invasion", LevelType.Race, true, false, 10, 0, 0));
        ALLMAPS.put("wle_s10_player_round_wk4_19", new LevelStats("wle_s10_player_round_wk4_19", "SOLO FULL-TILT RAGE", LevelType.Race, true, false, 10, 0, 0));
        ALLMAPS.put("wle_s10_player_round_wk4_20", new LevelStats("wle_s10_player_round_wk4_20", "Terminal Slime-ocity", LevelType.Race, true, false, 10, 0, 0));
        ALLMAPS.put("wle_s10_player_round_wk4_21", new LevelStats("wle_s10_player_round_wk4_21", "Spin", LevelType.Race, true, false, 10, 0, 0));
        ALLMAPS.put("wle_s10_player_round_wk4_22", new LevelStats("wle_s10_player_round_wk4_22", "Lane Changers", LevelType.Race, true, false, 10, 0, 0));

        ALLMAPS.put("wle_s10_player_round_wk5_01", new LevelStats("wle_s10_player_round_wk5_01", "Block Park", LevelType.Race, true, false, 10, 0, 0));
        ALLMAPS.put("wle_s10_player_round_wk5_02", new LevelStats("wle_s10_player_round_wk5_02", "The Drummatical Story", LevelType.Race, true, false, 10, 0, 0));
        ALLMAPS.put("wle_s10_player_round_wk5_03", new LevelStats("wle_s10_player_round_wk5_03", "Digital Temple", LevelType.Race, true, false, 10, 0, 0));
        ALLMAPS.put("wle_s10_player_round_wk5_04", new LevelStats("wle_s10_player_round_wk5_04", "Tower Escape", LevelType.Race, true, false, 10, 0, 0));
        ALLMAPS.put("wle_s10_player_round_wk5_05", new LevelStats("wle_s10_player_round_wk5_05", "Tower Dash", LevelType.Race, true, false, 10, 0, 0));
        ALLMAPS.put("wle_s10_player_round_wk5_06", new LevelStats("wle_s10_player_round_wk5_06", "Gpu Gauntlet", LevelType.Race, true, false, 10, 0, 0));
        ALLMAPS.put("wle_s10_player_round_wk5_07", new LevelStats("wle_s10_player_round_wk5_07", "Looooping", LevelType.Race, true, false, 10, 0, 0));
        ALLMAPS.put("wle_s10_player_round_wk5_08", new LevelStats("wle_s10_player_round_wk5_08", "Rad Bean Skatepark", LevelType.Race, true, false, 10, 0, 0));
        ALLMAPS.put("wle_s10_player_round_wk5_10", new LevelStats("wle_s10_player_round_wk5_10", "Siank Arena", LevelType.Race, true, false, 10, 0, 0));
        ALLMAPS.put("wle_s10_player_round_wk5_11", new LevelStats("wle_s10_player_round_wk5_11", "Pro Players Only", LevelType.Race, true, false, 10, 0, 0));
        ALLMAPS.put("wle_s10_player_round_wk5_12", new LevelStats("wle_s10_player_round_wk5_12", "Extreme Tower", LevelType.Race, true, false, 10, 0, 0));
        ALLMAPS.put("wle_s10_player_round_wk5_13", new LevelStats("wle_s10_player_round_wk5_13", "Dessert Village", LevelType.Race, true, false, 10, 0, 0));
        ALLMAPS.put("wle_s10_player_round_wk5_14", new LevelStats("wle_s10_player_round_wk5_14", "Extreme Trampoline Jumping", LevelType.Race, true, false, 10, 0, 0));
        ALLMAPS.put("wle_s10_player_round_wk5_15", new LevelStats("wle_s10_player_round_wk5_15", "Beast Route", LevelType.Race, true, false, 10, 0, 0));
        ALLMAPS.put("wle_s10_player_round_wk5_16", new LevelStats("wle_s10_player_round_wk5_16", "METROPOLIS", LevelType.Race, true, false, 10, 0, 0));
        ALLMAPS.put("wle_s10_player_round_wk5_17", new LevelStats("wle_s10_player_round_wk5_17", "Big Bookcase", LevelType.Race, true, false, 10, 0, 0));
        ALLMAPS.put("wle_s10_player_round_wk5_18", new LevelStats("wle_s10_player_round_wk5_18", "Digital Doom", LevelType.Race, true, false, 10, 0, 0));

        ALLMAPS.put("wle_s10_player_round_wk6_01", new LevelStats("wle_s10_player_round_wk6_01", "Hammer Heaven", LevelType.Race, true, false, 10, 0, 0));
        ALLMAPS.put("wle_s10_player_round_wk6_02", new LevelStats("wle_s10_player_round_wk6_02", "RISKY ROUTES", LevelType.Race, true, false, 10, 0, 0));
        ALLMAPS.put("wle_s10_player_round_wk6_03", new LevelStats("wle_s10_player_round_wk6_03", "Castle Rush", LevelType.Race, true, false, 10, 0, 0));
        ALLMAPS.put("wle_s10_player_round_wk6_04", new LevelStats("wle_s10_player_round_wk6_04", "Chaotic Race", LevelType.Race, true, false, 10, 0, 0));
        ALLMAPS.put("wle_s10_player_round_wk6_05", new LevelStats("wle_s10_player_round_wk6_05", "FREEFALL TOWER", LevelType.Race, true, false, 10, 0, 0));
        ALLMAPS.put("wle_s10_player_round_wk6_06", new LevelStats("wle_s10_player_round_wk6_06", "西西的天空之城 Castle in the Sky", LevelType.Race, true, false, 10, 0, 0));
        ALLMAPS.put("wle_s10_player_round_wk6_08", new LevelStats("wle_s10_player_round_wk6_08", "Flower Power", LevelType.Race, true, false, 10, 0, 0));
        ALLMAPS.put("wle_s10_player_round_wk6_09", new LevelStats("wle_s10_player_round_wk6_09", "Dimension Explorer", LevelType.Race, true, false, 10, 0, 0));
        ALLMAPS.put("wle_s10_player_round_wk6_10", new LevelStats("wle_s10_player_round_wk6_10", "Forked Passage", LevelType.Race, true, false, 10, 0, 0));
        ALLMAPS.put("wle_s10_player_round_wk6_12", new LevelStats("wle_s10_player_round_wk6_12", "The Bee Hive", LevelType.Race, true, false, 10, 0, 0));
        ALLMAPS.put("wle_s10_player_round_wk6_13", new LevelStats("wle_s10_player_round_wk6_13", "Yeets & Ladders", LevelType.Race, true, false, 10, 0, 0));
        ALLMAPS.put("wle_s10_player_round_wk6_14", new LevelStats("wle_s10_player_round_wk6_14", "Snek", LevelType.Race, true, false, 10, 0, 0));
        ALLMAPS.put("wle_s10_player_round_wk6_15", new LevelStats("wle_s10_player_round_wk6_15", "SCHOOL OF FISH", LevelType.Race, true, false, 10, 0, 0));
        ALLMAPS.put("wle_s10_player_round_wk6_17", new LevelStats("wle_s10_player_round_wk6_17", "Slippery Helixes", LevelType.Race, true, false, 10, 0, 0));
        ALLMAPS.put("wle_s10_player_round_wk6_18", new LevelStats("wle_s10_player_round_wk6_18", "Recess", LevelType.Race, true, false, 10, 0, 0));
        ALLMAPS.put("wle_s10_player_round_wk6_19", new LevelStats("wle_s10_player_round_wk6_19", "Parrot river", LevelType.Race, true, false, 10, 0, 0));

        ALLMAPS.put("current_wle_fp3_07_01", new LevelStats("current_wle_fp3_07_01", "Block Sledding", LevelType.Race, true, false, 10, 0, 0));
        ALLMAPS.put("current_wle_fp3_07_02", new LevelStats("current_wle_fp3_07_02", "Layup Wallop", LevelType.Race, true, false, 10, 0, 0));
        ALLMAPS.put("current_wle_fp3_07_03", new LevelStats("current_wle_fp3_07_03", "Minecart Mayhem", LevelType.Race, true, false, 10, 0, 0));
        ALLMAPS.put("current_wle_fp3_07_04", new LevelStats("current_wle_fp3_07_04", "Bouncing Pass", LevelType.Race, true, false, 10, 0, 0));
        ALLMAPS.put("current_wle_fp3_07_05", new LevelStats("current_wle_fp3_07_05", "Ball Factory", LevelType.Race, true, false, 10, 0, 0));
        ALLMAPS.put("current_wle_fp3_07_0_01", new LevelStats("current_wle_fp3_07_0_01", "Funky Sanctuaries", LevelType.Race, true, false, 10, 0, 0));
        ALLMAPS.put("current_wle_fp3_07_0_02", new LevelStats("current_wle_fp3_07_0_02", "Woo-F-O", LevelType.Race, true, false, 10, 0, 0));
        ALLMAPS.put("current_wle_fp3_07_0_03", new LevelStats("current_wle_fp3_07_0_03", "Travel Diary - Great Wall of China", LevelType.Race, true, false, 10, 0, 0));

        ALLMAPS.put("current_wle_fp3_08_01", new LevelStats("current_wle_fp3_08_01", "Grabbers Territory", LevelType.Race, true, false, 10, 360, 360));
        ALLMAPS.put("current_wle_fp3_08_02", new LevelStats("current_wle_fp3_08_02", "A Way Out", LevelType.Race, true, false, 10, 360, 360));
        ALLMAPS.put("current_wle_fp3_08_03", new LevelStats("current_wle_fp3_08_03", "Wall Block", LevelType.Race, true, false, 10, 0, 0));
        ALLMAPS.put("current_wle_fp3_08_04", new LevelStats("current_wle_fp3_08_04", "The dream island", LevelType.Race, true, false, 10, 0, 0));
        ALLMAPS.put("current_wle_fp3_08_05", new LevelStats("current_wle_fp3_08_05", "Rainbow pulsion", LevelType.Race, true, false, 10, 0, 0));
        ALLMAPS.put("current_wle_fp3_08_06", new LevelStats("current_wle_fp3_08_06", "WHIPPITY WOPPITY", LevelType.Race, true, false, 10, 0, 0));
        ALLMAPS.put("current_wle_fp3_08_09", new LevelStats("current_wle_fp3_08_09", "Big Fans Box Challenge", LevelType.Race, true, false, 10, 0, 0));
        ALLMAPS.put("current_wle_fp3_08_10", new LevelStats("current_wle_fp3_08_10", "Crazy boxes", LevelType.Race, true, false, 10, 0, 0));
        ALLMAPS.put("current_wle_fp3_08_13", new LevelStats("current_wle_fp3_08_13", "Season 1 Race Mashup", LevelType.Race, true, false, 10, 0, 0));
        ALLMAPS.put("current_wle_fp3_08_14", new LevelStats("current_wle_fp3_08_14", "Flippy Hoopshots", LevelType.Race, true, false, 10, 0, 0));
        ALLMAPS.put("current_wle_fp3_08_15", new LevelStats("current_wle_fp3_08_15", "Stumble Teams", LevelType.Race, true, false, 10, 0, 0));
        ALLMAPS.put("current_wle_fp3_08_16", new LevelStats("current_wle_fp3_08_16", "Twisting Tower", LevelType.Race, true, false, 10, 0, 0));
        ALLMAPS.put("current_wle_fp3_08_17", new LevelStats("current_wle_fp3_08_17", "PUSH 'N' PULL", LevelType.Race, true, false, 10, 0, 0));
        ALLMAPS.put("current_wle_fp3_08_18", new LevelStats("current_wle_fp3_08_18", "The Rising Blocks", LevelType.Race, true, false, 10, 0, 0));
        ALLMAPS.put("current_wle_fp3_08_19", new LevelStats("current_wle_fp3_08_19", "Puzzle Blokies Path", LevelType.Race, true, false, 10, 0, 0));
        ALLMAPS.put("current_wle_fp3_09_01", new LevelStats("current_wle_fp3_09_01", "The up tower", LevelType.Race, true, false, 10, 0, 0));
        ALLMAPS.put("current_wle_fp3_09_02", new LevelStats("current_wle_fp3_09_02", "Short shuriken", LevelType.Race, true, false, 10, 0, 0));
        ALLMAPS.put("current_wle_fp3_09_03", new LevelStats("current_wle_fp3_09_03", "Les mêmes mécaniques de + en + dure", LevelType.Race, true, false, 10, 0, 0));
        ALLMAPS.put("current_wle_fp3_09_04", new LevelStats("current_wle_fp3_09_04", "Digi-Lily Sliding", LevelType.Race, true, false, 10, 0, 0));
        ALLMAPS.put("current_wle_fp3_09_05", new LevelStats("current_wle_fp3_09_05", "STUMBLE MEDIEVAL TOWER", LevelType.Race, true, false, 10, 0, 0));
        ALLMAPS.put("current_wle_fp3_09_06", new LevelStats("current_wle_fp3_09_06", "Random Heights", LevelType.Race, true, false, 10, 0, 0));
        ALLMAPS.put("current_wle_fp3_09_07", new LevelStats("current_wle_fp3_09_07", "Climb scramble", LevelType.Race, true, false, 10, 0, 0));
        ALLMAPS.put("current_wle_fp3_09_08", new LevelStats("current_wle_fp3_09_08", "Collide Gaming", LevelType.Race, true, false, 10, 0, 0));
        ALLMAPS.put("current_wle_fp3_09_09", new LevelStats("current_wle_fp3_09_09", "Very Compressed Level", LevelType.Race, true, false, 10, 0, 0));
        ALLMAPS.put("current_wle_fp3_09_0_01", new LevelStats("current_wle_fp3_09_0_01", "Slippery Slope", LevelType.Race, true, false, 10, 0, 0));
        ALLMAPS.put("current_wle_fp3_09_0_02", new LevelStats("current_wle_fp3_09_0_02", "The Most Hardest Fall Guys LEVEL", LevelType.Race, true, false, 10, 0, 0));
        ALLMAPS.put("current_wle_fp3_09_0_03", new LevelStats("current_wle_fp3_09_0_03", "Free Falling", LevelType.Race, true, false, 10, 0, 0));
        ALLMAPS.put("current_wle_fp3_09_0_04", new LevelStats("current_wle_fp3_09_0_04", "Conveyor Problems", LevelType.Race, true, false, 10, 0, 0));
        ALLMAPS.put("current_wle_fp3_09_0_05", new LevelStats("current_wle_fp3_09_0_05", "Clocktower Climb", LevelType.Race, true, false, 10, 0, 0));
        ALLMAPS.put("current_wle_fp3_09_0_06", new LevelStats("current_wle_fp3_09_0_06", "Savour Your Happiness", LevelType.Race, true, false, 10, 0, 0));
        ALLMAPS.put("current_wle_fp3_09_0_0_01", new LevelStats("current_wle_fp3_09_0_0_01", "Pastel Paradise", LevelType.Race, true, false, 10, 0, 0));

        ALLMAPS.put("current_wle_fp3_10_01", new LevelStats("current_wle_fp3_10_01", "When Nature Falls", LevelType.Race, true, false, 10, 0, 0));
        ALLMAPS.put("current_wle_fp3_10_02", new LevelStats("current_wle_fp3_10_02", "The Slippery Conveyor", LevelType.Race, true, false, 10, 0, 0));
        ALLMAPS.put("current_wle_fp3_10_03", new LevelStats("current_wle_fp3_10_03", "The Slime Trials", LevelType.Race, true, false, 10, 0, 0));
        ALLMAPS.put("current_wle_fp3_10_04", new LevelStats("current_wle_fp3_10_04", "Friendly Obstacles", LevelType.Race, true, false, 10, 0, 0));
        ALLMAPS.put("current_wle_fp3_10_05", new LevelStats("current_wle_fp3_10_05", "Climb and Fall", LevelType.Race, true, false, 10, 0, 0));
        ALLMAPS.put("current_wle_fp3_10_06", new LevelStats("current_wle_fp3_10_06", "Stairs and some other things", LevelType.Race, true, false, 10, 0, 0));
        ALLMAPS.put("current_wle_fp3_10_07", new LevelStats("current_wle_fp3_10_07", "Meowgical World", LevelType.Race, true, false, 10, 0, 0));
        ALLMAPS.put("current_wle_fp3_10_08", new LevelStats("current_wle_fp3_10_08", "Polluelo Speed", LevelType.Race, true, false, 10, 0, 0));
        ALLMAPS.put("current_wle_fp3_10_09", new LevelStats("current_wle_fp3_10_09", "Pixel Parade", LevelType.Race, true, false, 10, 0, 0));
        ALLMAPS.put("current_wle_fp3_10_10", new LevelStats("current_wle_fp3_10_10", "Total Madness", LevelType.Race, true, false, 10, 0, 0));
        ALLMAPS.put("current_wle_fp3_10_11", new LevelStats("current_wle_fp3_10_11", "The Abstract Maze", LevelType.Race, true, false, 10, 0, 0));
        ALLMAPS.put("current_wle_fp3_10_12", new LevelStats("current_wle_fp3_10_12", "Fan Off", LevelType.Race, true, false, 10, 0, 0));
        ALLMAPS.put("current_wle_fp3_10_13", new LevelStats("current_wle_fp3_10_13", "cloud highway", LevelType.Race, true, false, 10, 0, 0));
        ALLMAPS.put("current_wle_fp3_10_14", new LevelStats("current_wle_fp3_10_14", "はねるの！？トビラ（Door Bounce）", LevelType.Race, true, false, 10, 0, 0));
        ALLMAPS.put("current_wle_fp3_10_15", new LevelStats("current_wle_fp3_10_15", "Speedrunners be like", LevelType.Race, true, false, 10, 0, 0));
        ALLMAPS.put("current_wle_fp3_10_16", new LevelStats("current_wle_fp3_10_16", "Tumble Tower", LevelType.Race, true, false, 10, 0, 0));
        ALLMAPS.put("current_wle_fp3_10_17", new LevelStats("current_wle_fp3_10_17", "Silver's Snake Run", LevelType.Race, true, false, 10, 0, 0));
        ALLMAPS.put("current_wle_fp3_10_18", new LevelStats("current_wle_fp3_10_18", "Now Boarding", LevelType.Race, true, false, 10, 0, 0));
        ALLMAPS.put("current_wle_fp3_10_19", new LevelStats("current_wle_fp3_10_19", "Slime Scale", LevelType.Race, true, false, 10, 0, 0));
        ALLMAPS.put("current_wle_fp3_10_20", new LevelStats("current_wle_fp3_10_20", "TUMBLEDOWN MINESHAFT", LevelType.Race, true, false, 10, 0, 0));
        ALLMAPS.put("current_wle_fp3_10_21", new LevelStats("current_wle_fp3_10_21", "Circuito CHILL 1", LevelType.Race, true, false, 10, 0, 0));
        ALLMAPS.put("current_wle_fp3_10_22", new LevelStats("current_wle_fp3_10_22", "STUMBLE SLIDER", LevelType.Race, true, false, 10, 0, 0));
        ALLMAPS.put("current_wle_fp3_10_23", new LevelStats("current_wle_fp3_10_23", "Controlled Chaos", LevelType.Race, true, false, 10, 0, 0));
        ALLMAPS.put("current_wle_fp3_10_24", new LevelStats("current_wle_fp3_10_24", "Xtreme Jumping", LevelType.Race, true, false, 10, 0, 0));
        ALLMAPS.put("current_wle_fp3_10_25", new LevelStats("current_wle_fp3_10_25", "Odin", LevelType.Race, true, false, 10, 0, 0));
        ALLMAPS.put("current_wle_fp3_10_26", new LevelStats("current_wle_fp3_10_26", "Ciudad nube", LevelType.Race, true, false, 10, 0, 0));
        ALLMAPS.put("current_wle_fp3_10_27", new LevelStats("current_wle_fp3_10_27", "Bean Voyage", LevelType.Race, true, false, 10, 0, 0));
        ALLMAPS.put("current_wle_fp3_10_28", new LevelStats("current_wle_fp3_10_28", "SLIP-SAW", LevelType.Race, true, false, 10, 0, 0));
        ALLMAPS.put("current_wle_fp3_10_29", new LevelStats("current_wle_fp3_10_29", "Bbq bacon burger", LevelType.Race, true, false, 10, 0, 0));

        ALLMAPS.put("current_wle_fp4_06_01", new LevelStats("current_wle_fp4_06_01", "PENTAGON CIRCUIT", LevelType.Race, true, false, 10, 0, 0));
        ALLMAPS.put("current_wle_fp4_06_02", new LevelStats("current_wle_fp4_06_02", "Pachislo", LevelType.Race, true, false, 10, 0, 0));
        ALLMAPS.put("current_wle_fp4_06_0_01", new LevelStats("current_wle_fp4_06_0_01", "AquArsene", LevelType.Race, true, false, 10, 0, 0));
        ALLMAPS.put("current_wle_fp4_06_0_02", new LevelStats("current_wle_fp4_06_0_02", "RainbowCloud", LevelType.Race, true, false, 10, 0, 0));
        ALLMAPS.put("current_wle_fp4_06_0_03", new LevelStats("current_wle_fp4_06_0_03", "Pink Cascade", LevelType.Race, true, false, 10, 0, 0));
        ALLMAPS.put("current_wle_fp4_06_0_04", new LevelStats("current_wle_fp4_06_0_04", "Conveyor Conundrum", LevelType.Race, true, false, 10, 0, 0));
        ALLMAPS.put("current_wle_fp4_06_0_05", new LevelStats("current_wle_fp4_06_0_05", "RICKETY STRAWBRIDGE", LevelType.Race, true, false, 10, 0, 0));
        ALLMAPS.put("current_wle_fp4_06_0_10_01", new LevelStats("current_wle_fp4_06_0_10_01", "The Bee Hive", LevelType.Race, true, false, 10, 0, 0));
        ALLMAPS.put("current_wle_fp4_06_1_01", new LevelStats("current_wle_fp4_06_1_01", "Buggin' Out", LevelType.Race, true, false, 10, 0, 0));
        ALLMAPS.put("current_wle_fp4_06_1_02", new LevelStats("current_wle_fp4_06_1_02", "RISE AND SLIDE", LevelType.Race, true, false, 10, 0, 0));
        ALLMAPS.put("current_wle_fp4_06_1_03", new LevelStats("current_wle_fp4_06_1_03", "Bean Mini Golf", LevelType.Race, true, false, 10, 0, 0));
        ALLMAPS.put("current_wle_fp4_06_1_04", new LevelStats("current_wle_fp4_06_1_04", "Youpii Youpii", LevelType.Race, true, false, 10, 0, 0));
        ALLMAPS.put("current_wle_fp4_06_1_05", new LevelStats("current_wle_fp4_06_1_05", "The climb of Trials", LevelType.Race, true, false, 10, 0, 0));
        ALLMAPS.put("current_wle_fp4_06_1_06", new LevelStats("current_wle_fp4_06_1_06", "Bouncy Castle", LevelType.Race, true, false, 10, 0, 0));

        ALLMAPS.put("current_wle_fp4_07_01", new LevelStats("current_wle_fp4_07_01", "Rotational Runner", LevelType.Race, true, false, 10, 0, 0));
        ALLMAPS.put("current_wle_fp4_07_02", new LevelStats("current_wle_fp4_07_02", "SPIRAL DASH ROAD", LevelType.Race, true, false, 10, 0, 0));
        ALLMAPS.put("current_wle_fp4_07_03", new LevelStats("current_wle_fp4_07_03", "simple stage", LevelType.Race, true, false, 10, 0, 0));
        ALLMAPS.put("current_wle_fp4_07_04", new LevelStats("current_wle_fp4_07_04", "Slip Slide Jump and Run", LevelType.Race, true, false, 10, 0, 0));
        ALLMAPS.put("current_wle_fp4_07_05", new LevelStats("current_wle_fp4_07_05", "Factory Valley", LevelType.Race, true, false, 10, 0, 0));
        ALLMAPS.put("current_wle_fp4_07_06", new LevelStats("current_wle_fp4_07_06", "Jumpy Beans", LevelType.Race, true, false, 10, 0, 0));
        ALLMAPS.put("current_wle_fp4_07_07", new LevelStats("current_wle_fp4_07_07", "Slimetastic Stumble", LevelType.Race, true, false, 10, 0, 0));
        ALLMAPS.put("current_wle_fp4_07_0_01", new LevelStats("current_wle_fp4_07_0_01", "Camino Ninja", LevelType.Race, true, false, 10, 0, 0));

        ALLMAPS.put("current_wle_fp4_08_01", new LevelStats("current_wle_fp4_08_01", "co-op guys", LevelType.Race, true, false, 10, 0, 0));
        ALLMAPS.put("current_wle_fp4_08_0_01", new LevelStats("current_wle_fp4_08_0_01", "The big slide", LevelType.Race, true, false, 10, 0, 0));
        ALLMAPS.put("current_wle_fp4_08_0_02", new LevelStats("current_wle_fp4_08_0_02", "Freefall Mountain", LevelType.Race, true, false, 10, 0, 0));
        ALLMAPS.put("current_wle_fp4_08_0_03", new LevelStats("current_wle_fp4_08_0_03", "Hazy Stairways", LevelType.Race, true, false, 10, 0, 0));
        ALLMAPS.put("current_wle_fp4_08_0_04", new LevelStats("current_wle_fp4_08_0_04", "Pillar Promenade", LevelType.Race, true, false, 10, 0, 0));
        ALLMAPS.put("current_wle_fp4_08_0_05", new LevelStats("current_wle_fp4_08_0_05", "Hidden Treasure of Magical Castle", LevelType.Race, true, false, 10, 0, 0));
        ALLMAPS.put("current_wle_fp4_08_0_06", new LevelStats("current_wle_fp4_08_0_06", "X-Course", LevelType.Race, true, false, 10, 0, 0));
        ALLMAPS.put("current_wle_fp4_08_0_07", new LevelStats("current_wle_fp4_08_0_07", "Speed Gauntlet", LevelType.Race, true, false, 10, 0, 0));
        ALLMAPS.put("current_wle_fp4_08_1_01", new LevelStats("current_wle_fp4_08_1_01", "Boost in Dash", LevelType.Race, true, false, 10, 0, 0));
        ALLMAPS.put("current_wle_fp4_08_1_02", new LevelStats("current_wle_fp4_08_1_02", "Rainbow Raceway", LevelType.Race, true, false, 10, 0, 0));
        ALLMAPS.put("current_wle_fp4_08_1_03", new LevelStats("current_wle_fp4_08_1_03", "Giddy up!", LevelType.Race, true, false, 10, 0, 0));
        ALLMAPS.put("current_wle_fp4_08_1_04", new LevelStats("current_wle_fp4_08_1_04", "Mad lab", LevelType.Race, true, false, 10, 0, 0));
        ALLMAPS.put("current_wle_fp4_08_2_01", new LevelStats("current_wle_fp4_08_2_01", "Convoluted Conveyors", LevelType.Race, true, false, 10, 0, 0));
        ALLMAPS.put("current_wle_fp4_08_3_01", new LevelStats("current_wle_fp4_08_3_01", "The Oasis", LevelType.Race, true, false, 10, 0, 0));

        ALLMAPS.put("current_wle_fp4_09_01", new LevelStats("current_wle_fp4_09_01", "Crate Collector", LevelType.Race, true, false, 10, 360, 360));
        ALLMAPS.put("current_wle_fp4_09_02", new LevelStats("current_wle_fp4_09_02", "Dribble Drills", LevelType.Race, true, false, 10, 360, 360));
        ALLMAPS.put("current_wle_fp4_09_03", new LevelStats("current_wle_fp4_09_03", "Spinning Slide Dodge", LevelType.Race, true, false, 10, 0, 0));
        ALLMAPS.put("current_wle_fp4_09_04", new LevelStats("current_wle_fp4_09_04", "Skyline Park", LevelType.Race, true, false, 10, 0, 0));
        ALLMAPS.put("current_wle_fp4_09_05", new LevelStats("current_wle_fp4_09_05", "Birthday bonanza", LevelType.Race, true, false, 10, 0, 0));
        ALLMAPS.put("current_wle_fp4_09_06", new LevelStats("current_wle_fp4_09_06", "The Chaotic Waterfall", LevelType.Race, true, false, 10, 0, 0));
        ALLMAPS.put("current_wle_fp4_09_0_01", new LevelStats("current_wle_fp4_09_0_01", "ICY PEAKS", LevelType.Race, true, false, 10, 0, 0));
        ALLMAPS.put("current_wle_fp4_09_1_01", new LevelStats("current_wle_fp4_09_1_01", "Push-Box Chaos", LevelType.Race, true, false, 10, 0, 0));
        ALLMAPS.put("current_wle_fp4_09_1_02", new LevelStats("current_wle_fp4_09_1_02", "Haute voltige", LevelType.Race, true, false, 10, 0, 0));
        ALLMAPS.put("current_wle_fp4_09_2_01", new LevelStats("current_wle_fp4_09_2_01", "DNA Test", LevelType.Race, true, false, 10, 0, 0));

        ALLMAPS.put("current_wle_fp4_10_01", new LevelStats("current_wle_fp4_10_01", "Bouncy Box Boulevard 3 Extreme Delivery", LevelType.Race, true, false, 10, 360, 360));
        ALLMAPS.put("current_wle_fp4_10_02", new LevelStats("current_wle_fp4_10_02", "Hot Blast", LevelType.Race, true, false, 10, 360, 360));
        ALLMAPS.put("current_wle_fp4_10_03", new LevelStats("current_wle_fp4_10_03", "Box Fan Blitz", LevelType.Race, true, false, 10, 360, 360));
        ALLMAPS.put("current_wle_fp4_10_04", new LevelStats("current_wle_fp4_10_04", "Woo-terfall Way", LevelType.Race, true, false, 10, 360, 360));
        ALLMAPS.put("current_wle_fp4_10_05", new LevelStats("current_wle_fp4_10_05", "Slime race", LevelType.Race, true, false, 10, 360, 360));
        ALLMAPS.put("current_wle_fp4_10_06", new LevelStats("current_wle_fp4_10_06", "Moving Day", LevelType.Race, true, false, 10, 360, 360));
        ALLMAPS.put("current_wle_fp4_10_07", new LevelStats("current_wle_fp4_10_07", "Birthday Dash", LevelType.Race, true, false, 10, 360, 360));
        ALLMAPS.put("current_wle_fp4_10_08_m", new LevelStats("current_wle_fp4_10_08_m", "Wall Breaker", LevelType.Race, true, false, 10, 360, 360));
        ALLMAPS.put("current_wle_fp4_10_08", new LevelStats("current_wle_fp4_10_08", "Chess History", LevelType.Race, true, false, 10, 360, 360));
        ALLMAPS.put("current_wle_fp4_10_11", new LevelStats("current_wle_fp4_10_11", "HOARDER BLOCKS", LevelType.Race, true, false, 10, 360, 360));
        ALLMAPS.put("current_wle_fp4_10_12", new LevelStats("current_wle_fp4_10_12", "Chickens run away", LevelType.Race, true, false, 10, 360, 360));
        ALLMAPS.put("current_wle_fp4_10_20", new LevelStats("current_wle_fp4_10_20", "Co-op and CO", LevelType.Race, true, false, 10, 360, 360));
        ALLMAPS.put("current_wle_fp4_10_21", new LevelStats("current_wle_fp4_10_21", "Construction Site", LevelType.Race, true, false, 10, 0, 0));
        ALLMAPS.put("current_wle_fp4_10_0_01", new LevelStats("current_wle_fp4_10_0_01", "Cheese Canyon", LevelType.Race, true, false, 10, 0, 0));
        ALLMAPS.put("current_wle_fp4_10_0_02", new LevelStats("current_wle_fp4_10_0_02", "Molehills", LevelType.Race, true, false, 10, 0, 0));

        ALLMAPS.put("wle_s10_bt_round_001", new LevelStats("wle_s10_bt_round_001", "Push Ups", LevelType.Race, true, false, 10, 0, 0));
        ALLMAPS.put("wle_s10_bt_round_002", new LevelStats("wle_s10_bt_round_002", "Heave & Haul", LevelType.Race, true, false, 10, 0, 0));
        ALLMAPS.put("wle_s10_bt_round_003", new LevelStats("wle_s10_bt_round_003", "Stepping Stones", LevelType.Race, true, false, 10, 0, 0));
        ALLMAPS.put("wle_s10_bt_round_004", new LevelStats("wle_s10_bt_round_004", "Double Trouble", LevelType.Race, true, false, 10, 0, 0));

        ALLMAPS.put("wle_s10_cf_round_001", new LevelStats("wle_s10_cf_round_001", "Blocky Bridges", LevelType.Race, true, false, 10, 360, 360));
        ALLMAPS.put("wle_s10_cf_round_002", new LevelStats("wle_s10_cf_round_002", "Gappy-go-Lucky", LevelType.Race, true, false, 10, 360, 360));
        ALLMAPS.put("wle_s10_cf_round_003", new LevelStats("wle_s10_cf_round_003", "Drop n' Drag", LevelType.Race, true, false, 10, 360, 360));
        ALLMAPS.put("wle_s10_cf_round_004", new LevelStats("wle_s10_cf_round_004", "Fun with Fans", LevelType.Race, true, false, 10, 360, 360));

        ALLMAPS.put("wle_mrs_bagel_opener_1", new LevelStats("wle_mrs_bagel_opener_1", "Tunnel of Love", LevelType.Race, true, false, 10, 0, 0));
        ALLMAPS.put("wle_mrs_bagel_opener_2", new LevelStats("wle_mrs_bagel_opener_2", "Pink Parade", LevelType.Race, true, false, 10, 0, 0));
        ALLMAPS.put("wle_mrs_bagel_opener_3", new LevelStats("wle_mrs_bagel_opener_3", "Prideful Path", LevelType.Race, true, false, 10, 0, 0));
        ALLMAPS.put("wle_mrs_bagel_opener_4", new LevelStats("wle_mrs_bagel_opener_4", "Coming Together", LevelType.Race, true, false, 10, 0, 0));
        ALLMAPS.put("wle_mrs_bagel_filler_1", new LevelStats("wle_mrs_bagel_filler_1", "Clifftop Capers", LevelType.Race, true, false, 10, 0, 0));
        ALLMAPS.put("wle_mrs_bagel_filler_2", new LevelStats("wle_mrs_bagel_filler_2", "Waveway Splits", LevelType.Race, true, false, 10, 0, 0));
        ALLMAPS.put("wle_mrs_bagel_filler_3", new LevelStats("wle_mrs_bagel_filler_3", "In the Groove", LevelType.Race, true, false, 10, 0, 0));
        ALLMAPS.put("wle_mrs_bagel_filler_4", new LevelStats("wle_mrs_bagel_filler_4", "Heartfall Heat", LevelType.Race, true, false, 10, 0, 0));
        ALLMAPS.put("wle_mrs_bagel_final_1", new LevelStats("wle_mrs_bagel_final_1", "Rainbow Rise", LevelType.Race, true, true, 10, 0, 0));
        ALLMAPS.put("wle_mrs_bagel_final_2", new LevelStats("wle_mrs_bagel_final_2", "Out and About", LevelType.Race, true, true, 10, 0, 0));

        ALLMAPS.put("wle_s10_orig_round_010", new LevelStats("wle_s10_orig_round_010", "Square Up", LevelType.Race, true, true, 10, 0, 0));
        ALLMAPS.put("wle_s10_orig_round_011", new LevelStats("wle_s10_orig_round_011", "Slide Showdown", LevelType.Race, true, true, 10, 0, 0));
        ALLMAPS.put("wle_s10_orig_round_017", new LevelStats("wle_s10_orig_round_017", "Bellyflop Battlers", LevelType.Race, true, true, 10, 0, 0));
        ALLMAPS.put("wle_s10_orig_round_018", new LevelStats("wle_s10_orig_round_018", "Apples & Oranges", LevelType.Race, true, true, 10, 0, 0));
        ALLMAPS.put("wle_s10_orig_round_024", new LevelStats("wle_s10_orig_round_024", "Wooseleum", LevelType.Race, true, true, 10, 0, 0));
        ALLMAPS.put("wle_s10_orig_round_025", new LevelStats("wle_s10_orig_round_025", "Mount Boom", LevelType.Race, true, true, 10, 0, 0));
        ALLMAPS.put("wle_s10_orig_round_030", new LevelStats("wle_s10_orig_round_030", "Mega Monument", LevelType.Race, true, true, 10, 0, 0));
        ALLMAPS.put("wle_s10_orig_round_031", new LevelStats("wle_s10_orig_round_031", "Transfer Turnpike", LevelType.Race, true, true, 10, 0, 0));
        ALLMAPS.put("wle_s10_round_004", new LevelStats("wle_s10_round_004", "Parkour Panic", LevelType.Race, true, true, 10, 0, 0));
        ALLMAPS.put("wle_s10_round_009", new LevelStats("wle_s10_round_009", "Firewall Finale", LevelType.Race, true, true, 10, 0, 0));

        ALLMAPS.put("round_biggestfan", new LevelStats("round_biggestfan", "Big Fans", LevelType.Race, false, false, 2, 210, 120));
        ALLMAPS.put("round_satellitehoppers_almond", new LevelStats("round_satellitehoppers_almond", "Cosmic Highway", LevelType.Race, false, false, 8, 180, 180));
        ALLMAPS.put("round_gauntlet_02", new LevelStats("round_gauntlet_02", "Dizzy Heights", LevelType.Race, false, false, 1, 180, 120));
        ALLMAPS.put("round_door_dash", new LevelStats("round_door_dash", "Door Dash", LevelType.Race, false, false, 1, 180, 120));
        ALLMAPS.put("round_iceclimb", new LevelStats("round_iceclimb", "Freezy Peak", LevelType.Race, false, false, 3, 180, 120));
        ALLMAPS.put("round_dodge_fall", new LevelStats("round_dodge_fall", "Fruit Chute", LevelType.Race, false, false, 1, 180, 120));
        ALLMAPS.put("round_see_saw_360", new LevelStats("round_see_saw_360", "Full Tilt", LevelType.Race, false, false, 6, 180, 180));
        ALLMAPS.put("round_chompchomp", new LevelStats("round_chompchomp", "Gate Crash", LevelType.Race, false, false, 1, 300, 120));
        ALLMAPS.put("round_gauntlet_01", new LevelStats("round_gauntlet_01", "Hit Parade", LevelType.Race, false, false, 1, 180, 120));
        ALLMAPS.put("round_gauntlet_04", new LevelStats("round_gauntlet_04", "Knight Fever", LevelType.Race, false, false, 2, 180, 120));
        ALLMAPS.put("round_drumtop", new LevelStats("round_drumtop", "Lily Leapers", LevelType.Race, false, false, 5, 300, 140));
        ALLMAPS.put("round_gauntlet_08", new LevelStats("round_gauntlet_08", "Party Promenade", LevelType.Race, false, false, 6, 300, 120));
        ALLMAPS.put("round_pipedup_s6_launch", new LevelStats("round_pipedup_s6_launch", "Pipe Dream", LevelType.Race, false, false, 6, 300, 150));
        ALLMAPS.put("round_follow_the_line", new LevelStats("round_follow_the_line", "Puzzle Path", LevelType.Race, false, false, 9, 150, 150));
        ALLMAPS.put("round_tunnel_race", new LevelStats("round_tunnel_race", "Roll On", LevelType.Race, false, false, 4, 120, 120));
        ALLMAPS.put("round_see_saw", new LevelStats("round_see_saw", "See Saw", LevelType.Race, false, false, 1, 180, 120));
        ALLMAPS.put("round_shortcircuit", new LevelStats("round_shortcircuit", "Short Circuit", LevelType.Race, false, false, 4, 300, 300));
        ALLMAPS.put("round_skeefall", new LevelStats("round_skeefall", "Ski Fall", LevelType.Race, false, false, 3, 300, 300));
        ALLMAPS.put("round_gauntlet_06", new LevelStats("round_gauntlet_06", "Skyline Stumble", LevelType.Race, false, false, 4, 180, 120));
        ALLMAPS.put("round_lava", new LevelStats("round_lava", "Slime Climb", LevelType.Race, false, false, 1, 140, 140));
        ALLMAPS.put("round_gauntlet_10_almond", new LevelStats("round_gauntlet_10_almond", "Space Race", LevelType.Race, false, false, 8, 150, 150));
        ALLMAPS.put("round_short_circuit_2_symphony_launch_show", new LevelStats("round_short_circuit_2_symphony_launch_show", "Speed Circuit", LevelType.Race, false, false, 7, 180, 180));
        ALLMAPS.put("round_slide_chute", new LevelStats("round_slide_chute", "Speed Slider", LevelType.Race, false, false, 9, 165, 120));
        ALLMAPS.put("round_starlink_almond", new LevelStats("round_starlink_almond", "Starchart", LevelType.Race, false, false, 8, 150, 150));
        ALLMAPS.put("round_slimeclimb_2", new LevelStats("round_slimeclimb_2", "The Slimescraper", LevelType.Race, false, false, 4, 190, 190));
        ALLMAPS.put("round_gauntlet_03", new LevelStats("round_gauntlet_03", "The Whirlygig", LevelType.Race, false, false, 1, 180, 120));
        ALLMAPS.put("round_tip_toe", new LevelStats("round_tip_toe", "Tip Toe", LevelType.Race, false, false, 1, 300, 120));
        ALLMAPS.put("round_gauntlet_09_symphony_launch_show", new LevelStats("round_gauntlet_09_symphony_launch_show", "Track Attack", LevelType.Race, false, false, 7, 90, 90));
        ALLMAPS.put("round_gauntlet_07", new LevelStats("round_gauntlet_07", "Treetop Tumble", LevelType.Race, false, false, 5, 180, 120));
        ALLMAPS.put("round_gauntlet_05", new LevelStats("round_gauntlet_05", "Tundra Run", LevelType.Race, false, false, 3, 180, 120));
        ALLMAPS.put("round_wall_guys", new LevelStats("round_wall_guys", "Wall Guys", LevelType.Race, false, false, 2, 300, 120));
        ALLMAPS.put("round_airtime", new LevelStats("round_airtime", "Airtime", LevelType.Hunt, false, false, 6, 300, 300));
        ALLMAPS.put("round_bluejay", new LevelStats("round_bluejay", "Bean Hill Zone", LevelType.Hunt, false, false, 7, 300, 300));
        ALLMAPS.put("round_hoops_revenge_symphony_launch_show", new LevelStats("round_hoops_revenge_symphony_launch_show", "Bounce Party", LevelType.Hunt, false, false, 7, 300, 300));
        ALLMAPS.put("round_king_of_the_hill", new LevelStats("round_king_of_the_hill", "Bubble Trouble", LevelType.Hunt, false, false, 5, 300, 300));
        ALLMAPS.put("round_1v1_button_basher", new LevelStats("round_1v1_button_basher", "Button Bashers", LevelType.Hunt, false, false, 4, 90, 90));
        ALLMAPS.put("round_ffa_button_bashers_squads_almond", new LevelStats("round_ffa_button_bashers_squads_almond", "Frantic Factory", LevelType.Hunt, false, false, 8, 300, 300));
        ALLMAPS.put("round_slippy_slide", new LevelStats("round_slippy_slide", "Hoop Chute", LevelType.Hunt, false, false, 9, 180, 180));
        ALLMAPS.put("round_hoops_blockade_solo", new LevelStats("round_hoops_blockade_solo", "Hoopsie Legends", LevelType.Hunt, false, false, 2, 300, 300));
        ALLMAPS.put("round_penguin_solos", new LevelStats("round_penguin_solos", "Pegwin Pool Party", LevelType.Hunt, false, false, 5, 300, 300));
        ALLMAPS.put("round_follow-the-leader_s6_launch", new LevelStats("round_follow-the-leader_s6_launch", "Leading Light", LevelType.Hunt, false, false, 6, 300, 300));
        ALLMAPS.put("round_tail_tag", new LevelStats("round_tail_tag", "Tail Tag", LevelType.Hunt, false, false, 1, 90, 90));
        ALLMAPS.put("round_1v1_volleyfall_symphony_launch_show", new LevelStats("round_1v1_volleyfall_symphony_launch_show", "Volleyfall", LevelType.Hunt, false, false, 7, 100, 100));
        ALLMAPS.put("round_fruitpunch_s4_show", new LevelStats("round_fruitpunch_s4_show", "Big Shots", LevelType.Survival, false, false, 4, 90, 90));
        ALLMAPS.put("round_blastballruins", new LevelStats("round_blastballruins", "Blastlantis", LevelType.Survival, false, false, 9, 270, 150));
        ALLMAPS.put("round_block_party", new LevelStats("round_block_party", "Block Party", LevelType.Survival, false, false, 1, 105, 105));
        ALLMAPS.put("round_hoverboardsurvival_s4_show", new LevelStats("round_hoverboardsurvival_s4_show", "Hoverboard Heroes", LevelType.Survival, false, false, 4, 140, 140));
        ALLMAPS.put("round_hoverboardsurvival2_almond", new LevelStats("round_hoverboardsurvival2_almond", "Hyperdrive Heroes", LevelType.Survival, false, false, 8, 170, 170));
        ALLMAPS.put("round_jump_club", new LevelStats("round_jump_club", "Jump Club", LevelType.Survival, false, false, 1, 90, 90));
        ALLMAPS.put("round_tunnel", new LevelStats("round_tunnel", "Roll Out", LevelType.Survival, false, false, 1, 150, 90));
        ALLMAPS.put("round_snowballsurvival", new LevelStats("round_snowballsurvival", "Snowball Survival", LevelType.Survival, false, false, 3, 60, 60));
        ALLMAPS.put("round_robotrampage_arena_2", new LevelStats("round_robotrampage_arena_2", "Stompin' Ground", LevelType.Survival, false, false, 5, 70, 70));
        ALLMAPS.put("round_spin_ring_symphony_launch_show", new LevelStats("round_spin_ring_symphony_launch_show", "The Swiveller", LevelType.Survival, false, false, 7, 180, 180));

        ALLMAPS.put("round_match_fall", new LevelStats("round_match_fall", "Perfect Match", LevelType.Logic, false, false, 1, 80, 80));
        ALLMAPS.put("round_pixelperfect_almond", new LevelStats("round_pixelperfect_almond", "Pixel Painters", LevelType.Logic, false, false, 8, 180, 180));
        ALLMAPS.put("round_fruit_bowl", new LevelStats("round_fruit_bowl", "Sum Fruit", LevelType.Logic, false, false, 5, 100, 100));
        ALLMAPS.put("round_basketfall_s4_show", new LevelStats("round_basketfall_s4_show", "Basketfall", LevelType.Team, false, false, 4, 90, 90));
        ALLMAPS.put("round_egg_grab", new LevelStats("round_egg_grab", "Egg Scramble", LevelType.Team, false, false, 1, 120, 120));
        ALLMAPS.put("round_egg_grab_02", new LevelStats("round_egg_grab_02", "Egg Siege", LevelType.Team, false, false, 2, 120, 120));
        ALLMAPS.put("round_fall_ball_60_players", new LevelStats("round_fall_ball_60_players", "Fall Ball", LevelType.Team, false, false, 1, 90, 90));
        ALLMAPS.put("round_ballhogs", new LevelStats("round_ballhogs", "Hoarders", LevelType.Team, false, false, 1, 90, 90));
        ALLMAPS.put("round_hoops", new LevelStats("round_hoops", "Hoopsie Daisy", LevelType.Team, false, false, 1, 120, 120));
        ALLMAPS.put("round_jinxed", new LevelStats("round_jinxed", "Jinxed", LevelType.Team, false, false, 1, 300, 300));
        ALLMAPS.put("round_chicken_chase", new LevelStats("round_chicken_chase", "Pegwin Pursuit", LevelType.Team, false, false, 3, 120, 120));
        ALLMAPS.put("round_territory_control_s4_show", new LevelStats("round_territory_control_s4_show", "Power Trip", LevelType.Team, false, false, 4, 100, 100));
        ALLMAPS.put("round_rocknroll", new LevelStats("round_rocknroll", "Rock 'n' Roll", LevelType.Team, false, false, 1, 180, 180));
        ALLMAPS.put("round_snowy_scrap", new LevelStats("round_snowy_scrap", "Snowy Scrap", LevelType.Team, false, false, 3, 180, 180));
        ALLMAPS.put("round_conveyor_arena", new LevelStats("round_conveyor_arena", "Team Tail Tag", LevelType.Team, false, false, 1, 90, 90));

        ALLMAPS.put("round_invisibeans", new LevelStats("round_invisibeans", "Sweet Thieves", LevelType.Invisibeans, false, false, 6, 180, 180));
        ALLMAPS.put("round_pumpkin_pie", new LevelStats("round_pumpkin_pie", "Treat Thieves", LevelType.Invisibeans, false, false, 8, 180, 180));

        ALLMAPS.put("round_blastball_arenasurvival_symphony_launch_show", new LevelStats("round_blastball_arenasurvival_symphony_launch_show", "Blast Ball", LevelType.Survival, false, true, 7, 270, 270));
        ALLMAPS.put("round_fall_mountain_hub_complete", new LevelStats("round_fall_mountain_hub_complete", "Fall Mountain", LevelType.Race, false, true, 1, 300, 300));
        ALLMAPS.put("round_floor_fall", new LevelStats("round_floor_fall", "Hex-A-Gone", LevelType.Survival, false, true, 1, 300, 300));
        ALLMAPS.put("round_hexaring_symphony_launch_show", new LevelStats("round_hexaring_symphony_launch_show", "Hex-A-Ring", LevelType.Survival, false, true, 7, 300, 300));
        ALLMAPS.put("round_hexsnake_almond", new LevelStats("round_hexsnake_almond", "Hex-A-Terrestrial", LevelType.Survival, false, true, 8, 300, 300));
        ALLMAPS.put("round_jump_showdown", new LevelStats("round_jump_showdown", "Jump Showdown", LevelType.Survival, false, true, 1, 300, 300));
        ALLMAPS.put("round_kraken_attack", new LevelStats("round_kraken_attack", "Kraken Slam", LevelType.Survival, false, true, 9, 300, 300));
        ALLMAPS.put("round_crown_maze", new LevelStats("round_crown_maze", "Lost Temple", LevelType.Race, false, true, 5, 300, 300));
        ALLMAPS.put("round_tunnel_final", new LevelStats("round_tunnel_final", "Roll Off", LevelType.Survival, false, true, 3, 300, 300));
        ALLMAPS.put("round_royal_rumble", new LevelStats("round_royal_rumble", "Royal Fumble", LevelType.Hunt, false, true, 1, 90, 90));
        ALLMAPS.put("round_thin_ice", new LevelStats("round_thin_ice", "Thin Ice", LevelType.Survival, false, true, 3, 300, 300));
        ALLMAPS.put("round_tiptoefinale_almond", new LevelStats("round_tiptoefinale_almond", "Tip Toe Finale", LevelType.Survival, false, true, 8, 300, 300));

    }


    public static HashMap<String, String> AllScenesToRound = new HashMap<>();

    static {
        AllScenesToRound.put("FallGuy_DoorDash", "round_door_dash");
        AllScenesToRound.put("FallGuy_Gauntlet_02_01", "round_gauntlet_02");
        AllScenesToRound.put("FallGuy_DodgeFall", "round_dodge_fall");
        AllScenesToRound.put("FallGuy_ChompChomp_01", "round_chompchomp");
        AllScenesToRound.put("FallGuy_Gauntlet_01", "round_gauntlet_01");
        AllScenesToRound.put("FallGuy_SeeSaw_variant2", "round_see_saw");
        AllScenesToRound.put("FallGuy_Lava_02", "round_lava");
        AllScenesToRound.put("FallGuy_TipToe", "round_tip_toe");
        AllScenesToRound.put("FallGuy_Gauntlet_03", "round_gauntlet_03");
        AllScenesToRound.put("FallGuy_Block_Party", "round_block_party");
        AllScenesToRound.put("FallGuy_JumpClub_01", "round_jump_club");
        AllScenesToRound.put("FallGuy_MatchFall", "round_match_fall");
        AllScenesToRound.put("FallGuy_Tunnel_01", "round_tunnel");
        AllScenesToRound.put("FallGuy_TailTag_2", "round_tail_tag");
        AllScenesToRound.put("FallGuy_EggGrab", "round_egg_grab");
        AllScenesToRound.put("FallGuy_FallBall_5", "round_fall_ball_60_players");
        AllScenesToRound.put("FallGuy_BallHogs_01", "round_ballhogs");
        AllScenesToRound.put("FallGuy_Hoops_01", "round_hoops");
        AllScenesToRound.put("FallGuy_TeamInfected", "round_jinxed");
        AllScenesToRound.put("FallGuy_RocknRoll", "round_rocknroll");
        AllScenesToRound.put("FallGuy_ConveyorArena_01", "round_conveyor_arena");
        AllScenesToRound.put("FallGuy_FallMountain_Hub_Complete", "round_fall_mountain_hub_complete");
        AllScenesToRound.put("FallGuy_FloorFall", "round_floor_fall");
        AllScenesToRound.put("FallGuy_JumpShowdown_01", "round_jump_showdown");
        AllScenesToRound.put("FallGuy_Arena_01", "round_royal_rumble");
        AllScenesToRound.put("FallGuy_BiggestFan", "round_biggestfan");
        AllScenesToRound.put("FallGuy_Hoops_Blockade", "round_hoops_blockade_solo");
        AllScenesToRound.put("FallGuy_Gauntlet_04", "round_gauntlet_04");
        AllScenesToRound.put("FallGuy_WallGuys", "round_wall_guys");
        AllScenesToRound.put("FallGuy_EggGrab_02", "round_egg_grab_02");
        AllScenesToRound.put("FallGuy_IceClimb_01", "round_iceclimb");
        AllScenesToRound.put("FallGuy_SkeeFall", "round_skeefall");
        AllScenesToRound.put("FallGuy_Gauntlet_05", "round_gauntlet_05");
        AllScenesToRound.put("FallGuy_SnowballSurvival", "round_snowballsurvival");
        AllScenesToRound.put("FallGuy_ChickenChase_01", "round_chicken_chase");
        AllScenesToRound.put("FallGuy_Snowy_Scrap", "round_snowy_scrap");
        AllScenesToRound.put("FallGuy_Tunnel_Final", "round_tunnel_final");
        AllScenesToRound.put("FallGuy_ThinIce", "round_thin_ice");
        AllScenesToRound.put("FallGuy_1v1_ButtonBasher", "round_1v1_button_basher");
        AllScenesToRound.put("FallGuy_Tunnel_Race_01", "round_tunnel_race");
        AllScenesToRound.put("FallGuy_ShortCircuit", "round_shortcircuit");
        AllScenesToRound.put("FallGuy_Gauntlet_06", "round_gauntlet_06");
        AllScenesToRound.put("FallGuy_SlimeClimb_2", "round_slimeclimb_2");
        AllScenesToRound.put("FallGuy_FruitPunch", "round_fruitpunch_s4_show");
        AllScenesToRound.put("FallGuy_HoverboardSurvival", "round_hoverboardsurvival_s4_show");
        AllScenesToRound.put("FallGuy_Basketfall_01", "round_basketfall_s4_show");
        AllScenesToRound.put("FallGuy_TerritoryControl_v2", "round_territory_control_s4_show");
        AllScenesToRound.put("FallGuy_KingOfTheHill2", "round_king_of_the_hill");
        AllScenesToRound.put("FallGuy_DrumTop", "round_drumtop");
        AllScenesToRound.put("FallGuy_Penguin_Solos", "round_penguin_solos");
        AllScenesToRound.put("FallGuy_Gauntlet_07", "round_gauntlet_07");
        AllScenesToRound.put("FallGuy_RobotRampage_Arena2", "round_robotrampage_arena_2");
        AllScenesToRound.put("FallGuy_FruitBowl", "round_fruit_bowl");
        AllScenesToRound.put("FallGuy_Crown_Maze_Topdown", "round_crown_maze");
        AllScenesToRound.put("FallGuy_Airtime", "round_airtime");
        AllScenesToRound.put("FallGuy_SeeSaw360", "round_see_saw_360");
        AllScenesToRound.put("FallGuy_FollowTheLeader", "round_follow-the-leader_s6_launch");
        AllScenesToRound.put("FallGuy_Gauntlet_08", "round_gauntlet_08");
        AllScenesToRound.put("FallGuy_PipedUp", "round_pipedup_s6_launch");
        AllScenesToRound.put("FallGuy_Invisibeans", "round_invisibeans");
        AllScenesToRound.put("FallGuy_BlueJay", "round_bluejay");
        AllScenesToRound.put("FallGuy_HoopsRevenge", "round_hoops_revenge_symphony_launch_show");
        AllScenesToRound.put("FallGuy_ShortCircuit2", "round_short_circuit_2_symphony_launch_show");
        AllScenesToRound.put("FallGuy_Gauntlet_09", "round_gauntlet_09_symphony_launch_show");
        AllScenesToRound.put("FallGuy_SpinRing", "round_spin_ring_symphony_launch_show");
        AllScenesToRound.put("FallGuy_1v1_Volleyfall", "round_1v1_volleyfall_symphony_launch_show");
        AllScenesToRound.put("FallGuy_BlastBall_ArenaSurvival", "round_blastball_arenasurvival_symphony_launch_show");
        AllScenesToRound.put("FallGuy_HexARing", "round_hexaring_symphony_launch_show");
        AllScenesToRound.put("FallGuy_SatelliteHoppers", "round_satellitehoppers_almond");
        AllScenesToRound.put("FallGuy_FFA_Button_Bashers", "round_ffa_button_bashers_squads_almond");
        AllScenesToRound.put("FallGuy_Hoverboard_Survival_2", "round_hoverboardsurvival2_almond");
        AllScenesToRound.put("FallGuy_PixelPerfect", "round_pixelperfect_almond");
        AllScenesToRound.put("FallGuy_Gauntlet_10", "round_gauntlet_10_almond");
        AllScenesToRound.put("FallGuy_Starlink", "round_starlink_almond");
        AllScenesToRound.put("FallGuy_HexSnake", "round_hexsnake_almond");
        AllScenesToRound.put("FallGuy_Tip_Toe_Finale", "round_tiptoefinale_almond");
        AllScenesToRound.put("FallGuy_BlastBallRuins", "round_blastballruins");
        AllScenesToRound.put("FallGuy_FollowTheLine", "round_follow_the_line");
        AllScenesToRound.put("FallGuy_Kraken_Attack", "round_kraken_attack");
        AllScenesToRound.put("FallGuy_SlippySlide", "round_slippy_slide");
        AllScenesToRound.put("FallGuy_SlideChute", "round_slide_chute");
        AllScenesToRound.put("FallGuy_UseShareCode", "user_creative_race_round");
    }

    public LevelStats(String levelId, String levelName, LevelType type, boolean isCreative, boolean isFinal, int season, int timeLimitInSeconds, int timeLimitInSecondsForSquad) {
        this.id = levelId;
        this.name = levelName;
        this.type = type;
        this.season = season;
        this.isCreative = isCreative;
        this.isFinal = isFinal;
        this.timeLimitInSeconds = timeLimitInSeconds;
        this.timeLimitInSecondsForSquad = timeLimitInSecondsForSquad;
        this.stats = new ArrayList<Round>();
    }

    public void Clear() {
        this.qualified = 0;
        this.gold = 0;
        this.silver = 0;
        this.bronze = 0;
        this.played = 0;
        this.kudos = 0;
        this.finishedCount = 0;
        this.stats.clear();
    }

}