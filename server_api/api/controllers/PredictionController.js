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
            /*console.log(new Date(date));
            console.log(station_id);
            console.log(typeof station_id);*/
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
                            if (err) console.log(err);
                        }
                    );
        			res.write("Prediction using "+analysisMode+" (date: "+new Date(date)+" - station: "+station_id+"):\n");
        			res.write("State:              "+state+"\n");
                    res.write("Free:               "+free+"\n");
                    res.write("Occup:              "+occup+"\n");
                    res.write("Prediction quality: "+prediction_quality+"\n");
                    res.end('\n'); 
    			}
        	);
            /*prediction_quality = PredictionService.analysis_mode.MEAN;
            PredictionService.predict(station_id,date,analysisMode,
                function (state,free,occup,prediction_quality)
                {
                    res.write("Prediction using "+analysisMode+" (date: "+new Date(date)+" - station: "+station_id+"):\n");
                    res.write("State:              "+state+"\n");
                    res.write("Free:               "+free+"\n");
                    res.write("Occup:              "+occup+"\n");
                    res.write("Prediction quality: "+prediction_quality+"\n");
                }
            );
            res.end('\n'); */
        },

    all:
        function (req, res, next)
        {
            //PredictionController.analysis(req, res, next);    
        }

};



