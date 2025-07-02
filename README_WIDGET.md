# Locket Widget Implementation Guide

## 📋 Overview

LocketWidgetProvider is a comprehensive Android home screen widget implementation that displays the latest posts from friends in your Locket app. Built with Java and following Android best practices, it features automatic updates via WorkManager, robust error handling, and performance optimizations.

## ✨ Key Features

- **Auto-Update Mechanism**: Uses WorkManager for efficient background updates every 15 minutes
- **Comprehensive Error Handling**: Network errors, API failures, offline mode support
- **Performance Optimized**: Image caching, memory management, battery optimization
- **Modern UI**: Supports light/dark themes, loading states, error states
- **Robust Caching**: Offline support with intelligent cache management
- **Click Interactions**: Widget clicks open app, refresh button for manual updates

## 🏗️ Architecture

### Core Components

```
widget/
├── LocketWidgetProvider.java          # Main widget provider
├── workers/
│   └── LocketWidgetUpdateWorker.java  # Background data fetching
├── network/
│   └── NetworkManager.java            # API calls and network handling
├── models/
│   ├── Post.java                      # Post data model
│   └── Friend.java                    # Friend data model
└── utils/
    ├── WidgetUpdateHelper.java        # Caching and utilities
    └── ImageLoader.java               # Async image loading
```

### Key Classes

#### 1. LocketWidgetProvider
- **Purpose**: Main widget controller
- **Features**:
  - Widget lifecycle management (onUpdate, onEnabled, onDisabled)
  - UI state management (loading, content, error states)
  - Click event handling
  - WorkManager integration
  - Comprehensive error handling

#### 2. LocketWidgetUpdateWorker
- **Purpose**: Background data fetching using WorkManager
- **Features**:
  - Periodic updates every 15 minutes
  - Network availability checking
  - Offline mode with cached data fallback
  - Intelligent retry mechanism
  - Battery optimization with constraints

#### 3. NetworkManager
- **Purpose**: API communication and data parsing
- **Features**:
  - Optimized HTTP client with timeouts
  - JSON parsing with error handling
  - Authentication header support
  - Flexible response format handling
  - Connection management and cleanup

#### 4. ImageLoader
- **Purpose**: Asynchronous image loading and processing
- **Features**:
  - Memory cache (4MB LRU cache)
  - Image optimization for widget sizes
  - Circular avatar processing
  - Thread-safe operations
  - Automatic bitmap recycling

#### 5. WidgetUpdateHelper
- **Purpose**: Data caching and utility functions
- **Features**:
  - SharedPreferences-based caching
  - Time formatting ("2 hours ago")
  - Content validation and sanitization
  - Cache validity checking
  - Debug logging

## 🎨 UI Design

### Widget Layout Features
- **Rounded corners** with modern card design
- **Friend avatar** (circular, 28dp)
- **Friend name** with text truncation
- **Post image** with aspect ratio preservation
- **Text content overlay** with gradient background
- **Timestamp** display with relative time
- **Refresh button** with ripple effect
- **Loading state** with progress indicator
- **Error state** with retry button

### Theme Support
- **Light theme**: Clean white background with subtle borders
- **Dark theme**: Dark background with appropriate contrast
- **Automatic theme switching** based on system settings

## 🔧 Configuration

### Widget Size Options
- **Minimum size**: 250dp × 180dp (4×3 cells)
- **Maximum resize**: 400dp × 300dp
- **Resize modes**: Both horizontal and vertical
- **Target cells**: 4×3 grid

### Update Intervals
- **Periodic updates**: Every 15 minutes
- **Manual refresh**: Via refresh button
- **Retry intervals**: 5 minutes for failed updates
- **Cache validity**: 24 hours maximum

## 📱 Usage Instructions

### Adding the Widget
1. Long-press on home screen
2. Select "Widgets" from the menu
3. Find "Locket" widget
4. Drag to desired location
5. Resize as needed

### Widget Interactions
- **Tap widget content**: Opens Locket app to specific post
- **Tap refresh button**: Manually triggers update
- **Tap retry (on error)**: Attempts to reload data

### Widget States
- **Loading**: Shows progress indicator while fetching data
- **Content**: Displays friend's latest post with image/text
- **Error**: Shows error message with retry option
- **Empty**: Displays placeholder when no posts available

## 🔄 Update Mechanism

### WorkManager Integration
```java
// Periodic updates every 15 minutes
PeriodicWorkRequest periodicWorkRequest = new PeriodicWorkRequest.Builder(
    LocketWidgetUpdateWorker.class, 15, TimeUnit.MINUTES)
    .setConstraints(constraints)
    .build();
```

### Update Flow
1. **Trigger**: Timer, user refresh, or widget addition
2. **Network check**: Verify connectivity
3. **API call**: Fetch latest posts
4. **Data validation**: Ensure post integrity
5. **Cache update**: Store for offline access
6. **UI update**: Refresh all widget instances
7. **Schedule next**: Set up next automatic update

