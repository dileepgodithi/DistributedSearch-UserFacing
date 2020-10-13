package model.frontend;

public class FrontEndSearchRequest {
    private String searchQuery;
    private long maxResults = Long.MAX_VALUE;
    private double minScore = 0.0;

    public String getSearchQuery() {
        return searchQuery;
    }

    public long getMaxResults() {
        return maxResults;
    }

    public double getMinScore() {
        return minScore;
    }
}
