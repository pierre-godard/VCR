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

var DFM_DEFAULT_STEP = 1;
var DFM_DEFAULT_FACTOR = 0.98;

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

// Calculates the variance given the array (using the specified mode)
function variance(array,mode)
{
	var avg = 0;
	/*switch(mode) 
	{
	    case PredictionService.analysis_mode.MEAN:
	    	avg = mean(array);
	        break;	
	    case PredictionService.analysis_mode.DFM:
	    	avg = decreasingFactor_mean(DFM_DEFAULT_FACTOR,DFM_DEFAULT_STEP,array);
	        break;
	    default:
	}*/
	avg = mean(array);
	var i = array.length;
	var v = 0;
 
	while( i-- )
	{
		v += Math.pow( (array[ i ] - avg), 2 );
	}
	v /= array.length;
	return v;
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
	    case PredictionService.analysis_mode.DFM:
	    	return decreasingFactor_mean(DFM_DEFAULT_FACTOR,DFM_DEFAULT_STEP,datas);
	        break;
	    default:
	}
	return 0;	// 0 as default return value, TODO throw ERROR ?
}

// return the level of quality of the prediction (0 -> 1)
// 0 -> prediction with no assurance
// 1 -> sure (nearly)
function quality_analysis(arr_free,arr_occup,mode)
{
	// Normalized. The inside walue is the raw quality. If this quality is supp to 10000
	// we considerate we are sure about the result (log(10000)/4 = 1)
	return Math.min(1,Math.log(1+(arr_free.length + arr_occup.length)/(1 + variance(arr_free,mode) + variance(arr_occup,mode)))/4);
}

// [OLD version -> predict_old]
// returns the measures matching the specified id and withing time stamp of [time]
function queryMeasures_old(id,time,callback)
{
	Measure.find({station: id, specif_time: Measure.date_to_specificTime(time)
			/*hour: time.getHours(),
		    date: time.getDate(),
		    month: time.getMonth(),
		    time_slice: Math.floor(time.getMinutes()*Measure.NB_TIME_SLICES/60)*/ },
		function(err, found) 
		{
			/*console.log("-- Time:        "+time);
      		console.log("found: "+found);
      		console.log("error: "+err);
      		for (var i = 0; i < found.length; i++) 
			{
				console.log(found[i]);
      			console.log("available_bike_stands: "+found[i].available_bike_stands);
				console.log("available_bikes:       "+found[i].available_bikes);
				console.log("last_update:           "+new Date(found[i].last_update));
			}*/
      		callback(found);
      	}
    );
}

// returns all the measures associated to the id and corresponding to the date
// for the perdiction calculation
function queryMeasures(id,time,callback)
{
	Measure.find({station: id, specif_time: Measure.date_to_specificTime(time)
			/*hour: time.getHours(),
		    date: time.getDate(),
		    month: time.getMonth(),
		    time_slice: Math.floor(time.getMinutes()*Measure.NB_TIME_SLICES/60)*/ },
		function(err, found) 
		{
      		callback(found);
      	}
    );
}

// If no period is found, used to define the default period
var DEFAULT_PERIOD = -1;
// used to loop through periods
var NB_SPECIFIC_PERIODS = 5; // TODO add in JSON

// Find the number of the period associated to the date (time) using the JSON file
function find_period(json_periods,time)
{
	var date = new Date(time);
	var year = String(date.getFullYear());
	for (var i = 0; i < NB_SPECIFIC_PERIODS; i++) 
	{ 
/*		console.log(Date(json_periods[year][i].begin));
		console.log(date);
		console.log(Date(json_periods[year][i].end));*/
	    if(new Date(json_periods[year][i].begin) <= date 
	    	&& new Date(json_periods[year][i].end) >= date)	// If the date is within the period
	    {
	    	return i;
	    }
	}
	return DEFAULT_PERIOD;
}

// Diff between arrays
// thx to http://stackoverflow.com/questions/1187518/javascript-array-difference
// NOT WORKING WITH DATES (and objects?)
Array.prototype.diff = function(a) 
{
    return this.filter(
    	function(i) 
	    {
	    	return a.indexOf(i) < 0;
	    }
	);
};

// Diff between arrays
// thx to http://stackoverflow.com/questions/1187518/javascript-array-difference
// WORKING WITH DATES!
function arr_diff(a1, a2)
{
	var a 		=[];
	var diff 	=[];
	for(var i=0;i<a1.length;i++)
	{
		a[a1[i]]=true;	
	}
	for(var i=0;i<a2.length;i++)
	{
		if(a[a2[i]])
		{
			delete a[a2[i]];
		}	
		else 
		{
			a[a2[i]]=true;
		}	
	}
	for(var k in a)
	{
		diff.push(k);
	}
	return diff;
}

