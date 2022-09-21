package game;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class GameHandlerImpl extends UnicastRemoteObject implements GameHandler{

    public Game game;

    public GameHandlerImpl(Game game) throws RemoteException {
        super();
        this.game = game;
    }

    @Override
    public GameState getGameState() throws RemoteException{
        return this.game.gameState;
    }

    @Override
    public String getPrimary() throws RemoteException{
        return this.game.primaryServer;
    }

    @Override
    public String getBackup() throws RemoteException{
        return this.game.backupServer;
    }

    @Override
    public void updateGameState(GameState gameState) throws RemoteException{
        this.game.gameState = gameState;
    }

    @Override
    public synchronized void updatePrimary(String newPrimary) throws RemoteException{
        this.game.primaryServer = newPrimary;
    }

    @Override
    public synchronized void updateBackup(String newBackup) throws RemoteException{
        this.game.backupServer = newBackup;
    }

    @Override
    public synchronized void updatePrimaryAndBackup(String newPrimary, String newBackup) throws RemoteException{
        this.game.primaryServer = newPrimary;
        this.game.backupServer = newBackup;
    }

    @Override
    public boolean ping() throws RemoteException {
        return true;
    }
    @Override
    public void deletePlayer(String playerId) throws RemoteException{
        this.game.gameState.playerList.remove(playerId);
        this.game.gameState.allPlayerPositions.remove(playerId);
        this.game.gameState.scoreBoard.remove(playerId);
    }
}
