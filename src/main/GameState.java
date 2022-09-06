package main;

import java.util.*;

public class GameState {
    public char[][] mazeMap;
    public List<Player> playerList;
    public Map<Player,Integer> scoreBoard;
    public Map<Player,int[]> allPlayerPositions;

    public GameState(int N, int K){
        this.mazeMap = new char[N][N];
        //Firstly, fill with 'o' (not treasure)
        for(char[] row : mazeMap)
            Arrays.fill(row,'o');
        //Secondly, randomly generate K treasures
        for(int i = 0; i < K; i++){
            Random r = new Random();
            int newX = 0, newY = 0;
            while(mazeMap[newX][newY] != 'o'){
                newX = r.nextInt(mazeMap.length);
                newY = r.nextInt(mazeMap.length);
            }
            this.mazeMap[newX][newY] = '*';
        }
        this.playerList = new ArrayList<Player>();
        this.scoreBoard = new HashMap<Player,Integer>();
        this.allPlayerPositions = new HashMap<Player,int[]>();
    }

    public boolean removeAndGenerate(int x, int y){
        //remove treasure that is picked up by a player and generate a new treasure
        Random r = new Random();
        int newX = x, newY = y;
        while(mazeMap[newX][newY] != 'o'){
            newX = r.nextInt(mazeMap.length);
            newY = r.nextInt(mazeMap.length);
        }
        this.mazeMap[x][y] = 'o';
        this.mazeMap[newX][newY] = '*';
        return true;
    }

    public boolean valid(int x, int y){
        //TODO : Avoid two player in the same position
        if(x >= 0 && x < mazeMap.length && y >= 0 && y < mazeMap.length)
            return true;
        else
            return false;
    }
}
