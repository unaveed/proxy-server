import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.net.URI;

public class ClientSocket implements Runnable
{
    private final String END = "\r\n";          // The end characters for a socket or server
    private BufferedReader mReader;             // Reads input from the server
    private PrintStream mPrintStream;           // Sends information to the client
    private Server mServer;                     // Used to communicate with the server
    private Socket mClient;                     // The socket for the client
    private int mClientID;                      // The id of the client

    public ClientSocket(Server server, Socket socket, int clientID)
    {
        mServer = server;
        mClient = socket;
        mClientID = clientID;
        try
        {
            mReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            mPrintStream = new PrintStream(socket.getOutputStream());

            System.out.println("Connection accepted with client " + clientID);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public void run()
    {
        try
        {
            while(true)
            {
                String message = mReader.readLine();
                System.out.println(message);

                if(message == null)
                    message = " ";

                String[] messageContents = message.split(" ");

                // Check that a GET command has been requested
                if("GET".equals(messageContents[0]))
                {
                    URI uri = new URI(messageContents[1]);
                    handleGetRequestTypeOne(messageContents, uri);
                }
                else if ("POST".equals(messageContents[0]) ||
                         "PUT".equals(messageContents[0]) ||
                         "DELETE".equals(messageContents[0]))
                {
                }
                // If a quit signal has been sent, close socket connection
                else if("quit".equals(messageContents[0]))
                {
                    break;
                }
                // Display an error message if the command is not recognized
                else
                {
                    mPrintStream.println("Invalid command.");
                }
            }

            System.out.println("Connection to client " + mClientID + " is closed.");

            // Notify the client is disconnecting and close the socket.
            mServer.clientDisconnected();
            mReader.close();
            mPrintStream.close();
            mClient.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

    }

    /**
     * Parses the valid message sent by the server.
     */
    private void handleGetRequestTypeOne(String[] messageContents, URI uri)
    {
        String hostname = uri.getHost();
        String path = uri.getPath();
        String flag = messageContents[2];

        sendGetRequest(hostname, path, flag);
    }

    private void handleGetRequestTypeTwo(String[] messageContents, String hostname)
    {
        String path = messageContents[1];
        String flag = messageContents[2];

        sendGetRequest(hostname, path, flag);
    }

    /**
     * Sends a request from the socket to the server based on the hostname and path
     */
    private void sendGetRequest(String hostname, String path, String flag)
    {
        try
        {
            //TODO: Port 80 needs to not be hard coded
            InetAddress address = InetAddress.getByName(hostname);
            Socket getRequest = new Socket(address, 80);

            //Send header information
            BufferedWriter writer = new BufferedWriter(
                    new OutputStreamWriter(getRequest.getOutputStream(), "UTF8"));
            writer.write("GET " + path + " " + flag + END);
            writer.write("Content-Type: application/x-www-form-urlencoded" + END);
            writer.write(END);

            writer.flush();

            //Get Response
            BufferedReader response = new BufferedReader(new InputStreamReader(getRequest.getInputStream()));
            String line;

            // Print response to the client
            while ((line = response.readLine()) != null)
            {
                mPrintStream.println(line);
            }

            writer.close();
            response.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}
