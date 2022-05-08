var MongoClient = require("mongodb").MongoClient;
var url = "mongodb://localhost:27017/mydb";
MongoClient.connect(url, function (err, client) {
    var db= client.db('A');
    if (err) throw err;
    db.collection("suggestions").find( { } ).toArray(function(err, result) {
        if (err) throw err;
        console.log(result);
        client.close();  
  });
});