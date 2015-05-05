// UtilService.js - in api/services

var fs = require('fs');
var parse = require('csv-parse');

// Based on https://gist.github.com/adamwdraper/4212319
// Loop through all files in a given directory
function walk(dir, done) 
{
    fs.readdir(dir, function (error, list) 
    {
        if (error) 
        {
            console.error(error);
            return done(error);
        }
 
        var i = 0;
 
        (function next () 
        {
            var file = list[i++];
 
            if (!file) 
            {
                return done(null);
            }
            
            file = dir + '/' + file;
            
            fs.stat(file, function (error, stat) 
            {
        
                if (stat && stat.isDirectory()) 
                {
                    walk(file, function (error) 
                    {
                        next();
                    });
                } 
                else 
                {
                    // do stuff to file here
                    console.log("found: "+file);
                    done(file);
                    next();
                }
            });
        })();
    });
};

module.exports = {

    load_all_measures: function (path, next)
    {
        walk(path,
            function(file)
            {
                if(file == null)
                {
                    console.log("Error while reading file: file is null");
                }
                else
                {
                    console.log("Loading: "+file);
                    UtilService.load_measures(file,
                        function (err)
                        { 
                            if (err) return next(err);   
                        }
                    );
                }
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
                            if (err) console.log(err);
                            //if (err) return next(err);
                            if(iter%1000==0)
                                console.log(iter+" items loaded");
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