package main;

public class Player {
    public String playerName;
    public String playerId;
    public int[] position;
    public int score;
    public String IP;
    public String Port;

    public Player(String playerName, String playerId, String IP, String Port){
        this.playerName = playerName;
        this.playerId = playerId;
        this.score = 0;
        this.IP = IP;
        this.Port = Port;
    }
}
