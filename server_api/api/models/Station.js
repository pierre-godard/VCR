/**
 * Station.js
 *
 * @description :: TODO: You might write a short summary of how this model works and what it represents here.
 * @docs        :: http://sailsjs.org/#!documentation/models
 */

module.exports = 
{

    attributes: 
	{
        number: 
		{
            type: 'integer',
            required: true,
            primaryKey: true,
            unique: true
        },
        name: 
		{
            type: 'string'
        },
        address: 
		{
            type: 'string'
        },
        latitude: 
		{
            type: 'float',
            required: true
        },
        longitude: 
		{
            type: 'float',
            required: true
        },
        bonus: 
		{
            type: 'boolean',
            defaultsTo: 'false'
        }
    },
    
    createOrUpdate: function(stations, next)
    {
        if (!(stations instanceof Array))
        {
            stations = [stations];
        }
        _.forEach(
            stations,
            function (station)
            {
                Station.create(station)
                .exec(
                    function (err, added)
                    {
                        // if (err) next(err);
                    }
                );
            }
        );
    }
    
};