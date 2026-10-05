package com.campus.campus_server.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campus.campus_server.entity.Course;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CourseMapper extends BaseMapper<Course> {
}