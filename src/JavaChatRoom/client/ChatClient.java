package JavaChatRoom.client;

import java.io.*;
import java.net.Socket;

/**
 * @author Zach Pratt
 */
public class ChatClient {

    protected static String hostName;
    protected static int portNumber;

    public static void main(String[] args) throws Exception {
        if (args.length != 2) {
            System.err.println(
                    "Usage: java ChatClient <host name> <port number>");
            System.exit(1);
        }
        setHostName(args[0]);
        setPortNumber(Integer.parseInt(args[1]));

        System.out.println("Connecting to " + hostName + ":" + portNumber);
        try (
                Socket mySocket = new Socket(hostName, portNumber);
                PrintWriter out = new PrintWriter(mySocket.getOutputStream(), true);
                BufferedReader in = new BufferedReader(
                        new InputStreamReader(mySocket.getInputStream()));
                BufferedReader stdIn = new BufferedReader(
                        new InputStreamReader(System.in))
        ) {
            System.out.println("Successfully connected to " + hostName + ":" + portNumber);
            String userInput;
            while ((userInput = stdIn.readLine()) != null) {
                out.println(userInput);
                System.out.println("echo: " + in.readLine());
            }
        }
        System.out.println("Exiting...");
        System.exit(0);
    }

    private static void setHostName(String hn) {
        if (!"".equals(hostName)) {
            hostName = hn;
        } else {
            hostName = "localhost";
        }
    }

    private static void setPortNumber(int pn) {
        if (portNumber != 0) {
            portNumber = pn;
        } else {
            portNumber = 8080;
        }
    }

    private static void openClientSocket() throws Exception {

    }
}
