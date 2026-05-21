package com.example.library.service;

import com.example.library.entity.User;
import java.util.List;
import java.util.Map;

public interface UserService {
    Map<String, Object> login(String username, String password);
    User getUserInfo(Long userId);
    void changePassword(Long userId, String oldPassword, String newPassword);
    List<User> listUsers(String keyword);
    void createUser(User user);
    void updateUser(Long id, User user);
    void deleteUser(Long id);
}
