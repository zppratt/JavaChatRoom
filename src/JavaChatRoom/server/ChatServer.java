package JavaChatRoom.server;

import JavaChatRoom.client.WorkerThread;

import java.net.ServerSocket;
import java.net.Socket;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author Zach Pratt
 */
public class ChatServer implements Runnable {

    protected int serverPort = 8080;
    protected ServerSocket serverSocket;
    protected boolean isStopped;
    private ExecutorService threadPool = Executors.newFixedThreadPool(10);
    private HashMap<WorkerThread, Socket> threadPoolList = new HashMap<>();

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
        } catch (NumberFormatException e) {
            System.err.println(
                    "Usage: java ChatServer <port number>");
            System.exit(1);
        }
    }

    /**
     * The server thread.
     */
    public void run() {
        while (!isStopped()) {
            // Open sockets for this server instance and a client
            try (
                    ServerSocket serverSocket =
                            new ServerSocket(serverPort);
                    Socket clientSocket = serverSocket.accept()
            ) {
                System.out.println("Successfully Connected to client" + this.getThreadPoolList().size());
                // Setup and execute a worker thread for this client
                WorkerThread thread = new WorkerThread(clientSocket, this);
                this.getThreadPool().execute(thread);
                // Associate this instance of the client with its worker thread
                this.getThreadPoolList().put(thread, clientSocket);
            } catch (IOException e) {
                if (this.isStopped()) {
                    System.out.println("Server Stopped.");
                    break;
                }
                throw new RuntimeException(
                        "Error accepting client connection", e);
            }
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
        for (Map.Entry<WorkerThread, Socket> client : getThreadPoolList().entrySet()) {
            client.getKey().outputChatMessage(message);
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
    public HashMap<WorkerThread, Socket> getThreadPoolList() {
        return threadPoolList;
    }

} // end of ChatServer