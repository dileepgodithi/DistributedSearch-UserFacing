package search;

import cluster.management.ServiceRegistry;
import networking.Controller;

public class SearchController implements Controller {
    private ServiceRegistry searchCoordinatorRegistry;

    public SearchController(ServiceRegistry serviceRegistry){
        this.searchCoordinatorRegistry = serviceRegistry;
    }
    @Override
    public byte[] handleRequest(byte[] requestPayLoad) {
        return new byte[0];
    }

    @Override
    public String getEndPoint() {
        return null;
    }
}
