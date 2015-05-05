/**
 * Measure.js
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
    if (value.last_update % 1000 != 0)
    {
        value.last_update *= 1000;
    }
    value.identifier = value.station + value.last_update * 100;
    var d = new Date(0);    // The 0 there is the key, which sets the date to the epoch
    d.setUTCMilliseconds(value.last_update);
    value.specif_time = Measure.date_to_specificTime(d);
    value.period = PredictionService.period(value.last_update);
    /*value.day = d.getDay();
    value.hour = d.getHours(); 
    value.date = d.getDate();
    value.month = d.getMonth();*/
    value.time_slice = Math.floor(d.getMinutes()*Measure.NB_TIME_SLICES/60);
    delete value['number'];
    delete value['name'];
    delete value['address'];
    delete value['position'];
    delete value['banking'];
    delete value['bike_stands'];
    delete value['contract_name'];
    delete value['longitude'];
    delete value['latitude'];
}

module.exports = 
{

    NB_TIME_SLICES: 12,

    attributes: 
    {
        identifier: 
        {
            type: 'integer',
            required: true,
            primaryKey: true,
            unique: true
        },
        last_update: 
        {
            type: 'integer',
            required: true
        },
        /*month: 
        {
            type: 'integer',
            required: true
        },
        date: 
        {
            type: 'integer',
            required: true
        },
        day: 
        {
            type: 'integer',
            required: true
        },
        hour: 
        {
            type: 'integer',
            required: true
        },
        time_slice: 
        {
            type: 'integer',
            required: true
        },*/
        station: 
        {
            model: 'Station',
            required: true
        },
        available_bike_stands: 
        {
            type: 'integer',
            required: true
        },
        available_bikes: 
        {
            type: 'integer',
            required: true
        },
        specif_time: 
        {
            type: 'string', // TODO as integer?
            required: true
        },
        period:
        {
            type: 'integer',
            required: true
        }
    },

    // d as date
    date_to_specificTime: function(d)
    {
        return /*d.getMonth()+"-"+d.getDate()+"-"+*/d.getDay()+"-"+d.getHours()+
            "-"+Math.floor(d.getMinutes()*Measure.NB_TIME_SLICES/60);
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
                Measure.create(measure)
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