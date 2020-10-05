package networking;

public interface Controller {
    byte[] handleRequest(byte[] requestPayLoad);
    
    String getEndPoint();
}
