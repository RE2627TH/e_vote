# E-Vote App - Complete Integration Guide

## System Overview

This guide documents the complete end-to-end integration of the E-Vote application with role-based user flows, candidate application system, and admin approval process.

## 🎯 Complete User Flow

### 1. **STUDENT FLOW** (Student Registration → Login → Vote)
```
Register (Role: Student) 
    ↓
Successful Registration
    ↓
Redirect to Login Screen
    ↓
Login with Email & Password
    ↓
Access Home Screen / Voting Dashboard
```

### 2. **CANDIDATE FLOW** (Candidate Registration → Application → Approval → Login)
```
Register (Role: Candidate)
    ↓
Successful Registration (User ID returned)
    ↓
Redirect to Candidate Application Form
    ↓
Fill Application Details (Position, Manifesto, Experience, etc.)
    ↓
Submit Application (Email notification sent: PENDING status)
    ↓
Admin Reviews & Approves/Rejects
    ↓
Email Notification Sent (APPROVED/REJECTED status)
    ↓
Only If APPROVED: Can Login as Candidate
    ↓
Access Candidate Dashboard
```

### 3. **ADMIN FLOW** (Admin manages candidate applications)
```
Admin Dashboard
    ↓
View Pending Candidate Applications
    ↓
Review Application Details
    ↓
Click Approve/Reject
    ↓
Add Rejection Reason (if rejecting)
    ↓
Application Status Updated in Database
    ↓
Email Notification Sent to Candidate
    ↓
Candidate Can Now Login (if approved)
```

---

## 📱 Android App Implementation

### Files Created/Modified:

#### 1. **Data Models** (`model/`)
- `CandidateApplicationModels.kt` - NEW
  - `CandidateApplicationRequest` - Candidate form submission
  - `CandidateApplicationResponse` - Application submission response
  - `ApplicationStatusRequest` - Check application status
  - `ApplicationStatusResponse` - Status details with approval info
  - `AdminApprovalRequest` - Admin approval/rejection
  - `AdminApprovalResponse` - Admin action response

- `RegisterResponse.kt` - UPDATED
  - Added `user` field with user details (ID, name, email, role)
  
- `RegisterRequest.kt` - UPDATED
  - Added `role` field to support Student/Candidate selection

#### 2. **ViewModels** (`viewmodel/`)
- `CandidateApplicationViewModel.kt` - NEW
  - `submitApplication()` - Submit candidate application
  - `checkApplicationStatus()` - Check approval status by user ID
  - `checkApplicationStatusById()` - Check status by application ID

- `RegisterViewModel.kt` - UPDATED
  - Added `role` parameter to registration
  - Added `userId` and `userRole` state variables
  - Stores user ID for candidate application flow

- `LoginViewModel.kt` - UPDATED
  - `checkCandidateApplicationStatus()` - Validates candidate approval before login
  - Blocks candidate login if application is not approved
  - Shows clear messages for pending/rejected applications

#### 3. **Screens** (`Screens/`)
- `CandidateApplicationScreen.kt` - NEW
  - Form with fields: Full Name, Email, Phone, Department, Position, Manifesto, Experience, Qualifications
  - Submit application and get application ID
  - Shows loading state and error messages

- `ApplicationStatusScreen.kt` - NEW (two composables)
  - `ApplicationSubmittedScreen()` - Shows after successful submission
    - Application reference number
    - Current status: PENDING
    - Next steps information
  - `ApplicationStatusScreen()` - Check status anytime
    - Shows PENDING, APPROVED, or REJECTED status
    - Shows rejection reason if applicable
    - Allows login if approved

- `RegistrationScreen.kt` - UPDATED
  - Role dropdown (Student/Candidate)
  - Passes role to register function
  - Routes to CandidateApplicationScreen if candidate role
  - Routes to LoginScreen if student role

- `LoginActivity.kt` - UPDATED (if needed)
  - Updated to handle candidate approval check during login

#### 4. **API & Networking** (`api/`)
- `ApiService.kt` - UPDATED
  - Added endpoints:
    - `submitCandidateApplication()` - POST to `candidate/apply_candidate.php`
    - `getCandidateApplicationStatus()` - POST to `candidate/get_candidate_status.php`
    - `approveCandidateApplication()` - POST to `admin/approve_candidate_v2.php`
  - Updated base paths to include `auth/`, `candidate/`, etc.

- `ApiClient.kt` - No changes needed
  - Uses existing BASE_URL configuration

#### 5. **Navigation** (`navigation/`)
- `Routes.kt` - UPDATED
  - Added `CANDIDATE_APPLICATION` route
  - Added `APPLICATION_STATUS` route
  - Added `APPLICATION_SUBMITTED` route
  - Added helper functions for routes with parameters

