package blockchain.drones;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;

// todo: time stamp, allow cancel in middle of charging

// Design questions: - How do we want to store userID and padID in our DB (as int, string?)
//                   - MESSAGING (contacting exactly the right pad, and extracting data)

@WebServlet(name = "ClientServlet")
public class ClientServlet extends HttpServlet {
    private static final String ARG_USER = "user";
    private static final String ARG_PAD = "pad";
    private static final String ARG_EXPECTED = "expected";

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String userID = request.getParameter(ARG_USER);
        String padID = request.getParameter(ARG_PAD);
        String powerExpected = request.getParameter(ARG_EXPECTED);

        DroneClient drone = DroneDB.loadDroneClient(userID);
        ChargingPad pad = DroneDB.loadChargingPad(padID);
        Transaction transaction = new Transaction(drone, pad, Double.valueOf(powerExpected));

        if (transaction.begin()) {
            Cache.add(transaction);
            // TODO: write okay back to response
        } else {
            // TODO: return error complaint
        }
    }



    @Override
    @SuppressWarnings("all")
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String userID = request.getParameter(ARG_USER);
        String padID = request.getParameter(ARG_PAD);
        String powerExpected = request.getParameter(ARG_EXPECTED);

        DroneClient drone = DroneDB.loadDroneClient(userID);
        ChargingPad pad = DroneDB.loadChargingPad(padID);
        Transaction active = new Transaction(drone, pad, Double.valueOf(powerExpected));
        if (Cache.contains(active)) {
            // still active
        } else {
            // not active, return payment results
        }

    }
}
