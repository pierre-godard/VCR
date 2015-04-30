// JCDecauxService.js - in api/services

var https = require('https');

// Request informations
var addressRest = 'https://developer.jcdecaux.com/rest/vls/stations/Lyon.json';
var address = 'https://api.jcdecaux.com/vls/v1/';
var contract = 'Lyon';
var apiKey = '8a078eb105498b9066de6e3b306311ab162a5819';

module.exports = {

    request: function (str, callback)
    {
        
        var result = "";
        
        var url = str + '?contract=' + contract + '&apiKey=' + apiKey;
        
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
            function (err)
            {
                callback(null, err);
            }
        );

    },
    
    requestStations: function (callback)
    {
        JCDecauxService.request(
            addressRest,
            function(data, err)
            {
                if (err) return callback(null, err);
                var objects = JSON.parse(data);
                callback(objects);
            }
        );
    },
    
    requestMeasures: function (callback)
    {
        JCDecauxService.request(
            address + "stations",
            function(data, err)
            {
                if (err) return callback(null, err);
                var objects = JSON.parse(data);
                callback(objects);
            }
        );
    }
    
};