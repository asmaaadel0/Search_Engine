<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8" />
    <meta http-equiv="X-UA-Compatible" content="IE=edge" />
    <meta name="viewport" content="width=device-width, initial-scale=1.0" />
    <link rel="stylesheet" href="styles/style.css" />
    <meta name="viewport" content="width=device-width, initial-scale=1.0" />
    <link
            href="https://fonts.googleapis.com/css2?family=Fira+Sans:ital@1&family=Oswald&display=swap"
            rel="stylesheet"
    />
    <script type="text/javascript" src = suggestions.js></script>
    <link rel="preconnect" href="https://fonts.googleapis.com" />
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin />
    <link
            href="https://fonts.googleapis.com/css2?family=GFS+Didot&display=swap"
            rel="stylesheet"
    />
    <script src="https://kit.fontawesome.com/5bbc336f0c.js" crossorigin="anonymous"></script>
    <title>Search</title>
</head>
<body onload = "init()">
<header>
    <img src="images/R1.jpg" alt="photo" />
    <h1>SEARCH</h1>
</header>
<main>
    <div class="container-out">
        <div class="container-in">
            <div class="search-container">
                <div class="search-engine">
                    <div class = "jumbotron">
                    <form action="searchreq" method="GET" id="searchreq">
                        <input
                                list ="suggestions"
                                type="text"
                                placeholder=" Search...."
                                name="search"
                                id="search-input"
                                autocomplete="off"
                                class="autocomplete"
                                onkeyup = "showSuggestion()"
                        />
                        <button type="button" id="but" onclick=record()><i class="fa-solid fa-microphone"></i></button>
                        <button type="submit">Submit</button>
                        <datalist id = "suggestions">

                        </datalist> <div id ="he"></div>
                    </form>
                </div>
                    <div id="search-results"></div>
                    <div id="search-data"></div>
                </div>
            </div>
        </div>
    </div>
</main>
<script>
    function record() {
        var recognition = new webkitSpeechRecognition();
        recognition.lang = "en-GB";

        recognition.onresult = function(event) {
            console.log(event);
            document.getElementById('search-input').value = event.results[0][0].transcript;
        }
        recognition.start();

    }
</script>
</body>
</html>
