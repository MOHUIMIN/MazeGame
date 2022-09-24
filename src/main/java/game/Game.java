package game;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class Game {

    public GameHandler gameHandler;
    public String[] tracker; //[0] : IP ADDRESS; [1] : PORT NUMBER
    public Player localPlayer;
    public String primaryServer; //[0] : IP ADDRESS; [1] : PORT NUMBER
    public String backupServer;//[0] : IP ADDRESS; [1] : PORT NUMBER
    public GameState gameState;

    public Game(String playerId, int trackerPort) throws RemoteException {
        this.gameHandler = new GameHandlerImpl(this);
        this.localPlayer = new Player(playerId) ;
        Registry registry = LocateRegistry.getRegistry(trackerPort);
        registry.rebind(playerId, this.gameHandler);
    }

    public void initialize(String firstPlayer, int playerListLength, int N, int K){
        //Use this function when a game just start
        //To initialize the gamestate and server info and update your local info to the server
        if(playerListLength == 1){
            //if you are the first player
            //you create a new gamestate, add your info to gamestate
            this.gameState = new GameState(N,K);

            this.gameState.playerList.add(this.localPlayer.playerId);
            this.localPlayer.position = this.gameState.randomValidPosition();
            this.gameState.allPlayerPositions.put(this.localPlayer.playerId,this.localPlayer.position);
            this.gameState.scoreBoard.put(this.localPlayer.playerId,this.localPlayer.score);

            //set server info, because you're the first, you become both primary and backup server
            this.primaryServer = this.localPlayer.playerId;
            this.backupServer = this.localPlayer.playerId;
        }
        else if(playerListLength == 2){
            //if you are the second
            //firstly, set server info and tell the primary server that you will become the backup
            this.primaryServer = firstPlayer;
            this.backupServer = this.localPlayer.playerId;// TODO : UPDATE THIS INFO TO PRIMARY SERVER
            this.updateBothServer(this.primaryServer, this.primaryServer,this.backupServer);

            //retrieve the latest game state and merge your gamestate to primary server's game state
            this.retrieveFromServer(this.primaryServer);

            this.gameState.playerList.add(this.localPlayer.playerId);
            this.localPlayer.position = this.gameState.randomValidPosition();
            this.gameState.allPlayerPositions.put(this.localPlayer.playerId,this.localPlayer.position);
            this.gameState.scoreBoard.put(this.localPlayer.playerId,this.localPlayer.score);

            this.updateServer(this.primaryServer);

        }
        else{
            //if you are just some random player that do not have to become server
            //set the primary and backup with the info that you get from the first player in tracker's playerlist
            String[] servers = this.getPrimaryAndBackup(firstPlayer);
            this.primaryServer = servers[0];
            this.backupServer = servers[1];

            //retrieve and merge your gamestate to primary server's game state
            this.retrieveFromServer(this.primaryServer);

            this.gameState.playerList.add(this.localPlayer.playerId);
            this.localPlayer.position = this.gameState.randomValidPosition();
            this.gameState.allPlayerPositions.put(this.localPlayer.playerId,this.localPlayer.position);
            this.gameState.scoreBoard.put(this.localPlayer.playerId,this.localPlayer.score);

            this.updateServer(this.primaryServer);
        }
    }

    //TODO : public boolean renderView(){return true;}

    public boolean printMap(){
        //print all player and treasure in terminal
        for(int i = 0; i < gameState.mazeMap.length; i++){
            for(int j = 0; j < gameState.mazeMap.length; j++){
                if(i == this.localPlayer.position[0] && j == this.localPlayer.position[1])
                    System.out.print("▲" + "   ");
                else{
                    boolean otherPlayerIsThere = false;
                    for(int[] position : this.gameState.allPlayerPositions.values()){
                        if(i == position[0] && j == position[1]){
                            otherPlayerIsThere = true;
                            break;
                        }
                    }
                    if(otherPlayerIsThere)
                        System.out.print("▼" + "   ");
                    else
                        System.out.print(gameState.mazeMap[i][j] + "   ");
                }

            }
            System.out.println();
        }
        System.out.println("position: " + this.localPlayer.position[0] + "," + this.localPlayer.position[1]);
        for(Map.Entry playerAndScore : this.gameState.scoreBoard.entrySet())
            System.out.println("Player: " + playerAndScore.getKey() + "  Score: " + playerAndScore.getValue());
        return true;
    }

    public List<Object> retrieveFromTracker(TrackerHandler trackerHandler, String playerId) throws RemoteException {

        //retrieve basic game info from tracker
        trackerHandler.register(playerId);

        List<Object> res = new ArrayList<>();
        res.add(trackerHandler.getN());
        res.add(trackerHandler.getK());
        res.add(trackerHandler.getPlayer());
        res.add(trackerHandler.getPlayerListLength());
        return res;
    }

    public boolean retrieveFromServer(String primaryServer){

        //retrieve latest game state from server
        GameHandler primaryGameHandler = null;
        try {
            primaryGameHandler = (GameHandler) Naming.lookup(primaryServer);
            this.gameState = primaryGameHandler.getGameState();
        } catch (MalformedURLException | NotBoundException | RemoteException e) {
            System.out.println("cannot retrieve from server");
            //TODO : DEAL WITH CONNECTION EXCEPTION
        }
        return true;
    }

    public boolean updateServer(String primaryServer) {
        //update the server's game state
        GameHandler primaryGameHandler = null;
        try {
            primaryGameHandler = (GameHandler) Naming.lookup(primaryServer);
            primaryGameHandler.updateGameState(this.gameState);
        } catch (MalformedURLException | NotBoundException | RemoteException e) {
            System.out.println("cannot update server");
            //TODO : DEAL WITH CONNECTION EXCEPTION
        }
        return true;
    }

    public String[] getPrimaryAndBackup(String randomServer){
        //get the randomServer's primary and backup server
        GameHandler randomGameHandler = null;
        String[] res = new String[2];
        try {
            randomGameHandler = (GameHandler) Naming.lookup(randomServer);
            String primaryServer = randomGameHandler.getPrimary();
            String backupServer = randomGameHandler.getBackup();

            res[0] = primaryServer;
            res[1] = backupServer;
        } catch (MalformedURLException | NotBoundException | RemoteException e) {
            System.out.println("cannot getPrimaryAndBackup");
        }
        return res;
    }

    public void updateBothServer(String targetServer, String newPrimary, String newBackup){
        //tell the randomServer, who is the new primary and new backup
        GameHandler randomGameHandler = null;
        try {
            randomGameHandler = (GameHandler) Naming.lookup(targetServer);
            randomGameHandler.updatePrimary(newPrimary);
            randomGameHandler.updateBackup(newBackup);

        } catch (MalformedURLException | NotBoundException | RemoteException e) {
            System.out.println("cannot updatePrimaryAndBackup");
        }
    }

    public boolean updateLocal(GameState gameState, int opNum){
        //operate the move
        switch (opNum){
            case 0:
                break;
            case 1:
                if(this.gameState.valid(this.localPlayer.position[0], this.localPlayer.position[1]  - 1))
                    this.localPlayer.position[1]--;
                else
                    System.out.println("---invalid move---");
                break;
            case 2:
                if(this.gameState.valid(this.localPlayer.position[0] + 1, this.localPlayer.position[1]))
                    this.localPlayer.position[0]++;
                else
                    System.out.println("---invalid move---");
                break;
            case 3:
                if(this.gameState.valid(this.localPlayer.position[0], this.localPlayer.position[1]  + 1))
                    this.localPlayer.position[1]++;
                else
                    System.out.println("---invalid move---");
                break;
            case 4:
                if(this.gameState.valid(this.localPlayer.position[0] - 1, this.localPlayer.position[1]))
                    this.localPlayer.position[0]--;
                else
                    System.out.println("---invalid move---");
                break;
        }
        int x = this.localPlayer.position[0], y = this.localPlayer.position[1];
        if(gameState.mazeMap[x][y] == '*') {
            //if you find treasure
            this.localPlayer.score++;
            this.gameState.removeAndGenerate(x,y);
        }
        // update your local gamestate after your move
        this.gameState.scoreBoard.put(this.localPlayer.playerId,this.localPlayer.score);
        this.gameState.allPlayerPositions.put(this.localPlayer.playerId,this.localPlayer.position);
        return true;
    }

    public boolean move(int opNum){
        switch (opNum){
            case 0:
            case 1:
            case 2:
            case 3:
            case 4:
                // var currentBackup = xxx;
                retrieveFromServer(this.primaryServer);
                updateLocal(this.gameState, opNum);
                //primaryHandler.updateServerGamestate(String serverID, String localID, int opNum)
                //if cannot connect -> invalid move
                updateServer(this.primaryServer);
                updateServer(this.backupServer);
                //TODO : THIS PART SHOULD SYNCHRONIZE
                break;
            case 9:
                break;
            default:
                System.out.println("invalid operation");
                //System.exit(0);
        }
        return true;
    }


    public static void main(String[] args) throws RemoteException {


        //initialization
        String[] tracker = {args[0],args[1]}; // tracker IP, tracker port
        String playerId = args[2]; // player ID

        Game game = new Game(playerId, Integer.parseInt(tracker[1]));

        TrackerHandler trackerHandler = null;
        try {
            trackerHandler = (TrackerHandler) Naming.lookup("Tracker");
        }
        catch (NotBoundException | MalformedURLException | RemoteException e) {
            System.out.println("cannot connect to tracker !");
        }

        //retrieve basic game info from tracker and register yourself
        List<Object> trackerInfo = game.retrieveFromTracker(trackerHandler, playerId); // also register this player
        int N = (int) trackerInfo.get(0);
        int K = (int) trackerInfo.get(1);
        String firstPlayer = (String) trackerInfo.get(2);// TODO : SHOULD BE LAST PLAYER !
        int playerListLength = (int) trackerInfo.get(3);

        //initialize game state and set primary/backup servers
        game.initialize(firstPlayer,playerListLength,N,K);


        Scanner scanner = new Scanner(System.in);
        game.printMap();
        System.out.println("input operation(0- >stay | 1 ->left | 2 ->down | 3 ->right | 4 ->up | 9 ->exit) : ");

        //START PING THREAD
        PingThread pingThread = new PingThread(game);
        pingThread.start();

        //operation in loop
        while(scanner.hasNextInt()){
            int opNum = scanner.nextInt();
            if(opNum == 9) break;
            game.move(opNum);
            game.printMap();
            System.out.println("input operation(0- >stay | 1 ->left | 2 ->down | 3 ->right | 4 ->up | 9 ->exit) : ");
        }
        System.out.println("Exit");
    }
}
