// JCDecauxService.js - in api/services

var https = require('https');

// Request informations
var address = 'https://api.jcdecaux.com/vls/v1/';
var contract = 'Lyon';
var apiKey = '8a078eb105498b9066de6e3b306311ab162a5819';

module.exports = {

    request: function (str, callback)
    {
        
        var result = "";
        
        var url = address + str + '?contract=' + contract + '&apiKey=' + apiKey;
        
        https.get(
            url,
            function (res)
            {
                res.on('data',
                    function (data)
                    {
                        result += data;
                    }
                );
                res.on('end',
                    function ()
                    {
                        callback(result);
                    }
                );
            }
        ).on('error',
            function (e)
            {
                console.error("An error occured while loading stations");
                console.error(e);
                callback();
            }
        );

    },
    
    requestStations: function (callback)
    {
        JCDecauxService.request(
            "stations",
            function(data)
            {
                var objects = JSON.parse(data);
                
                for (var row in objects)
                {
                    var object = objects[row];
                    if (object.position && object.position.lng && object.position.lat)
                    {
                        object.longitude = object.position.lng;
                        object.latitude = object.position.lat;
                        delete object.position;
                    }
                    else
                    {
                        delete objects[row];
                    }
                }
                
                callback(objects);
            }
        );
    }
    
};