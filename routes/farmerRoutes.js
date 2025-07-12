const express = require('express')
const router = express.Router();
const {createFarmer, getFarmer, getFarmers} = require('../controllers/farmerController.js');

router.route('/').get(getFarmers).post(createFarmer);

module.exports = router;