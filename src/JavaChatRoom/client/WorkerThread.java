package JavaChatRoom.client;

import JavaChatRoom.server.ChatServer;

import java.io.*;
import java.net.Socket;

/**
 * @author Zach Pratt
 */
public class WorkerThread implements Runnable {

    protected Socket clientSocket = null;
    protected ChatServer chatServer;
    protected InputStream input;
    protected OutputStream output;

    public WorkerThread(Socket clientSocket, ChatServer chatServer) {
        this.clientSocket = clientSocket;
        this.chatServer = chatServer;
    }

    public void run() {
        // Open socket to receive connections
        while (!chatServer.isStopped()) {
            try (
                    PrintWriter out =
                            new PrintWriter(clientSocket.getOutputStream(), true);
                    BufferedReader in = new BufferedReader(
                            new InputStreamReader(clientSocket.getInputStream()))
            ) {
                System.out.println("Successfully Connected to client" + chatServer.getThreadPoolList().size());
                String inputLine;
                // This while echoes the output
                while ((inputLine = in.readLine()) != null) {
                    chatServer.sendChatMessage(inputLine);
                    // out.println(inputLine); // old code
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

    // Print chat message from server
    public void outputChatMessage(String message) throws IOException {
        output.write(message.getBytes());
    }

} // end of WorkerThread
