package JavaChatRoom.server;

import JavaChatRoom.client.ChatClient;
import com.sun.xml.internal.ws.api.pipe.FiberContextSwitchInterceptor;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author Zach Pratt
 */
public class ChatServer implements Runnable {

    protected int serverPort = 8888;
    protected ServerSocket serverSocket;
    protected Socket clientSocket;
    protected Thread currentServer;
    protected boolean isStopped;
    private ExecutorService threadPool = Executors.newFixedThreadPool(10);
    private HashMap<String, WorkerThread> threadPoolList = new HashMap<>();

    /**
     * Setup a server on port.
     *
     * @param port The port to open for the server.
     */
    public ChatServer(int port) {
        this.serverPort = port;
    }

    /**
     * Setup the server on the entered port.
     *
     * @param args Must enter which port to open on.
     */
    public static void main(String[] args) {
        // Check command line parameters
        if (args.length != 1) {
            System.err.println(
                    "Usage: java ChatServer <port number>");
            System.exit(1);
        }
        // Parse the port number from the command line and setup new server with it
        try {
            final int serverPort = Integer.parseInt(args[0]);
            ChatServer server = new ChatServer(serverPort);
            new Thread(server).start();
        } catch (NumberFormatException e) { // if the port number can't be parsed...
            System.err.println(
                    "Usage: java ChatServer <port number>");
            System.exit(1);
        }
    }

    /**
     * The server thread.
     */
    public void run() {
        synchronized (this) {
            this.currentServer = Thread.currentThread();
        }
        // Open sockets for this server instance and a client
        try {
            serverSocket = new ServerSocket(serverPort);
            int i = 0; // iterator
            while (!isStopped()) {
                i++;
                // Setup and execute a worker thread for this client
                WorkerThread thread = new WorkerThread("Person " + i, serverSocket.accept(), this);
                this.getThreadPool().execute(thread);
            }
        } catch (IOException e) {
            if (this.isStopped()) {
                System.out.println("Server Stopped.");
            } else {
                System.out.println("Error starting server. Is there another instance already running?");
                System.exit(1);
            }
        }
        // Shutdown all workers
        this.getThreadPool().shutdownNow();
        this.stop();
        System.out.println("Server Stopped.");
    }

    /**
     * Whether or not the server is stopped
     *
     * @return isStopped
     */
    public synchronized boolean isStopped() {
        return this.isStopped;
    }

    /**
     * Stops the server and closes the server socket manually
     */
    public synchronized void stop() {
        this.isStopped = true;
        try {
            this.serverSocket.close();
            this.clientSocket.close();
        } catch (IOException e) {
            throw new RuntimeException("Error closing server", e);
        }
    }

    /**
     * Sends a chat message to all clients.
     *
     * @param message The message to send to all clients
     * @throws IOException
     */
    public synchronized void sendChatMessage(String message, WorkerThread sender) throws IOException {
        System.out.println("Sending a message to " + getThreadPoolList().size() + " clients");
        for (WorkerThread thread : getThreadPoolList().values()) {
            thread.printMessage(message, sender);
        }
    }

    /**
     * Returns the client threads created by this server instance.
     *
     * @return threadPool
     */
    public ExecutorService getThreadPool() {
        return threadPool;
    }

    /**
     * Returns a map of the associations between the workers and the clients.
     *
     * @return threadPoolList
     */
    public HashMap<String, WorkerThread> getThreadPoolList() {
        return threadPoolList;
    }

    /**
     * A worker to serve a client
     */
    public class WorkerThread implements Runnable {

        private String name;
        protected Socket clientSocket;
        protected ChatServer chatServer;
        protected BufferedReader in;
        protected PrintWriter out;

        public WorkerThread(String name, Socket clientSocket, ChatServer chatServer) {
            this.name = name;
            this.clientSocket = clientSocket;
            this.chatServer = chatServer;
        }

        /**
         * Take a message from a client and echo it back to all the other clients
         */
        public void run() {
            // Open socket to receive connections
            while (!chatServer.isStopped()) {
                try {
                    out = new PrintWriter(clientSocket.getOutputStream(), true);
                    in = new BufferedReader(
                            new InputStreamReader(clientSocket.getInputStream()));
                    System.out.println("Successfully Connected to " + getName());
                    name = promptClientForName(out, in); // get name from client
                    out.println("Hello " + name + "!");
                    getThreadPoolList().put(name, this);
                    chatServer.sendChatMessage(name + " has joined the chat.", null);
                    String inputLine;
                    // This while echoes the output
                    while ((inputLine = in.readLine()) != null && !inputLine.equals("/quit")) {
                        chatServer.sendChatMessage(inputLine, this);
                    }
                    sendChatMessage(getName() + " has left the chat.", null);
                    getThreadPoolList().remove(name);
                    out.close();
                    in.close();
                    return;
                } catch (IOException e) {
                    if (chatServer.isStopped()) {
                        System.out.println("Server Stopped.");
                        break;
                    }
                }
            }
        } // end of run()

        /**
         * Prints a message to the client associated with this thread.
         *
         * @param message The message to print.
         */

        public void printMessage(String message, WorkerThread sender) {
            if (sender != null) { // don't print sender for the join or leave message
                out.println(sender.getName() + " says: " + message);
            } else {
                out.println(message);
            }
        }

        /**
         * Get the name of this client.
         *
         * @return name
         */
        private String getName() {
            return name;
        }

        /**
         * Get the client's name
         *
         * @param out
         * @param in
         * @return
         */
        private String promptClientForName(PrintWriter out, BufferedReader in) throws IOException {
            out.println("What's your name?");
            return in.readLine();
        }

    } // end of WorkerThread

} // end of ChatServer