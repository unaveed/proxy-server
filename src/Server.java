import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URI;

public class Server
{
    ServerSocket mProxy;
    String mMessage;
    BufferedReader mReader;
    PrintStream mPrintStream;
    Socket mSocket;
    int mPort;

    public Server()
    {
        mProxy = null;
        mPort = 2000;
    }
    public Server(int port)
    {
        mProxy = null;
        mPort = port;
    }

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
        //TODO: Delete this
        mPrintStream.println("Valid command");
        String hostname = "www." + uri.getHost();
        String path = uri.getPath();
        String flag = messageContents[2];
        String end = "\r\n";

        sendGetRequest(hostname, path, flag, end);
    }

    private void handleGetRequestTypeTwo(String[] messageContents, String hostname)
    {
        String path = messageContents[1];
        String flag = messageContents[2];
        String end = "\r\n";

        sendGetRequest(hostname, path, flag, end);
    }

    private void sendGetRequest(String hostname, String path, String flag, String end)
    {
        try
        {
            //TODO: Port 80 needs to not be hard coded
            InetAddress address = InetAddress.getByName(hostname);
            Socket getRequest = new Socket(address, 80);

            // Send header information
            BufferedWriter writer = new BufferedWriter(
                    new OutputStreamWriter(getRequest.getOutputStream(), "UTF8"));
            writer.write("GET" + path + flag + end);
            writer.write("Content-Length: " + 2 + end);
            writer.write("Content-Type: application/x-www-form-urlencoded" + end);
            writer.write(end);

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
        Server server = new Server();
        server.initialize();
    }
}
