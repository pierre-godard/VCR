/**
 * PredictionController
 *
 * @description :: Server-side logic for managing predictions
 * @help        :: See http://links.sailsjs.org/docs/controllers
 */

module.exports = {

	analysis:
		function (req, res, next)
        {
            res.writeHead(200, {'Content-Type': 'text/plain; charset=utf-8'});
        	var station_id		= parseInt(req.param('id'));
			var date 			= Date.now();
            var delta_date      = parseInt(req.param('delta'));
            var date            = new Date(date + delta_date*60000).getTime(); // increm date by delta_date minutes
			var analysisMode 	= PredictionService.analysis_mode.DFM;
        	PredictionService.predict(station_id,date,analysisMode,
        		function (state,free,occup,prediction_quality)
                {
                    var object = 
                    {
                        station: station_id,
                        predict_bike_stands: free,
                        predict_bikes: occup,
                        predict_state: state,
                        confidence: prediction_quality,
                        time: date
                    };
                    Prediction.create(
                        object,
                        function (err, added)
                        {
                            if (err) 
                            {
                                console.log(err);
                            }
                        }
                    );
                    res.write(JSON.stringify(object));
                    res.end('\n'); 
    			}
        	);
        },

    all:
        function (req, res, next)
        {
        }

};



