public class HttpResponses {
    private String response;

    private String date;
    private String server;
    private String lastModified;
    private String eTag;
    private int contentLenght;
    private String contentType;
    private String vary;
    private byte[] data;

    public HttpResponses(){
        ;
    }

    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        this.response = response;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getServer() {
        return server;
    }

    public void setServer(String server) {
        this.server = server;
    }

    public String getLastModified() {
        return lastModified;
    }

    public void setLastModified(String lastModified) {
        this.lastModified = lastModified;
    }

    public String geteTag() {
        return eTag;
    }

    public void seteTag(String eTag) {
        this.eTag = eTag;
    }

    public int getContentLenght() {
        return contentLenght;
    }

    public void setContentLenght(int contentLenght) {
        this.contentLenght = contentLenght;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public String getVary() {
        return vary;
    }

    public void setVary(String vary) {
        this.vary = vary;
    }

    public String ConstructResponse(){
        //TODO: construct response here.

        return null;
    }
}
