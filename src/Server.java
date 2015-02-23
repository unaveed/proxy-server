import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;

//TODO: Close the connection when it is HTTP/1.1 and be able to get localhost type addresses
// TODO: Question for TA, does the port the proxy sends to need to be specified or just use 80?
public class Server
{
    private final int CLIENTS_LIMIT = 2000; // Set the number of concurrent clients
    private ServerSocket mProxy;            // Starts a server socket
    private Socket mSocket;                 // Opens a socket
    private int mPort;                      // Port which the server accepts requests
    private int mCurrentConnections;        // Number of concurrent connections
    private int mId;                        // ID's for clients joining the server

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
     * Start the proxy and open sockets for multiple clients to connect to.
     */
    public void initialize()
    {
        try
        {
            mProxy = new ServerSocket(mPort);
            System.out.println("Server started on port " + mPort);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        while(true)
        {
            try
            {
                // Accept new clients by starting a thread for each incoming connection
                mSocket = mProxy.accept();
                mCurrentConnections++;

                // Send message to client that the server is at it's limit for concurrent connections.
                // TODO: Maybe have a more 'official' error message
                if(mCurrentConnections > CLIENTS_LIMIT)
                {
                    PrintStream outMessage = new PrintStream(mSocket.getOutputStream());
                    outMessage.println("The server cannot accept anymore clients, please try again later.");
                    outMessage.close();
                    mSocket.close();
                }
                // Start a separate thread for each incoming client
                else
                {
                    ClientSocket client = new ClientSocket(this, mSocket, ++mId);
                    Thread threadedClient = new Thread(client);
                    threadedClient.start();
                    System.out.println(mCurrentConnections + " client(s) currently connected.");
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

        // Check if a port number was provided and use that for the server.
        if (args.length > 0)
        {
            int port = Integer.parseInt(args[0]);
            server = new Server(port);
        }
        else
            server = new Server();

        server.initialize();
    }

    /**
     * Signal that a client has disconnected.
     */
    public void clientDisconnected()
    {
        System.out.println("A client has disconnected. There are now " + mCurrentConnections + " clients connection.");
        mCurrentConnections--;
    }
}
