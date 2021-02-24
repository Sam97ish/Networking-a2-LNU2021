import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.*;

public class HttpResponses {
    private String response;
    private Hashtable<String, String> RspHeaders;
    public TreeSet<String> forbidden;

    public HttpResponses(){

        RspHeaders = new Hashtable<>();
        forbidden = new TreeSet<String>();
        forbidden.add("real.html"); //forbidden file.
    }

    public HttpResponses(TreeSet<String> forbiddenList){

        RspHeaders = new Hashtable<>();
        forbidden = forbiddenList;
    }

    private byte[] GETresponseConstructor(String[] headers, HttpMessage request, int conLength, long lastMod, String type){

        //first status line.
        RspHeaders.put(headers[0], request.getHttpVersion()+" 200 OK");

        //Date header.
        Date now = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("EEEE,MMMM d,yyyy h:mm,a", Locale.ENGLISH);
        sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
        String formattedDate = sdf.format(now);

        RspHeaders.put(headers[1], formattedDate);

        //Content-Type header.
        RspHeaders.put(headers[2],type);

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
                + "\r\n";

        //System.out.println(rsp);

        return rsp.getBytes();
    }


    public byte[] GETresponse(HttpMessage request, String defaultDir) throws IOException {

        File here = new File("");
        //System.out.println(here.getAbsolutePath());
        String[] GETresponseHeaders;
        GETresponseHeaders = new String[]{"StatusLine", "Date", "Content-Type", "Content-Length", "Last-Modified"};


        //Checking if any directory is specified.
        if(request.getFile().equals("/")){
            request.setFile(defaultDir);
        }

        int startDir = request.getFile().indexOf("/",1);
        if(startDir == -1 && request.getFile().contains("hello.html")){//If they're looking for a redirected file.
            return ERRORresponse(request,"302"); //redirecting to the file.
        }else if (startDir == -1){
            request.setFile(defaultDir+request.getFile());
        }

        //getting the extension of the file.
        String fileWanted = request.getFile();
        int index = fileWanted.indexOf(".");
        String ext = fileWanted.substring(index+1);
        byte[] rsp;

        File dirCheck = new File(here.getAbsolutePath()+request.getFile());
        //check if any file is wanted.
        if(dirCheck.isDirectory()){
            request.setFile(request.getFile() + "/index.html");
        }

        //checking if file wanted is forbidden.
        for(String x : forbidden){
            //System.out.println("Here");
            if (request.getFile().contains(x)){
                return ERRORresponse(request,"403");
            }
        }

        switch (ext){
            case "png":

                File photo = new File(here.getAbsolutePath()+request.getFile());
                byte[] photoBytes = Files.readAllBytes(Paths.get("."+request.getFile()));
                String typePhoto = "png";
                long lastModPhoto = photo.lastModified();
                int contentLenghtPhoto = photoBytes.length;

                byte[] headPhoto = GETresponseConstructor(GETresponseHeaders,request,contentLenghtPhoto,lastModPhoto,typePhoto);

                rsp = new byte[headPhoto.length+photoBytes.length];

                ByteBuffer buffPhoto = ByteBuffer.wrap(rsp);

                buffPhoto.put(headPhoto); buffPhoto.put(photoBytes);

                rsp = buffPhoto.array();

                break;
            default:
                //System.out.println(request.getFile());

                File page = new File(here.getAbsolutePath()+request.getFile());
                Scanner scan = new Scanner(page);
                String type = "text/html";
                String contents="";

                while(scan.hasNextLine()){
                    contents = contents + scan.nextLine() + "\r\n";
                }

                scan.close();
                long lastMod = page.lastModified();
                int contentLenght = contents.getBytes().length;

                byte[] head = GETresponseConstructor(GETresponseHeaders,request,contentLenght,lastMod,type);
                byte[] body = contents.getBytes();

                rsp = new byte[head.length+body.length];

                ByteBuffer buff = ByteBuffer.wrap(rsp);

                buff.put(head); buff.put(body);

                rsp = buff.array();
                break;

        }


        return rsp;
    }

    public  byte[] POSTresponse(){
        //TODO: construct response here.

        return null;
    }

    private byte[] ErrorConstructor(String[] headers, HttpMessage request, int conLength, long lastMod, String type, String errorCode){

        //first status line.
        RspHeaders.put(headers[0], request.getHttpVersion()+" "+errorCode+ " Error");

        //Date header.
        Date now = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("EEEE,MMMM d,yyyy h:mm,a", Locale.ENGLISH);
        sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
        String formattedDate = sdf.format(now);

        RspHeaders.put(headers[1], formattedDate);

        //Content-Type header.
        RspHeaders.put(headers[2],type);

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
                + headers[4] + ": " + RspHeaders.get(headers[4]) + "\r\n";

        if (errorCode.equals("302")){
            rsp = rsp + "Location: " + "memes/hello.html" + "\r\n";
        }

        rsp = rsp + "\r\n";

        //System.out.println(rsp);

        return rsp.getBytes();
    }

    public byte[] ERRORresponse(HttpMessage request, String errorCode){
        String[] errorHeaders;
        errorHeaders = new String[]{"StatusLine", "Date", "Content-Type", "Content-Length", "Last-Modified"};
        File page;
        switch (errorCode){
            case "500":
                page = new File("public/errors/internalError.html");
                break;
            case "403":
                page = new File("public/errors/forbidden.html");
                break;
            case "302":
                page = new File("public/errors/redirect.html");
                break;
            default:
                page = new File("public/errors/error.html");
                break;
        }


        Scanner scan = null;
        try {
            scan = new Scanner(page);
        } catch (FileNotFoundException e) {
            System.err.println("could not find error.html");
            e.printStackTrace();
            System.exit(8);
        }
        String type = "text/html";
        String contents="";

        byte[] rsp;

        while(scan.hasNextLine()){
            contents = contents + scan.nextLine() + "\r\n";
        }

        scan.close();

        long lastMod = page.lastModified();
        int contentLenght = contents.getBytes().length;

        byte[] head = ErrorConstructor(errorHeaders,request,contentLenght,lastMod,type,errorCode);
        byte[] body = contents.getBytes();

        rsp = new byte[head.length+body.length];

        ByteBuffer buff = ByteBuffer.wrap(rsp);

        buff.put(head); buff.put(body);

        rsp = buff.array();

        return rsp;
    }
}
