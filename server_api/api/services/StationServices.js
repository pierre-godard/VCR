
module.exports = {

    populateStations: function() 
	{
		http.get("https://api.jcdecaux.com/vls/v1/stations?contract=Lyon&apiKey=8a078eb105498b9066de6e3b306311ab162a5819", 
			function(stations_json)
			{
				sails.models.Station.create(JSON.parse(stations_json));	
			}
		);

    }
};
