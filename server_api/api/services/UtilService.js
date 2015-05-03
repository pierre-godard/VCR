// UtilService.js - in api/services

var fs = require('fs');
var parse = require('csv-parse');

module.exports = {

    load_measures: function (path, next)
    {
        var i = 0;
        var parser = parse({delimiter: ';'});
        
        //var input = fs.createReadStream('./assets/extrait.csv');
        var input = fs.createReadStream('./data/velov.csv');
        
        var iter = 0;
        parser.on(
            'readable',
            function()
            {
                while(record = parser.read())
                {
                    var object = {
                        last_update: record[1],
                        available_bike_stands: record[4],
                        available_bikes: record[5],
                        station: record[0]
                    };
                    Measure.create(
                        object,
                        function (err, added)
                        {
                            if (err) console.log(err);
                            //if (err) return next(err);
                        }
                    );
                    if(iter%1000==0)
                        console.log(iter+" items loaded");
                    iter++;
                }
            }
        );
        parser.on(
            'end',
            function()
            {
                next();
            }
        );
        parser.on(
            'error',
            function(err)
            {
                return next(err);
            }
        );
        input.pipe(parser);
    }
    
    
};