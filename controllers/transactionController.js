const asyncHandler = require('express-async-handler');
const Farmer = require('../models/Farmer.js');
const FeedType = require('../models/FeedType.js');
const Milk = require('../models/Milk.js');
const Feed = require('../models/Feed.js');

/**
 * @desc Record a milk collection transaction
 * @route POst /api/transaction/milk
 * @access Private/staff
 */

const recordMilkCollection = asyncHandler(async (req, res) => {
  const { farmerId, quantity, session } = req.body;

  // --- Validation ---
  if (!farmerId || !quantity || !session) {
    res.status(400);
    throw new Error('Missing farmerId, quantity, or session');
  }

  // Check if farmer exists and is active
  const farmer = await Farmer.findById(farmerId);
  if (!farmer || !farmer.isActive) {
    res.status(404);
    throw new Error('Farmer not found or is not active');
  }

  // --- Create Transaction ---
  const milkTransaction = await Milk.create({
    farmer: farmerId,
    quantity,
    session,
  });

  res.status(201).json(milkTransaction);
});

/**
 * @desc    Record a feed distribution transaction
 * @route   POST /api/transactions/feed
 * @access  Private/Staff
 */
const recordFeedDistribution = asyncHandler(async (req, res) => {
  const { farmerId, feedTypeId, quantity } = req.body;

  // --- Validation ---
  if (!farmerId || !feedTypeId || !quantity) {
    res.status(400);
    throw new Error('Missing farmerId, feedTypeId, or quantity');
  }

  // Check if farmer exists and is active
  const farmer = await Farmer.findById(farmerId);
  if (!farmer || !farmer.isActive) {
    res.status(404);
    throw new Error('Farmer not found or is not active');
  }

  // Check if feed type exists and is active
  const feedType = await FeedType.findById(feedTypeId);
  if (!feedType || !feedType.isActive) {
    res.status(404);
    throw new Error('Feed type not found or is not available');
  }

  // --- Create Transaction ---
  // We store the price at the time of transaction to ensure billing is accurate
  // even if the admin changes the price later.
  const feedTransaction = await Feed.create({
    farmer: farmerId,
    feedType: feedTypeId,
    quantity,
    priceAtTransaction: feedType.price,
  });

  res.status(201).json(feedTransaction);
});

module.exports = {
  recordMilkCollection,
  recordFeedDistribution,
};