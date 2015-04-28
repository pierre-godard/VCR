/**
 * Measure.js
 *
 * @description :: TODO: You might write a short summary of how this model works and what it represents here.
 * @docs        :: http://sailsjs.org/#!documentation/models
 */

module.exports = {

    attributes: {
        last_update: {
            type: 'date',
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
    }
};