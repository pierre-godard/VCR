/**
 * Measure.js
 *
 * @description :: TODO: You might write a short summary of how this model works and what it represents here.
 * @docs        :: http://sailsjs.org/#!documentation/models
 */

module.exports = {

    NB_TIME_SLICES: 12,

    attributes: {
        last_update: {
            type: 'integer',
            required: true,
            primaryKey: true
        },
        day: {
            type: 'string',
            required: true
        },
        hour: {
            type: 'integer',
            required: true
        },
        time_slice: {
            type: 'integer',
            required: true
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
        var d = new Date(0);    // The 0 there is the key, which sets the date to the epoch
        d.setUTCSeconds(value.last_update);
        value.day = d.getDay();
        value.hour = d.getHours(); 
        value.time_slice = Math.floor(d.getMinutes()/Measure.NB_TIME_SLICES);
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