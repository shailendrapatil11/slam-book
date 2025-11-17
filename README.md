# Digital Slambook - Backend API

A multi-tenant SaaS platform for digital slam books using Spring Boot WebFlux and MongoDB.

## 🚀 Technology Stack

- **Java**: 17
- **Spring Boot**: 3.2.0
- **Spring WebFlux**: Reactive programming
- **MongoDB**: NoSQL database with reactive driver
- **Spring Security**: JWT-based authentication
- **Gradle**: Build tool

## 📋 Prerequisites

- JDK 17 or higher
- MongoDB 5.0 or higher (or MongoDB Atlas)
- Gradle 8.x
- IDE (IntelliJ IDEA / Eclipse / VS Code)

## 🛠️ Setup Instructions

### 1. Clone the Repository

```bash
git clone <repository-url>
cd digital-slambook
```

### 2. Configure MongoDB

**Option A: Local MongoDB**
```bash
# Start MongoDB
mongod --dbpath /path/to/data
```

**Option B: MongoDB Atlas**
- Create account at https://www.mongodb.com/cloud/atlas
- Create a cluster
- Get connection string
- Update `application.yml` with connection string

### 3. Configure Application

Update `src/main/resources/application.yml`:

```yaml
spring:
  data:
    mongodb:
      uri: mongodb://localhost:27017/slambook_db  # Change this
      
jwt:
  secret: your-super-secret-256-bit-key-change-in-production  # MUST CHANGE
  
spring:
  mail:
    username: your-email@gmail.com  # Add your email
    password: your-app-password     # Add app password
```

### 4. Environment Variables (Production)

Create `.env` file:
```
JWT_SECRET=your-production-secret-key-minimum-256-bits
MONGODB_URI=mongodb+srv://user:pass@cluster.mongodb.net/slambook
MAIL_USERNAME=your-email@gmail.com
MAIL_PASSWORD=your-app-password
```

### 5. Build the Project

```bash
./gradlew clean build
```

### 6. Run the Application

```bash
./gradlew bootRun
```

Or run directly:
```bash
java -jar build/libs/digital-slambook-1.0.0.jar
```

The application will start on `http://localhost:8080`

## 📊 Database Initialization

### Create Super Admin User

First time setup - create super admin manually in MongoDB:

```javascript
db.users.insertOne({
    email: "admin@slambook.com",
    password: "$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewY5.JBlyNjaQe5.", // Admin@123
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

Login with:
- Email: `admin@slambook.com`
- Password: `Admin@123`

## 🔑 API Endpoints

### Authentication APIs

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| POST | `/api/v1/auth/login` | User login | No |
| POST | `/api/v1/auth/register` | User registration | No |
| POST | `/api/v1/auth/refresh` | Refresh access token | No |
| GET | `/api/v1/auth/verify-email` | Verify email | No |
| POST | `/api/v1/auth/forgot-password` | Request password reset | No |

### Super Admin APIs

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| POST | `/api/v1/admin/colleges` | Create college | Super Admin |
| GET | `/api/v1/admin/colleges` | List all colleges | Super Admin |
| GET | `/api/v1/admin/colleges/{id}` | Get college by ID | Super Admin |
| PUT | `/api/v1/admin/colleges/{id}` | Update college | Super Admin |
| DELETE | `/api/v1/admin/colleges/{id}` | Delete college | Super Admin |
| GET | `/api/v1/admin/analytics` | System analytics | Super Admin |

### College Admin APIs (To be implemented)

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/v1/college/join-requests` | List pending requests |
| PUT | `/api/v1/college/join-requests/{id}/approve` | Approve request |
| PUT | `/api/v1/college/join-requests/{id}/reject` | Reject request |
| GET | `/api/v1/college/users` | List all users |
| PUT | `/api/v1/college/settings` | Update settings |

