package blockchain.drones;

import java.util.concurrent.ConcurrentHashMap;

class Cache extends ConcurrentHashMap<String, Transaction> {

    private Cache() {
        super();
    }

    private static volatile Cache ourInstance = new Cache();

    public static void add(Transaction t) {
        ourInstance.put(t.getPad().getID(), t);
    }

    public static boolean contains(Transaction t) {
        return ourInstance.get(t.getPad().getID()).equals(t);
    }

    public static boolean remove(Transaction t)  {
        return ourInstance.remove(t.getPad().getID(), t);
    }
}
