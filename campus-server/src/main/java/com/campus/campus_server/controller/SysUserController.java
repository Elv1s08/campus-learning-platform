package com.campus.campus_server.controller;
import com.campus.campus_server.dto.RegisterRequest;
import com.campus.campus_server.entity.SysUser;
import com.campus.campus_server.service.SysUserService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import com.campus.campus_server.dto.LoginRequest;
import com.campus.campus_server.entity.SysUser;
import java.util.List;
import com.campus.campus_server.common.ApiResponse;
import com.campus.campus_server.util.JwtUtil;
import com.campus.campus_server.dto.UpdateProfileRequest;

@RestController
@RequestMapping("/api/users")
public class SysUserController {

    private final SysUserService sysUserService;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    // Spring 通过构造器注入这三个 Bean，并将引用保存到成员变量
    public SysUserController(SysUserService sysUserService,
                             PasswordEncoder passwordEncoder,
                             JwtUtil jwtUtil) {
        this.sysUserService = sysUserService;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    @GetMapping
    public ApiResponse<List<SysUser>> list(
            @RequestAttribute("role") String role) {

        if (!"ADMIN".equals(role)) {
            return ApiResponse.error(403, "没有管理员权限");
        }

        return ApiResponse.success(sysUserService.list());
    }

    @PostMapping("/register")
    public ApiResponse<Boolean> add(
            @Valid @RequestBody RegisterRequest request) {

        long count = sysUserService.lambdaQuery()
                .eq(SysUser::getUsername, request.getUsername())
                .count();

        if (count > 0) {
            return ApiResponse.error("用户名已存在");
        }

        SysUser user = new SysUser();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRealName(request.getRealName());
        user.setRole("STUDENT");
        user.setStatus(1);

        return ApiResponse.success(sysUserService.save(user));
    }

    @GetMapping("/{id}")
    public ApiResponse<SysUser> getById(
            @PathVariable Long id,
            @RequestAttribute("role") String role) {

        if (!"ADMIN".equals(role)) {
            return ApiResponse.error(403, "没有管理员权限");
        }

        SysUser user = sysUserService.getById(id);

        if (user == null) {
            return ApiResponse.error("用户不存在");
        }

        return ApiResponse.success(user);
    }

    @PutMapping("/{id}")
    public ApiResponse<Boolean> update(
            @PathVariable Long id,
            @RequestBody SysUser user,
            @RequestAttribute("role") String role) {

        if (!"ADMIN".equals(role)) {
            return ApiResponse.error(403, "没有管理员权限");
        }

        if (sysUserService.getById(id) == null) {
            return ApiResponse.error("用户不存在");
        }

        user.setId(id);

        if (user.getPassword() != null && !user.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        }else{
            user.setPassword(null);
        }

        return ApiResponse.success(sysUserService.updateById(user));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Boolean> delete(
            @PathVariable Long id,
            @RequestAttribute("role") String role) {

        if (!"ADMIN".equals(role)) {
            return ApiResponse.error(403, "没有管理员权限");
        }

        if (sysUserService.getById(id) == null) {
            return ApiResponse.error("用户不存在");
        }

        return ApiResponse.success(sysUserService.removeById(id));
    }

    @GetMapping("/me")
    public ApiResponse<SysUser> getCurrentUser(
            @RequestAttribute("userId") String userId) {

        SysUser user = sysUserService.getById(Long.valueOf(userId));

        if (user == null) {
            return ApiResponse.error("用户不存在");
        }

        return ApiResponse.success(user);
    }


    @PostMapping("/login")
    public ApiResponse<String> login(@Valid @RequestBody LoginRequest request) {
        SysUser user = sysUserService.lambdaQuery()
                .eq(SysUser::getUsername, request.getUsername())
                .eq(SysUser::getStatus, 1)
                .one();

        if (user != null && passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            String token = jwtUtil.generateToken(user);
            return ApiResponse.success(token);
        }

        return ApiResponse.error("用户名或密码错误");
    }

    @PutMapping("/me")
    public ApiResponse<Boolean> updateCurrentUser(
            @RequestAttribute("userId") String userId,
            @Valid @RequestBody UpdateProfileRequest request) {

        if (request.getRealName() == null &&
                request.getPassword() == null) {
            return ApiResponse.error("没有需要修改的内容");
        }

        Long id = Long.valueOf(userId);

        if (sysUserService.getById(id) == null) {
            return ApiResponse.error("用户不存在");
        }

        SysUser user = new SysUser();
        user.setId(id);

        if (request.getRealName() != null) {
            user.setRealName(request.getRealName());
        }

        if (request.getPassword() != null) {
            user.setPassword(
                    passwordEncoder.encode(request.getPassword())
            );
        }

        return ApiResponse.success(sysUserService.updateById(user));
    }

}