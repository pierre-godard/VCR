/**
 * MeasureController
 *
 * @description :: Server-side logic for managing measures
 * @help        :: See http://links.sailsjs.org/docs/controllers
 */

module.exports = {

    // Populate the database with JCDecaux data
    pull:
        function (req, res, next)
        {
            JCDecauxService.requestMeasures(
                function (measures, err)
                {
                    
                    if (err) return next(err);
                    
                    Measure.createOrUpdate(
                        measures,
                        function (err2)
                        {
                            if (err2) return next(err2);
                        }
                    );
                    
                    LastMeasure.createOrUpdate(
                        measures,
                        function (err2)
                        {
                            if (err2) return next(err2);
                        }
                    );
                    
                    res.status(201);
                    res.end();
                    
                }
            );         
        },
    
    // Populate the database from the csv velov.csv
    load:
        function (req, res, next)
        {
            async.parallel([
                UtilService.load_all_measures(
                    './data/stations',
                    function (err)
                    {
                        if (err) return next(err);
                        res.write("All files loaded");   
                        res.status(201);
                        res.end();              
                    }
                )
                ], 
                function(err)
                {
                }
            );
            
        },

    loadone:
        function (req, res, next)
        {
            UtilService.load_measures('./data/stations/sta'+req.param('id')+".csv",
                function (err)
                {
                    
                    if (err) 
                    {
                        return next(err);
                        console.error("error: "+err);
                    }
                    res.write("station "+req.param('id')+" loaded"); 
                    res.status(201);
                    res.end();
                }
            );
        },
    today:
        function (req, res, next)
        {
            var oneDay = 1000 * 60 * 60 * 24;
            Measure.find({station: parseInt(req.param('id')),date:Math.floor(Date.now() / oneDay)},
                function(err, found) 
                {
                    res.write(JSON.stringify(found));
                    res.end();
                }
            );
        }
    
};

