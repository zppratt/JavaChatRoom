package client;

import org.apache.commons.validator.routines.InetAddressValidator;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * @author Zach Pratt
 * @project Project 2
 * @course CS250
 * @date 18 October 2015
 * @duedate 20 October 2015
 * "By placing this statement in my work, I certify that I have read and understand the IPFW
Honor Code. I am fully aware of the following sections of the Honor Code: Extent of the
Honor Code, Responsibility of the Student and Penalty. This project or subject material
has not been used in another class by me or any other student. Finally, I certify that this
site is not for commercial purposes, which is a violation of the IPFW Responsible Use of
Computing (RUC) Policy."
 */
public class ChatClient {

    private static Socket socket;
    private static PrintWriter out;
    private static BufferedReader in;
    private static BufferedReader stdIn;

    /**
     * Receive input in a loop from the user and pass it along to the server and print out all responses.
     *
     * @param args The hostname and port.
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {

        checkArgs(args);

        // Setup connection with server and in and out streams
        try {
            System.out.println("Attempting connection to " + args[0] + ":" + Integer.valueOf(args[1]));
            socket = new Socket(args[0], Integer.valueOf(args[1])); // use command line arguments for host and port
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(
                    new InputStreamReader(socket.getInputStream()));
            stdIn = new BufferedReader(
                    new InputStreamReader(System.in));
        } catch (IOException e) {
            System.out.println("Error setting up connection to server. Is it running?");
            System.exit(1);
        }
        System.out.println("Successfully connected to " + socket.getInetAddress().getHostAddress()
                + ":" + socket.getPort());

        // Print any input from server
        Thread outputThread = new Thread(new Runnable() {
            @Override
            public void run() {
                String message;
                try {
                    while ((message = in.readLine()) != null) {
                        System.out.println(message);
                    }
                } catch (IOException e) {
                    System.out.println("Error with server stream.");
                    System.exit(1);
                }
            }
        });
        outputThread.start();

        // Read input from user
        String message;
        while ((message = stdIn.readLine()) != null) {
            out.println(message);
        }
    }

    /**
     * Checks the validity of the command line arguments
     *
     * @param args The command line arguments
     */
    private static void checkArgs(String[] args) {
        if (    !(args.length == 2) ||
                !(args[0].equals("localhost") ||  // "localhost" is a valid hostname
                InetAddressValidator.getInstance().isValid(args[0])) || // is valid inet address
                !(Integer.valueOf(args[1]) > 256) || // ports less than 256 reserved for well-known services
                !(Integer.valueOf(args[1]) < 65535) // maximum port value allowed
                ) {
            System.err.println(
                    "Usage: java ChatClient <host name> <port number>");
            System.exit(1);
        }
    }

} // end ChatClient
