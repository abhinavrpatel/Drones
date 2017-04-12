package blockchain.drones;

import java.util.concurrent.ConcurrentHashMap;

class Cache extends ConcurrentHashMap<String, Transaction> {

    private Cache() {
        super();
    }

    private static volatile Cache activeTransactions = new Cache();

    private static volatile Cache completedTransactions = new Cache();

    public static void addActive(Transaction t) {
        activeTransactions.put(t.getPad().getID(), t);
    }

    public static boolean containsActive(Transaction t) {
        return activeTransactions.get(t.getPad().getID()).equals(t);
    }

    public static boolean removeActive(Transaction t)  {
        if (activeTransactions.remove(t.getPad().getID(), t)) {
            addCompleted(t);
            return true;
        }
        return false;
    }

    public static boolean containsCompleted(Transaction t) {
        return completedTransactions.containsKey(t.getPad().getID());
    }

    public static boolean removeCompleted(Transaction t) {
        return completedTransactions.remove(t.getPad().getID(), t);
    }

    private static void addCompleted(Transaction t) {
        completedTransactions.put(t.getPad().getID(), t);
    }
}
