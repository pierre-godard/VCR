/**
 * StationController
 *
 * @description :: Server-side logic for managing stations
 * @help        :: See http://links.sailsjs.org/docs/controllers
 */

module.exports = {

    // Populate the database with JCDecaux data
    pull:
        function (req, res, next)
        {
            JCDecauxService.requestStations(
                function (stations, err)
                {
                    
                    if (err) return next(err);
                    
                    Station.createOrUpdate(
                        stations,
                        function (err2)
                        {
                            if (err2) return next(err2);
                        }
                    );
                    
                    res.status(201);
                    res.end();
                }
            );
            
        }

};