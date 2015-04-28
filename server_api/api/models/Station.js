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
            primaryKey: true
        },
        name: 
		{
            type: 'string',
            required: true
        },
        address: 
		{
            type: 'string',
            required: true
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
            defaultTo: 'false'
        },
        state: 
		{
            type: 'string',
            enum: ['OPEN', 'CLOSE'],
            defaultsTo: 'CLOSE'
        },
        measures: 
		{
            collection: 'Measure',
            via: 'station'
        }
    }
};
