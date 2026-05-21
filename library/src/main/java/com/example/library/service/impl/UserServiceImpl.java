package com.example.library.service.impl;

import com.example.library.common.ErrorCodes;
import com.example.library.common.Md5Util;
import com.example.library.entity.User;
import com.example.library.exception.BizException;
import com.example.library.repository.UserRepository;
import com.example.library.service.UserService;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepo;

    public UserServiceImpl(UserRepository userRepo) {
        this.userRepo = userRepo;
    }

    // Simple in-memory token store: token -> userId
    private static final Map<String, Long> tokenStore = new HashMap<>();

    @Override
    public Map<String, Object> login(String username, String password) {
        User user = userRepo.findByUsername(username)
                .orElseThrow(() -> new BizException(ErrorCodes.LOGIN_FAILED, "用户名或密码错误"));
        String encrypted = Md5Util.encrypt(password);
        if (!encrypted.equals(user.getPassword())) {
            throw new BizException(ErrorCodes.LOGIN_FAILED, "用户名或密码错误");
        }
        String token = UUID.randomUUID().toString().replace("-", "");
        tokenStore.put(token, user.getId());
        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("userId", user.getId());
        result.put("username", user.getUsername());
        result.put("role", user.getRole());
        return result;
    }

    @Override
    public User getUserInfo(Long userId) {
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new BizException(ErrorCodes.NOT_FOUND, "用户不存在"));
        user.setPassword(null); // don't expose password
        return user;
    }

    @Override
    public void changePassword(Long userId, String oldPassword, String newPassword) {
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new BizException(ErrorCodes.NOT_FOUND, "用户不存在"));
        if (!Md5Util.encrypt(oldPassword).equals(user.getPassword())) {
            throw new BizException(ErrorCodes.OLD_PASSWORD_WRONG, "旧密码错误");
        }
        userRepo.updatePassword(userId, Md5Util.encrypt(newPassword));
    }

    @Override
    public List<User> listUsers(String keyword) {
        List<User> users;
        if (keyword != null && !keyword.isBlank()) {
            users = userRepo.searchByUsername(keyword);
        } else {
            users = userRepo.findAll();
        }
        users.forEach(u -> u.setPassword(null));
        return users;
    }

    @Override
    public void createUser(User user) {
        if (userRepo.findByUsername(user.getUsername()).isPresent()) {
            throw new BizException(ErrorCodes.USERNAME_EXISTS, "用户名已存在");
        }
        User newUser = new User();
        newUser.setUsername(user.getUsername());
        newUser.setPassword(Md5Util.encrypt(user.getPassword()));
        newUser.setRealName(user.getRealName());
        newUser.setEmployeeId(user.getEmployeeId());
        newUser.setGender(user.getGender());
        newUser.setAge(user.getAge());
        newUser.setRole(user.getRole() != null ? user.getRole() : "NORMAL");
        userRepo.insert(newUser);
    }

    @Override
    public void updateUser(Long id, User user) {
        User existing = userRepo.findById(id)
                .orElseThrow(() -> new BizException(ErrorCodes.NOT_FOUND, "用户不存在"));
        existing.setRealName(user.getRealName());
        existing.setEmployeeId(user.getEmployeeId());
        existing.setGender(user.getGender());
        existing.setAge(user.getAge());
        if (user.getRole() != null) existing.setRole(user.getRole());
        userRepo.update(existing);
    }

    @Override
    public void deleteUser(Long id) {
        if (!userRepo.findById(id).isPresent()) {
            throw new BizException(ErrorCodes.NOT_FOUND, "用户不存在");
        }
        userRepo.deleteById(id);
    }

    public static Long getUserIdFromToken(String token) {
        return tokenStore.get(token);
    }

    public static void removeToken(String token) {
        tokenStore.remove(token);
    }
}
