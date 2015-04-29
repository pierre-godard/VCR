/**
 * StationController
 *
 * @description :: Server-side logic for managing stations
 * @help        :: See http://links.sailsjs.org/docs/controllers
 */

module.exports = {

    pull:
        function (req, res, next)
        {
            
            JCDecauxService.requestStations(
                function (stations)
                {
                    _.forEach(
                        stations,
                        function (station)
                        {
                            Station.update(
                                {number: station.number},
                                station,
                                function(err)
                                {
                                    if (err) return next(err);
                                }
                            );
                        }
                    );
                    
                    res.status(201);
                    res.end();
                }
            );
            
        }

};