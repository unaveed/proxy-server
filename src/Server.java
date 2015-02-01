import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URLEncoder;

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
                        //TODO: Port 80 needs to not be hard coded
                        InetAddress address = InetAddress.getByName("www.cs.utah.edu");
                        Socket getRequest = new Socket(address, 80);

                        // Send header information
                        BufferedWriter writer = new BufferedWriter(
                                new OutputStreamWriter(getRequest.getOutputStream(), "UTF8"));
                        writer.write("GET /~kobus/simple.html HTTP/1.0rn");
                        writer.write("Content-Length: " + 2 + "\r\n");
                        writer.write("Content-Type: application/x-www-form-urlencoded\r\n");
                        writer.write("\r\n");

                        writer.flush();

                        //Get Response
                        BufferedReader response = new BufferedReader(new InputStreamReader(getRequest.getInputStream()));
                        String line;

                        while((line = response.readLine()) != null)
                        {
                            System.out.println(line);
                        }

                        writer.close();
                        response.close();
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
