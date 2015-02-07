import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.net.URI;

/**
 * Created by mantis on 2/7/15.
 */
public class ClientSocket implements Runnable
{
    private final String END = "\r\n";
    private BufferedReader mReader;
    private PrintStream mPrintStream;
    private Server mServer;
    private Socket mClient;
    private int mClientID;

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
                String[] messageContents = message.split(" ");

                if("GET".equals(messageContents[0]))
                {
                    URI uri = new URI(messageContents[1]);
                    handleGetRequestTypeOne(messageContents, uri);
                }
                else if("quit".equals(messageContents[0]))
                {
                    break;
                }
                else
                {
                    mPrintStream.println("Invalid command.");
                }
            }

            System.out.println("Connection to client " + mClientID + " is closed.");
            mServer.clientDisconnect();
            mReader.close();
            mPrintStream.close();
            mClient.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

    }

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
