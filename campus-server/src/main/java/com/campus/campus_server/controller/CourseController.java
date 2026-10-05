package com.campus.campus_server.controller;

import com.campus.campus_server.common.ApiResponse;
import com.campus.campus_server.dto.CreateCourseRequest;
import com.campus.campus_server.dto.UpdateCourseRequest;
import com.campus.campus_server.entity.Course;
import com.campus.campus_server.service.CourseService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/courses")
public class CourseController {

    private final CourseService courseService;

    public CourseController(CourseService courseService) {
        this.courseService = courseService;
    }

    @PostMapping
    public ApiResponse<Boolean> create(
            @RequestAttribute("role") String role,
            @Valid @RequestBody CreateCourseRequest request) {

        if (!"ADMIN".equals(role)) {
            return ApiResponse.error(403, "没有管理员权限");
        }

        boolean exists = courseService.lambdaQuery()
                .eq(Course::getCourseCode, request.getCourseCode())
                .exists();

        if (exists) {
            return ApiResponse.error("课程编号已存在");
        }

        Course course = new Course();
        course.setCourseCode(request.getCourseCode());
        course.setCourseName(request.getCourseName());
        course.setDescription(request.getDescription());
        course.setCoverUrl(request.getCoverUrl());
        course.setTeacherId(request.getTeacherId());
        course.setStatus(1);

        return ApiResponse.success(courseService.save(course));
    }

    @GetMapping
    public ApiResponse<List<Course>> list(
            @RequestAttribute("role") String role) {

        if ("ADMIN".equals(role)) {
            return ApiResponse.success(courseService.list());
        }

        return ApiResponse.success(
                courseService.lambdaQuery()
                        .eq(Course::getStatus, 1)
                        .list()
        );
    }

    @GetMapping("/{id}")
    public ApiResponse<Course> getById(
            @PathVariable Long id,
            @RequestAttribute("role") String role) {

        Course course = courseService.getById(id);

        if (course == null ||
                (!"ADMIN".equals(role) && course.getStatus() != 1)) {
            return ApiResponse.error("课程不存在");
        }

        return ApiResponse.success(course);
    }

    @PutMapping("/{id}")
    public ApiResponse<Boolean> update(
            @PathVariable Long id,
            @RequestAttribute("role") String role,
            @Valid @RequestBody UpdateCourseRequest request) {

        if (!"ADMIN".equals(role)) {
            return ApiResponse.error(403, "没有管理员权限");
        }

        if (courseService.getById(id) == null) {
            return ApiResponse.error("课程不存在");
        }

        Course course = new Course();
        course.setId(id);
        course.setCourseName(request.getCourseName());
        course.setDescription(request.getDescription());

        return ApiResponse.success(courseService.updateById(course));
    }

    @PatchMapping("/{id}/status")
    public ApiResponse<Boolean> changeStatus(
            @PathVariable Long id,
            @RequestParam Integer status,
            @RequestAttribute("role") String role) {

        if (!"ADMIN".equals(role)) {
            return ApiResponse.error(403, "没有管理员权限");
        }

        if (status != 0 && status != 1) {
            return ApiResponse.error("状态只能是 0 或 1");
        }

        if (courseService.getById(id) == null) {
            return ApiResponse.error("课程不存在");
        }

        Course course = new Course();
        course.setId(id);
        course.setStatus(status);
        return ApiResponse.success(courseService.updateById(course));
    }

}