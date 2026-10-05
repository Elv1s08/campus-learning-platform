package com.campus.campus_server.dto;

import jakarta.validation.constraints.NotBlank;

public class UpdateCourseRequest {
    @NotBlank(message = "课程名称不能为空")
    private String courseName;

    private String description;

    public String getCourseName() {
        return courseName;
    }

    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}