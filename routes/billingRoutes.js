// routes/billingRoutes.js

const express = require('express');
const router = express.Router();
const {
  setBillingSettings,
  getBillingSettings,
} = require('../controllers/billingController.js');

// A single route to handle both getting and setting/updating the price configuration
router.route('/settings').post(setBillingSettings).get(getBillingSettings);

module.exports = router;

