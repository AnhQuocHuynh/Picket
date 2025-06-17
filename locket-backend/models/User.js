const mongoose = require('mongoose');
const bcrypt = require('bcryptjs');

const UserSchema = new mongoose.Schema({
    username: {
        type: String,
        required: [true, 'Username is required'],
        unique: true,
        trim: true,
        minlength: [3, 'Username must be at least 3 characters'],
        maxlength: [20, 'Username cannot exceed 20 characters']
    },
    email: {
        type: String,
        required: [true, 'Email is required'],
        unique: true,
        lowercase: true,
        trim: true,
        match: [/^\w+([.-]?\w+)*@\w+([.-]?\w+)*(\.\w{2,3})+$/, 'Please enter a valid email']
    },
    password: {
        type: String,
        required: [true, 'Password is required'],
        minlength: [6, 'Password must be at least 6 characters'],
        select: false // Don't include password in queries by default
    },
    profilePicture: {
        type: String,
        default: null
    },
    displayName: {
        type: String,
        trim: true,
        maxlength: [50, 'Display name cannot exceed 50 characters']
    },
    bio: {
        type: String,
        trim: true,
        maxlength: [150, 'Bio cannot exceed 150 characters']
    },
    dateOfBirth: {
        type: Date
    },
    location: {
        type: String,
        trim: true,
        maxlength: [100, 'Location cannot exceed 100 characters']
    },
    website: {
        type: String,
        trim: true,
        maxlength: [200, 'Website URL cannot exceed 200 characters']
    },
    friends: [{
        type: mongoose.Schema.ObjectId,
        ref: 'User'
    }],
    friendsCount: {
        type: Number,
        default: 0
    },
    postsCount: {
        type: Number,
        default: 0
    },
    isVerified: {
        type: Boolean,
        default: false
    },
    isPrivate: {
        type: Boolean,
        default: false
    },
    isActive: {
        type: Boolean,
        default: true
    },
    emailVerified: {
        type: Boolean,
        default: false
    },
    lastLogin: {
        type: Date,
        default: Date.now
    },
    lastSeen: {
        type: Date,
        default: Date.now
    }
}, {
    timestamps: true // Automatically add createdAt and updatedAt
});

// Encrypt password before saving
UserSchema.pre('save', async function(next) {
    // Only run this function if password was actually modified
    if (!this.isModified('password')) return next();

    // Hash the password with cost of 12
    this.password = await bcrypt.hash(this.password, 12);
    next();
});

// Instance method to check password
UserSchema.methods.comparePassword = async function(candidatePassword) {
    return await bcrypt.compare(candidatePassword, this.password);
};

// Instance method to get user data without password
UserSchema.methods.toJSON = function() {
    const userObject = this.toObject();
    delete userObject.password;
    return userObject;
};

// Instance method to get public profile
UserSchema.methods.toPublicProfile = function() {
    return {
        _id: this._id,
        username: this.username,
        displayName: this.displayName,
        profilePicture: this.profilePicture,
        bio: this.bio,
        location: this.location,
        website: this.website,
        friendsCount: this.friendsCount,
        postsCount: this.postsCount,
        isVerified: this.isVerified,
        isPrivate: this.isPrivate,
        createdAt: this.createdAt
    };
};

// Update friends count when friends array changes
UserSchema.pre('save', function(next) {
    if (this.isModified('friends')) {
        this.friendsCount = this.friends.length;
    }
    next();
});

module.exports = mongoose.model('User', UserSchema); 