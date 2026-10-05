package com.campus.campus_server.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Getter
@Setter
@TableName("course_enrollment")
public class CourseEnrollment {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;
    private Long courseId;
    private LocalDateTime enrolledAt;
}