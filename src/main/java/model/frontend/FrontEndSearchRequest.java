package model.frontend;

public class FrontEndSearchRequest {
    private String searchQuery;
    private int maxResults = Integer.MAX_VALUE;
    private double minScore = 0.0;

    public String getSearchQuery() {
        return searchQuery;
    }

    public int getMaxResults() {
        return maxResults;
    }

    public double getMinScore() {
        return minScore;
    }
}
