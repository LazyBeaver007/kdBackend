const mongoose = require('mongoose');

const feedTypeSchema = new mongoose.Schema(
    {
        name: {
            type: String,
            required: [true, 'Please add a feed name'],
            trim: true,
            unique: true,
        },

        unit: {
            type: String,
            required: [true, 'please specify the unit'],
            default: 'Bag',
        },

        price: {
            type: Number,
            required: [true, 'Please set a price for this feed type'],
            default: 0,
        },

        //Allows admin to temporarily disable a feed type without deleting it
        isActive: {
            type: Boolean,
            default: true,
        },

    },
    {
        timestamps: true,
    }
);

module.exports = mongoose.model('FeedType', feedTypeSchema);