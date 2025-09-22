package br.com.construcao.sistemas.service;


import br.com.construcao.sistemas.model.User;
import br.com.construcao.sistemas.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }


    public User createUser(User user){
        return userRepository.save(user);
    }

    public void deleteUser(User user){
        userRepository.delete(user);
    }

    public List<User> findAllUser(){
        return userRepository.findAll();
    }

    public User findUserById(Long id){
        return userRepository.findById(id).get();
    }
}
