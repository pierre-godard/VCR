/**
 * Measure.js
 *
 * @description :: TODO: You might write a short summary of how this model works and what it represents here.
 * @docs        :: http://sailsjs.org/#!documentation/models
 */

module.exports = {

    attributes: {
        last_update: {
            type: 'integer',
            required: true,
            primaryKey: true
        },
        station: {
            model: 'Station',
            required: true
        },
        available_bike_stands: {
            type: 'integer',
            required: true
        },
        available_bikes: {
            type: 'integer',
            required: true
        }
    },
    
    beforeValidate: function(value, cb)
    {
        if (value.position && value.position.lng && value.position.lat)
        {
            value.longitude = value.position.lng;
            value.latitude = value.position.lat;
        }
        value.station = value.number;
        delete value['number'];
        delete value['name'];
        delete value['address'];
        delete value['position'];
        delete value['banking'];
        delete value['bike_stands'];
        delete value['contract_name'];
        delete value['longitude'];
        delete value['latitude'];
        cb();
    }
    
};