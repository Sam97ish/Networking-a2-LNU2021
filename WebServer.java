/*
  TCPEchoServer.java
  A simple echo server with error handling
  Author: Husmu Aldeen ALKHAFAJI - ha223cz.
*/

import java.io.*;
import java.net.*;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Set;

public class WebServer extends Networking{
    public static String MYDIR;
    public static int MYPORT = 8080;
    public boolean readyToSend;


    /**
     * Constructor
     * @param ip : provided ip in the arguments.
     * @param port : provided port in the arguments.
     */
    public WebServer(String ip, int port) {
        super(ip, port);
        readyToSend = false;
    }

    /**
     * @role: Creates serverSocket and accetps client sockets and passes them to a handler thread.
     * @return: void.
     * @throws IOException : check ip, port.
     */
    @Override
    void contact() {
        try{
            //create a new server socket.
            ServerSocket ss = new ServerSocket(MYPORT);

            //start listening from here, creating a new socket using serverSocket everytime a new client contacts.
            while(true) {

                Socket s = ss.accept(); //connect with next client.

                Handler h = new Handler(s);//hand it over to client thread.

                Thread client = new Thread(h);
                client.start();//thread starts.
            }

        } catch (IOException e) {
            System.err.println("could not create the socket successfully, check the IP or the port: "+ getIP() + ", "+ getMYPORT());
            e.printStackTrace();
            System.exit(2);
        }


    }

    /**
     * @role:  A handler thread that controls the communication with a client socket through TCP.
     */
    class Handler implements Runnable{
        private final Socket client;
        public HttpMessage httpMsg; //to store http message components and to parse.
        public HttpResponses httpRsp; //to store response part and construct it.
        private byte[] data;
        private int dataReceived;
        int count = 0;
        boolean found = false;
        boolean dataStart = false;
        int collectIndx = 0;
        boolean writeFile= false;
        boolean wrote = false;
        int foundFirst = 0;

        public Handler(Socket c){
            httpMsg = new HttpMessage();
            httpRsp = new HttpResponses();
            client = c;
        }

        @Override
        public void run() {

            try {

                //input stream to receive messages.
                InputStream input = client.getInputStream();
                byte[] receiver = new byte[1000000]; //more than max tcp packet size, should be more than enough.

                while(true) {

                    int bytesRead = input.read(receiver);
                    dataReceived += bytesRead;

                    String receivedRequest ="";

                    ArrayList<Byte> byteArrayList = new ArrayList<>();

                    for (byte b : receiver) {
                        byteArrayList.add(b);
                    }

                    while(input.available()>0) {

                        bytesRead = input.read(receiver);

                        for (byte b : receiver) {
                            byteArrayList.add(b);
                        }
                    }

                    byte[] buf = new byte[byteArrayList.size()];

                    for(int i = 0; i < byteArrayList.size(); i++) {
                        buf[i] = byteArrayList.get(i);
                    }

                    if(bytesRead != -1){
                        //string received.
                        receivedRequest = new String(buf,0,bytesRead);
                    }

                    //System.out.println(bytesRead);
                    if(receivedRequest.contains("GET") || receivedRequest.contains("POST")){//new request, must parse headers.
                        //System.out.println("parsed");
                        HTTPrequestParser(receivedRequest, httpMsg);
                    }

                    if(httpMsg.getHttpMethod().equals("POST")){

                        if(data == null) {
                            data = new byte[buf.length];
                        }
                        byte[] boundary = httpMsg.getBoundaryBytes();


                        while (count<bytesRead){

                            //first char of boundary is matching
                            if(buf[count] == boundary[0] && !found){
                                //check the rest of them.
                                int boundcount = 0;
                                int bufcount = count;

                                while(boundcount < boundary.length && boundary[boundcount] == buf[bufcount]){
                                    boundcount++;
                                    bufcount++;
                                }

                                if(boundcount >= boundary.length){
                                    //System.out.println("Found first boundary");
                                    count = bufcount;

                                    if(foundFirst>=1) {
                                        System.out.println("Found the right boundary.");
                                        //foundFirst=0;
                                        found = true;
                                    }
                                    foundFirst++;
                                }
                            }

                            if(found && !dataStart){
                                //System.out.println("First boundary found, now looking for CRLF");
                                String crlf = "\r\n\r\n";
                                byte[] newline = crlf.getBytes();

                                if(buf[count] == newline[0]){
                                    //first one found, look for the rest.
                                    int linecount = 0;
                                    int buflinecount=count;

                                    while(linecount < newline.length && newline[linecount] == buf[buflinecount]){
                                        linecount++;
                                        buflinecount++;
                                    }

                                    if(linecount >= newline.length){
                                        System.out.println("Found new line after the boundary, data should start after");
                                        count = buflinecount;
                                        dataStart = true;

                                    }

                                }
                            }

                            if(dataStart && !writeFile){
                                //time to extract this bs.
                                int countdata = count;
                                boolean saveData = true;
                                byte[] finalBound = httpMsg.getFinalBoundaryBytes();
                                while (countdata<bytesRead){
                                    if(buf[countdata] == finalBound[0]){
                                        //check if we arrived at the end.

                                        int endBoundcount = 0;
                                        int bufEndcount = countdata;

                                        while(endBoundcount < finalBound.length && finalBound[endBoundcount] == buf[bufEndcount]){
                                            endBoundcount++;
                                            bufEndcount++;
                                        }

                                        if(endBoundcount >= finalBound.length){
                                            System.out.println("Found boundary end, time to rap this up");
                                            count = bufEndcount;
                                            countdata = bufEndcount;
                                            saveData = false;
                                            writeFile = true;
                                        }
                                    }

                                    if(saveData){
                                        //collect data.
                                        data[collectIndx]=buf[countdata];

                                        collectIndx++;

                                    }
                                    countdata++;
                                }
                                count = countdata;
                            }
                            if(writeFile){
                                /*
                                String test = new String(data,0,data.length);
                                File here = new File("");
                                File f = new File(here.getAbsolutePath()+"/test.txt");
                                FileWriter w = new FileWriter(here.getAbsolutePath() + "/test.txt");
                                w.write(test);
                                w.close();
                                 */
                                System.out.println("Writing the file");
                                if(wrote) {
                                    Files.write(Path.of("image.png"), data, StandardOpenOption.APPEND);
                                }else{
                                    Files.write(Path.of("image.png"), data, StandardOpenOption.CREATE);
                                    wrote = true;
                                }
                                //Files.write(new File("image.png").toPath(), data);

                            }
                            count++;
                            //bytesRead = input.read(buf);
                        }
                        /*
                        //extract data.
                        char[] seenChars = new char[4];
                        int count = 0;
                        do{
                            for(int i = 0; i<buf.length;i++){
                                byte ch = buf[i];
                                char letter = (char) ch;
                                seenChars[i%4] = letter;
                                System.out.println("Looking for CRLF, The letter is: " + letter);

                                if(seenChars[0] == 'C' && seenChars[1]== 'R' && seenChars[2]== 'L' && seenChars[3]== 'F'){
                                    count = count+1;
                                    System.out.println("FOUND CRLF");
                                }

                            }
                        }while(input.available()!=0);

                        System.out.println("Counter of CRLF: " + count);

                         */

                    }

                    byte[] responseMessage = HTTPresponseCreator(httpMsg, httpRsp);
                    if(readyToSend){

                        //Sending response.
                        if((!client.isClosed()) && responseMessage.length>0 && input.available()==0) {
                            System.out.println("Sending response dataReceive: "+dataReceived);
                            OutputStream output = client.getOutputStream();
                            output.write(responseMessage, 0, responseMessage.length);
                            output.flush();
                            readyToSend = false;
                        }
                    }

                    //System.out.println("The full received request is: \n" + receivedRequest);

                    //printing parsed message.
                    //System.out.println("The request line is:\n" + httpMsg.getRequestLine());
                    //System.out.println("The HTTP Method is: \n" + httpMsg.getHttpMethod());
                    //System.out.println("The file wanted is:\n " + httpMsg.getFile());
                    //System.out.println("HTTP version is:\n " + httpMsg.getHttpVersion());
                    //System.out.println("The headers are:\n " + httpMsg.getRequestHeaders().toString());
                    //System.out.println("The body is:\n" + httpMsg.getRequestBody());




                }
            } catch (IOException e) {
                System.err.println("Could not send or receive data, check IP and port " );
                e.printStackTrace();
                //System.exit(3);
            }catch(Exception e){
                System.err.println("Connection was lost");
                e.printStackTrace();
            }


        }
    }

