/**
 * StationController
 *
 * @description :: Server-side logic for managing stations
 * @help        :: See http://links.sailsjs.org/docs/controllers
 */

var https = require('https');

module.exports = {

    pull:
        function (req, res, next)
        {
            
            JCDecauxService.requestStations(
                function (stations)
                {
                    
                    Station.create(
                        stations,
                        function(err)
                        {
                            if (err) return next(err);
                        }
                    );
                    
                    res.status(201);
                    res.end();
                }
            );
            
        }

};