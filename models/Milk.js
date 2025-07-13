const mongoose = require('mongoose');


const milkSchema = new mongoose.Schema(
    {
        farmer: {
            type: mongoose.Schema.Types.ObjectId,
            required: true,
            ref: 'Farmer',
        },

        quantity: {
            type: Number,
            required: [true, 'Please eneter milk quatity in litres'],
        },

        session: {
            type: String,
            required: true,
            enum: ['Morning','Evening'],
        },

        data: {
            type: Date,
            default: Date.now,
        }
    },
    {
        timestamps: true,
    }
);

module.exports = mongoose.model('Milk', milkSchema)