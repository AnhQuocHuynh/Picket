# 📱 LOCKET ANDROID PROJECT STRUCTURE

## 🏗️ **Tổng quan dự án**
- **Project Type:** Android App (Java)
- **Target:** Clone ứng dụng Locket (Photo sharing with widgets)
- **Team Size:** 4 người
- **Vai trò:** Backend integration + Login/Register screens

---

## 📂 **Cấu trúc thư mục chính**

```
D:\Locket\                           # Android Frontend Project
├── app/
│   ├── src/main/
│   │   ├── java/com/example/locket/
│   │   │   ├── data/                # Data Layer
│   │   │   │   ├── api/
│   │   │   │   │   └── AuthApi.java         ✅ API interfaces
│   │   │   │   ├── model/
│   │   │   │   │   ├── AuthModels.java     ✅ Request/Response models
│   │   │   │   │   └── User.java           ✅ User data model
│   │   │   │   └── repository/
│   │   │   │       └── AuthRepository.java ✅ Data repository
│   │   │   ├── di/
│   │   │   │   └── NetworkModule.java      ✅ Dependency Injection
│   │   │   ├── ui/
│   │   │   │   ├── auth/
│   │   │   │   │   ├── LoginActivity.java  ✅ Login screen
│   │   │   │   │   └── RegisterActivity.java ✅ Register screen
│   │   │   │   └── main/
│   │   │   │       └── MainActivity.java   ✅ Main screen
│   │   │   ├── utils/
│   │   │   │   └── SessionManager.java     ✅ Token management
│   │   │   ├── LocketApplication.java      ✅ Application class
│   │   │   └── TestNetworkActivity.java    ✅ API testing activity
│   │   ├── res/
│   │   │   ├── layout/                     ✅ UI layouts
│   │   │   ├── values/                     ✅ Strings, colors, themes
│   │   │   └── xml/
│   │   │       └── network_security_config.xml ✅ Network security
│   │   └── AndroidManifest.xml             ✅ App configuration
│   └── build.gradle.kts                    ✅ App dependencies
├── gradle/
├── build.gradle.kts                        ✅ Project configuration
└── settings.gradle.kts
```

---

## 🔧 **Components đã hoàn thành**

### ✅ **1. Authentication System**
- **AuthApi.java** - Retrofit API interfaces
- **AuthModels.java** - Request/Response models
- **AuthRepository.java** - Business logic layer
- **LoginActivity.java** - Login UI
- **RegisterActivity.java** - Register UI
- **SessionManager.java** - JWT token management

### ✅ **2. Network Configuration**
- **NetworkModule.java** - Retrofit + Hilt DI setup
- **network_security_config.xml** - Allow HTTP for development
- **AndroidManifest.xml** - Network permissions + security config

### ✅ **3. Testing Infrastructure**
- **TestNetworkActivity.java** - API endpoint testing
- **activity_test_network.xml** - Test UI layout

### ✅ **4. Backend Integration**
- **Base URL:** `http://10.0.2.2:3000` (Android Emulator)
- **Backend Location:** `D:\locket-backend\Locket_Backend\`
- **Database:** MongoDB Atlas
- **Authentication:** JWT tokens

---

## 📋 **Dependencies & Libraries**

### **Build Configuration (build.gradle.kts)**
```kotlin
// Networking
implementation "com.squareup.retrofit2:retrofit:2.9.0"
implementation "com.squareup.retrofit2:converter-gson:2.9.0"
implementation "com.squareup.okhttp3:logging-interceptor:4.11.0"

// Dependency Injection
implementation "com.google.dagger:hilt-android:2.48"
kapt "com.google.dagger:hilt-compiler:2.48"

// UI Components
implementation "androidx.appcompat:appcompat:1.6.1"
implementation "com.google.android.material:material:1.10.0"
implementation "androidx.constraintlayout:constraintlayout:2.1.4"

// Lifecycle
implementation "androidx.lifecycle:lifecycle-viewmodel:2.7.0"
implementation "androidx.lifecycle:lifecycle-livedata:2.7.0"
```

---

## 🔄 **Changelog - Cập nhật quan trọng**

### **2024-12-25 - Initial Setup & Authentication**
- ✅ Setup Android project với Java
- ✅ Cấu hình Retrofit cho API calls
- ✅ Implement authentication system (Login/Register)
- ✅ Setup Hilt dependency injection
- ✅ Tạo TestNetworkActivity để test API
- ✅ Cấu hình network security cho development
- ✅ Fix validation issues (username length, password format)
- ✅ Kết nối thành công với MongoDB Atlas backend

### **Upcoming Features**
- 🔄 Photo upload functionality
- 🔄 Widget implementation
- 🔄 Friend system integration
- 🔄 Real-time notifications
- 🔄 UI/UX improvements

---

## 🌐 **API Endpoints**

### **Authentication**
- `POST /api/auth/register` - User registration
- `POST /api/auth/login` - User login
- `GET /api/auth/profile` - Get user profile
- `PUT /api/auth/profile` - Update user profile

### **Testing**
- `GET /api/health` - Server health check

---

## 🛠️ **Development Setup**

### **Prerequisites**
1. Android Studio
2. Java 11+
3. Backend server running on `http://localhost:3000`

### **Run Instructions**
1. Clone repository
2. Open in Android Studio
3. Sync project
4. Start backend server
5. Run app on emulator/device
6. Use TestNetworkActivity for API testing

### **Test Credentials**
- **Email:** `testuser@backend.com`
- **Password:** `Password123`

---

## 🔒 **Security Considerations**

### **Network Security**
- HTTP allowed for development (network_security_config.xml)
- Production should use HTTPS only
- JWT tokens stored securely in SessionManager

### **Validation**
- Client-side input validation
- Server-side validation enforced
- Username: 3-20 characters, alphanumeric + underscore
- Password: Min 6 chars, 1 uppercase, 1 lowercase, 1 number

---

## 📝 **Notes & TODOs**

### **Current Status**
- ✅ Authentication system working
- ✅ Backend integration successful
- ✅ Network configuration complete
- ⏳ UI/UX polish needed
- ⏳ Additional features pending

### **Known Issues**
- None currently

### **Team Responsibilities**
- **Your role:** MongoDB integration + Login/Register screens ✅
- **Others:** Widget implementation, photo features, UI design

---

*Last Updated: 2024-12-25*
*Next Review: TBD* 