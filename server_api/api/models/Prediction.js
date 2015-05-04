/**
* Prediction.js
*
* @description :: TODO: You might write a short summary of how this model works and what it represents here.
* @docs        :: http://sailsjs.org/#!documentation/models
*/

module.exports = {

  	attributes: {
      	// Ne pas oublier la confidence
      	station: {
            model: 'Station',
            required: true
        },
        predict_bike_stands: {
            type: 'integer',
            required: true
        },
        predict_bikes: {
            type: 'integer',
            required: true
        },
        predict_state: {
        	type: 'integer',
            required: true
        },
        confidence: {
        	type: 'float',
            required: true
        },
        time: {
        	type: 'integer',
            required: true
        }
  	}
};

