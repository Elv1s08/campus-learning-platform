package com.campus.campus_server.controller;

import com.campus.campus_server.entity.SysUser;
import com.campus.campus_server.service.SysUserService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class SysUserController {

    private final SysUserService sysUserService;

    public SysUserController(SysUserService sysUserService) {
        this.sysUserService = sysUserService;
    }

    @GetMapping
    public List<SysUser> list() {
        return sysUserService.list();
    }

    @PostMapping
    public boolean add(@RequestBody SysUser user) {
        return sysUserService.save(user);
    }

    @GetMapping("/{id}")
    public SysUser getById(@PathVariable Long id) {
        return sysUserService.getById(id);
    }

    @PutMapping("/{id}")
    public boolean update(@PathVariable Long id, @RequestBody SysUser user) {
        user.setId(id);
        return sysUserService.updateById(user);
    }

    @DeleteMapping("/{id}")
    public boolean delete(@PathVariable Long id) {
        return sysUserService.removeById(id);
    }
}