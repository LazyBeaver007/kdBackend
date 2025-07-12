const asyncHandler = require('express-async-handler');
const qrcode = require('qrcode');
const Farmer = require('../models/Farmer.js');



const createFarmer = asyncHandler(async(req,res)=>
{
    const {name, phone, address} = req.body;

    if(!name || !phone || !address)
    {
        res.status(400);
        throw new Error('Please provide all details');
    }

    const lastFarmer = await Farmer.findOne({ userId: { $exists: true, $ne: null } }).sort({ 'createdAt': -1 });

    let nextId = 1001;
    if(lastFarmer && lastFarmer.userId)
    {
        const lastId = parseInt(lastFarmer.userId.split('-')[1]);
        nextId = lastId + 1;
    }
    const userId = `KDF-${nextId}`;

    const qrCodeUrl = await qrcode.toDataURL(userId);

    const farmer = await Farmer.create({
    userId,
    name,
    phone,
    address,
    qrCodeUrl,
    // photoUrl will be handled in a future step with file uploads
  });

  if (farmer) {
    res.status(201).json({
      _id: farmer._id,
      userId: farmer.userId,
      name: farmer.name,
      phone: farmer.phone,
      address: farmer.address,
      qrCodeUrl: farmer.qrCodeUrl,
      isActive: farmer.isActive,
    });
  } else {
    res.status(400);
    throw new Error('Invalid farmer data');
  }


});

const getFarmers = asyncHandler(async(req,res)=>
{
    const farmers = await Farmer.find({});
    res.status(200).json(farmers);
});

module.exports = 
{
    createFarmer,
    getFarmers,
};