//shortens document.getEgetElementById
function element(id) {
    return document.getElementById(id);
    }
    let allSearchData = ""; //decleared to collect all search names
    
    //gets each inputs data starting from second input
    function getResults() {
    //gets value of input
    let search = element("search-input").value;
    allSearchData = ""; //clears data for each word typed
    
    hideSearchResults();
    clearSearchResults();
    clearSearchData(); //
    //starts searching from the second input
    if (search.length > 1) {
        let counter = 0; // counts to 10
        for (let x of names) {
        if (counter < 10) {
            //checks for similarities
            if (x.toLowerCase().includes(search.toLowerCase())) {
            //populates the suggestion div
            element("search-results").innerHTML +=
                "<div class='search-item' onclick='displayData(\"" +
                x +
                "\")'><p>" +
                x +
                "</p></div>";
    
            counter++;
            }
        }
        if (x.toLowerCase().includes(search.toLowerCase()))
            //saves all the realated names
            allSearchData += "<p>" + x + "</p>";
        }
        displaySearchResults();
    }
    }
    //displays the suggestion div
    function displaySearchResults() {
    element("search-results").style.display = "block";
    }
    //clears the suggestion div
    function clearSearchResults() {
    element("search-results").innerHTML = "";
    }
    
    //hides the suggestion div
    function hideSearchResults() {
    element("search-results").style.display = "none";
    }
    //displays names when you click a suggestions
    function displayData(name) {
    element("search-data").innerHTML = "<p>" + name + "</p>";
    hideSearchResults();
    }
    //displays all related names to your search when you hit enter
    function displayAllData(names) {
    element("search-data").innerHTML = names;
    hideSearchResults();
    }
    //clears names displayed from search result
    function clearSearchData() {
    element("search-data").innerHTML = "";
    }
    //gets results after each input
    element("search-input").oninput = function() {
    getResults();
    };
    
    element("search-input").addEventListener("keyup", function(event) {
    // Number 13 is the "Enter" key on the keyboard
    if (event.keyCode === 13) {
        // Cancel the default action, if needed
        event.preventDefault();
        // Trigger the button element with a click
        displayAllData(allSearchData);
    }
    });