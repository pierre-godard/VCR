/**
* LastMeasure.js
*
* @description :: TODO: You might write a short summary of how this model works and what it represents here.
* @docs        :: http://sailsjs.org/#!documentation/models
*/

var formatter = function (value)
{
    if (value.position && value.position.lng && value.position.lat)
    {
        value.longitude = value.position.lng;
        value.latitude = value.position.lat;
    }
    if (value.number)
    {
        value.station = value.number;
    }
    value.identifier = value.station + value.last_update * 1000;
    delete value['number'];
    delete value['name'];
    delete value['bonus'];
    delete value['status'];
    delete value['address'];
    delete value['position'];
    delete value['banking'];
    delete value['bike_stands'];
    delete value['contract_name'];
    delete value['longitude'];
    delete value['latitude'];
}

module.exports = {

    attributes: {
        identifier: {
            type: 'integer',
            required: true
        },
        last_update: {
            type: 'integer',
            required: true
        },
        station: {
            model: 'Station',
            required: true,
            primaryKey: true,
            unique: true
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
        formatter(value);
        cb();
    },
    
    createOrUpdate: function(measures, next)
    {
        if (!(measures instanceof Array))
        {
            measures = [measures];
        }
        _.forEach(
            measures,
            function (measure)
            {
                LastMeasure.create(measure)
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
