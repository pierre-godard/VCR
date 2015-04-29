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
        	var station_id		= 8001;
			var date 			= Date.now();
			var analysisMode 	= analysis_mode.MEAN;
        	PredictionService.predict(station_iddate,analysisMode,
        		function (state)
                {
        			res.write("Prediction using "+analysisMode+" (date: "+new Date(date)+" - station: "+station_id+"): "
        				+state+"\n");
    				res.end('\n');
    			}
        	);

        }

};