### User APIs (To be implemented)

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/v1/users/me` | Get own profile |
| PUT | `/api/v1/users/me` | Update profile |
| GET | `/api/v1/users/{id}` | Get user profile |
| GET | `/api/v1/users/search` | Search users |

### SlamBook APIs (To be implemented)

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/v1/slambook/entries` | Create entry |
| GET | `/api/v1/slambook/entries/for-me` | Get entries for me |
| GET | `/api/v1/slambook/entries/by-me` | Get entries by me |
| PUT | `/api/v1/slambook/entries/{id}` | Update entry |
| DELETE | `/api/v1/slambook/entries/{id}` | Delete entry |

## 🧪 Testing the APIs

### Using cURL

**1. Super Admin Login**
```bash
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "admin@slambook.com",
    "password": "Admin@123"
  }'
```

**2. Create College**
```bash
curl -X POST http://localhost:8080/api/v1/admin/colleges \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN" \
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
    "subscriptionPlan": "PREMIUM",
    "maxUsers": 500
  }'
```

**3. Student Registration**
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
    "batch": "2024",
    "rollNumber": "CS2024001"
  }'
```

### Using Postman

1. Import the API collection (to be created)
2. Set environment variables:
    - `BASE_URL`: http://localhost:8080
    - `ACCESS_TOKEN`: (obtained from login)

## 📁 Project Structure

```
src/main/java/com/slambook/
├── config/              # Configuration classes
│   ├── MongoConfig.java
│   ├── SecurityConfig.java
│   └── WebFluxConfig.java
├── controller/          # REST controllers
│   ├── AuthController.java
│   └── SuperAdminController.java
├── dto/                 # Data Transfer Objects
│   ├── request/
│   └── response/
├── exception/           # Custom exceptions
├── model/              # Domain models
│   ├── College.java
│   ├── User.java
│   ├── SlamBookEntry.java
│   ├── Template.java
│   └── Notification.java
├── repository/         # MongoDB repositories
├── security/           # Security components
│   ├── JwtTokenProvider.java
│   ├── JwtAuthenticationFilter.java
│   └── CustomUserDetails.java
├── service/            # Business logic
│   ├── AuthService.java
│   └── CollegeService.java
└── SlambookApplication.java
```

## 🔐 Security

- **JWT Authentication**: Stateless authentication with access & refresh tokens
- **Password Encryption**: BCrypt with strength 12
- **Role-Based Access**: SUPER_ADMIN, COLLEGE_ADMIN, STUDENT
- **CORS**: Configured for frontend origins
- **Multi-Tenant Isolation**: Data isolated by collegeId

## 🚦 Next Steps

### Phase 1 - Complete (MVP Foundation)
- ✅ Project setup
- ✅ Database models
- ✅ Authentication system
- ✅ College management
- ✅ Super admin endpoints

### Phase 2 - In Progress
- ⏳ College admin endpoints
- ⏳ User management service
- ⏳ Join request approval workflow
- ⏳ User profile management

### Phase 3 - Pending
- ⏳ SlamBook entry CRUD
- ⏳ Template management
- ⏳ Notification system
- ⏳ Search functionality
- ⏳ File upload (profile pictures, attachments)

### Phase 4 - Future
- ⏳ Analytics dashboard
- ⏳ Email notifications
- ⏳ Content moderation
- ⏳ Export to PDF
- ⏳ Real-time features (WebSocket)

## 🐛 Troubleshooting

### MongoDB Connection Issues
```
Error: MongoSocketException
Solution: Check MongoDB is running and URI is correct
```

### JWT Token Invalid
```
Error: 401 Unauthorized
Solution: Check token in Authorization header: "Bearer {token}"
```

### Port Already in Use
```
Error: Port 8080 is already in use
Solution: Change port in application.yml or kill process using port
```

## 📝 Development Tips

1. **Use Reactive Types**: Always return Mono or Flux
2. **Never Block**: Avoid .block() calls in production code
3. **Error Handling**: Use switchIfEmpty() and error operators
4. **Logging**: Use appropriate log levels
5. **Validation**: Always validate input with @Valid

## 🤝 Contributing

1. Create feature branch
2. Follow code style
3. Write tests
4. Submit pull request

## 📄 License

[Your License Here]

## 👥 Contact

For questions or support, contact: [your-email@example.com]

---

**Happy Coding! 🎓📖**