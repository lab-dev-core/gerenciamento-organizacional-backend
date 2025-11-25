package com.gestaoformativa.service;

import jakarta.transaction.Transactional;
import com.gestaoformativa.model.Role;
import com.gestaoformativa.model.User;
import org.springframework.security.crypto.password.PasswordEncoder;
import com.gestaoformativa.repository.RoleRepository;
import com.gestaoformativa.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import javax.persistence.EntityNotFoundException;
import java.util.List;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public User findByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("Usuário não encontrado com username: " + username));
    }

    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Usuário não encontrado com id: " + id));
    }

    public User saveUser(User user) {
        return userRepository.save(user);
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public User createUser(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }

    @Transactional
    public User updateUser(Long id, User updatedData) {
        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Usuário não encontrado com ID: " + id));

        existingUser.setName(updatedData.getName());
        existingUser.setCity(updatedData.getCity());
        existingUser.setState(updatedData.getState());
        existingUser.setPhone(updatedData.getPhone());
        existingUser.setEducation(updatedData.getEducation());
        existingUser.setAge(updatedData.getAge());
        existingUser.setCommunityYears(updatedData.getCommunityYears());
        existingUser.setCommunityMonths(updatedData.getCommunityMonths());
        existingUser.setMissionLocation(updatedData.getMissionLocation());
        existingUser.setLifeStage(updatedData.getLifeStage());

        if (updatedData.getRole() != null) {
            existingUser.setRole(updatedData.getRole());
        }


//        if (updatedData.getPassword() != null) {
//             existingUser.setPassword(passwordEncoder.encode(updatedData.getPassword()));
//        }

        return userRepository.save(existingUser);
    }

    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }

    public void deleteAllUsers() {
        userRepository.deleteAll();
    }

    @Transactional
    public User assignRole(Long userId, Long roleId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + userId));

        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new EntityNotFoundException("Role not found: " + roleId));

        if (user.getRole() != null && user.getRole().getId().equals(role.getId())) {
            return user;
        }

        user.setRole(role);
        return userRepository.save(user);
    }
}