// controllers/billingController.js

const asyncHandler = require('express-async-handler');
const BillingSetting = require('../models/BillingSetting.js');
const Farmer = require('../models/Farmer.js');
const Milk = require('../models/Milk.js');
const Feed = require('../models/Feed.js');
const Bill = require('../models/Bill.js');

/**
 * @desc    Set or update the billing settings for a specific month
 * @route   POST /api/billing/settings
 * @access  Private/Admin
 */
const setBillingSettings = asyncHandler(async (req, res) => {
  const { year, month, generalMilkPrice, specialPrices } = req.body;

  if (!year || !month || generalMilkPrice === undefined) {
    res.status(400).send({ message: 'Year, month, and generalMilkPrice are required.' });
    return;
  }

  const updatedSettings = await BillingSetting.findOneAndUpdate(
    { year, month },
    { $set: { generalMilkPrice, specialPrices: specialPrices || [] } },
    { new: true, upsert: true, runValidators: true }
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
        res.status(400).send({ message: 'Year and month query parameters are required.' });
        return;
    }

    const settings = await BillingSetting.findOne({ year, month });

    if (settings) {
        res.status(200).json(settings);
    } else {
        res.status(200).json({
            message: "No settings found for this period. Please create them.",
            year: parseInt(year),
            month: parseInt(month),
            generalMilkPrice: 0,
            specialPrices: []
        });
    }
});

/**
 * @desc    Generate all farmer bills for a specific month
 * @route   POST /api/billing/generate
 * @access  Private/Admin
 */
const generateMonthlyBills = asyncHandler(async (req, res) => {
    const { year, month } = req.body;
    if (!year || !month) {
        res.status(400).send({ message: 'Year and month are required to generate bills.' });
        return;
    }

    // --- 1. Define Date Ranges (as per FR-5.2) ---
    // Milk: 1st of the month to the end of the month
    const milkStartDate = new Date(year, month - 1, 1);
    const milkEndDate = new Date(year, month, 0, 23, 59, 59, 999); // Last day of the month

    // Feed: 7th of the month to the 6th of the next month
    const feedStartDate = new Date(year, month - 1, 7);
    const feedEndDate = new Date(year, month, 6, 23, 59, 59, 999);

    // --- 2. Fetch Prices and Farmers ---
    const settings = await BillingSetting.findOne({ year, month });
    if (!settings) {
        res.status(400).send({ message: `Price settings for ${month}/${year} are not defined. Please set them first.` });
        return;
    }
    const allActiveFarmers = await Farmer.find({ isActive: true });

    let generatedBills = [];
    let errors = [];

    // --- 3. Loop Through Each Farmer and Calculate Bill ---
    for (const farmer of allActiveFarmers) {
        // --- Calculate Total Milk Amount ---
        const milkTransactions = await Milk.find({
            farmer: farmer._id,
            date: { $gte: milkStartDate, $lte: milkEndDate }
        });

        let totalMilkQuantity = 0;
        milkTransactions.forEach(t => totalMilkQuantity += t.quantity);

        // Determine the correct milk price for this farmer
        const specialPrice = settings.specialPrices.find(p => p.farmer.equals(farmer._id));
        const milkPrice = specialPrice ? specialPrice.price : settings.generalMilkPrice;
        const totalMilkAmount = totalMilkQuantity * milkPrice;

        // --- Calculate Total Feed Amount ---
        const feedTransactions = await Feed.find({
            farmer: farmer._id,
            date: { $gte: feedStartDate, $lte: feedEndDate }
        });

        let totalFeedAmount = 0;
        feedTransactions.forEach(t => totalFeedAmount += (t.quantity * t.priceAtTransaction));

        // --- 4. Create or Update the Bill Document ---
        const finalPayableAmount = totalMilkAmount - totalFeedAmount;

        try {
            const bill = await Bill.findOneAndUpdate(
                { farmer: farmer._id, year, month },
                {
                    $set: {
                        milkDateRange: { from: milkStartDate, to: milkEndDate },
                        feedDateRange: { from: feedStartDate, to: feedEndDate },
                        totalMilkQuantity,
                        totalMilkAmount,
                        totalFeedAmount,
                        finalPayableAmount,
                        paymentStatus: 'Unpaid', // Reset to Unpaid on regeneration
                        milkTransactions: milkTransactions.map(t => t._id),
                        feedTransactions: feedTransactions.map(t => t._id),
                    }
                },
                { new: true, upsert: true, runValidators: true }
            );
            generatedBills.push(bill);
        } catch (error) {
            errors.push({ farmer: farmer.name, error: error.message });
        }
    }

    res.status(201).json({
        message: 'Bill generation process completed.',
        generatedCount: generatedBills.length,
        errorCount: errors.length,
        errors: errors,
        generatedBills,
    });
});


module.exports = {
  setBillingSettings,
  getBillingSettings,
  generateMonthlyBills,
};
