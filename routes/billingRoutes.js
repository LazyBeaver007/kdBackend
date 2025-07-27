// routes/billingRoutes.js

const express = require('express');
const router = express.Router();
const {
  setBillingSettings,
  getBillingSettings,
  generateMonthlyBills,
} = require('../controllers/billingController.js');

// Route for getting and setting price configuration
router.route('/settings').post(setBillingSettings).get(getBillingSettings);

// Route to trigger the bill generation process for a specific month
router.post('/generate', generateMonthlyBills);

module.exports = router;
