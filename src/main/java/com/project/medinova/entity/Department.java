package com.project.medinova.entity;

public enum Department {
    GENERAL_MEDICINE("Nội tổng quát"),
    PEDIATRICS("Nhi"),
    OBSTETRICS_GYNECOLOGY("Sản – Phụ"),
    SURGERY("Ngoại tổng quát"),
    CARDIOLOGY("Tim mạch"),
    NEUROLOGY("Thần kinh"),
    ORTHOPEDICS("Chấn thương chỉnh hình"),
    ONCOLOGY("Ung bướu"),
    GASTROENTEROLOGY("Tiêu hóa"),
    RESPIRATORY("Hô hấp"),
    NEPHROLOGY("Thận"),
    ENDOCRINOLOGY("Nội tiết"),
    HEMATOLOGY("Huyết học"),
    RHEUMATOLOGY("Cơ xương khớp"),
    DERMATOLOGY("Da liễu"),
    INFECTIOUS_DISEASE("Truyền nhiễm");

    private final String displayName;

    Department(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}


