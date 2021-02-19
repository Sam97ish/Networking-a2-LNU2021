import java.io.File;
import java.io.FileNotFoundException;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.*;

public class HttpResponses {
    private String response;
    private Hashtable<String, String> RspHeaders;

    public HttpResponses(){
        RspHeaders = new Hashtable<>();
    }

    private String GETresponseConstructor(String[] headers, HttpMessage request, int conLength, long lastMod, String content){

        //first status line.
        RspHeaders.put(headers[0], request.getHttpVersion()+" 200 OK");

        //Date header.
        Date now = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("EEEE,MMMM d,yyyy h:mm,a", Locale.ENGLISH);
        sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
        String formattedDate = sdf.format(now);

        RspHeaders.put(headers[1], formattedDate);

        //Content-Type header.
        RspHeaders.put(headers[2],"text/html");

        //Content-Length
        RspHeaders.put(headers[3], String.valueOf(conLength));

        //last mod.
        Date mod = new Date(lastMod);
        SimpleDateFormat sdf2 = new SimpleDateFormat("EEEE,MMMM d,yyyy h:mm,a", Locale.ENGLISH);
        sdf2.setTimeZone(TimeZone.getTimeZone("GMT"));
        String formattedModDate = sdf2.format(mod);

        RspHeaders.put(headers[4], formattedModDate);

        String rsp = RspHeaders.get(headers[0]) + "\r\n"
                + headers[1] + ": " + RspHeaders.get(headers[1]) + "\r\n"
                + headers[2] + ": " + RspHeaders.get(headers[2]) + "\r\n"
                + headers[3] + ": " + RspHeaders.get(headers[3]) + "\r\n"
                + headers[4] + ": " + RspHeaders.get(headers[4]) + "\r\n"
                + "\r\n"
                + content;

        //System.out.println(rsp);

        return rsp;
    }


    public String GETresponse(HttpMessage request) throws FileNotFoundException {
        //TODO: construct response here.

        //File here = new File(".");
        //System.out.println(here.getAbsolutePath());

        File page = new File("."+request.getFile());
        Scanner scan = new Scanner(page);

        String contents="";

        while(scan.hasNextLine()){
            contents = contents + scan.nextLine() + "\r\n";
        }

        long lastMod = page.lastModified();
        int contentLenght = contents.getBytes().length;
        String[] GETresponseHeaders;
        GETresponseHeaders = new String[]{"StatusLine", "Date", "Content-Type", "Content-Length", "Last-Modified"};

        String rsp = GETresponseConstructor(GETresponseHeaders,request,contentLenght,lastMod,contents);


        return rsp;
    }

    public  String POSTresponse(){
        //TODO: construct response here.

        return null;
    }

    public String ERRORresponse(){
        //TODO: construct response here.

        return null;
    }
}
