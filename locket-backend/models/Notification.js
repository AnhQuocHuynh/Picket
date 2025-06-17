const mongoose = require('mongoose');

const NotificationSchema = new mongoose.Schema({
    recipient: {
        type: mongoose.Schema.ObjectId,
        ref: 'User',
        required: [true, 'Recipient is required']
    },
    sender: {
        type: mongoose.Schema.ObjectId,
        ref: 'User',
        required: [true, 'Sender is required']
    },
    type: {
        type: String,
        enum: [
            'friend_request',
            'friend_accepted',
            'new_post',
            'post_like',
            'post_comment',
            'mention',
            'system'
        ],
        required: [true, 'Notification type is required']
    },
    title: {
        type: String,
        required: [true, 'Notification title is required'],
        maxlength: [100, 'Title cannot exceed 100 characters']
    },
    message: {
        type: String,
        required: [true, 'Notification message is required'],
        maxlength: [500, 'Message cannot exceed 500 characters']
    },
    relatedPost: {
        type: mongoose.Schema.ObjectId,
        ref: 'Post'
    },
    relatedFriendship: {
        type: mongoose.Schema.ObjectId,
        ref: 'Friendship'
    },
    data: {
        type: mongoose.Schema.Types.Mixed // For additional data
    },
    isRead: {
        type: Boolean,
        default: false
    },
    isDelivered: {
        type: Boolean,
        default: false
    },
    deliveredAt: {
        type: Date
    },
    readAt: {
        type: Date
    }
}, {
    timestamps: true
});

// Indexes for efficient queries
NotificationSchema.index({ recipient: 1, createdAt: -1 });
NotificationSchema.index({ recipient: 1, isRead: 1 });
NotificationSchema.index({ recipient: 1, type: 1 });

// Static method to create notification
NotificationSchema.statics.createNotification = async function(data) {
    const {
        recipientId,
        senderId,
        type,
        title,
        message,
        relatedPostId = null,
        relatedFriendshipId = null,
        additionalData = {}
    } = data;

    // Don't send notification to yourself
    if (recipientId.toString() === senderId.toString()) {
        return null;
    }

    return await this.create({
        recipient: recipientId,
        sender: senderId,
        type,
        title,
        message,
        relatedPost: relatedPostId,
        relatedFriendship: relatedFriendshipId,
        data: additionalData
    });
};

// Static method to mark as read
NotificationSchema.statics.markAsRead = async function(notificationIds, userId) {
    return await this.updateMany(
        {
            _id: { $in: notificationIds },
            recipient: userId,
            isRead: false
        },
        {
            isRead: true,
            readAt: new Date()
        }
    );
};

// Static method to mark all as read
NotificationSchema.statics.markAllAsRead = async function(userId) {
    return await this.updateMany(
        {
            recipient: userId,
            isRead: false
        },
        {
            isRead: true,
            readAt: new Date()
        }
    );
};

// Static method to get unread count
NotificationSchema.statics.getUnreadCount = async function(userId) {
    return await this.countDocuments({
        recipient: userId,
        isRead: false
    });
};

// Static method to get notifications with pagination
NotificationSchema.statics.getNotifications = async function(userId, page = 1, limit = 20) {
    const skip = (page - 1) * limit;
    
    const notifications = await this.find({ recipient: userId })
        .populate('sender', 'username displayName profilePicture')
        .populate('relatedPost', 'imageUrl caption')
        .sort({ createdAt: -1 })
        .skip(skip)
        .limit(limit);

    const total = await this.countDocuments({ recipient: userId });
    const unreadCount = await this.getUnreadCount(userId);

    return {
        notifications,
        pagination: {
            page,
            pages: Math.ceil(total / limit),
            total,
            unreadCount
        }
    };
};

// Method to mark as delivered
NotificationSchema.methods.markAsDelivered = function() {
    this.isDelivered = true;
    this.deliveredAt = new Date();
    return this.save();
};

// Virtual for time ago
NotificationSchema.virtual('timeAgo').get(function() {
    const now = new Date();
    const diffMs = now - this.createdAt;
    const diffMins = Math.floor(diffMs / 60000);
    const diffHours = Math.floor(diffMins / 60);
    const diffDays = Math.floor(diffHours / 24);

    if (diffMins < 1) return 'just now';
    if (diffMins < 60) return `${diffMins}m ago`;
    if (diffHours < 24) return `${diffHours}h ago`;
    if (diffDays < 7) return `${diffDays}d ago`;
    return this.createdAt.toLocaleDateString();
});

// Include virtuals in JSON
NotificationSchema.set('toJSON', { virtuals: true });

module.exports = mongoose.model('Notification', NotificationSchema); 