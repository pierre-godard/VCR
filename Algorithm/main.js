var http = require('http');

var station_state = Object.freeze({
	EMPTY: "empty", 
	NEAR_EMPTY: "near empty", 
	INTERM: "places available",
	NEAR_FULL: "near full",
	FULL: "full"
});

// Mean of the array passed as parameter
function mean (array) {
	var sum_val = 0;
	for (i = 0; i < array.length; i++) { 
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
function decreasingFactor_mean (factor,step,array) {
	var sum_val = 0;
	var final_denominator = 0;
	var current_factor = 1;
	for (i = 0; i < array.length; i++) { 
    	sum_val += current_factor*array[i];
    	final_denominator += current_factor;
    	if(i%step == 0) {
    		current_factor *= factor;
    	}    	
	}
	return sum_val/final_denominator;
}

function predict (id,time) {
	var week_day = time.getDay();

}
 
var server = http.createServer(function(request, response){
	var arr = [10,20,30];
    response.writeHead(200, {'Content-Type': 'text/plain'});
    response.write("Mean of: "+arr+" = "+mean(arr)+"\n");
    response.end('Hello World\n');
});
 
server.listen(3000);
 
console.log('Adresse du serveur: http://localhost:3000');