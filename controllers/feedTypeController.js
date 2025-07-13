

const asyncHandler = require('express-async-handler');
const FeedType = require('../models/FeedType.js');

/**
 * @desc    Create a new feed type
 * @route   POST /api/feedtypes
 * @access  Private/Admin
 */
const createFeedType = asyncHandler(async (req, res) => {
  const { name, unit, price } = req.body;

  if (!name || !unit || price === undefined) {
    res.status(400);
    throw new Error('Please provide name, unit, and price for the feed type');
  }

  const feedTypeExists = await FeedType.findOne({ name });

  if (feedTypeExists) {
    res.status(400);
    throw new Error('A feed type with this name already exists');
  }

  const feedType = await FeedType.create({
    name,
    unit,
    price,
  });

  if (feedType) {
    res.status(201).json(feedType);
  } else {
    res.status(400);
    throw new Error('Invalid feed type data');
  }
});

/**
 * @desc    Get all feed types
 * @route   GET /api/feedtypes
 * @access  Private/Admin
 */
const getFeedTypes = asyncHandler(async (req, res) => {
  const feedTypes = await FeedType.find({});
  res.status(200).json(feedTypes);
});

/**
 * @desc    Update a feed type
 * @route   PUT /api/feedtypes/:id
 * @access  Private/Admin
 */
const updateFeedType = asyncHandler(async (req, res) => {
  const { name, unit, price, isActive } = req.body;
  const feedType = await FeedType.findById(req.params.id);

  if (feedType) {
    feedType.name = name || feedType.name;
    feedType.unit = unit || feedType.unit;
    if (price !== undefined) {
      feedType.price = price;
    }
    if (isActive !== undefined) {
      feedType.isActive = isActive;
    }

    const updatedFeedType = await feedType.save();
    res.status(200).json(updatedFeedType);
  } else {
    res.status(404);
    throw new Error('Feed type not found');
  }
});

module.exports = {
  createFeedType,
  getFeedTypes,
  updateFeedType,
};
