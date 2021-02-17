/*
  TCPEchoClient.java
  A simple echo client with error handling
  Author: Husmu Aldeen ALKHAFAJI - ha223cz.
*/

import java.io.*;
import java.net.*;

public class TCPEchoClient extends Networking {
    private static int BUFSIZE;
    private static String IP;
    private Socket soc;
    private static int MYPORT;
    private static String MSG= "An Echo Message!";
    private static double delay = 1;
    private static double transferSec = 1;

    //Getters and setters.

    public Socket getSoc() {
        return soc;
    }

    public void setSoc(Socket soc) {
        this.soc = soc;
    }

    public static int getBUFSIZE() {
        return BUFSIZE;
    }

    public static void setBUFSIZE(int BUFSIZE) {
        TCPEchoClient.BUFSIZE = BUFSIZE;
    }

    @Override
    public String getIP() {
        return IP;
    }

    @Override
    public void setIP(String IP) {
        this.IP = IP;
    }

    @Override
    public int getMYPORT() {
        return MYPORT;
    }

    @Override
    public void setMYPORT(int MYPORT) {
        this.MYPORT = MYPORT;
    }

    public static String getMSG() {
        return MSG;
    }

    public static double getDelay() {
        return delay;
    }

    public static void setDelay(double delay) {
        TCPEchoClient.delay = delay;
    }

    public static double getTransferSec() {
        return transferSec;
    }

    public static void setTransferSec(double transferSec) {
        TCPEchoClient.transferSec = transferSec;
    }

    /**
     * Constructor
     * @param ip : provided ip in the arguments.
     * @param port : provided port in the arguments.
     */
    public TCPEchoClient(String ip, int port) {
        super(ip, port);
    }

    /**
     * @role:  sends and receive through TCP packets using a TCP socket.
     * @return: void.
     * @throws IOException : check ip, port.
     */
    @Override
     public void contact() {
        try {
            byte[] buf = new byte[BUFSIZE];

            //send message
            OutputStream output = soc.getOutputStream();
            output.write(MSG.getBytes(),0,MSG.length());
            output.flush();

            //Input stream to receive messages.
            InputStream input = soc.getInputStream();
            int bytesRead = input.read(buf);

            String recivedMsg = new String(buf,0,bytesRead);

            //check if all the message is received
            while(input.available()>0){
                bytesRead = input.read(buf);
                recivedMsg += new String(buf,0,bytesRead);
            }

            if (recivedMsg.compareTo(MSG) == 0)
                System.out.printf("%d bits sent and received and the message is: %s\n", recivedMsg.length(),recivedMsg);
            else
                System.out.printf("Sent and received msg not equal!\n");

        } catch (IOException e) {
            System.err.println("Could not send or receive data, check IP and port " );
            e.printStackTrace();
            System.exit(3);
        }

    }

    public static void main(String[] args){
        //Checking if arguments are provided.
        if (args.length != 5) {
            System.err.printf("usage: %s server_name port bufferSize msgTransferRate the_message\n", args[1]);
            System.exit(1);
        }

        //Gathering information from arguments.
        IP = args[0]; //getting the ip from the first argument.
        MYPORT = Integer.valueOf(args[1]); // port from the second argument.
        MSG = args[4];

        //Creating a new client instance.
        TCPEchoClient client = new TCPEchoClient(IP,MYPORT);

        setBUFSIZE(Integer.valueOf(args[2])); //buffer size from the third argument.
        setTransferSec(Integer.valueOf(args[3])); // transfer rate from the fifth argument.

        try {
            Socket soc = new Socket(client.getIP(),client.getMYPORT());
            client.setSoc(soc);

            //This is used for pausing.
            if(transferSec > 0) {
                delay = 1000 / transferSec;
            }else if(transferSec < 0){ //checking if transfer rate is negative.
                System.err.println("The transfer rate per second cannot be negative.");
                System.exit(11);
            }

            //Used for VG-Task1.
            int numPackets = 0;
            double passed = System.currentTimeMillis();

            do{
                //Used for pausing.
                double diff = System.currentTimeMillis();


                client.contact();

                numPackets++;

                /*
                 * VG-Task 1:
                 * This if statement is used to report how many packets were sent in one second
                 * if the number of packets is too high.
                 */
                if(System.currentTimeMillis()-passed > 1000){//one second has passed, time to abort
                    System.out.println("One second passed. \nNumber of packets sent: "+ numPackets);
                    double remaining = transferSec - numPackets;
                    numPackets = 0;
                    System.out.println("Number of packets remains (In the passed second): "+ remaining);
                    passed = System.currentTimeMillis();
                    //break;
                }

                while(System.currentTimeMillis() - diff < delay+50){
                    //System.out.println("Here");
                    ; //do nothing to pass time if the number of packets is too low for one second.
                }


            }while(transferSec != 0);


        } catch (IOException e) {
            System.err.println("could not create the socket successfully, check the IP or the port: "+ client.getIP() + ", "+ client.getMYPORT());
            e.printStackTrace();
            System.exit(2);
        }

    }
}
