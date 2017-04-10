package blockchain.drones;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;

import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;


public class Transaction {
    private final DroneClient user;
    private final ChargingPad pad;

    private double powerExpected;

    //private static final String API_ENDPOINT = "https://svcs.sandbox.paypal.com/Invoice/";
    private static final String API_ENDPOINT = "https://api.sandbox.paypal.com";

    private static final String ACTION_CREATE = "/v1/invoicing/invoices"; // type = POST
    private static final String ACTION_SEND = "/v1/invoicing/invoices/%s/send";
    private static final String ACTION_FETCH = "/v1/invoicing/invoices/%s";

    private String invoiceID;


    /**
     * Builds an empty instance of a transaction between the given user and
     * charging pad. It also constructs a transaction where the user intends
     * to purchase the given amount of power
     *
     * @param user - The client, and the person who is purchasing the power
     * @param pad - The vendor/merchant who is selling the power
     * @param powerExpected - The amount of power that the client intends to buy
     */
    public Transaction(DroneClient user, ChargingPad pad, double powerExpected) {
        this.user = user;
        this.pad = pad;
        this.powerExpected = powerExpected;
    }



    @Override
    public int hashCode() {
        return invoiceID.hashCode();
    }


    @Override
    public boolean equals(Object o) {
        if (o == null || !o.getClass().equals(Transaction.class))
            return false;
        Transaction t = ((Transaction) o);
        return invoiceID.equals(t.invoiceID) && pad.equals(t.pad) && user.equals(t.user);
    }


    /**
     * Returns the client in this transaction
     */
    protected DroneClient getClient() {
        return user;
    }


    /**
     * Returns the merchant in this transaction
     */
    protected ChargingPad getPad() {
        return pad;
    }




    /**
     * Handles everything that must be taken care of before power is
     * transferred to the drone. It handles creation of the payment and
     * waiting until that payment is made.
     *
     * @return whether the payment procedure was successful
     */
    protected boolean begin() {
        invoiceID = createInvoice();
        if (invoiceID != null) {
            if(sendInvoice(invoiceID)) {
                try {
                    waitUntilPaid(invoiceID);
                } catch (DroneException e) {
                    // they cancelled the invoice and then the transaction
                    // TODO: handle this case
                    e.printStackTrace();
                    return false;
                }
            }

        }
        return false;
    }




    /**
     * Creates an invoice between the client and the merchant. The client is charged
     * in full for the power that they intended to use.
     *
     * @return the invoice ID if it was successfully created, else null
     */
    private String createInvoice() {
        try {
            String address = API_ENDPOINT + ACTION_CREATE;
            final JSONObject createPayload = buildCreatePayload();

            URL object = new URL(address);

            HttpURLConnection con = (HttpURLConnection) object.openConnection();
            con.setDoOutput(true);
            con.setDoInput(true);
            con.setRequestProperty("Content-Type", "application/json");
            con.setRequestProperty("Authorization", "Bearer " + API.getAccessToken());
            con.setRequestMethod("POST");

            //OutputStreamWriter wr = new OutputStreamWriter(con.getOutputStream());
            DataOutputStream wr = new DataOutputStream(con.getOutputStream());
            wr.write(createPayload.toString().getBytes("utf-8"));
            wr.flush();

            int responseCode = con.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                JSONObject createResponse = extractJSON(con.getInputStream());
                return createResponse.getString("id");
            } else {
                System.err.println(con.getResponseMessage());
                System.exit(1);
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
            System.exit(1);
        } catch (ProtocolException e) {
            e.printStackTrace();
            System.exit(1);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            System.exit(1);
        } catch (JSONException e) {
            e.printStackTrace();
            System.exit(1);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }

        return null;
    }



