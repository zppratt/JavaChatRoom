package JavaChatRoom.client;

import java.awt.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import org.apache.commons.validator.routines.InetAddressValidator;

import javax.swing.*;

/**
 * @author Zach Pratt
 */
public class ChatClient {

    private static Socket socket;
    private static PrintWriter out;
    private static BufferedReader in;
    private static BufferedReader stdIn;
    private static JFrame window;
    private static TextArea chat;

    /**
     * Receive input in a loop from the user and pass it along to the server and print out all responses.
     *
     * @param args The hostname and port.
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {

        //Schedule a job for the event-dispatching thread:
        //creating and showing this application's GUI.
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                createAndShowGUI();
            }
        });

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
        if (!(args[0].equals("localhost") ||  // "localhost" is a valid hostname
                InetAddressValidator.getInstance().isValid(args[0])) || // is valid inet address
                !(Integer.valueOf(args[1]) > 256) || // ports less than 256 reserved for well-known services
                !(Integer.valueOf(args[1]) < 65535) // maximum port value allowed
                ) {
            System.err.println(
                    "Usage: java ChatClient <host name> <port number>");
            System.exit(1);
        }
    }

    /**
     * Create the GUI and show it.  For thread safety,
     * this method should be invoked from the
     * event-dispatching thread.
     */
    private static void createAndShowGUI() {
        //Create and set up the window.
        window = new JFrame("Chat Client");
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        chat = new TextArea();
        window.setPreferredSize(new Dimension(500, 300));
        window.getContentPane().add(chat, BorderLayout.CENTER);

        //Display the window.
        window.pack();
        window.setVisible(true);
    }

} // end ChatClient
