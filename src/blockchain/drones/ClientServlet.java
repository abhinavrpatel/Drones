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
        double power;
        try {
            power = Double.valueOf(powerExpected);
        } catch (NumberFormatException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid parameters given");
            return;
        }

        if (userID == null || padID == null || powerExpected == null
                || userID.equals("") || padID.equals("") || power <= 0) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid parameters given");
            return;
        }

        DroneClient drone = DroneDB.loadDroneClient(userID);
        ChargingPad pad = DroneDB.loadChargingPad(padID);
        Transaction transaction = new Transaction(drone, pad, power);

        if (transaction.begin()) {
            Cache.add(transaction);
            response.setStatus(HttpServletResponse.SC_CREATED);
        } else {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "We were unable to create your transaction");
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