### Error Handling Flow
1. **Network errors**: Use cached data if available
2. **API errors**: Show error state with retry option
3. **Data errors**: Validate and filter invalid posts
4. **Image errors**: Use placeholder images
5. **Cache errors**: Fall back to network requests

## ⚡ Performance Optimizations

### Memory Management
- **Image caching**: 4MB LRU cache for bitmaps
- **Automatic cleanup**: Recycle unused bitmaps
- **Size optimization**: Resize images for widget display
- **Memory monitoring**: Track cache hit/miss ratios

### Battery Optimization
- **WorkManager constraints**: Only update when battery not low
- **Network awareness**: Avoid updates on metered connections
- **Efficient scheduling**: Use system's job scheduler
- **Background limits**: Respect Android's background execution limits

### Network Optimization
- **Connection pooling**: Reuse HTTP connections
- **Timeout settings**: 10s connect, 15s read timeouts
- **Retry logic**: Exponential backoff for failed requests
- **Data compression**: Use efficient JSON parsing

## 🔒 Security Considerations

### Network Security
- **HTTPS only**: All API calls use secure connections
- **Certificate pinning**: Optional certificate validation
- **User-Agent headers**: Identify requests as widget traffic
- **Token management**: Secure storage of auth tokens

### Data Privacy
- **Local caching**: Minimal data stored locally
- **Cache encryption**: Optional encryption for sensitive data
- **Data retention**: Automatic cleanup of old cached data
- **Permission model**: Only request necessary permissions

## 🧪 Testing

### Test Scenarios
1. **Widget lifecycle**: Add, update, remove widget
2. **Network conditions**: Online, offline, slow network
3. **Data scenarios**: Empty data, malformed JSON, large datasets
4. **Error conditions**: API failures, timeout scenarios
5. **UI responsiveness**: Different screen sizes and orientations

### Performance Testing
- **Memory usage**: Monitor heap size and allocations
- **Battery impact**: Measure background processing time
- **Network usage**: Track data consumption
- **Cache efficiency**: Verify hit ratios and cleanup

## 📋 API Integration

### Expected API Format
```json
{
  "data": [
    {
      "id": "post_123",
      "content": "Having a great day!",
      "image_url": "https://example.com/image.jpg",
      "timestamp": 1634567890000,
      "friend": {
        "id": "user_456",
        "name": "John Doe",
        "avatar_url": "https://example.com/avatar.jpg"
      }
    }
  ]
}
```

### Configuration
- **Base URL**: Set in `NetworkManager.BASE_URL`
- **Endpoints**: Currently uses `/posts/latest`
- **Authentication**: Bearer token support (implement in `getAuthToken()`)
- **Rate limiting**: Respect API rate limits

## 🔧 Installation & Setup

### Prerequisites
- Android Studio 4.0+
- Android API 24+ (Android 7.0)
- WorkManager dependency
- Internet permission

### Dependencies Added
```gradle
implementation libs.androidx.work.runtime
implementation libs.retrofit
implementation libs.converter.gson
implementation libs.okhttp
implementation libs.logging.interceptor
```

### Permissions Required
```xml
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
<uses-permission android:name="android.permission.WAKE_LOCK" />
```

## 🐛 Troubleshooting

### Common Issues

#### Widget Not Updating
- Check network connectivity
- Verify API endpoint accessibility
- Review WorkManager logs
- Ensure proper permissions

#### Images Not Loading
- Verify image URLs are accessible
- Check network security config
- Review ImageLoader cache stats
- Test with different image formats

#### High Battery Usage
- Review update frequency settings
- Check WorkManager constraints
- Monitor background execution time
- Optimize image processing

### Debug Logging
Enable detailed logging by filtering for these tags:
- `LocketWidgetProvider`
- `LocketWidgetUpdateWorker`
- `NetworkManager`
- `ImageLoader`
- `WidgetUpdateHelper`

## 🔮 Future Enhancements

### Planned Features
- **Multiple friend support**: Show posts from multiple friends
- **Widget configuration**: Allow users to customize update frequency
- **Rich notifications**: Push updates for new posts
- **Gesture support**: Swipe gestures for navigation
- **Widget variants**: Different sizes and layouts

### Technical Improvements
- **Room database**: Replace SharedPreferences for complex data
- **Compose UI**: Migrate to Jetpack Compose for widgets (API 31+)
- **Advanced caching**: Implement disk cache for images
- **Analytics**: Track widget usage and performance metrics

## 📄 License

This widget implementation follows the same license as the main Locket application.

## 🤝 Contributing

When contributing to the widget:
1. Follow existing code style and architecture
2. Add comprehensive error handling
3. Include performance considerations
4. Update tests for new functionality
5. Document any API changes

---

For questions or issues with the widget implementation, please refer to the main project documentation or create an issue in the repository. 