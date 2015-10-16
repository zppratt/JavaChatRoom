package JavaChatRoom.client;

import java.io.*;
import java.net.Socket;

/**
 * @author Zach Pratt
 */
public class ChatClient {

    protected static String hostName;
    protected static int portNumber;

    /**
     * Receive input in a loop from the user and pass it along to the server and print out all responses.
     * @param args The hostname and port.
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        if (args.length != 2) {
            System.err.println(
                    "Usage: java ChatClient <host name> <port number>");
            System.exit(1);
        }
        setHostName(args[0]);
        setPortNumber(Integer.parseInt(args[1]));

        // Setup connection with server and in and out streams
        System.out.println("Connecting to " + hostName + ":" + portNumber);
        try (
                Socket mySocket = new Socket(hostName, portNumber);
                PrintWriter out = new PrintWriter(mySocket.getOutputStream(), true);
                BufferedReader in = new BufferedReader(
                        new InputStreamReader(mySocket.getInputStream()));
                BufferedReader stdIn = new BufferedReader(
                        new InputStreamReader(System.in))
        ) {
            // Send input from user to server and print response
            System.out.println("Successfully connected to " + hostName + ":" + portNumber);
            String userInput;
            while ((userInput = stdIn.readLine()) != null) {
                out.println(userInput);

                /**
                 * TODO don't wait for the user to input to print an output...
                 */

                System.out.println("from server: " + in.readLine());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("Exiting...");
        System.exit(0);
    }

    /**
     * Set the hostname for this client.
     * @param hn The hostname to set.
     */
    private static void setHostName(String hn) {
        if (!"".equals(hostName)) {
            hostName = hn;
        } else {
            hostName = "localhost";
        }
    }

    /**
     * Set the port number for this client.
     * @param pn The port number to set.
     */
    private static void setPortNumber(int pn) {
        if (portNumber != 0) {
            portNumber = pn;
        } else {
            portNumber = 8080;
        }
    }
}
