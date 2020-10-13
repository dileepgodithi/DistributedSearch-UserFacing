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
        var searchQuery = $("#search_box").val();
        var minScore = parseFloat($("#score").val());
        if(isNaN(minScore))
            minScore = 0;
        var maxResults = parseInt($("#num_results").val());
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
        //$("#results_table").append("<tr><td>Results received</td></tr>");
        $("#results_table").append(data);
        for(var i = 0; i < data.search_results.length; i++){
            var title = data.search_results[i].title;
            var extension = data.search_results[i].extension;
            var score = data.search_results[i].score;
            var fullPath = data.documents_location + "/" + title + "." + extension;
            $("#results_table").append("<tr><td><a href='" + fullPath + "'>"+title+"</a></td><td>"+score+"</td></tr>");
        }
    }
});