    public static void main(String[] args){

        //checking if all arguments are met.
        if (args.length != 2) {
            System.err.printf("usage: %s port /public (directory for the web server)\n", args[1]);
            System.exit(1);
        }

        MYPORT = Integer.valueOf(args[0]);
        MYDIR = args[1];
        File dirCheck = new File("."+MYDIR);
        if(!dirCheck.isDirectory()){
            System.err.println("Something is wrong with your directory " + MYDIR);
            System.exit(20);
        }


        WebServer server = null;
        try {
            //create new TCP server.
            server = new WebServer(Inet4Address.getLocalHost().getHostAddress(), MYPORT);
            //start listening and handling clients.
            server.contact();
        } catch (UnknownHostException e) {
            System.err.println("Could not create TCP server!");
            e.printStackTrace();
            System.exit(5);
        }


    }

    public void HTTPrequestParser(String receivedRequest, HttpMessage msg){

        try {
            msg.httpParser(receivedRequest);
        } catch (HttpMessage.HttpFormatException e) {
            System.err.println("Could not parse request!\n");
            e.printStackTrace();
        } catch (IOException e) {
            System.err.println("Could not parse request!\n");
            e.printStackTrace();
        }
    }

    public byte[] HTTPresponseCreator(HttpMessage request, HttpResponses rsp){
        //WARNING, HTTPrequestParser must have been executed before this.

        byte[] response = null;

        switch (request.getHttpMethod()) {
            case "GET":
                //System.out.println("Inside switch statement.");
                try {
                    response = rsp.GETresponse(request,MYDIR);
                } catch (IOException e) {
                    response = rsp.ERRORresponse(request,"404");//file not found
                    e.printStackTrace();
                }
                break;
            case "POST":
                //TODO: implement this.
                try {
                    response = rsp.POSTresponse(request);
                } catch (IOException e) {
                    System.err.println("Could not write POST request body to file.");
                    response = rsp.ERRORresponse(request,"500");//internal error
                    e.printStackTrace();
                }
                break;
            default:
                //Respond with an error response.
                response = rsp.ERRORresponse(request,"500");//internal error
                break;
        }

        readyToSend = true;
        return response;
    }
}
