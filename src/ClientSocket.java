import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;

public class ClientSocket implements Runnable
{
    private final String END = "\r\n";          // The end characters for a socket or server
    private BufferedReader mReader;             // Reads input from the server
    private PrintStream mPrintStream;           // Sends information to the client
    private Server mServer;                     // Used to communicate with the server
    private Socket mClient;                     // The socket for the client
    private int mClientID;                      // The id of the client
    private Request mRequest;

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
            ArrayList<String> headers = new ArrayList<String>();
            String line = mReader.readLine();

            while(true)
            {
                if(line == null || line.equals(""))
                    break;

                System.out.println("Sent: " + line);
                headers.add(line);

                line = mReader.readLine();
            }

            if(headers.size() > 0)
            {
                String[] initialHeaderContents = headers.get(0).split(" ");
                System.out.println(Arrays.toString(initialHeaderContents));
                if(initialHeaderContents[0].equals("GET"))
                {
                    Request request = new Request(headers);

                    if(request.isValid())
                    {
                        // Send request
                        sendRequest(request);
                    }
                    else
                    {
                        // Send error code
                        mPrintStream.println("Unable to make a request object");
                    }
                }
                // TODO other HTTP methods
                else if(initialHeaderContents[0].equals("POST"))
                {
                    // Send not implemented
                    mPrintStream.println("Reached the not implemented thing");
                }
                else
                {
                    // Send other error code
                    mPrintStream.println("Reached the other error code");
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        finally
        {
            try
            {
                mReader.close();
                mPrintStream.close();
                mClient.close();
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }

    /**
     * Checks if the input is a mValid URL, returns hostname if it is. Null otherwise.
     */
    private String validURL(String input)
    {
        try
        {
            URI uri = new URI(input);
            return uri.getHost();
        }
        catch(Exception e)
        {
            return null;
        }
    }

    /**
     * Parses the mValid message sent by the server.
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
            writer.write(END + END);

            writer.flush();

            //Get Request
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

    private void sendRequest(Request request)
    {
        try
        {
            InetAddress address = InetAddress.getByName(request.getHostName());
            Socket requestSocket = new Socket(address, 80);

            BufferedWriter writer = new BufferedWriter(
                    new OutputStreamWriter(requestSocket.getOutputStream())
            );
            writer.write(request.generateRequest());
            writer.flush();

            //Get Request
            BufferedReader response = new BufferedReader(new InputStreamReader(requestSocket.getInputStream(),"UTF8"));
            String line;

            // Print response to the client
            while ((line = response.readLine()) != null)
            {
                mPrintStream.println(line);
//                System.out.println("reader: " + line);
            }

            System.out.println("Request...");
            System.out.println(request.generateRequest());

            writer.close();
            response.close();
            requestSocket.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}
