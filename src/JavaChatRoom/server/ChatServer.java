package JavaChatRoom.server;

import JavaChatRoom.client.WorkerThread;

import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author Zach Pratt
 */
public class ChatServer implements Runnable {

    protected int serverPort = 8080;
    protected ServerSocket serverSocket;
    protected boolean isStopped;
    protected Thread runningThread;
    protected ExecutorService threadPool = Executors.newFixedThreadPool(10);
    protected ArrayList<WorkerThread> threadPoolList = new ArrayList<>();

    public ChatServer(int port) {
        this.serverPort = port;
    }

    public static void main(String[] args) {
        // Check command line parameters
        if (args.length != 1) {
            System.err.println(
                    "Usage: java ChatServer <port number>");
            System.exit(1);
        }
        // Parse the port number from the command line and setup new server with it
        final int portNumber = Integer.parseInt(args[0]);
        ChatServer server = new ChatServer(portNumber);
        // Start the server
        new Thread(server).start();
    }

    // The server process
    public void run() {
        synchronized (this) {
            this.runningThread = Thread.currentThread();
        }
        // Open socket to receive connections
        openServerSocket();
        while (!isStopped()) {
            Socket clientSocket;
            try {
                // Wait for a connection
                clientSocket = this.serverSocket.accept();
                System.out.println("Successfully Connected to client" + this.threadPoolList.size());
                PrintWriter p = new PrintWriter(clientSocket.getOutputStream(), true);
                p.println("Hello");
            } catch (IOException e) {
                if (isStopped()) {
                    System.out.println("Server Stopped.");
                    break;
                }
                throw new RuntimeException(
                        "Error accepting client connection", e);
            }
            // Setup and execute a worker thread for this client
            WorkerThread thread = new WorkerThread(clientSocket, this);
            this.threadPool.execute(thread);
            this.threadPoolList.add(thread);
        }
        // Shutdown all workers
        this.threadPool.shutdown();
        this.stop();
        System.out.println("Server Stopped.");
    }

    private synchronized boolean isStopped() {
        return this.isStopped;
    }

    public synchronized void stop() {
        this.isStopped = true;
        try {
            this.serverSocket.close();
        } catch (IOException e) {
            throw new RuntimeException("Error closing server", e);
        }
    }

    // Open the socket for this server
    private void openServerSocket() {
        try {
            this.serverSocket = new ServerSocket(this.serverPort);
        } catch (IOException e) {
            throw new RuntimeException("Cannot open port 8080", e);
        }
    }

    public synchronized void sendChatMessage(String message) throws IOException {
        for (WorkerThread thread : threadPoolList) {
            thread.outputChatMessage(message);
        }
    }

} // end of ChatServer