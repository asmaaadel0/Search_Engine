var req;
var isIE;
var he;
var s;
var searchQuery;
var suggestions;
function init() {
    he = document.getElementById("he");
    suggestions = document.getElementById("suggestions");
    searchQuery = document.getElementById("search-input");
}

function showSuggestion() {
    //     var url = "http://localhost:8084/APT_-_Search_Engine/"+ escape(searchWords.value);
    var url = "Suggestion?searchQuery=" + escape(searchQuery.value);
    console.log(searchQuery.value);

    //he.innerHTML = searchQuery.value;

    //      var url = "Search?action=complete&id=" + escape(searchWords.value);
    req = initRequest();
    req.open("GET", url, true);
    req.onreadystatechange = callback;
    req.send(null);
}

function initRequest() {
    if (window.XMLHttpRequest) {
        if (navigator.userAgent.indexOf('MSIE') != -1) {
            isIE = true;
        }
        return new XMLHttpRequest();
    } else if (window.ActiveXObject) {
        isIE = true;
        return new ActiveXObject("Microsoft.XMLHTTP");
    }
}
function callback() {
    if (req.readyState == 4) {
        if (req.status == 200) {
            // he.innerHTML = req.responseText;
            var allSuggestions = req.responseText;
            var choices = allSuggestions.split("-")
            //     searchQuery.innerHTML = req.responseText + "<br>";

            //  he.innerHTML = choices;
            //   he.innerHTML = choices + "<br>" +(choices.length - 1).toString() + "22222222222222222222:";
            var string = "";
            for(var i = 0; i< choices.length - 1 ; i++)
            {
                string += '<option value = "' + choices[i] + '"/>';
                //       string +=  choices[i] + "<br>";

            }

            //string += choices
            // he.innerHTML = "awdada";
            //he.innerHTML = string;
            suggestions.innerHTML = string;

        }
    }
}
