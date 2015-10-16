package JavaChatRoom.server;

import com.sun.xml.internal.ws.api.pipe.FiberContextSwitchInterceptor;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
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
    private ArrayList<WorkerThread> threadPoolList = new ArrayList<>();

    /**
     * Setup a server on port.
     * @param port The port to open for the server.
     */
    public ChatServer(int port) {
        this.serverPort = port;
    }

    /**
     * Setup the server on the entered port.
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
            try (
                    ServerSocket s = new ServerSocket(serverPort)
            ) {
                serverSocket = s;
                String clientName; // store client name
                int i = 0; // iterator
                while(!isStopped()) {
                    i++;
                    // Setup and execute a worker thread for this client
                    WorkerThread thread = new WorkerThread("Person " + i, serverSocket.accept(), this);
                    this.getThreadPool().execute(thread);
                    // Now kith
                    this.getThreadPoolList().add(thread);
                }
            } catch (IOException e) {
                if (this.isStopped()) {
                    System.out.println("Server Stopped.");
                    //break;
                }
                throw new RuntimeException(
                        "Error accepting client connection", e);
            }
        // Shutdown all workers
        this.getThreadPool().shutdown();
        this.stop();
        System.out.println("Server Stopped.");
    }

    /**
     * Whether or not the server is stopped
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
     * @param message The message to send to all clients
     * @throws IOException
     */
    public synchronized void sendChatMessage(String message) throws IOException {
        for (WorkerThread thread: threadPoolList) {
            thread.printMessage(message);
        }
    }

    /**
     * Returns the client threads created by this server instance.
     * @return threadPool
     */
    public ExecutorService getThreadPool() {
        return threadPool;
    }

    /**
     * Returns a map of the associations between the workers and the clients.
     * @return threadPoolList
     */
    public ArrayList<WorkerThread> getThreadPoolList() {
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
                try (
                        PrintWriter o = new PrintWriter(clientSocket.getOutputStream(), true);
                        BufferedReader i = new BufferedReader(
                                new InputStreamReader(clientSocket.getInputStream()))
                ) {
                    System.out.println("Successfully Connected to " + getName());
                    out = o;
                    in = i;
                    String inputLine;
                    // This while echoes the output
                    while ((inputLine = in.readLine()) != null) {
                        chatServer.sendChatMessage(inputLine);
                    }
                } catch (IOException e) {
                    if (chatServer.isStopped()) {
                        System.out.println("Server Stopped.");
                        break;
                    }
                    throw new RuntimeException(
                            "Error accepting client connection", e);
                }
            }
        } // end of run()

        /**
         * Prints a message to the client associated with this thread.
         * @param message The message to print.
         */
        public void printMessage(String message) {
            out.println("Worker says:" + message);
        }

        /**
         * Get the name of this client.
         * @return name
         */
        private String getName() {
            return name;
        }

    } // end of WorkerThread

} // end of ChatServer