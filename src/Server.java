import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;

//TODO: Close the connection when it is HTTP/1.1 and be able to get localhost type addresses
// TODO: Question for TA, does the port the proxy sends to need to be specified or just use 80?
public class Server
{
    private final String END = "\r\n";
    private final int CLIENTS_LIMIT = 2;
    private ServerSocket mProxy;
    private Socket mSocket;
    private int mPort;
    private int mCurrentConnections;
    private int mId;

    public Server()
    {
        mProxy = null;
        mSocket = null;
        mPort = 2112;
        mCurrentConnections = 0;
        mId = 0;
    }

    public Server(int port)
    {
        mProxy = null;
        mSocket = null;
        mPort = port;
        mCurrentConnections = 0;
        mId = 0;
    }

    /**
     * Start the proxy and open a socket to do GET requests.
     */
    public void initialize()
    {
        try
        {
            mProxy = new ServerSocket(mPort);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        while(true)
        {
            try
            {
                mSocket = mProxy.accept();
                mCurrentConnections++;
                if(mCurrentConnections > CLIENTS_LIMIT)
                {
                    PrintStream outMessage = new PrintStream(mSocket.getOutputStream());
                    outMessage.println("The server cannot accept anymore clients, please try again later.");
                    outMessage.close();
                    mSocket.close();
                }
                else
                {
                    ClientSocket client = new ClientSocket(this, mSocket, ++mId);
                    Thread threadedClient = new Thread(client);
                    threadedClient.start();
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }

    }



    public static void main(String[] args)
    {
        Server server;
        if (args.length > 0)
        {
            int port = Integer.parseInt(args[0]);
            server = new Server(port);
        }
        else
            server = new Server();

        server.initialize();
    }

    public void clientDisconnect()
    {
        mCurrentConnections--;
    }
}
