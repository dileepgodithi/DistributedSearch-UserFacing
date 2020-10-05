$(document).ready(function(){
    $("#submit_button").on("click", function(){
        $.ajax({
            method : "POST",
            contentType : "application/json",
            data : createRequest(),
            url : "search",
            dataType : "json",
            success : onHttpResponse
        });
    });

    function createRequest(){
        var searchQuery = $("search_box").val();
        var minScore = parseFloat($("score").val());
        if(isNaN(minScore))
            minScore = 0;
        var maxResults = parseInt($("num_results").val());
        if(isNaN(maxResults))
            maxResults = Number.MAX_SAFE_INTEGER;

        var request = {
            search_query : searchQuery,
            min_score : minScore,
            max_results : maxResults
        }

        return JSON.stringify(request);
    }

    function onHttpResponse(data, status){
        if(status === "success"){
            console.log(data);
            appendResults(data);
        }
        else{
            console.log("Error getting results");
        }
    }

    function appendResults(data){
        //to be implemented
        $("results_table").append(data);
    }
});