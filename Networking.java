abstract public class Networking {
    private static int MYPORT;
    private static String IP;

    public Networking(String ip, int port){
        MYPORT = port; IP = ip;
    }

    public String getIP() {
        return IP;
    }

    public void setIP(String IP) {
        Networking.IP = IP;
    }

    public int getMYPORT() {
        return MYPORT;
    }

    public void setMYPORT(int MYPORT) {
        Networking.MYPORT = MYPORT;
    }

    abstract void contact(); //a method that starts a connection either with UDP or TCP.
}