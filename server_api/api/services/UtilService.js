// UtilService.js - in api/services

var fs = require('fs');
var parse = require('csv-parse');

module.exports = {

    load_measures: function (path, next)
    {
        var i = 0;
        var parser = parse({delimiter: ';'});
        var input = fs.createReadStream('./data/sta10004.csv');
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
                    i++;
                    
                    // 34726203
                    if (i % 100 == 0)
                    {
                        console.log(i / 727 + "%");
                    }
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