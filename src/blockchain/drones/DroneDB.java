package blockchain.drones;


final class DroneDB {

    public static DroneClient loadDroneClient(String id) {
        return new DroneClient("1", "abhinavrpatel-buyer@gmail.com");
    }


    public static ChargingPad loadChargingPad(String id) {
        return new ChargingPad("1", 3d, "abhinavrpatel-facilitator@gmail.com");
    }


    public static boolean store(ChargingPad pad) {
        return false;
    }


    public static boolean store(DroneClient client) {
        return false;
    }
}
