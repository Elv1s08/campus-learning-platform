package com.campus.campus_server.service.impl;

import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.campus.campus_server.entity.Course;
import com.campus.campus_server.mapper.CourseMapper;
import com.campus.campus_server.service.CourseService;
import org.springframework.stereotype.Service;

@Service
public class CourseServiceImpl
        extends ServiceImpl<CourseMapper, Course>
        implements CourseService {
}