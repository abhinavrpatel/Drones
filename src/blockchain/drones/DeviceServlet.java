package blockchain.drones;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * TODO: how expensive is it to read/write/construct java objects from database?
 *
 * -- if it isnt expensive, I will just store DroneClient and ChargingPad directly in DB, and load
 */
@WebServlet(name = "DeviceServlet")
public class DeviceServlet extends HttpServlet {
    private static final String ARG_USER = "user";
    private static final String ARG_PAD = "pad";
    private static final String ARG_CONSUMED = "consumed";

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String userID = request.getParameter(ARG_USER);
        String padID = request.getParameter(ARG_PAD);
        String powerConsumed = request.getParameter(ARG_CONSUMED);

    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // this would handle the periodic checkups by charging pads
    }
}
