const express = require('express')
const router = express.Router();
const {
    createFarmer,  
    getFarmers,
    getFarmerById,
    updateFarmer,

} = require('../controllers/farmerController.js');

// Route for getting all farmers (with search) and creating a new farmer
// GET /api/farmers
// GET /api/farmers?search=John
// POST /api/farmers
router.route('/').get(getFarmers).post(createFarmer);

// Route for getting a single farmer by ID and updating them
// GET /api/farmers/60d21b4667d0d8992e610c85
// PUT /api/farmers/60d21b4667d0d8992e610c85
router.route('/:id').get(getFarmerById).put(updateFarmer);

module.exports = router;