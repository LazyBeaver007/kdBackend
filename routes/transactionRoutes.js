// routes/transactionRoutes.js

const express = require('express');
const router = express.Router();
const {
  recordMilkCollection,
  recordFeedDistribution,
} = require('../controllers/transactionController.js');

// Route for recording a milk collection entry
router.post('/milk', recordMilkCollection);

// Route for recording a feed distribution entry
router.post('/feed', recordFeedDistribution);

module.exports = router;
