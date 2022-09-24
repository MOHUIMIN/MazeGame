package game;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface TrackerHandler extends Remote {

    public int getN() throws RemoteException;
    public int getK() throws RemoteException;
    public String getPlayer() throws RemoteException;
    public int getPlayerListLength() throws RemoteException;
    public void register(String playerId) throws RemoteException;
    public void deletePlayer(String playerId) throws RemoteException;

}
