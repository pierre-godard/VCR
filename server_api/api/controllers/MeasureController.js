/**
 * MeasureController
 *
 * @description :: Server-side logic for managing measures
 * @help        :: See http://links.sailsjs.org/docs/controllers
 */

module.exports = {

    pull:
        function (req, res, next)
        {
            
            JCDecauxService.requestMeasures(
                function (measures)
                {
                    _.forEach(
                        measures,
                        function (measure)
                        {
                            Measure.update(
                                {last_update: measure.last_update},
                                measure,
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

