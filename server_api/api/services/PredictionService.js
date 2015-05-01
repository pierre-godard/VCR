// ==============================================================
//	main.js
//	Prediction
//	Description:
//		This service predicts the state of a station
//	Created: 	28/04/2015
//	By: 		Samuel MAGNAN
// ==============================================================

var http = require('http');

// Mean of the array passed as parameter
function mean (array) 
{
	var sum_val = 0;
	for (i = 0; i < array.length; i++) 
	{ 
    	sum_val += array[i];
	}
	return sum_val/i;
}

// Mean of the array passed as parameter
// Each element weight is decreased using the factor after each step
// ex 0.5 , 1 , [10,20,30]
// Step 1: factor = 1
// Step 2: factor = 0.5
// Step 3: factor = 0.25
// => mean = (1*10+0.5*20+0.25*30)/(1+0.5+0.35)
function decreasingFactor_mean (factor,step,array) 
{
	var sum_val 			= 0;
	var final_denominator 	= 0;
	var current_factor 		= 1;
	for (i = 0; i < array.length; i++) 
	{ 
    	sum_val += current_factor*array[i];
    	final_denominator += current_factor;
    	if(i%step == 0) 
    	{
    		current_factor *= factor;
    	}    	
	}
	return sum_val/final_denominator;
}

// Analyse a set of datas using the selected mode
// Returns the caracteristic value associated with the analysis
function analysis (datas,mode) 
{
	console.log("Analysis: using "+mode);
	switch(mode) 
	{
	    case PredictionService.analysis_mode.MEAN:
	    	return mean(datas);
	        break;	// no use but used to keep the code clear
	    case PredictionService.analysis_mode.FDM:
	    	return decreasingFactor_mean(datas);
	        break;
	    default:
	}
	return 0;	// 0 as default return value, TODO throw ERROR ?
}


// returns the measures matching the specified id and withing time stamp of [time]
function queryMeasures(id,time)
{
	Measure.find({station: id, day: time.getDay(), hour: time.getHours(), 
		time_slice: Math.floor(time.getMinutes()/Measure.NB_TIME_SLICES) },
		function(err, found) 
		{
      		//console.log("found: "+found);
      		//console.log("error: "+err);
      		return found;
      	}
    );
}

// If no period is found, used to define the default period
var DEFAULT_PERIOD = -1;

// Find the number of the period associated to the date (time)
function find_period(json_periods,time)
{
	var date = new Date(time);
	var year = String(date.getFullYear());
	for (var i = 0; i < json_periods[year].length; i++) 
	{ 
	    if(Date(json_periods[year][i].begin) <= date 
	    	&& Date(json_periods[year][i].end) >= date)	// If the date is within the period
	    {
	    	return i;
	    }
	}
	return DEFAULT_PERIOD;
}

// thx to http://stackoverflow.com/questions/1187518/javascript-array-difference
Array.prototype.diff = function(a) {
    return this.filter(function(i) {return a.indexOf(i) < 0;});
};

// Generate the "period" which excludes every specific period (classic time) (array of dates)
function generate_defaultPeriod(json_periods,step,year_begin,year_end)
{

}

// generate the selected period (arrays of dates)
function generate_specificPeriod(json_periods,year_begin,year_end,period)
{
	var dates = [];
	for (var y = year_begin; y <= year_end;y++)
	{
		for (var d = Date(json_periods[y][period].end); d > Date(json_periods[y][period].end); d.setDate(d.getDate() - 1)) 
		{
			dates.push(d);
		}
	}
	return dates;
}

module.exports = {

	// Velov station potential state
	station_state: Object.freeze(
	{
		EMPTY: 		"empty", 
		NEAR_EMPTY: "near empty", 
		INTERM: 	"places available",
		NEAR_FULL: 	"near full",
		FULL: 		"full",
		UNKNOWN: 	"unknown" 
	}),

	// Values associated to the states to determine ranges.
	// Ranges are used to find the state depending on the elem position in the ranges
	station_values: Object.freeze(
	{
		EMPTY_LIMIT: 		1, 
		NEAR_EMPTY_LIMIT: 	2, 
		INTERM_LIMIT: 		0,
		NEAR_FULL_LIMIT: 	2,
		FULL_LIMIT: 		1,
		UNKNOWN: 			NaN 
	}),

	// The various analysis methods that can be picked
	analysis_mode: Object.freeze(
	{
		MEAN: 	"mean",
		FDM: 	"decreasing factor mean",
		NONE: 	"none" 
	}),
	
	// Gives a prediction of the state
	// of a Velov station (id)
	// at a certain point in time (time) 
	// using the selected analysis algorithm (analysisMode) 
	predict: function (id,time,analysisMode,callback) 
	{
		// ----- Data fetching
		var WEEK_SIZE 			= 7;
		var station_free		= [];
		var station_occup		= [];
		var query_result		= [];
		var limit_date 			= new Date(2012, 0, 1);
		var curr_date			= new Date();
		var util 				= require('util');
		var json_timePeriods 	= require('../../data/time/vacances.json');
		console.log(util.inspect(json_timePeriods, {showHidden: false, depth: null}));
		console.log(json_timePeriods["2014"].Hiver.begin);
		console.log(find_period(json_timePeriods,time));
		var date = new Date(time);
		var year = String(date.getFullYear());
		for (var d = new Date(time); d > limit_date; d.setDate(d.getDate() - WEEK_SIZE)) 
		{
			curr_date = new Date(d);
			query_result = queryMeasures(id,curr_date);
			if(query_result == undefined) // no data has been found corresponding to id (unlikely, or call para error) or time (possible)
			{
				console.log("skippind query result ("+id+" - "+curr_date);
				continue;	
			}			
			console.log(query_result+'\n');
			for (i = 0; i < query_result.length; i++) 
			{ 
				station_free.push(query_result[i].available_bike_stands);
				station_occup.push(query_result[i].available_bike);
			}
		}

		// ----- Data analysis
		var free_overTime 	= analysis(station_free,analysisMode);
		var occup_overTime 	= analysis(station_occup,analysisMode);
		//var diff_overTime 	= max_overTime - curr_overTime;

		console.log("free_overTime:  "+free_overTime);
		console.log("occup_overTime: "+occup_overTime);
		//console.log("diff_overTime: "+diff_overTime);

		// ----- State selection
		if (isNaN(occup_overTime) || isNaN(free_overTime))
		{
			callback(PredictionService.station_state.UNKNOWN);
		}
		else if (free_overTime < PredictionService.station_values.FULL_LIMIT)
		{
			callback(PredictionService.station_state.FULL);
		} 
		else if (free_overTime <= PredictionService.station_values.NEAR_FULL_LIMIT)
		{
			callback(PredictionService.station_state.NEAR_FULL);
		}
		else if (occup_overTime < PredictionService.station_values.EMPTY_LIMIT)
		{
			callback(PredictionService.station_state.EMPTY);
		}
		else if (occup_overTime <= PredictionService.station_values.NEAR_EMPTY_LIMIT)
		{
			callback(PredictionService.station_state.NEAR_EMPTY);
		}
		callback(PredictionService.station_state.INTERM);
	}
};