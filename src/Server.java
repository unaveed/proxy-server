import java.io.*;
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
                printStream.println(parrot + message);

            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }
}
