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
    const idRegex = /-(\d+)$/;
    if(lastFarmer && lastFarmer.userId && idRegex.test(lastFarmer.userId))
    {
        const lastId = parseInt(lastFarmer.userId.match(idRegex)[1]);
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
    const keyword = req.query.search
    ? {
      name: {
        $regex: req.query.search,
        $options: 'i',
      },
    }
    : {};

    const farmers = await Farmer.find({...keyword});
    res.status(200).json(farmers);
});

const getFarmerById = asyncHandler(async (req,res)=>
{
  const farmer = await Farmer.findById(req.params.id);

  if (farmer)
  {
    res.status(200).json(farmer);
  }
  else 
  {
    res.status(404);
    throw new Error('Farmer not found');
  }
});


const updateFarmer = asyncHandler(async (req, res)=>
{
  const farmer = await Farmer.findById(req.params.id);

  if(farmer) 
  {
    farmer.name = req.body.name || farmer.name;
    farmer.phone = req.body.phone || farmer.phone;
    farmer.address = req.body.address || farmer.address;

    if(req.body.isActive !== undefined)
    {
      farmer.isActive = req.body.isActive;
    }

    const updatedFarmer = await farmer.save();
    res.status(200).json(updatedFarmer);
  }
  else 
  {
    res.status(404);
    throw new Error('Farmer not found');
  }
});

module.exports = 
{
    createFarmer,
    getFarmers,
    getFarmerById,
    updateFarmer,
};