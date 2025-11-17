# 🚀 Digital Slambook - Complete Setup Guide

## 📂 Complete Project Structure

```
digital-slambook/
├── build.gradle
├── settings.gradle
├── gradlew
├── gradlew.bat
├── .gitignore
├── README.md
│
├── src/
│   ├── main/
│   │   ├── java/com/slambook/
│   │   │   ├── SlambookApplication.java ✅
│   │   │   │
│   │   │   ├── config/
│   │   │   │   ├── MongoConfig.java ✅
│   │   │   │   ├── SecurityConfig.java ✅
│   │   │   │   └── WebFluxConfig.java ✅
│   │   │   │
│   │   │   ├── model/
│   │   │   │   ├── College.java ✅
│   │   │   │   ├── User.java ✅
│   │   │   │   ├── SlamBookEntry.java ✅
│   │   │   │   ├── Template.java ✅
│   │   │   │   └── Notification.java ✅
│   │   │   │
│   │   │   ├── dto/
│   │   │   │   ├── request/
│   │   │   │   │   └── (All Request DTOs) ✅
│   │   │   │   └── response/
│   │   │   │       └── (All Response DTOs) ✅
│   │   │   │
│   │   │   ├── repository/
│   │   │   │   └── (All Repositories) ✅
│   │   │   │
│   │   │   ├── service/
│   │   │   │   ├── AuthService.java ✅
│   │   │   │   ├── CollegeService.java ✅
│   │   │   │   ├── UserService.java ✅
│   │   │   │   ├── SlamBookService.java ✅
│   │   │   │   └── NotificationService.java ✅
│   │   │   │
│   │   │   ├── controller/
│   │   │   │   ├── AuthController.java ✅
│   │   │   │   ├── SuperAdminController.java ✅
│   │   │   │   ├── CollegeAdminController.java ✅
│   │   │   │   ├── UserController.java ✅
│   │   │   │   ├── SlamBookController.java ✅
│   │   │   │   └── NotificationController.java ✅
│   │   │   │
│   │   │   ├── security/
│   │   │   │   ├── JwtTokenProvider.java ✅
│   │   │   │   ├── JwtAuthenticationFilter.java ✅
│   │   │   │   ├── SecurityContextRepository.java ✅
│   │   │   │   └── CustomUserDetails.java ✅
│   │   │   │
│   │   │   └── exception/
│   │   │       ├── GlobalExceptionHandler.java ✅
│   │   │       ├── NotFoundException.java ✅
│   │   │       ├── BadRequestException.java ✅
│   │   │       ├── UnauthorizedException.java ✅
│   │   │       ├── ForbiddenException.java ✅
│   │   │       └── ConflictException.java ✅
│   │   │
│   │   └── resources/
│   │       ├── application.yml ✅
│   │       ├── application-dev.yml ✅
│   │       └── application-prod.yml ✅
│   │
│   └── test/
│       └── java/com/slambook/
│           └── SlambookApplicationTests.java
│
└── postman/
    └── Digital_Slambook_API.postman_collection.json ✅
```

## ⚡ Quick Start (5 Minutes)

### Step 1: Create Project Structure

```bash
# Create project directory
mkdir digital-slambook
cd digital-slambook

# Create Gradle wrapper files
gradle wrapper --gradle-version 8.5
```

### Step 2: Create `settings.gradle`

```gradle
rootProject.name = 'digital-slambook'
```

### Step 3: Copy Files

Copy all the provided files into their respective directories as shown in the structure above.

### Step 4: Build & Run

```bash
# Build the project
./gradlew clean build

# Run the application
./gradlew bootRun
```

The application will start on `http://localhost:8080`

## 🔧 Configuration

### Database (Already Configured)
Your MongoDB Atlas connection is already set in `application.yml`:
```
mongodb+srv://shailendradilippatil:HfqoIrvrKzaUtJsb@clustergemini.43p8vrp.mongodb.net/SlamBook
```

### JWT Secret (Production)
For production, set environment variable:
```bash
export JWT_SECRET="your-super-secure-256-bit-secret-key"
```

### Email Configuration (Optional)
For email functionality:
```bash
export MAIL_USERNAME="your-email@gmail.com"
export MAIL_PASSWORD="your-app-password"
```

## 🗄️ Database Setup

### Option 1: Auto-Create Super Admin (Recommended)

After first run, connect to MongoDB and run:

```javascript
use SlamBook

db.users.insertOne({
    email: "admin@slambook.com",
    password: "$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewY5.JBlyNjaQe5.",
    role: "SUPER_ADMIN",
    profile: {
        firstName: "Super",
        lastName: "Admin"
    },
    emailVerified: true,
    createdAt: new Date(),
    updatedAt: new Date(),
    isActive: true
})
```

**Login Credentials:**
- Email: `admin@slambook.com`
- Password: `Admin@123`

### Option 2: MongoDB Compass

1. Open MongoDB Compass
2. Connect to your Atlas cluster
3. Select `SlamBook` database
4. Insert the above document in `users` collection

## 📮 Testing with Postman

### Import Collection

1. Open Postman
2. Click **Import**
3. Paste the provided Postman collection JSON
4. Collection will be imported with all endpoints

### Setup Environment

Create Postman environment with variables:
```
BASE_URL: http://localhost:8080
ACCESS_TOKEN: (will be auto-set after login)
REFRESH_TOKEN: (will be auto-set after login)
COLLEGE_ID: (will be auto-set after creating college)
COLLEGE_CODE: (will be auto-set after creating college)
USER_ID: (will be auto-set after login)
```

