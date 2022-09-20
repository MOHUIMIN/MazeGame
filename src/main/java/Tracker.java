package main.java;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.concurrent.ConcurrentLinkedDeque;

public class Tracker {
    public int N;
    public int K;
    public ConcurrentLinkedDeque<String> playerList;
    public TrackerHandler trackerHandler;

    public Tracker(int N, int K) throws RemoteException {
        this.N = N;
        this.K = K;
        this.playerList = new ConcurrentLinkedDeque<>();
        this.trackerHandler = new TrackerHandlerImpl(this);
    }

    //TODO : DELETE DEAD PLAYER FROM playerList (USE REMOVE(Object o) function of deque)

    public static void main(String[] args) throws RemoteException {

        int port = Integer.parseInt(args[0]);
        int N = Integer.parseInt(args[1]);
        int K = Integer.parseInt(args[2]);

        Tracker tracker = new Tracker(N,K);

        try {
            Registry registry = LocateRegistry.createRegistry(port);
            registry.rebind("Tracker", tracker.trackerHandler);
            System.out.println(" Tracker is ready ...");
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

}