- `AppNavGraph.kt` - UPDATED
  - Added route: `candidate_application/{userId}`
  - Added route: `application_submitted/{appId}`
  - Added route: `application_status/{appId}`
  - Uncommented Application Status screens

---

## 🗄️ Backend (PHP) Implementation

### Database Changes

**New Table: `candidate_applications`**
```sql
CREATE TABLE candidate_applications (
  id INT AUTO_INCREMENT PRIMARY KEY,
  user_id INT NOT NULL,
  full_name VARCHAR(255) NOT NULL,
  email VARCHAR(255) NOT NULL,
  phone VARCHAR(20),
  department VARCHAR(100),
  position VARCHAR(100),
  manifesto LONGTEXT,
  experience LONGTEXT,
  qualifications LONGTEXT,
  profile_photo VARCHAR(500),
  status ENUM('pending', 'approved', 'rejected') DEFAULT 'pending',
  rejection_reason TEXT,
  submitted_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  reviewed_at TIMESTAMP NULL,
  reviewed_by INT,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
  FOREIGN KEY (reviewed_by) REFERENCES users(id)
)
```

**Updated: `users` Table**
```sql
ALTER TABLE users ADD COLUMN is_approved BOOLEAN DEFAULT 0;
```

### New PHP Files

#### 1. **`candidate/apply_candidate.php`** - UPDATED/CREATED
- Endpoint: `POST /candidate/apply_candidate.php`
- Input: Candidate application form data
- Output: Application ID, status pending, email sent confirmation
- Process:
  1. Validates all required fields
  2. Checks if user is a candidate
  3. Checks if application already exists
  4. Inserts application with pending status
  5. Sends email with application ID

#### 2. **`candidate/get_candidate_status.php`** - UPDATED/CREATED
- Endpoint: `POST /candidate/get_candidate_status.php`
- Input: `user_id` or `application_id`
- Output: Application details with current status
- Returns:
  - Application ID, full details
  - Status: pending/approved/rejected
  - Can-login flag (true only if approved)
  - Rejection reason (if applicable)

#### 3. **`admin/approve_candidate_v2.php`** - NEW
- Endpoint: `POST /admin/approve_candidate_v2.php`
- Input: `application_id`, `action` (approve/reject), `rejection_reason`, `admin_id`
- Process:
  1. Updates `candidate_applications` status
  2. Updates `users.is_approved` if approved
  3. Sends email to candidate
  4. Returns success with candidate login info
- Security: Should be protected (admin only)

#### 4. **`email/CandidateEmailService.php`** - UPDATED
- Methods:
  - `sendApplicationReceivedEmail()` - Confirmation email with reference number
  - `sendApplicationStatusEmail()` - Status update (pending/approved/rejected)
  - `sendApplicationSubmission()` - Legacy method (for compatibility)
  - `sendApplicationStatusUpdate()` - Legacy method (for compatibility)
- Features:
  - HTML formatted emails
  - Clear status information
  - Login instructions for approved candidates
  - Rejection reasons for rejected applications

### Updated PHP Files

#### 1. **`auth/register.php`** - UPDATED
- Now saves `role` field (student/candidate)
- Returns user object with ID, name, email, role, student_id, department
- Response format:
```json
{
  "success": true,
  "message": "Registration successful",
  "user": {
    "id": 123,
    "name": "John Doe",
    "email": "john@example.com",
    "role": "candidate",
    "student_id": "12345",
    "department": "CSE"
  },
  "token": "base64_encoded_token"
}
```

#### 2. **`auth/login.php`** - UPDATED
- No changes needed, but can add `is_approved` check for candidates
- Already returns user role, which helps identify candidates

#### 3. **`config/schema.sql`** - NEW
- Contains all database table definitions
- Run this to set up the database schema

---

## 🔄 API Endpoints Summary

### Registration & Authentication
- `POST /auth/register.php` - Register new user (student/candidate)
- `POST /auth/login.php` - User login

### Candidate Applications
- `POST /candidate/apply_candidate.php` - Submit candidate application
- `POST /candidate/get_candidate_status.php` - Get application status

### Admin Functions
- `POST /admin/approve_candidate_v2.php` - Approve/Reject candidate application

---

## ⚙️ Setup Instructions

### 1. **Database Setup**
```bash
# In phpMyAdmin or MySQL command line:
mysql -u root < path/to/s_vote_api/config/schema.sql
```

### 2. **Android App Setup**
- Update `ApiClient.kt` BASE_URL if needed (currently set to: `http://10.228.242.24/s_vote_api/`)
- Ensure all model classes are imported in relevant files
- All ViewModels should be used with `viewModel()` composable