### Quick Test Flow

1. **Super Admin Login** → Get tokens
2. **Create College** → Get college code
3. **Register Student** → Use college code
4. **Student Login** → Test as student
5. **Create Slam Book Entry** → Test core feature

## 🏗️ Build Commands

### Development
```bash
./gradlew bootRun
```

### Production Build
```bash
./gradlew clean build -x test
java -jar build/libs/digital-slambook-1.0.0.jar
```

### With Custom Profile
```bash
./gradlew bootRun --args='--spring.profiles.active=prod'
```

## 📦 Creating JAR

```bash
# Create executable JAR
./gradlew bootJar

# JAR location
build/libs/digital-slambook-1.0.0.jar

# Run JAR
java -jar build/libs/digital-slambook-1.0.0.jar
```

## 🐳 Docker Setup (Optional)

Create `Dockerfile`:

```dockerfile
FROM eclipse-temurin:17-jdk-alpine
VOLUME /tmp
COPY build/libs/digital-slambook-1.0.0.jar app.jar
ENTRYPOINT ["java","-jar","/app.jar"]
```

Build and run:
```bash
# Build JAR first
./gradlew bootJar

# Build Docker image
docker build -t digital-slambook:latest .

# Run container
docker run -p 8080:8080 \
  -e JWT_SECRET="your-secret" \
  -e MONGODB_URI="your-mongodb-uri" \
  digital-slambook:latest
```

## 🧪 Testing APIs

### 1. Health Check
```bash
curl http://localhost:8080/actuator/health
```

### 2. Super Admin Login
```bash
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "admin@slambook.com",
    "password": "Admin@123"
  }'
```

### 3. Create College
```bash
curl -X POST http://localhost:8080/api/v1/admin/colleges \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -d '{
    "name": "MIT College",
    "email": "admin@mit.edu",
    "phone": "+1234567890",
    "address": {
      "street": "123 College St",
      "city": "Boston",
      "state": "MA",
      "country": "USA",
      "pincode": "02139"
    },
    "subscriptionPlan": "PREMIUM"
  }'
```

### 4. Register Student
```bash
curl -X POST http://localhost:8080/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "collegeCode": "MIT1A2B",
    "email": "student@mit.edu",
    "password": "Student@123",
    "firstName": "John",
    "lastName": "Doe",
    "course": "Computer Science",
    "batch": "2024"
  }'
```

## 🔍 Troubleshooting

### Port Already in Use
```bash
# Find process using port 8080
lsof -i :8080

# Kill process
kill -9 PID

# Or change port in application.yml
server:
  port: 8081
```

### MongoDB Connection Issues
```bash
# Test connection
mongosh "mongodb+srv://shailendradilippatil:HfqoIrvrKzaUtJsb@clustergemini.43p8vrp.mongodb.net/SlamBook"

# Check network access in MongoDB Atlas
# Add your IP to whitelist or allow all (0.0.0.0/0)
```

### Build Failures
```bash
# Clean build
./gradlew clean

# Build with debug
./gradlew build --stacktrace

# Skip tests
./gradlew build -x test
```

## 📊 API Endpoints Summary

| Feature | Endpoint | Method | Auth |
|---------|----------|--------|------|
| Login | `/api/v1/auth/login` | POST | No |
| Register | `/api/v1/auth/register` | POST | No |
| Create College | `/api/v1/admin/colleges` | POST | Super Admin |
| Get All Colleges | `/api/v1/admin/colleges` | GET | Super Admin |
| Approve Join | `/api/v1/college/join-requests/{id}/approve` | PUT | College Admin |
| My Profile | `/api/v1/users/me` | GET | User |
| Search Users | `/api/v1/users/search` | GET | User |
| Create Entry | `/api/v1/slambook/entries` | POST | User |
| Get My Entries | `/api/v1/slambook/entries/for-me` | GET | User |
| Add Reaction | `/api/v1/slambook/entries/{id}/reactions` | POST | User |
| Notifications | `/api/v1/notifications` | GET | User |

## 🎯 Next Steps

### Phase 1 - Complete ✅
- Authentication & Authorization
- College Management
- User Management
- Slam Book CRUD
- Notifications

### Phase 2 - Enhancement
- [ ] Template Management Service
- [ ] File Upload Service (Profile Pictures)
- [ ] Email Service (Verification, Notifications)
- [ ] Search Optimization
- [ ] Caching (Redis)

### Phase 3 - Advanced
- [ ] Analytics Dashboard
- [ ] WebSocket for Real-time Notifications
- [ ] Export to PDF
- [ ] Content Moderation
- [ ] Batch Operations

## 📝 Development Tips

1. **Hot Reload**: Use Spring DevTools for auto-restart
2. **Debug**: Set breakpoints in IDE and run in debug mode
3. **Logs**: Check console for detailed logs
4. **Postman**: Save test cases in collection
5. **Git**: Commit frequently with meaningful messages

## 🤝 Contributing

1. Fork the repository
2. Create feature branch: `git checkout -b feature/amazing-feature`
3. Commit changes: `git commit -m 'Add amazing feature'`
4. Push to branch: `git push origin feature/amazing-feature`
5. Open Pull Request

## 📄 License

This project is licensed under the MIT License.

## 💬 Support

For issues or questions:
- Create an issue on GitHub
- Email: support@digitalslambook.com
- Documentation: https://docs.digitalslambook.com

---

**Built with ❤️ using Spring Boot WebFlux & MongoDB**

Happy Coding! 🎉