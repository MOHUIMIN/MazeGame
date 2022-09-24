package game;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.Random;

public class PingThread extends Thread{
    public Game game;

    public PingThread(Game game){
        this.game = game;
    }

    public String getRandomPlayer(){
        int size = this.game.gameState.playerList.size();
        Random r = new Random();
        String randomPlayer = this.game.gameState.playerList.get(r.nextInt(size));
        return randomPlayer;
    }

    public boolean ping(String playerId){
        GameHandler gameHandler = null;
        try {
            gameHandler = (GameHandler) Naming.lookup(playerId);
            return gameHandler.ping();
        } catch (MalformedURLException | NotBoundException | RemoteException e) {
            //System.out.println("ping failed : " + playerId);
            return false;
        }
    }

    public void handleFail(String type, String failServer) throws MalformedURLException, NotBoundException, RemoteException {
        TrackerHandler trackerHandler = null;
        trackerHandler = (TrackerHandler) Naming.lookup("Tracker");

        synchronized (Naming.lookup(this.game.localPlayer.playerId)){

        System.out.println("fail type : " + type);
        System.out.println("fail server : " + failServer);
        System.out.println("Before My primary : " + this.game.primaryServer);
        System.out.println("Before My backup : " + this.game.backupServer);
        /*
         * fail type : primary
         * fail server : P2
         * Before My primary : P2
         * Before My backup : P3
         * After My primary : P3
         * After My backup : P2
         */


          /*
          PingThread start...
          fail type : primary
          fail server : P1
          Before My primary : P2
          Before My backup : P3
          bug !!!!!!!
          fail type : random
          fail server : P1
          Before My primary : P2
          Before My backup : P3
          After My primary : P2
          After My backup : P3
          */


        if(type.equals("primary") && !this.game.primaryServer.equals(failServer)){
            System.out.println("bug !!!!!!!");
            return;
        }
        if(type.equals("backup") && !this.game.backupServer.equals(failServer)){
            System.out.println("bug !!!!!!!");
            return;
        }
        if(type.equals("random") &&
                (this.game.primaryServer.equals(failServer) || this.game.backupServer.equals(failServer))
        ){
            System.out.println("bug !!!!!!!");
            return;
        }

        //if fail is from primary server, contact your backup server
        if(type.equals("primary")){
            GameHandler backupHandler = null;
            try {
                //TODO : BUG !!!!!鸠占鹊巢现象
                backupHandler = (GameHandler) Naming.lookup(this.game.backupServer);
                String backup_primary = backupHandler.getPrimary();//backup's primary
                String thisGame_primary = this.game.primaryServer;//this game's primary
                if(backup_primary.equals(thisGame_primary)){
                    //YOU ARE THE FIRST ONE THAT FIND THE CRASH
                    //update both yourself and the new primary server(former backup server)
                    backupHandler.updatePrimaryAndBackup(this.game.backupServer,this.game.localPlayer.playerId);
                    this.game.primaryServer = this.game.backupServer;
                    this.game.backupServer = this.game.localPlayer.playerId;
                }
                else {
                    //YOU ARE NOT THE FIRST ONE
                    String newPrimary = backupHandler.getPrimary();
                    String newBackup = backupHandler.getBackup();
                    if(newPrimary.equals(newBackup)){
                        // if backup server is the first one, you become new backup
                        backupHandler.updateBackup(this.game.localPlayer.playerId);
                        this.game.primaryServer = this.game.backupServer;
                        this.game.backupServer = this.game.localPlayer.playerId;
                    }
                    else{
                        this.game.primaryServer = newPrimary;
                        this.game.backupServer = newBackup;
                    }

                }
            } catch (MalformedURLException | NotBoundException | RemoteException e) {
                System.out.println("regenerate primary fail");
            }
            trackerHandler.deletePlayer(failServer);
        }

        //if fail is from backup server, contact your primary server
        else if(type.equals("backup")){
            GameHandler primaryHandler = null;
            try {
                primaryHandler = (GameHandler) Naming.lookup(this.game.primaryServer);
                String primary_backup = primaryHandler.getBackup();//primary's backup
                String thisGame_backup = this.game.backupServer;//this game's backup
                if(primary_backup.equals(thisGame_backup)){
                    //YOU ARE THE FIRST ONE THAT FIND THE CRASH
                    //update both yourself and the primary server
                    primaryHandler.updateBackup(this.game.localPlayer.playerId);
                    this.game.backupServer = this.game.localPlayer.playerId;
                }
                else {
                    //YOU ARE NOT THE FIRST ONE
                    if(primary_backup.equals(this.game.primaryServer)){
                        //if primary server is the first one, you become the new backup
                        primaryHandler.updateBackup(this.game.localPlayer.playerId);
                        this.game.backupServer = this.game.localPlayer.playerId;
                    }
                    else
                        this.game.backupServer = primaryHandler.getBackup();
                }
            } catch (MalformedURLException | NotBoundException | RemoteException e) {
                System.out.println("regenerate backup fail");
            }
            trackerHandler.deletePlayer(failServer);
        }
        //TODO : ELSE CLAUSE CAN BE DELETED ?

        //if fail is from random client, delete it globally and locally
        else {
            GameHandler primaryHandler = null;
            GameHandler backupHandler = null;

            try {
                trackerHandler.deletePlayer(failServer);

                primaryHandler = (GameHandler) Naming.lookup(this.game.primaryServer);
                backupHandler = (GameHandler) Naming.lookup(this.game.backupServer);

                primaryHandler.deletePlayer(failServer);
                backupHandler.deletePlayer(failServer);
                this.game.gameHandler.deletePlayer(failServer);


            }catch (MalformedURLException | NotBoundException | RemoteException e){
                System.out.println("delete dead player fail");
            }
        }
        System.out.println("After My primary : " + this.game.primaryServer);
        System.out.println("After My backup : " + this.game.backupServer);
        }
    }

    public void run(){
        //RUN THE thread IN THE FOLLOWING STEPS every 1.5 seconds:
        //1. PING PRIMARY server and handle fail if needed
        //2. Ping backup server and handle fail if needed
        //3. ping some random player and handle fail if needed
        System.out.println("PingThread start...");
        while (true)
        {
            String primaryServer = this.game.primaryServer;
            if(!this.ping(primaryServer)){
                //PRIMARY REGENERATE
                try {
                    handleFail("primary",primaryServer);
                } catch (MalformedURLException | NotBoundException | RemoteException e) {
                    e.printStackTrace();
                }
            }

            String backupServer = this.game.backupServer;
            if(!this.ping(backupServer)){
                //BACKUP REGENERATE
                try {
                    handleFail("backup",backupServer);
                } catch (MalformedURLException | NotBoundException | RemoteException e) {
                    e.printStackTrace();
                }
            }

            String randomPlayer = this.getRandomPlayer();
            if(!this.ping(randomPlayer)){
                //DEAL WITH NORMAL PING FAIL
                try {
                    handleFail("random",randomPlayer);
                } catch (MalformedURLException | NotBoundException | RemoteException e) {
                    e.printStackTrace();
                }
            }
            try{
                Thread.sleep(1500);//sleep for 1500 ms
            } catch (InterruptedException ex){
                //TODO : DO WHAT ?
            }
        }
    }
}