### 3. **Testing the Flow**

#### Test 1: Student Registration → Login
1. Open app
2. Go to Registration
3. Select Role: "Student"
4. Fill form and submit
5. Should navigate to Login
6. Login with credentials
7. Should go to Home screen

#### Test 2: Candidate Application → Approval → Login
1. Open app
2. Go to Registration
3. Select Role: "Candidate"
4. Fill form and submit
5. Should navigate to Candidate Application Form
6. Fill all application fields
7. Submit application
8. Should see "Application Submitted" screen with reference number
9. Email should be received with application ID and pending status
10. Admin approves application (via admin panel)
11. Candidate receives approval email
12. Candidate can now login with email/password
13. Should have access to Candidate Dashboard

#### Test 3: Admin Approval Process
1. Admin access (via admin panel/screen)
2. View pending applications
3. Click on application to review
4. Click "Approve" or "Reject"
5. If rejecting, add reason
6. Submit action
7. Candidate receives email with decision
8. If approved, candidate can login

---

## 📊 Database Structure

### Users Table (Updated Fields)
```
- id (INT, PK)
- name (VARCHAR)
- email (VARCHAR, UNIQUE)
- password (VARCHAR)
- role (ENUM: student, candidate, admin)
- dob (DATE, nullable)
- student_id (VARCHAR, nullable)
- department (VARCHAR, nullable)
- is_approved (BOOLEAN, default 0)
- created_at (TIMESTAMP)
- updated_at (TIMESTAMP)
```

### Candidate Applications Table (New)
```
- id (INT, PK)
- user_id (INT, FK)
- full_name (VARCHAR)
- email (VARCHAR)
- phone (VARCHAR)
- department (VARCHAR)
- position (VARCHAR)
- manifesto (LONGTEXT)
- experience (LONGTEXT)
- qualifications (LONGTEXT)
- profile_photo (VARCHAR)
- status (ENUM: pending, approved, rejected)
- rejection_reason (TEXT)
- submitted_at (TIMESTAMP)
- reviewed_at (TIMESTAMP)
- reviewed_by (INT, FK)
- created_at (TIMESTAMP)
- updated_at (TIMESTAMP)
```

---

## ✅ Checklist for Deployment

- [ ] Database schema created
- [ ] `candidate/apply_candidate.php` working
- [ ] `candidate/get_candidate_status.php` working
- [ ] `admin/approve_candidate_v2.php` working
- [ ] Email service configured and tested
- [ ] Android app compiled without errors
- [ ] API endpoints tested with Postman
- [ ] Registration flow tested (Student)
- [ ] Registration flow tested (Candidate)
- [ ] Candidate application form tested
- [ ] Admin approval system tested
- [ ] Email notifications working
- [ ] Login with approval check working
- [ ] All navigation routes functional

---

## 🐛 Troubleshooting

### Issue: "Application already submitted"
- **Cause**: User already has a pending application
- **Solution**: Only one active application per user allowed

### Issue: Candidate can't login even after approval
- **Cause**: `is_approved` not updated or status check failed
- **Solution**: Check `LoginViewModel.checkCandidateApplicationStatus()` is being called

### Issue: Email not received
- **Cause**: XAMPP mail not configured or firewall blocking
- **Solution**: Configure XAMPP mail in `php.ini`, or use external SMTP service

### Issue: Navigation not working
- **Cause**: Routes not properly defined or arguments mismatch
- **Solution**: Check `AppNavGraph.kt` and route parameters match exactly

---

## 📝 Key Implementation Notes

1. **User ID Storage**: When registering, user ID is returned from backend and stored for candidate application submission
2. **Role-Based Navigation**: Registration screen checks selected role and routes accordingly
3. **Approval Check**: LoginViewModel checks candidate application status before allowing login
4. **Email Notifications**: All major actions send emails (submit, approve, reject)
5. **Error Handling**: All API calls have error handling with user-friendly messages
6. **State Management**: ViewModels handle all state; UI is reactive to state changes

---

## 🔐 Security Considerations

- [ ] Implement JWT tokens (currently using Base64 encoding)
- [ ] Add admin role verification to approve endpoint
- [ ] Validate user ownership before allowing application status check
- [ ] Implement rate limiting on API endpoints
- [ ] Add SQL injection protection (already using prepared statements)
- [ ] Implement CSRF tokens for form submissions
- [ ] Use HTTPS in production

---

## 📞 Support

For issues or questions, refer to:
1. This documentation
2. Code comments in respective files
3. API response messages for debugging
4. Server logs: `XAMPP/apache/logs/`
5. Android logcat for app debugging

**Complete Integration Status: ✅ 100% READY FOR TESTING**