// Generate the "period" which excludes every specific period (classic time) (array of dates)
// All dates - specific periods
function generate_defaultPeriod(json_periods,limit_time,year_begin,year_end)
{
	var WEEK_SIZE 			= 7;
	var all_dates 			= [];
	var specific_dates 		= [];
	for (var i = 0; i < NB_SPECIFIC_PERIODS; i++) 
	{
		specific_dates = specific_dates.concat(generate_specificPeriod(json_periods,limit_time,year_begin,year_end,i)); 
	}
/*	console.log(new Date(limit_time));
	console.log(new Date(limit_time).getFullYear());
	console.log(year_begin);*/
	for (var d = new Date(limit_time); d.getFullYear() >= year_begin; d.setDate(d.getDate() - WEEK_SIZE)) 
	{
		//console.log("date: "+d);
		if(limit_time.getDay()==d.getDay()) // Only if same day of the week, algo choice XXX
		{
			all_dates.push(new Date(d));				
		}
	}
/*	console.log("All dates:");
	console.log(all_dates);
	console.log("Specific dates:");
	console.log(specific_dates);
	console.log("diff 1");
	console.log(all_dates.diff(specific_dates)); // not working
	console.log("diff 2");
	console.log(_.difference(all_dates,specific_dates)); // not working
	console.log("diff 3");
	console.log(arr_diff(all_dates,specific_dates)); // working!*/
	var defPeriod_dates = arr_diff(all_dates,specific_dates);
	defPeriod_dates.pop(); // We remove last element which is strangly not a date but 'diff'
	return defPeriod_dates;
}

// generate the selected period (arrays of dates)
function generate_specificPeriod(json_periods,limit_time,year_begin,year_end,period)
{
	var dates = [];
	var limit_date = new Date(limit_time);
/*	console.log(year_begin);
	console.log(year_end);*/
	for (var y = year_begin; y <= year_end;y++)
	{
/*		console.log(json_periods[String(y)][period].end);
		console.log(json_periods[String(y)][period].begin);*/
		for (var d = new Date(json_periods[String(y)][period].end); d > new Date(json_periods[String(y)][period].begin); d.setDate(d.getDate() - 1)) 
		{
			if(limit_time.getDay()==d.getDay()) // Only if same day of the week, algo choice XXX
			{
				d.setHours(limit_date.getHours());
				d.setMinutes(limit_date.getMinutes());
				d.setSeconds(limit_date.getSeconds());
				dates.push(new Date(d));				
			}
		}
	}
	return dates;
}

// Uses the recent datas to update de prediction
// in order to match the reality
function prediction_adapt()
{
	// TODO
	// predict = predict(futur) + (predict(now) - now)
	// predict = (predict_quality*predict(futur)+predict)/(1+predict_quality)
}

