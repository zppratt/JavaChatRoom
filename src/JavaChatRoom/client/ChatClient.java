package JavaChatRoom.client;

import java.io.*;
import java.net.Socket;

/**
 * @author Zach Pratt
 */
public class ChatClient {

    protected static Socket mySocket;
    protected static String hostName;
    protected static int portNumber;
    protected static PrintWriter out;
    protected static BufferedReader in;
    protected static BufferedReader stdIn;

    public static void main(String[] args) throws Exception {
        if (args.length != 2) {
            System.err.println(
                    "Usage: java ChatClient <host name> <port number>");
            System.exit(1);
        }
        setHostName(args[0]);
        setPortNumber(Integer.parseInt(args[1]));
        System.out.println("Connecting to " + hostName + ":" + portNumber);
        openClientSocket();
        setupStreams();
        System.out.println("Successfully connected to " + hostName + ":" + portNumber);
        while (!stdIn.readLine().equals("/quit")) {
            System.out.println("Server response: " + in.readLine());
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
        mySocket = new Socket(hostName, portNumber);
    }

    private static void setupStreams() throws Exception {
        out = new PrintWriter(mySocket.getOutputStream(), true);
        in = new BufferedReader(
                new InputStreamReader(mySocket.getInputStream()));
        stdIn = new BufferedReader(
                new InputStreamReader(System.in));
    }
}
