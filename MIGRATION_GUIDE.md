# Migration Guide - Add Department Column

## Vấn đề
Sau khi thay đổi từ `specialization` (String) sang `department` (Enum), database chưa có cột `department` trong bảng `doctors`.

## Giải pháp

### Cách 1: Chạy SQL Script trực tiếp (Khuyến nghị)

1. Kết nối đến PostgreSQL database:
```bash
psql -h localhost -U medinova_user -d medinova
```

Hoặc nếu dùng Docker:
```bash
docker exec -it <postgres_container_name> psql -U medinova_user -d medinova
```

2. Chạy migration script:
```bash
\i migration_add_department_column.sql
```

Hoặc copy và paste nội dung file `migration_add_department_column.sql` vào psql.

### Cách 2: Sử dụng Hibernate Auto Update (Tự động)

Nếu bạn muốn Hibernate tự động tạo cột, bạn có thể:

1. Tạm thời set `spring.jpa.hibernate.ddl-auto=create-drop` (⚠️ CẢNH BÁO: Sẽ xóa toàn bộ dữ liệu!)
2. Hoặc set `spring.jpa.hibernate.ddl-auto=update` và restart application - Hibernate sẽ tự động thêm cột mới

**Lưu ý**: Với `update`, Hibernate có thể không tự động migrate dữ liệu từ `specialization` sang `department`.

### Cách 3: Sử dụng psql command line

```bash
# Nếu dùng Docker
docker exec -i <postgres_container_name> psql -U medinova_user -d medinova < migration_add_department_column.sql

# Hoặc nếu chạy PostgreSQL local
psql -h localhost -U medinova_user -d medinova -f migration_add_department_column.sql
```

## Kiểm tra kết quả

Sau khi chạy migration, kiểm tra:

```sql
-- Kiểm tra cột đã được thêm chưa
SELECT column_name, data_type, is_nullable 
FROM information_schema.columns 
WHERE table_name = 'doctors' AND column_name = 'department';

-- Kiểm tra dữ liệu đã được migrate chưa
SELECT id, specialization, department 
FROM doctors 
LIMIT 10;
```

## Rollback (nếu cần)

Nếu cần rollback:

```sql
-- Xóa constraint
ALTER TABLE doctors DROP CONSTRAINT IF EXISTS doctors_department_check;

-- Xóa cột
ALTER TABLE doctors DROP COLUMN IF EXISTS department;
```

## Lưu ý

- Script migration sẽ giữ lại cột `specialization` để backward compatibility
- Tất cả doctors không có department sẽ được set mặc định là `GENERAL_MEDICINE`
- Sau khi migration thành công, bạn có thể xóa cột `specialization` nếu không cần thiết





