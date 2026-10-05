package com.campus.campus_server.service.impl;

import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.campus.campus_server.entity.Subject;
import com.campus.campus_server.mapper.SubjectMapper;
import com.campus.campus_server.service.SubjectService;
import org.springframework.stereotype.Service;

@Service
public class SubjectServiceImpl
        extends ServiceImpl<SubjectMapper, Subject>
        implements SubjectService {
}