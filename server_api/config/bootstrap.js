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

module.exports.bootstrap = function (cb)
{

    JCDecauxService.requestStations(
        function (stations, err)
        {

            if (err) return cb(err);

            UtilService.post_information({
                identifier: 'station_request',
                title: 'Stations update',
                description: 'Updating static data on stations',
                progression: 0
            });

            if (err) return cb(err);

            Station.createOrUpdate(
                stations,
                function (err2)
                {
                    if (err2) return cb(err);
                }
            );
            
            UtilService.post_information({
                identifier: 'station_request',
                title: 'Stations update',
                description: 'Updating static data on stations',
                progression: 100
            });
            
            console.log("       Setting up the stations");

        }
    );
    
    var onTickFunction = function ()
    {
        JCDecauxService.requestMeasures(
            function (measures, err)
            {

                if (err) return console.log(err);
                
                var timestamp = new Date().getTime();
                
                UtilService.post_information({
                    identifier: 'measure_request_' + timestamp,
                    title: 'Measures update',
                    description: 'Updating dynamic data on stations',
                    progression: 0
                });

                Measure.createOrUpdate(
                    measures,
                    function (err2)
                    {
                        if (err2) return console.log(err2);
                    }
                );
            
                Information.update({
                    identifier: 'measure_request'
                },{
                    progression: 50
                }).exec(
                    function (err2, added)
                    {
                        if (err2) return console.log(err2);
                    }
                );

                LastMeasure.createOrUpdate(
                    measures,
                    function (err2)
                    {
                        if (err2) return console.log(err2);
                    }
                );
            
                UtilService.post_information({
                    identifier: 'measure_request_' + timestamp,
                    title: 'Measures update',
                    description: 'Updating dynamic data on stations',
                    progression: 100
                });
                
                console.log("       Fetching new measures");

            }
        );
    }
    
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