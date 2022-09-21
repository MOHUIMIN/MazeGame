package game;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class TrackerHandlerImpl extends UnicastRemoteObject implements TrackerHandler{

    public Tracker tracker;

    public TrackerHandlerImpl(Tracker tracker) throws RemoteException {
        super();
        this.tracker = tracker;
    }

    @Override
    public int getN() throws RemoteException{
        return this.tracker.N;
    }

    @Override
    public int getK() throws RemoteException{
        return this.tracker.K;
    }

    @Override
    public String getPlayer() throws RemoteException{
        return this.tracker.playerList.peekFirst();
        // TODO : SHOULD BE random PLAYER !
    }

    @Override
    public int getPlayerListLength() throws RemoteException{
        return this.tracker.playerList.size();
    }

    @Override
    public void register(String playerId) throws RemoteException{
        this.tracker.playerList.add(playerId);
    }

    @Override
    public void deletePlayer(String playerId) throws RemoteException{
        this.tracker.playerList.remove(playerId);
    }
}
