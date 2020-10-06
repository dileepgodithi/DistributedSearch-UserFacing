package search;

import cluster.management.ServiceRegistry;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.google.protobuf.InvalidProtocolBufferException;
import model.frontend.FrontEndSearchRequest;
import model.frontend.FrontEndSearchResponse;
import model.proto.SearchModel;
import networking.Controller;
import networking.WebClient;
import org.apache.zookeeper.KeeperException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SearchController implements Controller {
    private final String DOCUMENTS_LOCATION = "books";
    private final String ENDPOINT = "/search";
    private final ServiceRegistry searchCoordinatorRegistry;
    private final ObjectMapper objectMapper;
    private final WebClient webClient;

    public SearchController(ServiceRegistry serviceRegistry){
        this.searchCoordinatorRegistry = serviceRegistry;
        this.webClient = new WebClient();
        this.objectMapper = new ObjectMapper();
        this.objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        this.objectMapper.setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE);
    }

    @Override
    public byte[] handleRequest(byte[] requestPayLoad) {
        try {
            FrontEndSearchRequest frontEndSearchRequest = objectMapper.readValue(requestPayLoad, FrontEndSearchRequest.class);

            FrontEndSearchResponse frontEndSearchResponse = createFrontEndResponse(frontEndSearchRequest);
            return objectMapper.writeValueAsBytes(frontEndSearchResponse);
        } catch (IOException e) {
            e.printStackTrace();
            return new byte[0];
        }
    }

    private FrontEndSearchResponse createFrontEndResponse(FrontEndSearchRequest frontEndSearchRequest){
        SearchModel.Response searchClusterResponse = sendRequestToSearchCluster(frontEndSearchRequest.getSearchQuery());

        List<FrontEndSearchResponse.SearchResultInfo> filteredResults = filterResults(searchClusterResponse,
                frontEndSearchRequest.getMaxResults(),
                frontEndSearchRequest.getMinScore());

        return new FrontEndSearchResponse(filteredResults, DOCUMENTS_LOCATION);
    }

    private List<FrontEndSearchResponse.SearchResultInfo> filterResults(SearchModel.Response searchClusterResponse,
                                                                        int maxResults,
                                                                        double minScore){

        double maxScore = getMaxScore(searchClusterResponse);

        List<FrontEndSearchResponse.SearchResultInfo> filteredResults = new ArrayList<>();
        for(int i = 0; i < maxResults && i < searchClusterResponse.getRelevantDocumentsCount(); i++){
            int normalizedDocScore = normalizeScore(searchClusterResponse.getRelevantDocuments(i).getScore(), maxScore);

            if(normalizedDocScore < minScore)
                continue;

            String docName = searchClusterResponse.getRelevantDocuments(i).getDocumentName();

            String[] titleAndExtension = docName.split("\\.");
            String title = titleAndExtension[0];
            String extension = titleAndExtension.length == 2 ? titleAndExtension[1] : "";

            FrontEndSearchResponse.SearchResultInfo resultInfo =
                    new FrontEndSearchResponse.SearchResultInfo(title, extension, normalizedDocScore);

            filteredResults.add(resultInfo);
        }

        return filteredResults;
    }

    private int normalizeScore(double score, double maxScore){
        return (int) (score/maxScore * 100);
    }

    private double getMaxScore(SearchModel.Response searchClusterResponse){
        if(searchClusterResponse.getRelevantDocumentsCount() == 0)
            return 0.0;

        return searchClusterResponse.getRelevantDocumentsList().stream()
                .map(documentStats -> documentStats.getScore())
                .max(Double::compareTo)
                .get();
    }
    private SearchModel.Response sendRequestToSearchCluster(String searchQuery) {
        SearchModel.Request searchRequest = SearchModel.Request.newBuilder().setSearchQuery(searchQuery).build();

        try {
            String coordinatorAddress = searchCoordinatorRegistry.getRandomServiceAddress();
            if(coordinatorAddress == null){
                System.out.println("Search Cluster Coordinator not available");
                return SearchModel.Response.getDefaultInstance();
            }
            byte[] responseBody = webClient.sendTask(coordinatorAddress, searchRequest.toByteArray()).join();

            return SearchModel.Response.parseFrom(responseBody);

        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (KeeperException e) {
            e.printStackTrace();
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }
        return SearchModel.Response.getDefaultInstance();
    }

    @Override
    public String getEndPoint() {
        return ENDPOINT;
    }
}
