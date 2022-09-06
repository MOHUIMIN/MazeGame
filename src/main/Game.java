package main;

import java.util.Scanner;

public class Game {
    public String[] tracker; //[0] : IP ADDRESS; [1] : PORT NUMBER
    public Player localPlayer;
    public String[] primaryServer; //[0] : IP ADDRESS; [1] : PORT NUMBER
    public String[] backupServer;//[0] : IP ADDRESS; [1] : PORT NUMBER
    public GameState gameState;

    public Game(String playerName, String playerId){
        this.localPlayer = new Player(playerName,playerId,"IP","PORT") ;// TODO: use real IP
        this.localPlayer.position = new int[]{0,0}; // TODO: randomly generate a valid position
        this.primaryServer = new String[2];
        this.backupServer = new String[2];
        this.gameState = new GameState(5,5);

    }

    //TODO : public boolean renderView(){return true;}
    public boolean printMap(){
        for(int i = 0; i < gameState.mazeMap.length; i++){
            for(int j = 0; j < gameState.mazeMap.length; j++){
                if(i == this.localPlayer.position[0] && j == this.localPlayer.position[1])
                    System.out.print("▲" + "   ");
                else
                    System.out.print(gameState.mazeMap[i][j] + "   ");
            }
            System.out.println();
        }
        System.out.println("position: " + this.localPlayer.position[0] + "," + this.localPlayer.position[1]);
        System.out.println("score: " + this.localPlayer.score);
        return true;
    }

    public boolean retrieveFromTracker(String[] tracker){
        //TODO: Really connect to the tracker
        this.primaryServer[0] = this.localPlayer.IP;
        this.primaryServer[1] = this.localPlayer.Port;
        this.backupServer[0] = this.localPlayer.IP;
        this.backupServer[1] = this.localPlayer.Port;
        return true;
    }

    public boolean retrieveFromServer(String[] primaryServer){
        if(this.primaryServer[0].equals(this.localPlayer.IP) && this.primaryServer[1].equals(this.localPlayer.Port))
            //if localplayer is primary server, do nothing
            return true;
        //TODO: really retrieve from server
        return true;
    }

    public boolean updateServer(String[] primaryServer) {
        if (this.primaryServer[0].equals(this.localPlayer.IP) && this.primaryServer[1].equals(this.localPlayer.Port)){
            //if localplayer is primary server, just update local game state
            this.gameState.allPlayerPositions.put(this.localPlayer, this.localPlayer.position);
            this.gameState.scoreBoard.put(this.localPlayer, this.localPlayer.score);
        }
        //TODO: really update the server
        return true;
    }

    public boolean updateLocal(GameState gameState, int opNum){
        switch (opNum){
            case 0:
                break;
            case 1:
                if(this.gameState.valid(this.localPlayer.position[0], this.localPlayer.position[1]  - 1))
                    this.localPlayer.position[1]--;
                else
                    System.out.println("invalid move");
                break;
            case 2:
                if(this.gameState.valid(this.localPlayer.position[0] + 1, this.localPlayer.position[1]))
                    this.localPlayer.position[0]++;
                else
                    System.out.println("invalid move");
                break;
            case 3:
                if(this.gameState.valid(this.localPlayer.position[0], this.localPlayer.position[1]  + 1))
                    this.localPlayer.position[1]++;
                else
                    System.out.println("invalid move");
                break;
            case 4:
                if(this.gameState.valid(this.localPlayer.position[0] - 1, this.localPlayer.position[1]))
                    this.localPlayer.position[0]--;
                else
                    System.out.println("invalid move");
                break;
        }
        int x = this.localPlayer.position[0], y = this.localPlayer.position[1];
        if(gameState.mazeMap[x][y] == '*') {
            this.localPlayer.score++;
            this.gameState.removeAndGenerate(x,y);
        }
        return true;
    }

    public boolean move(int opNum){
        switch (opNum){
            case 0:
            case 1:
            case 2:
            case 3:
            case 4:
                retrieveFromServer(this.primaryServer);
                updateLocal(this.gameState, opNum);
                updateServer(this.primaryServer);
                break;
            case 9:
                break;
            default:
                System.out.println("invalid operation");
                //System.exit(0);
        }
        return true;
    }

    public static void main(String[] args) {
        //initialization
        String[] tracker = {"IP","Port"};
        Game game = new Game("Name","ID");
        game.retrieveFromTracker(tracker);
        game.retrieveFromServer(game.primaryServer);

        //operation in loop
        Scanner scanner = new Scanner(System.in);
        game.printMap();
        System.out.println("input operation(0- >stay | 1 ->left | 2 ->down | 3 ->right | 4 ->up | 9 ->exit) : ");

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
