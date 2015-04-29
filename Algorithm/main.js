var http = require('http');

function mean (array) {
	var mean_val = 0;
	for (i = 0; i < array.length; i++) { 
    	mean_val += array[i];
	}
	return mean_val/i;
}

function predict () {

}
 
var server = http.createServer(function(request, response){
	var arr = [10,20,30];
    response.writeHead(200, {'Content-Type': 'text/plain'});
    response.write("Mean of: "+arr+" = "+mean(arr)+"\n");
    response.end('Hello World\n');
});
 
server.listen(3000);
 
console.log('Adresse du serveur: http://localhost:3000');