    /**
     * Sends an invoice reminder via email to the client to pay the invoice
     *
     * @param id - The ID of the Invoice
     * @return whether or not this invoice was sent successfully
     */
    private static boolean sendInvoice(String id) {
        try {
            String address = String.format(API_ENDPOINT + ACTION_SEND, id);
            URL object = new URL(address);
            HttpURLConnection con = (HttpURLConnection) object.openConnection();
            con.setDoOutput(true);
            con.setDoInput(true);
            con.setRequestProperty("Content-Type", "application/json");
            con.setRequestProperty("Authorization", "Bearer " + API.getAccessToken());
            con.setRequestMethod("POST");
            return true;
        } catch (MalformedURLException e) {
            e.printStackTrace();
            System.exit(1);
        } catch (ProtocolException e) {
            e.printStackTrace();
            System.exit(1);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
        return false;
    }





    /**
     * Waits until the invoice with the given ID is paid.
     *
     * @param invoice - The ID of the targeted invoice
     * @throws DroneException if the invoice was cancelled by the client
     */
    private void waitUntilPaid(String invoice) throws DroneException {
        long sleepTime = 1000L * 60; //millis, 1000 millis is one second
        do {
            try {
                Thread.sleep(sleepTime);
                System.out.println("Sleeping now for length: " + sleepTime);
            } catch (InterruptedException e) {
                e.printStackTrace();
                System.exit(1);
            }
        } while (!isInvoicePaid(invoice));
    }


    /**
     * Pings PayPal to check whether a client has completed payment on the
     * invoice with the given ID.
     *
     * @param invoice - the ID of the invoice of interest
     * @return whether the invoice is paid
     * @throws DroneException if the invoice is cancelled
     */
    protected static boolean isInvoicePaid(String invoice) throws DroneException {
        String address = String.format(API_ENDPOINT + ACTION_FETCH, invoice);
        // send GET to the given url, add content-type header and authorization headers
        // retrieve JSONObject and read "status" field
        try {
            URL endpoint = new URL(address);
            HttpURLConnection connection = (HttpURLConnection) endpoint.openConnection();
            connection.setDoInput(true);
            connection.setDoOutput(true);
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Authorization", "Bearer " + API.getAccessToken());
            connection.setRequestMethod("GET");

            int responseCode = connection.getResponseCode();

            if (responseCode == HttpURLConnection.HTTP_OK) {
                JSONObject response = extractJSON(connection.getInputStream());
                String status = response.getString("status");
                switch (status) {
                    case "PAID":
                    case "MARKED_AS_PAID":
                        return true;
                    case "SENT":
                    case "UNPAID":
                        return false;
                    default:
                        throw new DroneException(status);
                }
            } else {
                System.err.println(connection.getResponseMessage());
                System.exit(1);
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
            System.exit(1);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        } catch (JSONException e) {
            e.printStackTrace();
            System.exit(1);
        }
        return false;
    }



    protected boolean complete() {

        return true;
    }




    private static JSONObject extractJSON(InputStream input) {
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(input, "utf-8"));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line + "\n");
            }
            br.close();
            return new JSONObject(sb.toString());
        } catch (JSONException e) {
            e.printStackTrace();
            System.exit(1);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
        return null;
    }



    /**
     * Preparse JSON to be sent during the create invoice operation
     *
     * @return the prepared JSON
     */
    private JSONObject buildCreatePayload() {
        try {
            JSONObject result = new JSONObject();
            JSONObject merchantInfo = buildMerchantInfo(new JSONObject());
            JSONObject billingInfo = new JSONObject().put("email", user.getEmail());
            JSONArray items = buildCreateItems(new JSONArray());
            result.put("merchant_info", merchantInfo);
            result.put("billing_info", billingInfo);
            result.put("items", items);
            result.put("note", "this is the invoice for the drones project before transaction. Please pay before we give you power");
            return result;
        } catch (JSONException e) {
            e.printStackTrace();
            System.exit(1);
        }
        return null;
    }


    /**
     * Helper for buildCreatePayload. Builds the merchant_info JSON object
     * as required by PayPal's REST invoicing API
     *
     * @param json - the JSONObject to be built
     * @return the built JSONObject for merchant_info
     * @throws JSONException if there was error during creation
     */
    private JSONObject buildMerchantInfo(JSONObject json) throws JSONException {
        JSONObject phone = new JSONObject()
                .put("country_code", "001")
                .put("national_number", "4085551234");

        json.put("email", pad.getEmail())
                .put("first_name", "Abhinav")
                .put("last_name", "Patel")
                .put("business_name", "Blockchain at Berkeley's Drones Project")
                .put("phone", new JSONObject().put("country_code", "001")
                .put("phone", phone));
        return json;
    }


    /**
     * Helper for buildCreatePayload. Builds the items JSON array
     * as required by PayPal's REST invoicing API
     *
     * @param json - the JSONArray to be built
     * @return the built JSONArray for items
     * @throws JSONException if there was error during creation
     */
    private JSONArray buildCreateItems(JSONArray json) throws JSONException {
        JSONObject price = new JSONObject()
                .put("currency", "USD")
                .put("value", "" + pad.getUnitPrice());

        JSONObject item = new JSONObject();
        item.put("name", "Power for Drone")
                .put("quantity", powerExpected)
                .put("unit_price", price);
        return json;
    }

}
