package com.campus.campus_server.service.impl;

import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.campus.campus_server.entity.SysUser;
import com.campus.campus_server.mapper.SysUserMapper;
import com.campus.campus_server.service.SysUserService;
import org.springframework.stereotype.Service;

@Service
public class SysUserServiceImpl
        extends ServiceImpl<SysUserMapper, SysUser>
        implements SysUserService {
}