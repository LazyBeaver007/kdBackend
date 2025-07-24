const express = require('express')
const connectDB = require('./config/db')
const farmerRoutes = require('./routes/farmerRoutes');
const feedTypeRoutes = require('./routes/feedTypeRoutes');
const transactionRoutes = require('./routes/transactionRoutes');


connectDB();

const app = express();
const PORT = 8000;
app.use(express.json());

app.get('/',(req, res) => {
    res.status(200).json({message: 'Welcome to Backend API'});
});

app.use('/api/farmers',farmerRoutes);
app.use('/api/feedtypes', feedTypeRoutes);
app.use('/api/transactions', transactionRoutes);

app.listen(PORT,()=>{
    console.log(`Server is running on http://localhost:${PORT}`);
});
