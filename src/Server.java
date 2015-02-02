import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URI;

public class Server
{
    final String END = "\r\n";
    ServerSocket mProxy;
    String mMessage;
    BufferedReader mReader;
    PrintStream mPrintStream;
    Socket mSocket;
    int mPort;

    public Server()
    {
        mProxy = null;
        mPort = 2112;
    }
    public Server(int port)
    {
        mProxy = null;
        mPort = port;
    }

    /**
     *  Start the proxy and open a socket to do GET requests.
     */
    public void initialize()
    {
        try
        {
            mProxy = new ServerSocket(mPort);
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }

        try
        {
            mSocket = mProxy.accept();
            mReader = new BufferedReader(new InputStreamReader(mSocket.getInputStream()));
            mPrintStream = new PrintStream(mSocket.getOutputStream());

            while(true)
            {
                mMessage = mReader.readLine();
                String[] messageContents = mMessage.split(" ");

                if(messageContents[0].equals("GET"))
                {
                    URI uri = new URI(messageContents[1]);
                    String host = uri.getHost();

                    if(host != null)
                    {
                        handleGetRequestTypeOne(messageContents, uri);
                    }
                    else
                    {
                        handleGetRequestTypeTwo(messageContents, mReader.readLine().split(" ")[1]);
                    }
                }
                else if(messageContents[0].equals("quit"))
                {
                    mPrintStream.println("Closing server...");

                    mReader.close();
                    mPrintStream.close();
                    mSocket.close();

                    return;
                }
                else
                {
                    mPrintStream.println("Invalid message format");
                }
            }
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

            while((line = response.readLine()) != null)
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

    public static void main(String[] args)
    {
        Server server;
        if(args.length > 0)
        {
            int port = Integer.parseInt(args[0]);
            server = new Server(port);
        }
        else
            server = new Server();

        server.initialize();
    }
}
