const express = require('express');
const router = express.Router();
const {
  createFeedType,
  getFeedTypes,
  updateFeedType,
} = require('../controllers/feedTypeController.js');

// Route for getting all feed types and creating a new one
router.route('/').get(getFeedTypes).post(createFeedType);

// Route for updating a specific feed type by its ID
router.route('/:id').put(updateFeedType);

module.exports = router;
