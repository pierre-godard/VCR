/**
 * DefaultController
 *
 * @description :: Server-side logic for managing defaults
 * @help        :: See http://links.sailsjs.org/docs/controllers
 */

module.exports = {
    
    view:
        function (req, res, next)
        {
            Information.find({})
            .exec(
                function (err, informations)
                {
                    if (err) return next(err);
                    if (!(informations instanceof Array))
                    {
                        informations = [informations];
                    }
                    informations = informations.slice(-30);
                    res.view(
                        'homepage',
                        {informations: informations}
                    );
                }
            );
        }
	
};

