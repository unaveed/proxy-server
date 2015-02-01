import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class Server
{
    public static void main(String[] args)
    {
        ServerSocket proxy = null;
        String message;
        BufferedReader reader;
        PrintStream printStream;
        Socket socket;
        int port = 2000;

        try
        {
            // Start a new socket at the specified port
            proxy = new ServerSocket(port);
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }

        try
        {
            socket = proxy.accept();
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            printStream = new PrintStream(socket.getOutputStream());
            String parrot = "I'm a parrot: ";

            while(true)
            {
                message = reader.readLine();
                String[] contents = message.split(" ");

                if(contents[0].equals("GET"))
                {
                    printStream.println("Valid command");
                    //TODO: This needs error checking on the url
                    String hostname = contents[1];

                    //TODO: Need to get the last part of the GET request

                    try
                    {
                        InetAddress address = InetAddress.getByName("www.cs.utah.edu");
                        //TODO: Port 80 needs to not be hard coded
                        Socket getRequest = new Socket("www.cs.utah.edu/~kobus/simple.html", 80);
                        BufferedReader response = new BufferedReader(new InputStreamReader(getRequest.getInputStream()));

                        String line;
                        while((line = response.readLine()) != null)
                        {
                            printStream.println(line);
                        }
                        response.close();
                        getRequest.close();
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                }
                else if(contents[0].equals("quit"))
                {
                    printStream.println("Closing server...");

                    reader.close();
                    printStream.close();
                    socket.close();

                    return;
                }
                else
                {
                    printStream.println(parrot + message);
                }
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }
}
