package networking;

public class WebServer {
    private int port;
    private Controller controller;

    public WebServer(int port ,Controller controller){
        this.port = port;
        this.controller = controller;
    }
}
