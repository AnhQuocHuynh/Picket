const express = require('express');
const Post = require('../models/Post');
const { authenticate } = require('../middleware/auth');
const { validatePost, validateComment } = require('../middleware/validation');

const router = express.Router();

// @route   GET /api/posts
// @desc    Get all posts (feed)
// @access  Private
router.get('/', authenticate, async (req, res) => {
    try {
        const page = parseInt(req.query.page) || 1;
        const limit = parseInt(req.query.limit) || 10;
        const skip = (page - 1) * limit;

        const posts = await Post.find({ isActive: true })
            .populate('user', 'username profilePicture')
            .populate('likes.user', 'username')
            .populate('comments.user', 'username')
            .sort({ createdAt: -1 })
            .skip(skip)
            .limit(limit);

        const total = await Post.countDocuments({ isActive: true });

        res.json({
            success: true,
            data: posts,
            pagination: {
                page,
                pages: Math.ceil(total / limit),
                total
            }
        });
    } catch (error) {
        console.error('Get posts error:', error);
        res.status(500).json({
            success: false,
            message: 'Server error fetching posts',
            error: error.message
        });
    }
});

// @route   POST /api/posts
// @desc    Create a new post
// @access  Private
router.post('/', authenticate, validatePost, async (req, res) => {
    try {
        const { imageUrl, caption } = req.body;

        if (!imageUrl) {
            return res.status(400).json({
                success: false,
                message: 'Image URL is required'
            });
        }

        const post = await Post.create({
            user: req.user.id,
            imageUrl,
            caption: caption || ''
        });

        // Populate user data
        await post.populate('user', 'username profilePicture');

        res.status(201).json({
            success: true,
            message: 'Post created successfully',
            data: post
        });
    } catch (error) {
        console.error('Create post error:', error);
        res.status(500).json({
            success: false,
            message: 'Server error creating post',
            error: error.message
        });
    }
});

// @route   GET /api/posts/:id
// @desc    Get single post by ID
// @access  Private
router.get('/:id', authenticate, async (req, res) => {
    try {
        const post = await Post.findById(req.params.id)
            .populate('user', 'username profilePicture')
            .populate('likes.user', 'username')
            .populate('comments.user', 'username');

        if (!post || !post.isActive) {
            return res.status(404).json({
                success: false,
                message: 'Post not found'
            });
        }

        res.json({
            success: true,
            data: post
        });
    } catch (error) {
        console.error('Get post error:', error);
        res.status(500).json({
            success: false,
            message: 'Server error fetching post',
            error: error.message
        });
    }
});

// @route   PUT /api/posts/:id
// @desc    Update post
// @access  Private
router.put('/:id', authenticate, validatePost, async (req, res) => {
    try {
        const post = await Post.findById(req.params.id);

        if (!post || !post.isActive) {
            return res.status(404).json({
                success: false,
                message: 'Post not found'
            });
        }

        // Check if user owns the post
        if (post.user.toString() !== req.user.id) {
            return res.status(403).json({
                success: false,
                message: 'Not authorized to update this post'
            });
        }

        const { caption } = req.body;
        post.caption = caption;
        await post.save();

        await post.populate('user', 'username profilePicture');

        res.json({
            success: true,
            message: 'Post updated successfully',
            data: post
        });
    } catch (error) {
        console.error('Update post error:', error);
        res.status(500).json({
            success: false,
            message: 'Server error updating post',
            error: error.message
        });
    }
});

// @route   DELETE /api/posts/:id
// @desc    Delete post
// @access  Private
router.delete('/:id', authenticate, async (req, res) => {
    try {
        const post = await Post.findById(req.params.id);

        if (!post || !post.isActive) {
            return res.status(404).json({
                success: false,
                message: 'Post not found'
            });
        }

        // Check if user owns the post
        if (post.user.toString() !== req.user.id) {
            return res.status(403).json({
                success: false,
                message: 'Not authorized to delete this post'
            });
        }

        // Soft delete
        post.isActive = false;
        await post.save();

        res.json({
            success: true,
            message: 'Post deleted successfully'
        });
    } catch (error) {
        console.error('Delete post error:', error);
        res.status(500).json({
            success: false,
            message: 'Server error deleting post',
            error: error.message
        });
    }
});

// @route   POST /api/posts/:id/like
// @desc    Like/unlike a post
// @access  Private
router.post('/:id/like', authenticate, async (req, res) => {
    try {
        const post = await Post.findById(req.params.id);

        if (!post || !post.isActive) {
            return res.status(404).json({
                success: false,
                message: 'Post not found'
            });
        }

        // Check if post is already liked by user
        const likeIndex = post.likes.findIndex(like => 
            like.user.toString() === req.user.id
        );

        if (likeIndex > -1) {
            // Unlike post
            post.likes.splice(likeIndex, 1);
            await post.save();

            res.json({
                success: true,
                message: 'Post unliked',
                liked: false,
                likesCount: post.likes.length
            });
        } else {
            // Like post
            post.likes.push({ user: req.user.id });
            await post.save();

            res.json({
                success: true,
                message: 'Post liked',
                liked: true,
                likesCount: post.likes.length
            });
        }
    } catch (error) {
        console.error('Like post error:', error);
        res.status(500).json({
            success: false,
            message: 'Server error liking post',
            error: error.message
        });
    }
});

// @route   POST /api/posts/:id/comment
// @desc    Add comment to post
// @access  Private
router.post('/:id/comment', authenticate, validateComment, async (req, res) => {
    try {
        const post = await Post.findById(req.params.id);

        if (!post || !post.isActive) {
            return res.status(404).json({
                success: false,
                message: 'Post not found'
            });
        }

        const { text } = req.body;

        const newComment = {
            user: req.user.id,
            text
        };

        post.comments.push(newComment);
        await post.save();

        // Populate the new comment
        await post.populate('comments.user', 'username');

        res.status(201).json({
            success: true,
            message: 'Comment added successfully',
            comment: post.comments[post.comments.length - 1]
        });
    } catch (error) {
        console.error('Add comment error:', error);
        res.status(500).json({
            success: false,
            message: 'Server error adding comment',
            error: error.message
        });
    }
});

// @route   GET /api/posts/user/:userId
// @desc    Get posts by user ID
// @access  Private
router.get('/user/:userId', authenticate, async (req, res) => {
    try {
        const page = parseInt(req.query.page) || 1;
        const limit = parseInt(req.query.limit) || 10;
        const skip = (page - 1) * limit;

        const posts = await Post.find({ 
            user: req.params.userId, 
            isActive: true 
        })
        .populate('user', 'username profilePicture')
        .sort({ createdAt: -1 })
        .skip(skip)
        .limit(limit);

        const total = await Post.countDocuments({ 
            user: req.params.userId, 
            isActive: true 
        });

        res.json({
            success: true,
            data: posts,
            pagination: {
                page,
                pages: Math.ceil(total / limit),
                total
            }
        });
    } catch (error) {
        console.error('Get user posts error:', error);
        res.status(500).json({
            success: false,
            message: 'Server error fetching user posts',
            error: error.message
        });
    }
});

module.exports = router; 