import cluster.management.ServiceRegistry;
import networking.WebServer;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import search.SearchController;

import java.io.IOException;

public class Application implements Watcher {
    private final static String ZOOKEEPER_ADDRESS = "localhost:2181";
    private final static int SESSION_TIMEOUT = 3000;
    private ZooKeeper zooKeeper;
    public static void main(String[] args) throws IOException, InterruptedException {
        int serverPort = args.length == 1 ? Integer.parseInt(args[0]) : 9000;
        Application application = new Application();
        ZooKeeper zooKeeper = application.connectToZookeeper();

        ServiceRegistry serviceRegistry = new ServiceRegistry(zooKeeper, ServiceRegistry.COORDINATORS_REGISTRY_ZNODE);
        SearchController searchController = new SearchController(serviceRegistry);
        WebServer webServer = new WebServer(serverPort, searchController);
        webServer.startServer();

        System.out.println("Server listening on port: "+serverPort);
        application.run();
        application.close();
    }

    private void run() throws InterruptedException {
        synchronized (zooKeeper){
            zooKeeper.wait();
        }
    }

    private void close() throws InterruptedException {
        zooKeeper.close();
    }

    private ZooKeeper connectToZookeeper() throws IOException {
        zooKeeper = new ZooKeeper(ZOOKEEPER_ADDRESS, SESSION_TIMEOUT, this);
        return zooKeeper;
    }

    @Override
    public void process(WatchedEvent watchedEvent) {
        switch (watchedEvent.getType()){
            case None :
                if(watchedEvent.getState() == Event.KeeperState.SyncConnected){
                    System.out.println("Successfully connected to zookeeper");
                }
                else {
                    synchronized (zooKeeper){
                        System.out.println("Disconnected from zookeeper event");
                        zooKeeper.notifyAll();
                    }
                }
        }
    }
}
