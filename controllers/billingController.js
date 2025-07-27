// controllers/billingController.js

const asyncHandler = require('express-async-handler');
const BillingSetting = require('../models/BillingSetting.js');

/**
 * @desc    Set or update the billing settings for a specific month
 * @route   POST /api/billing/settings
 * @access  Private/Admin
 */
const setBillingSettings = asyncHandler(async (req, res) => {
  const { year, month, generalMilkPrice, specialPrices } = req.body;

  // --- Validation ---
  if (!year || !month || generalMilkPrice === undefined) {
    res.status(400);
    throw new Error('Year, month, and generalMilkPrice are required.');
  }

  // Find the existing setting for the given month and year, or create a new one.
  // 'upsert: true' creates a new document if one doesn't exist.
  // 'new: true' returns the modified document rather than the original.
  const updatedSettings = await BillingSetting.findOneAndUpdate(
    { year, month },
    {
      $set: {
        generalMilkPrice,
        specialPrices: specialPrices || [], // Ensure specialPrices is an array
      },
    },
    {
      new: true,
      upsert: true,
      runValidators: true,
    }
  );

  res.status(200).json(updatedSettings);
});

/**
 * @desc    Get the billing settings for a specific month
 * @route   GET /api/billing/settings
 * @access  Private/Admin
 */
const getBillingSettings = asyncHandler(async (req, res) => {
  const { year, month } = req.query;

  if (!year || !month) {
    res.status(400);
    throw new Error('Year and month query parameters are required.');
  }

  const settings = await BillingSetting.findOne({ year, month });

  if (settings) {
    res.status(200).json(settings);
  } else {
    // If no settings are found, return a default structure
    res.status(200).json({
        message: "No settings found for this period. Please create them.",
        year: parseInt(year),
        month: parseInt(month),
        generalMilkPrice: 0,
        specialPrices: []
    });
  }
});

module.exports = {
  setBillingSettings,
  getBillingSettings,
};

