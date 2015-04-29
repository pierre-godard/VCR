var http = require('http');

// Velov station potential state
var station_state = Object.freeze(
{
	EMPTY: "empty", 
	NEAR_EMPTY: "near empty", 
	INTERM: "places available",
	NEAR_FULL: "near full",
	FULL: "full"
});

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
	var sum_val = 0;
	var final_denominator = 0;
	var current_factor = 1;
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

// Gives a prediction of the state
// of a Velov station (id)
// at a certain point in time (time)
function predict (id,time) 
{
	//var week_day = time.getDay();
	var WEEK_SIZE = 7;
	var selected_days = [];
	var limit_date = new Date(2012, 0, 1);
	for (var d = new Date(time); d > limit_date; d.setDate(d.getDate() - WEEK_SIZE)) 
	{
		selected_days.push(new Date(d));
	}
	return selected_days;
}
 
var server = http.createServer(function(request, response)
{
	var mean_arr = [10,20,32];
	var mean_fac = 0.8;
	var mean_step = 1;
    response.writeHead(200, {'Content-Type': 'text/plain'});
    response.write("Mean of: "+mean_arr+" = "+mean(mean_arr)+"\n");
    response.write("Decreasing factor mean of: "+mean_arr+" (fac: "+mean_fac+", step: "+mean_step+") = "
    	+decreasingFactor_mean(mean_fac,mean_step,mean_arr)+"\n");
    response.write("Prediction: "+predict(1,Date.now())+"\n");
    response.end('--- END ---\n');
});
 
server.listen(3000);
 
console.log('Adresse du serveur: http://localhost:3000');