// calendar JSON file
var json_timePeriods 	= require('../../data/time/vacances.json');

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
		DFM: 	"decreasing factor mean",
		NONE: 	"none" 
	}),

	// returns the period associated to the time
	period: function (time)
	{
		find_period(json_timePeriods,time);
	},
	
	// [OLD version]
	// Gives a prediction of the state
	// of a Velov station (id)
	// at a certain point in time (time) 
	// using the selected analysis algorithm (analysisMode) 
	// TODO -> not useful anymore, smalled database but a lot slower
	predict_old: function (id,time,analysisMode,callback) 
	{
		// ----- Data fetching
		var t_init = new Date().getTime();
		var WEEK_SIZE 			= 7;
		var station_free		= [];
		var station_occup		= [];
		var query_result		= [];
		var util 				= require('util');
		var json_timePeriods 	= require('../../data/time/vacances.json');
		var date 				= new Date(time);
		var year 				= date.getFullYear();
		var period 				= find_period(json_timePeriods,time);
		var dates 				= [];
		var callback_nb			= 0;
		if(period == DEFAULT_PERIOD)
		{
			dates = generate_defaultPeriod(json_timePeriods,date,2013,year);
			//console.log("Default period");
		}
		else
		{
			dates = generate_specificPeriod(json_timePeriods,date,2013,year,period);
			//console.log("Specific period: " + period);
		}
		//console.log("Dates: "+dates);
		var t0 = new Date().getTime();
		//var t_query_cb_0 = 0;
		//var t_query_cb_1 = 0;
		for (var j = 0; j < dates.length; j++) 
		{
			queryMeasures(id,new Date(dates[j]),
				function(query_result)
				{
					//t_query_cb_0 = new Date().getTime();
					if(query_result == undefined || query_result.length == 0) // no data has been found corresponding to id (unlikely, or call para error) or time (possible)
					{
						//console.log("                Skipping query result ("+id+")");
					}	
					else
					{		
						for (i = 0; i < query_result.length; i++) 
						{ 
							station_free.push(query_result[i].available_bike_stands);
							station_occup.push(query_result[i].available_bikes);
							console.log("measure date:   "+new Date(query_result[i].last_update));
							console.log("free - occup:   "+query_result[i].available_bike_stands+"/"+query_result[i].available_bikes);
						}
						//console.log("                Query result used     ("+id+")");
					}
					callback_nb++; // before to avoid dates.length - 1 at each loop
					//t_query_cb_1 = new Date().getTime();
					console.log("delta t = "+(t_query_cb_1-t_query_cb_0));
					if(callback_nb == dates.length) // TODO via async ?
					{		
						// ----- Data analysis
						
						var t1 = new Date().getTime();
						var free_overTime 		= analysis(station_free,analysisMode);
						var occup_overTime 		= analysis(station_occup,analysisMode);
						var prediction_quality  = quality_analysis(station_free,station_occup,analysisMode);
						var t2 = new Date().getTime();
						console.log((t_0-t_init)+"-"+(t1-t0)+"-"+(t2-t1));

						//var diff_overTime 	= max_overTime - curr_overTime;
						console.log("station_free:   "+station_free);
						console.log("station_occup:  "+station_occup);
						console.log("free_overTime:  "+free_overTime);
						console.log("occup_overTime: "+occup_overTime);
						//console.log("diff_overTime: "+diff_overTime);
						var state;
						// ----- State selection
						if (isNaN(occup_overTime) || isNaN(free_overTime))
						{
							state = PredictionService.station_state.UNKNOWN;
						}
						else if (free_overTime < PredictionService.station_values.FULL_LIMIT)
						{
							state = PredictionService.station_state.FULL;
						} 
						else if (free_overTime <= PredictionService.station_values.NEAR_FULL_LIMIT)
						{
							state = PredictionService.station_state.NEAR_FULL;
						}
						else if (occup_overTime < PredictionService.station_values.EMPTY_LIMIT)
						{
							state = PredictionService.station_state.EMPTY;
						}
						else if (occup_overTime <= PredictionService.station_values.NEAR_EMPTY_LIMIT)
						{
							state = PredictionService.station_state.NEAR_EMPTY;
						}
						else
						{
							state = PredictionService.station_state.INTERM;
						}
						callback(state,free_overTime,occup_overTime,prediction_quality);
					}
				}
			);
		}
	},
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
		var util 				= require('util');
		var date 				= new Date(time);
		var year 				= date.getFullYear();
		var dates 				= [];
		var callback_nb			= 0;
		queryMeasures(id,new Date(dates[j]),
			function(query_result)
			{
				//t_query_cb_0 = new Date().getTime();
				if(query_result == undefined || query_result.length == 0) // no data has been found corresponding to id (unlikely, or call para error) or time (possible)
				{
					//console.log("                Skipping query result ("+id+")");
				}	
				else
				{		
					for (i = 0; i < query_result.length; i++) 
					{ 
						station_free.push(query_result[i].available_bike_stands);
						station_occup.push(query_result[i].available_bikes);
						console.log("measure date:   "+new Date(query_result[i].last_update));
						console.log("free - occup:   "+query_result[i].available_bike_stands+"/"+query_result[i].available_bikes);
					}
					//console.log("                Query result used     ("+id+")");
				}
				callback_nb++; // before to avoid dates.length - 1 at each loop
				//t_query_cb_1 = new Date().getTime();
				console.log("delta t = "+(t_query_cb_1-t_query_cb_0));
				if(callback_nb == dates.length) // TODO via async ?
				{		
					// ----- Data analysis
					
					var t1 = new Date().getTime();
					var free_overTime 		= analysis(station_free,analysisMode);
					var occup_overTime 		= analysis(station_occup,analysisMode);
					var prediction_quality  = quality_analysis(station_free,station_occup,analysisMode);
					var t2 = new Date().getTime();
					console.log((t_0-t_init)+"-"+(t1-t0)+"-"+(t2-t1));

					//var diff_overTime 	= max_overTime - curr_overTime;
					console.log("station_free:   "+station_free);
					console.log("station_occup:  "+station_occup);
					console.log("free_overTime:  "+free_overTime);
					console.log("occup_overTime: "+occup_overTime);
					//console.log("diff_overTime: "+diff_overTime);
					var state;
					// ----- State selection
					if (isNaN(occup_overTime) || isNaN(free_overTime))
					{
						state = PredictionService.station_state.UNKNOWN;
					}
					else if (free_overTime < PredictionService.station_values.FULL_LIMIT)
					{
						state = PredictionService.station_state.FULL;
					} 
					else if (free_overTime <= PredictionService.station_values.NEAR_FULL_LIMIT)
					{
						state = PredictionService.station_state.NEAR_FULL;
					}
					else if (occup_overTime < PredictionService.station_values.EMPTY_LIMIT)
					{
						state = PredictionService.station_state.EMPTY;
					}
					else if (occup_overTime <= PredictionService.station_values.NEAR_EMPTY_LIMIT)
					{
						state = PredictionService.station_state.NEAR_EMPTY;
					}
					else
					{
						state = PredictionService.station_state.INTERM;
					}
					callback(state,free_overTime,occup_overTime,prediction_quality);
				}
			}
		);

	}
};