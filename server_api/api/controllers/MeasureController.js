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
                        
                    }
                )
                ], 
                function(err)
                {
                    console.log("All files loaded");
                }
            );
                    
            res.status(201);
            res.end();
            
        }
    
};

