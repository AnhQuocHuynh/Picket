# Locket Backend API

Backend API cho ứng dụng Locket Clone được xây dựng với Node.js, Express, và MongoDB.

## 🚀 Tính năng

- ✅ User Authentication (Register/Login)
- ✅ JWT Token Authentication
- ✅ Password Hashing với bcrypt
- ✅ CRUD Operations cho Posts
- ✅ Like/Unlike Posts
- ✅ Comment System
- ✅ Input Validation
- ✅ Error Handling
- ✅ CORS Support
- ✅ Security Middleware (Helmet)

## 📋 Yêu cầu hệ thống

- Node.js (v16 trở lên)
- MongoDB (v4.4 trở lên)
- npm hoặc yarn

## 🛠️ Cài đặt

### 1. Clone repository
```bash
cd locket-backend
```

### 2. Install dependencies
```bash
npm install
```

### 3. Cài đặt MongoDB
- **Windows**: Download từ [MongoDB Official](https://www.mongodb.com/try/download/community)
- **macOS**: `brew install mongodb-community`
- **Ubuntu**: `sudo apt install mongodb`

### 4. Khởi động MongoDB
```bash
# Windows
net start MongoDB

# macOS/Linux
sudo systemctl start mongod
```

### 5. Cấu hình Environment Variables
Tạo file `.env` trong thư mục root:
```env
PORT=3000
NODE_ENV=development
MONGODB_URI=mongodb://localhost:27017/locket_db
JWT_SECRET=your_jwt_secret_key_here_make_it_long_and_complex
JWT_EXPIRE=7d
CORS_ORIGIN=http://localhost:3000
```

### 6. Chạy server
```bash
# Development mode với nodemon
npm run dev

# Production mode
npm start
```

## 📡 API Endpoints

### Authentication
```
POST   /api/auth/register    - Đăng ký user mới
POST   /api/auth/login       - Đăng nhập
GET    /api/auth/profile     - Lấy profile user (Private)
PUT    /api/auth/profile     - Cập nhật profile (Private)
```

### Posts
```
GET    /api/posts            - Lấy danh sách posts (Private)
POST   /api/posts            - Tạo post mới (Private)
GET    /api/posts/:id        - Lấy post theo ID (Private)
PUT    /api/posts/:id        - Cập nhật post (Private)
DELETE /api/posts/:id        - Xóa post (Private)
POST   /api/posts/:id/like   - Like/Unlike post (Private)
POST   /api/posts/:id/comment - Thêm comment (Private)
GET    /api/posts/user/:userId - Lấy posts của user (Private)
```

### Health Check
```
GET    /api/health           - Kiểm tra server status
GET    /                     - API information
```

## 📝 Ví dụ API Usage

### Register User
```javascript
POST /api/auth/register
Content-Type: application/json

{
    "username": "john_doe",
    "email": "john@example.com",
    "password": "Password123"
}
```

### Login User
```javascript
POST /api/auth/login
Content-Type: application/json

{
    "email": "john@example.com",
    "password": "Password123"
}
```

### Create Post
```javascript
POST /api/posts
Authorization: Bearer <jwt_token>
Content-Type: application/json

{
    "imageUrl": "https://example.com/image.jpg",
    "caption": "My awesome post!"
}
```

## 🔒 Authentication

API sử dụng JWT (JSON Web Tokens) để authentication. Sau khi login thành công, client sẽ nhận được token. Token này cần được gửi trong header cho các protected routes:

```
Authorization: Bearer <jwt_token>
```

## 📊 Database Schema

### User Schema
```javascript
{
    username: String (unique, required),
    email: String (unique, required),
    password: String (hashed, required),
    profilePicture: String,
    friends: [ObjectId],
    isActive: Boolean,
    lastLogin: Date,
    createdAt: Date,
    updatedAt: Date
}
```

### Post Schema
```javascript
{
    user: ObjectId (ref: User),
    imageUrl: String (required),
    caption: String,
    likes: [{
        user: ObjectId (ref: User),
        createdAt: Date
    }],
    comments: [{
        user: ObjectId (ref: User),
        text: String,
        createdAt: Date
    }],
    isActive: Boolean,
    createdAt: Date,
    updatedAt: Date
}
```

## 🧪 Testing API

Bạn có thể test API bằng:
- **Postman**: Import collection và test endpoints
- **cURL**: Command line testing
- **Thunder Client**: VS Code extension

### Ví dụ với cURL:
```bash
# Health check
curl http://localhost:3000/api/health

# Register user
curl -X POST http://localhost:3000/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username":"testuser","email":"test@example.com","password":"Password123"}'
```

## 🐛 Troubleshooting

### MongoDB Connection Issues
```bash
# Kiểm tra MongoDB status
sudo systemctl status mongod

# Restart MongoDB
sudo systemctl restart mongod
```

### Port 3000 đã được sử dụng
```bash
# Tìm process đang sử dụng port 3000
lsof -i :3000

# Kill process
kill -9 <PID>
```

## 📁 Cấu trúc thư mục
```
locket-backend/
├── models/
│   ├── User.js
│   └── Post.js
├── routes/
│   ├── auth.js
│   └── posts.js
├── middleware/
│   ├── auth.js
│   └── validation.js
├── config.js
├── database.js
├── server.js
├── package.json
└── README.md
```

## 🔮 Tính năng sắp tới

- [ ] File Upload với Multer
- [ ] Real-time notifications với Socket.IO
- [ ] Friend system
- [ ] Image resizing với Sharp
- [ ] Rate limiting
- [ ] Email verification
- [ ] Password reset functionality

## 📞 Support

Nếu gặp vấn đề, hãy check:
1. MongoDB đã khởi động chưa
2. Port 3000 có available không
3. Dependencies đã cài đầy đủ chưa
4. .env file đã cấu hình đúng chưa 