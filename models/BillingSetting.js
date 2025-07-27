const mongoose = require('mongoose');

const specialPriceSchema = new mongoose.Schema({
    //Link to the farmer who gets the special Price
    farmer: 
    {
        type: mongoose.Schema.Types.ObjectId,
        ref: 'Farmer',
        required: true,
    },

    //special price for this farmer
    price: 
    {
        type: Number,
        required: true,
    },
});

/**
 * Mongoose schema for the BillingSetting collection.
 * Allows the Admin to set milk prices for a specific billing period.
 * Corresponds to requirements FR-4.2 and FR-4.3.
 */
const billingSettingSchema = new mongoose.Schema(
  {
    // The billing period this setting applies to.
    // We store the year and month (1-12).
    year: {
      type: Number,
      required: true,
    },
    month: {
      type: Number,
      required: true,
      min: 1,
      max: 12,
    },
    // The general milk price per litre for this period (FR-4.2)
    generalMilkPrice: {
      type: Number,
      required: [true, 'Please set a general milk price'],
      default: 0,
    },
    // An array of special prices for specific farmers that override the general price (FR-4.3)
    specialPrices: [specialPriceSchema],
  },
  {
    timestamps: true,
  }
);

// Create a compound unique index to ensure there's only one setting document per month/year.
billingSettingSchema.index({ year: 1, month: 1 }, { unique: true });

module.exports = mongoose.model('BillingSetting', billingSettingSchema);
