# Cách chạy Spring Boot Project

## 1. Khởi động PostgreSQL Database

Đầu tiên, cần khởi động PostgreSQL bằng Docker Compose:

```bash
docker-compose up -d
```

Kiểm tra xem database đã chạy chưa:
```bash
docker-compose ps
```

## 2. Chạy Spring Boot Application

### Cách 1: Sử dụng Maven Wrapper (Khuyến nghị)

```bash
./mvnw spring-boot:run
```

Hoặc trên Windows:
```bash
mvnw.cmd spring-boot:run
```

### Cách 2: Build và chạy JAR file

**Build project:**
```bash
./mvnw clean package
```

**Chạy JAR file:**
```bash
java -jar target/medinova-0.0.1-SNAPSHOT.jar
```

### Cách 3: Sử dụng Maven (nếu đã cài đặt Maven)

```bash
mvn spring-boot:run
```

Hoặc build và chạy:
```bash
mvn clean package
java -jar target/medinova-0.0.1-SNAPSHOT.jar
```

## 3. Chạy với các profile khác nhau (nếu có)

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

## 4. Dừng ứng dụng

Nhấn `Ctrl + C` để dừng ứng dụng

## 5. Dừng PostgreSQL

```bash
docker-compose down
```

Hoặc dừng và xóa dữ liệu:
```bash
docker-compose down -v
```

## Lưu ý

- Đảm bảo PostgreSQL đang chạy trước khi khởi động Spring Boot application
- Application sẽ tự động tạo các bảng database từ JPA entities khi chạy lần đầu
- Port mặc định của Spring Boot là 8080 (nếu không cấu hình khác)

