package com.example.library.controller;

import com.example.library.common.ApiResponse;
import com.example.library.entity.User;
import com.example.library.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/user")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/login")
    public ApiResponse<Map<String, Object>> login(@RequestBody Map<String, String> body) {
        String username = body.get("username");
        String password = body.get("password");
        Map<String, Object> result = userService.login(username, password);
        return ApiResponse.success(result);
    }

    @PostMapping("/logout")
    public ApiResponse<Void> logout(HttpServletRequest request) {
        String token = request.getHeader("X-Token");
        if (token != null) {
            com.example.library.service.impl.UserServiceImpl.removeToken(token);
        }
        return ApiResponse.success();
    }

    @GetMapping("/info")
    public ApiResponse<User> info(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return ApiResponse.success(userService.getUserInfo(userId));
    }

    @GetMapping("/{id}")
    public ApiResponse<User> getById(@PathVariable Long id) {
        return ApiResponse.success(userService.getUserInfo(id));
    }

    @PutMapping("/password")
    public ApiResponse<Void> changePassword(HttpServletRequest request, @RequestBody Map<String, String> body) {
        Long userId = (Long) request.getAttribute("userId");
        userService.changePassword(userId, body.get("oldPassword"), body.get("newPassword"));
        return ApiResponse.success();
    }

    @GetMapping("/list")
    public ApiResponse<List<User>> list(@RequestParam(required = false) String keyword) {
        return ApiResponse.success(userService.listUsers(keyword));
    }

    @PostMapping("/add")
    public ApiResponse<Void> add(@RequestBody User user) {
        userService.createUser(user);
        return ApiResponse.success();
    }

    @PutMapping("/{id}")
    public ApiResponse<Void> update(@PathVariable Long id, @RequestBody User user) {
        userService.updateUser(id, user);
        return ApiResponse.success();
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        userService.deleteUser(id);
        return ApiResponse.success();
    }
}
