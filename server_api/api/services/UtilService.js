// UtilService.js - in api/services

var fs = require('fs');
var parse = require('csv-parse');

var walk = function (dir, list, next)
{
    console.log("[LOAD] list length: "+list.length);
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
        UtilService.post_information({
            identifier: 'load_measures' + path,
            title: 'Load measures',
            description: 'Updating dynamic data on stations (from: ' + path + ')',
            progression: 100
        });
        
        var i = 0;
        var parser = parse({delimiter: ';'});
        
        var input = fs.createReadStream(path);
        
        var iter = 0;
        var objects = [];
        parser.on(
            'readable',
            function()
            {
                while(record = parser.read())
                {
                    objects.push({
                        last_update: record[1],
                        available_bike_stands: record[4],
                        available_bikes: record[5],
                        station: record[0]
                    });
                    if(iter%1000==0)
                        console.log(iter+" items loaded from "+path);
                    iter++;
                }
            }
        );
        parser.on(
            'end',
            function()
            {
                console.log("Start loading to database of "+path);
                Measure.create(
                    objects,
                    function (err, added)
                    {
                        console.log("End of loading to database of "+path);
                        if (err) return next(err);
                        next();
                    }
                );
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
    },
    
    post_information: function (information)
    {
        Information.destroy({identifier: information.identifier})
        .exec(
            function (err)
            {
                Information.create(
                    information,
                    function (err2, added)
                    {
                        if (err2) console.log(information);
                        if (err2) return console.log(err2);
                    }
                );
            }
        );
    }
    
    
};