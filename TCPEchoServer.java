/*
  TCPEchoServer.java
  A simple echo server with error handling
  Author: Husmu Aldeen ALKHAFAJI - ha223cz.
*/

import java.io.*;
import java.net.*;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

public class TCPEchoServer extends Networking{
    public static String MYDIR;
    public static int MYPORT = 8080;


    /**
     * Constructor
     * @param ip : provided ip in the arguments.
     * @param port : provided port in the arguments.
     */
    public TCPEchoServer(String ip, int port) {
        super(ip, port);

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

                h.run();//thread starts.
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
        private Socket client;
        public HttpMessage httpMsg; //to store http message components and to parse.
        public HttpResponses httpRsp; //to store response part and construct it.

        public Handler(Socket c){
            httpMsg = new HttpMessage();
            httpRsp = new HttpResponses();
            client = c;
        }

        @Override
        public void run() {

            try {
                while(true) {

                    byte[] buf = new byte[65535]; //max tcp packet size, should be more than enough.

                    //input stream to receive messages.
                    InputStream input = client.getInputStream();
                    int bytesRead = input.read(buf);

                    //string received.
                    String receivedRequest = new String(buf,0,bytesRead);
                    System.out.println("The full received request is: \n" + receivedRequest);

                    //TODO: parse the receivedRequest and determine what is wanted.

                    HTTPrequestParser(receivedRequest, httpMsg);

                    //TODO: Create response message and send it back to the client.

                    String responseMessage = HTTPresponseCreator(httpMsg, httpRsp);


                    //Sending received message
                    OutputStream output = client.getOutputStream();
                    output.write(responseMessage.getBytes(),0,responseMessage.length());
                    output.flush();

                    //System.out.printf("TCP echo request from %s", client.getInetAddress().getHostAddress());
                    //System.out.printf(" using port %d\n", client.getPort());

                }
            } catch (IOException e) {
                System.err.println("Could not send or receive data, check IP and port " );
                e.printStackTrace();
                System.exit(3);
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


        TCPEchoServer server = null;
        try {
            //create new TCP server.
            server = new TCPEchoServer(Inet4Address.getLocalHost().getHostAddress(), MYPORT);
            //start listening and handling clients.
            server.contact();
        } catch (UnknownHostException e) {
            System.err.println("Could not create TCP server!");
            e.printStackTrace();
            System.exit(5);
        }


    }

    public void HTTPrequestParser(String receivedRequest, HttpMessage msg){
        //TODO: extract the request parts here.

        try {
            msg.httpParser(receivedRequest);
        } catch (HttpMessage.HttpFormatException e) {
            System.err.println("Could not parse request!\n");
            e.printStackTrace();
        } catch (IOException e) {
            System.err.println("Could not parse request!\n");
            e.printStackTrace();
        }

        /*

        //printing parsed message.
        System.out.println("The request line is:\n" + msg.getRequestLine());
        System.out.println("The HTTP Method is: \n" + msg.getHttpMethod());
        System.out.println("The file wanted is:\n " + msg.getFile());
        System.out.println("HTTP version is:\n " + msg.getHttpVersion());
        System.out.println("The headers are:\n " + msg.getRequestHeaders().keySet().toString());
        System.out.println("The body is:\n" + msg.getRequestBody());

         */

    }

    public String HTTPresponseCreator(HttpMessage request, HttpResponses rsp){
        //WARNING, HTTPrequestParser must have been executed before this.

        String response = null;

        switch (request.getHttpMethod()) {
            case "GET":
                //System.out.println("Inside switch statement.");
                try {
                    response = rsp.GETresponse(request);
                } catch (FileNotFoundException e) {
                    response = rsp.ERRORresponse();
                    e.printStackTrace();
                }
                break;
            case "POST":
                //TODO: implement this.
                response = rsp.POSTresponse();
                break;
            default:
                //Respond with an error response.
                //TODO: implement this.
                response = rsp.ERRORresponse();
                break;
        }

        return response;
    }
}
