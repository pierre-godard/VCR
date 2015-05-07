/**
 * Bootstrap
 * (sails.config.bootstrap)
 *
 * An asynchronous bootstrap function that runs before your Sails app gets lifted.
 * This gives you an opportunity to set up your data model, run jobs, or perform some special logic.
 *
 * For more information on bootstrapping your app, check out:
 * http://sailsjs.org/#/documentation/reference/sails.config/sails.config.bootstrap.html
 */

var onStartFunction = function(next)
{
    JCDecauxService.requestStations(
        function (stations, err)
        {
            if (err) return next(err);
            
            console.log("       Setting up the stations");

            Station.createOrUpdate(
                stations,
                function (err2)
                {
                    if (err2) return next(err2);
                }
            );
            
            UtilService.post_information({
                identifier: 'station_request',
                title: 'Stations update',
                description: 'Updating static data on stations',
                progression: 100
            });
            
        }
    );
}

var onTickFunction = function ()
{
    JCDecauxService.requestMeasures(
        function (measures, err)
        {

            if (err) return console.error(err);

            console.log("       Fetching new measures");

            Measure.createOrUpdate(
                measures,
                function (err2)
                {
                    if (err2) return console.error(err2);
                }
            );

            LastMeasure.createOrUpdate(
                measures,
                function (err2)
                {
                    if (err2) return console.error(err2);
                }
            );

            var timestamp = new Date().getTime();
            UtilService.post_information({
                identifier: 'measure_request_' + timestamp,
                title: 'Measures update',
                description: 'Updating dynamic data on stations',
                progression: 100
            });


        }
    );
}

module.exports.bootstrap = function (cb)
{
    onStartFunction(
        function (err)
        {
            if (err) 
            {
                console.error("Error: Bootstrap.onStartFunction");
                return cb(err);
            }
        }
    );
    
    onTickFunction();
    
    var CronJob = require('cron').CronJob;
    var job = new CronJob(
        {
            cronTime: '00 * * * * *',
            onTick: onTickFunction,
            start: false,
            timeZone: "America/Los_Angeles"
        }
    );
    job.start();
    
    // It's very important to trigger this callback method when you are finished
    // with the bootstrap!  (otherwise your server will never lift, since it's waiting on the bootstrap)
    cb();
};