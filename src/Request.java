import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

public class Request
{
    private String mHostName;
    private String mPath;
    private String mFlag;
    private String mErrorCode;
    private ArrayList<String> mHeaders;
    boolean mValid;
    private final String CRLF = "\r\n";


    public String getFlag()
    {
        return mFlag;
    }

    public String getHostName()
    {
        return mHostName;
    }

    public String getPath()
    {
        return mPath;
    }

    public boolean isValid()
    {
        return mValid;
    }

    public Request()
    {
        mHostName = null;
        mPath = null;
        mFlag = null;
        mValid = false;
    }

    public Request(List<String> headers)
    {
        buildRequest(headers);
    }

    private void buildRequest(List<String> headers)
    {
        if(headers.size() > 0)
        {
            String[] initialHeader = headers.get(0).split(" ");
            if(initialHeader.length > 2)
            {
                try
                {
                    mFlag = initialHeader[2];
                    if(mFlag.equals("HTTP/1.1") || !mFlag.equals("HTTP/1.0"))
                    {
                        setError(" ");
                        return;
                    }

                    URI uri = new URI(initialHeader[1]);
                    mHostName = uri.getHost();
                    mPath = uri.getPath();
                }
                catch (URISyntaxException e)
                {
                    try
                    {
                        String[] hostHeader = headers.get(1).split(" ");
                        URI uri = new URI(hostHeader[1]);
                        mPath = initialHeader[1];
                        mHostName = uri.getHost();

                    }
                    catch (Exception e2)
                    {
                        setError(" ");
                        return;
                    }
                }

                mValid = true;
                mHeaders = new ArrayList<String>();
                for(int i = 1; i < headers.size(); i++)
                {
                    if(headers.get(i).contains("Connection:"))
                    {
                        if(!headers.get(i).equals("Connection: close"))
                        {
                            mHeaders.add("Connection: close");
                            continue;
                        }
                    }

                    mHeaders.add(headers.get(i));
                }
            }
            else
            {
                setError("");
                return;
            }
        }
        else
        {
            setError("");
            return;
        }
    }

    public String generateRequest()
    {
        if(mValid)
        {
            StringBuilder request = new StringBuilder();
            request.append("GET ").append(mPath).append(" ").append(mFlag).append(CRLF);
            for(int i = 0; i < mHeaders.size(); i++)
                request.append(mHeaders.get(i)).append(CRLF);

            request.append(CRLF);
            return request.toString();
        }

        return null;
    }

    private void setError(String errorCode)
    {
        mHostName = null;
        mPath = null;
        mFlag = null;
        mValid = false;
        mErrorCode = errorCode;
    }
}
