package blockchain.drones;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Created by eric on 4/14/17.
 */
public class Utils {

    public static boolean checkParams(String powerExpected, String userID, String padID, HttpServletResponse response) throws ServletException, IOException {
        double power;
        try {
            power = Double.valueOf(powerExpected);
        } catch (NumberFormatException | NullPointerException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid parameters given");
            return false;
        }

        if (userID == null || padID == null || userID.equals("") || padID.equals("") || power <= 0) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid parameters given");
            return false;
        }
        return  true;
    }
}
