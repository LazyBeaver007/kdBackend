const mongoose = require('mongoose');

const farmerSchema = new mongoose.Schema(
    {
            userId: {
      type: String,
      required: true,
      unique: true,
    },
            name: {
            type: String,
            required: [true, 'Please add a name'],
            trim: true,
                },
            
            phone: {
            type: String,
            required: [true, 'Please add a phone number'],
            },
            
            address: {
            type: String,
            required: [true, 'Please add an address'],
            },
           
            photoUrl: {
            type: String,
            default: '',
            },
            
            qrCodeUrl: {
            type: String,
            default: '',
            },

            isActive:{
           
            type: Boolean,
            default: true,
            },
        },
        {
        timestamps: true,
    }
);


module.exports = mongoose.model('Farmer', farmerSchema)