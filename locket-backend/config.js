require('dotenv').config();

module.exports = {
    PORT: process.env.PORT || 3000,
    NODE_ENV: process.env.NODE_ENV || 'development',
    
    // MongoDB Configuration
    MONGODB_URI: process.env.MONGODB_URI || 'mongodb://localhost:27017/locket_db',
    
    // JWT Configuration
    JWT_SECRET: process.env.JWT_SECRET || 'your_jwt_secret_key_here_make_it_long_and_complex',
    JWT_EXPIRE: process.env.JWT_EXPIRE || '7d',
    
    // CORS Configuration
    CORS_ORIGIN: process.env.CORS_ORIGIN || '*'
}; 