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
        try {
            input = clientSocket.getInputStream();
            output = clientSocket.getOutputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(input, "UTF-8"));

            String line;
            // Read in client text and send back to client
            while (true) {
                line = reader.readLine();

                System.out.println(line);

                if (line.equals("/quit")) {
                    break;
                }
                chatServer.sendChatMessage(line);
            }
            output.close();
            input.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    } // end of run()

    // Print chat message from server
    public void outputChatMessage(String message) throws IOException {
        output.write(message.getBytes());
    }

} // end of WorkerThread
