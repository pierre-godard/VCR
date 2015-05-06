// UtilService.js - in api/services

var fs = require('fs');
var parse = require('csv-parse');

var walk = function (dir, list, next)
{
    //console.log("[LOAD] list length: "+list.length);
    if (list.length)
    {
        var file = dir + "/" + list.pop();
        fs.stat(
            file,
            function (err, stat)
            {
                console.log("[LOAD] found "+file);
                if (stat && stat.isDirectory())
                {
                    walk(dir, list, next);
                }
                else
                {
                    UtilService.load_measures(
                        file,
                        function ()
                        {
                            walk(dir, list, next);
                            console.log("[LOAD] "+file+" loaded");
                        }
                    );
                }
            }
        );
    }
    else
    {
        next();
    }
}

module.exports = {

    load_all_measures: function (dir, next)
    {
        fs.readdir(
            dir,
            function (err, list) 
            {
                console.log("--- Starting to load ---");
                if (err)
                {
                    console.log("[LOAD] Load init error: "+err);
                    return next(err);
                }
                walk(
                    dir,
                    list,
                    function ()
                    {
                        if (err) return next(err);
                        next();
                    }
                );
            }
        );
    },

    load_measures: function (path, next)
    {
        var i = 0;
        var parser = parse({delimiter: ';'});
        
        // var input = fs.createReadStream('./assets/extrait.csv');
        // var input = fs.createReadStream('./data/velov.csv');
        var input = fs.createReadStream(path);
        
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
                            if (err) return next(err);
                            if(iter%1000==0)
                                console.log(iter+" items loaded from "+path);
                            iter++;
                        }
                    );
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