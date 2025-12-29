# Hướng dẫn chạy Unit Tests

## Tổng quan

Dự án đã có các unit tests cho các controllers:
- `AuthControllerTest` - Test các API authentication
- `ClinicControllerTest` - Test các API quản lý clinic
- `UserControllerTest` - Test các API quản lý user
- `UserProfileControllerTest` - Test các API profile và medical history

## Cách chạy Tests

### 1. Chạy tất cả tests

```bash
# Sử dụng Maven Wrapper
./mvnw test

# Hoặc nếu đã cài Maven
mvn test
```

### 2. Chạy test cho một class cụ thể

```bash
# Chạy test cho AuthController
./mvnw test -Dtest=AuthControllerTest

# Chạy test cho ClinicController
./mvnw test -Dtest=ClinicControllerTest

# Chạy test cho UserController
./mvnw test -Dtest=UserControllerTest

# Chạy test cho UserProfileController
./mvnw test -Dtest=UserProfileControllerTest
```

### 3. Chạy test cho một method cụ thể

```bash
# Ví dụ: Chạy test method testLogin_Success trong AuthControllerTest
./mvnw test -Dtest=AuthControllerTest#testLogin_Success
```

### 4. Chạy tests với coverage report

```bash
# Cần thêm plugin jacoco vào pom.xml (tùy chọn)
./mvnw test jacoco:report
```

## Cấu trúc Test

### AuthControllerTest
- ✅ `testLogin_Success` - Test login thành công
- ✅ `testLogin_InvalidCredentials` - Test login với credentials sai
- ✅ `testLogin_ValidationError` - Test validation error
- ✅ `testRegister_Success` - Test đăng ký thành công
- ✅ `testRegister_EmailExists` - Test đăng ký với email đã tồn tại
- ✅ `testRegister_ValidationError` - Test validation error
- ✅ `testLogout_Success` - Test logout thành công
- ✅ `testValidateToken_Success` - Test validate token thành công
- ✅ `testValidateToken_Expired` - Test validate token đã hết hạn

### ClinicControllerTest
- ✅ `testCreateClinic_Success` - Test tạo clinic thành công (ADMIN)
- ✅ `testCreateClinic_Forbidden` - Test tạo clinic với role không đủ quyền
- ✅ `testCreateClinic_Unauthorized` - Test tạo clinic khi chưa đăng nhập
- ✅ `testCreateClinic_ValidationError` - Test validation error
- ✅ `testGetClinicById_Success` - Test lấy clinic theo ID
- ✅ `testGetClinicById_NotFound` - Test clinic không tồn tại
- ✅ `testGetAllClinics_Success` - Test lấy tất cả clinics
- ✅ `testUpdateClinic_Success` - Test update clinic (ADMIN)
- ✅ `testUpdateClinic_Forbidden` - Test update với role không đủ quyền
- ✅ `testUpdateClinic_NotFound` - Test update clinic không tồn tại
- ✅ `testDeleteClinic_Success` - Test xóa clinic (ADMIN)
- ✅ `testDeleteClinic_Forbidden` - Test xóa với role không đủ quyền
- ✅ `testDeleteClinic_NotFound` - Test xóa clinic không tồn tại

### UserControllerTest
- ✅ `testGetAllUsers_Success` - Test lấy tất cả users (ADMIN)
- ✅ `testGetAllUsers_Forbidden` - Test với role không đủ quyền
- ✅ `testGetAllUsers_Unauthorized` - Test khi chưa đăng nhập
- ✅ `testGetUserById_Success` - Test lấy user theo ID (ADMIN)
- ✅ `testGetUserById_NotFound` - Test user không tồn tại
- ✅ `testUpdateUserRole_Success` - Test update role (ADMIN)
- ✅ `testUpdateUserRole_Forbidden` - Test với role không đủ quyền
- ✅ `testUpdateUserRole_NotFound` - Test user không tồn tại
- ✅ `testUpdateUserRole_InvalidRole` - Test với role không hợp lệ
- ✅ `testUpdateUserRole_ValidationError` - Test validation error

### UserProfileControllerTest
- ✅ `testGetUserProfile_Success` - Test lấy profile
- ✅ `testGetUserProfile_Unauthorized` - Test khi chưa đăng nhập
- ✅ `testUpdateMedicalHistory_Success` - Test update medical history
- ✅ `testUpdateMedicalHistory_Unauthorized` - Test khi chưa đăng nhập
- ✅ `testGetMedicalHistory_Success` - Test lấy medical history
- ✅ `testGetMedicalHistory_NotFound` - Test khi không có medical history
- ✅ `testGetMedicalHistory_Unauthorized` - Test khi chưa đăng nhập

## Công nghệ sử dụng

- **JUnit 5** - Framework testing
- **MockMvc** - Test Spring MVC controllers
- **Mockito** - Mock dependencies với `@Mock` và `@InjectMocks`
- **MockMvcBuilders.standaloneSetup()** - Setup controller test không cần full Spring context
- **GlobalExceptionHandler** - Exception handling trong tests
- **Jackson ObjectMapper** - Serialize/Deserialize JSON với Java 8 time support

## Lưu ý

1. **Test Pattern**: Sử dụng `@ExtendWith(MockitoExtension.class)`, `@Mock`, và `@InjectMocks` thay vì `@WebMvcTest` và `@MockBean` (do Spring Boot 4.0.1 compatibility)
2. **MockMvc Setup**: Sử dụng `MockMvcBuilders.standaloneSetup()` với `setControllerAdvice()` để include exception handler
3. **ObjectMapper**: Sử dụng `objectMapper.findAndRegisterModules()` để enable Java 8 time types support
4. **JSON Assertions**: Sử dụng `jsonPath()` để verify JSON response
5. **Exception Handling**: GlobalExceptionHandler được thêm vào MockMvc để test exception responses

## Troubleshooting

### Lỗi: "No tests found"
- Kiểm tra tên class test phải kết thúc bằng `Test` hoặc `Tests`
- Đảm bảo test class nằm trong package `src/test/java`

### Lỗi: "Security context not found"
- Đảm bảo sử dụng `@WithMockUser` cho các test cần authentication
- Kiểm tra `@WebMvcTest` annotation đã được thêm

### Lỗi: "CSRF token not found"
- Thêm `.with(csrf())` cho các POST/PUT/DELETE requests

## Xem kết quả test

Sau khi chạy tests, kết quả sẽ hiển thị trong console. Nếu muốn xem chi tiết hơn:

```bash
# Chạy với verbose output
./mvnw test -X
```

Kết quả test sẽ được lưu tại: `target/surefire-reports/`

