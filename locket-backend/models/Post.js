const mongoose = require('mongoose');

const PostSchema = new mongoose.Schema({
    user: {
        type: mongoose.Schema.ObjectId,
        ref: 'User',
        required: [true, 'Post must belong to a user']
    },
    imageUrl: {
        type: String,
        required: [true, 'Post must have an image']
    },
    caption: {
        type: String,
        trim: true,
        maxlength: [200, 'Caption cannot exceed 200 characters']
    },
    likes: [{
        user: {
            type: mongoose.Schema.ObjectId,
            ref: 'User'
        },
        createdAt: {
            type: Date,
            default: Date.now
        }
    }],
    comments: [{
        user: {
            type: mongoose.Schema.ObjectId,
            ref: 'User',
            required: true
        },
        text: {
            type: String,
            required: [true, 'Comment text is required'],
            maxlength: [100, 'Comment cannot exceed 100 characters']
        },
        createdAt: {
            type: Date,
            default: Date.now
        }
    }],
    isActive: {
        type: Boolean,
        default: true
    }
}, {
    timestamps: true,
    toJSON: { virtuals: true },
    toObject: { virtuals: true }
});

// Virtual for likes count
PostSchema.virtual('likesCount').get(function() {
    return this.likes.length;
});

// Virtual for comments count
PostSchema.virtual('commentsCount').get(function() {
    return this.comments.length;
});

// Index for better query performance
PostSchema.index({ user: 1, createdAt: -1 });
PostSchema.index({ createdAt: -1 });

module.exports = mongoose.model('Post', PostSchema); 