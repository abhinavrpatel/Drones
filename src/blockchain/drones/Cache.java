package blockchain.drones;

import java.util.Hashtable;

//TODO: figure out how to effectively use HashTable for ChargingPad, DroneClient, Transaction

// store objects in DB using hibernate (??)
class Cache extends Hashtable<String, Transaction> {

    private Cache() {
        super();
    }

    private static volatile Cache ourInstance = new Cache();

    public static Cache getInstance() {
        return ourInstance;
    }

    public static void add(Transaction t) {
        synchronized(ourInstance) {
            ourInstance.put(t.getPad().getID(), t);
        }
    }

    public static boolean contains(Transaction t) {
        return true;
    }

    public static boolean remove(Transaction t)  {
        return false;
    }
}
