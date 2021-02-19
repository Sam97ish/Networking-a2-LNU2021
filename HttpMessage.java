import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.Hashtable;

public class HttpMessage {
    private String requestLine;
    private String HttpMethod;
    private String file;
    private String HttpVersion;
    private Hashtable<String, String> requestHeaders;
    private String requestBody;


    public HttpMessage(){
        requestHeaders = new Hashtable<>();
    }


    public String getRequestLine() {
        return requestLine;
    }

    public String getHttpMethod() {
        return HttpMethod;
    }

    public void setHttpMethod(String httpMethod) {
        HttpMethod = httpMethod;
    }

    public String getFile() {
        return file;
    }

    public void setFile(String file) {
        this.file = file;
    }

    public String getHttpVersion() {
        return HttpVersion;
    }

    public void setHttpVersion(String httpVersion) {
        HttpVersion = httpVersion;
    }

    public void setRequestLine(String requestLine) {
        this.requestLine = requestLine;
    }

    public Hashtable<String, String> getRequestHeaders() {
        return requestHeaders;
    }

    public void setRequestHeaders(Hashtable<String, String> requestHeaders) {
        this.requestHeaders = requestHeaders;
    }

    public String getRequestBody() {
        return requestBody;
    }

    public void setRequestBody(String requestBody) {
        this.requestBody = requestBody;
    }

    private void appendHeader(String header) throws HttpFormatException {

        int index = header.indexOf(":");
        if (index == -1) {
            throw new HttpFormatException("Invalid Header: " + header);
        }
        //System.out.println(header);
        String headerKey = header.substring(0, index);
        String headerValue = header.substring(index + 1, header.length());
        //System.out.println(headerKey +", "+ headerValue);

        requestHeaders.put(headerKey, headerValue);
    }
    private void appendBody(String bodyln){
        requestBody = requestBody + bodyln + "\r\n";
    }

    public void httpParser(String msg) throws IOException, HttpFormatException {
        BufferedReader reader = new BufferedReader(new StringReader(msg));

        setRequestLine(reader.readLine());
        int index = requestLine.indexOf(" ");
        HttpMethod = requestLine.substring(0,index);
        int indexVersion = requestLine.indexOf(" ",index+1);
        file = requestLine.substring(index+1, indexVersion).trim();
        HttpVersion = requestLine.substring(indexVersion+1,requestLine.length()).trim();

        String header = reader.readLine();
        while (header.length() > 0) {
            appendHeader(header);
            header = reader.readLine();
        }

        String bodyLine = reader.readLine();
        while (bodyLine != null) {
            appendBody(bodyLine);
            bodyLine = reader.readLine();
        }
    }


    public static class HttpFormatException extends Exception {

        public HttpFormatException(String message) {
            super(message);
        }
    }
}
