// models/Bill.js

const mongoose = require('mongoose');

/**
 * Mongoose schema for the Bill collection.
 * Stores the finalized, calculated bill for a farmer for a specific period.
 * Corresponds to requirements in section FR-5.
 */
const billSchema = new mongoose.Schema(
  {
    // Link to the farmer this bill is for
    farmer: {
      type: mongoose.Schema.Types.ObjectId,
      ref: 'Farmer',
      required: true,
    },
    // The billing period
    year: {
      type: Number,
      required: true,
    },
    month: {
      type: Number,
      required: true,
    },
    // The specific date ranges used for calculation (as per FR-5.2)
    milkDateRange: {
      from: Date,
      to: Date,
    },
    feedDateRange: {
      from: Date,
      to: Date,
    },
    // --- Calculated Totals ---
    totalMilkQuantity: { type: Number, default: 0 },
    totalMilkAmount: { type: Number, default: 0 },
    totalFeedAmount: { type: Number, default: 0 },
    finalPayableAmount: { type: Number, default: 0 },
    // --- Status ---
    paymentStatus: {
      type: String,
      enum: ['Unpaid', 'Paid'],
      default: 'Unpaid',
    },
    // --- Itemized Details ---
    // Storing the IDs of the transactions included in this bill for historical accuracy
    milkTransactions: [{ type: mongoose.Schema.Types.ObjectId, ref: 'Milk' }],
    feedTransactions: [{ type: mongoose.Schema.Types.ObjectId, ref: 'Feed' }],
  },
  {
    timestamps: true, // Adds createdAt (Bill Generation Date) and updatedAt
  }
);

// Ensure a farmer can only have one bill per month/year period.
billSchema.index({ farmer: 1, year: 1, month: 1 }, { unique: true });

module.exports = mongoose.model('Bill', billSchema);
