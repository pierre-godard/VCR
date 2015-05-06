/**
 * Information.js
 *
 * @description :: TODO: You might write a short summary of how this model works and what it represents here.
 * @docs        :: http://sailsjs.org/#!documentation/models
 */

module.exports = {

    attributes:
    {

        identifier:
        {
            type: 'string',
            required: true,
            primaryKey: true,
            unique: true
        },

        title:
        {
            type: 'string',
            required: true
        },

        description:
        {
            type: 'string',
            defaultsTo: ''
        },

        progression:
        {
            type: 'integer',
            defaultsTo: 100
        }
    },
    
    beforeValidate: function(value, cb)
    {
        if (value.progression)
        {
            if (value.progression <   0) value.progression =   0;
            if (value.progression > 100) value.progression = 100;
        }
        cb();
    }
    
};