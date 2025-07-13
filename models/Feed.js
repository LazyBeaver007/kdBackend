

const mongoose = require('mongoose');


const feedSchema = new mongoose.Schema(
  {
    // Link to the Farmer who is taking the feed
    farmer: {
      type: mongoose.Schema.Types.ObjectId,
      required: true,
      ref: 'Farmer',
    },
    // Link to the type of feed being distributed
    feedType: {
      type: mongoose.Schema.Types.ObjectId,
      required: true,
      ref: 'FeedType',
    },
    quantity: {
      type: Number,
      required: [true, 'Please enter the quantity'],
    },
    // The price of the feed type at the time of the transaction.
    // We store this here to protect against future price changes affecting past bills.
    priceAtTransaction: {
        type: Number,
        required: true,
    },
    // The date of the transaction, for billing purposes.
    date: {
        type: Date,
        default: Date.now,
    }
  },
  {
    timestamps: true,
  }
);

module.exports = mongoose.model('Feed', feedSchema);
