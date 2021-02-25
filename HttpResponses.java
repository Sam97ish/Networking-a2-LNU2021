import java.io.*;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.*;

public class HttpResponses {
    private String response;
    private Hashtable<String, String> RspHeaders;
    public TreeSet<String> forbidden;
    private int fname=1;

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


        int startDir = request.getFile().indexOf("/",1);
        if(startDir == -1 && request.getFile().contains("hello.html")){//If they're looking for a redirected file.
            return ERRORresponse(request,"302"); //redirecting to the file.
        }else {
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
                byte[] photoBytes = Files.readAllBytes(Paths.get(here.getAbsolutePath()+request.getFile()));
                String typePhoto = "image/png";
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

    public  byte[] POSTresponse(HttpMessage request) throws IOException {
        //TODO: construct response here.
        byte[] rsp = new byte[0];
        String[] POSTresponseHeaders;
        POSTresponseHeaders = new String[]{"StatusLine", "Date", "Content-Type", "Content-Length", "Last-Modified"};

        String img = request.getRequestBody().substring(request.getRequestBody().indexOf("png")+32);
        //System.out.println("img:" + img);
        byte[] image = img.getBytes();
        Files.write(new File("image.png").toPath(), image);


        int contentLenght = image.length;
        long lastMod = 0;
        String type = "image/png";
        byte[] head = GETresponseConstructor(POSTresponseHeaders,request,contentLenght,lastMod,type);

        rsp = new byte[head.length+image.length];

        ByteBuffer buff = ByteBuffer.wrap(rsp);

        buff.put(head); buff.put(image);

        rsp = buff.array();

        /*
        String imgHeader= request.getRequestHeaders().get("Content-Type");
        int endIndex=imgHeader.indexOf(";");
        String conType="";
        if(endIndex!=-1) {
            conType = imgHeader.substring(0, endIndex);
        }
        System.out.println(conType);

        switch(conType){//handling both txt and png.
            case "application/x-www-form-urlencoded":
                File here = new File(".");
                int strt=request.getRequestBody().indexOf("=");
                String data = request.getRequestBody().substring(strt+1).replace("+"," ");
                String template = "Your name is: ";
                File f = new File(here.getAbsolutePath()+String.valueOf(fname)+".txt");

                FileWriter w = new FileWriter(here.getAbsolutePath() + String.valueOf(fname)+".txt");
                w.write(template+data);
                w.close();

                String fin = template+data;
                int contentLenght = fin.getBytes().length;
                long lastMod = f.lastModified();
                String type = "text/html";
                byte[] body = fin.getBytes();
                byte[] head = GETresponseConstructor(POSTresponseHeaders,request,contentLenght,lastMod,type);

                rsp = new byte[head.length+body.length];

                ByteBuffer buff = ByteBuffer.wrap(rsp);

                buff.put(head); buff.put(body);

                rsp = buff.array();
                break;

            default: //default is text/html.

                break;
        }

 */
        return rsp;
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
        //System.out.println("=======================================================================================");

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
