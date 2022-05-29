let req;
let isIE;
let he;
let s;
let searchQuery;
let suggestions;

function init() {
    he = document.getElementById("he");
    suggestions = document.getElementById("suggestions");
    searchQuery = document.getElementById("search-input");
}

function showSuggestion() {
    const url = "Suggestion?searchQuery=" + escape(searchQuery.value);
    console.log(searchQuery.value);
    req = initRequest();
    req.open("GET", url, true);
    req.onreadystatechange = callback;
    req.send(null);
}

function initRequest() {
    if (window.XMLHttpRequest) {
        if (navigator.userAgent.indexOf('MSIE') !== -1) {
            isIE = true;
        }
        return new XMLHttpRequest();
    } else if (window.ActiveXObject) {
        isIE = true;
        return new ActiveXObject("Microsoft.XMLHTTP");
    }
}
function callback() {
    if (req.readyState === 4) {
        if (req.status === 200) {
            const allSuggestions = req.responseText;
            const choices = allSuggestions.split("-");
            let string = "";
            for(let i = 0; i< choices.length - 1 ; i++)
            {
                string += '<option value = "' + choices[i] + '"/>';

            }
            suggestions.innerHTML = string;

        }
    }
}
