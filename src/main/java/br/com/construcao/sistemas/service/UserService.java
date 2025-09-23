package br.com.construcao.sistemas.service;


import br.com.construcao.sistemas.controller.dto.mapper.MyModelMapper;
import br.com.construcao.sistemas.controller.dto.request.login.UpdatePasswordRequest;
import br.com.construcao.sistemas.controller.dto.request.login.UpdateUserRequest;
import br.com.construcao.sistemas.controller.dto.request.user.CreateUserRequest;
import br.com.construcao.sistemas.controller.dto.response.user.UserResponse;
import br.com.construcao.sistemas.controller.exceptions.ConflictException;
import br.com.construcao.sistemas.controller.exceptions.NotFoundException;
import br.com.construcao.sistemas.model.User;
import br.com.construcao.sistemas.repository.UserRepository;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    private final UserRepository repo;
    private final PasswordEncoder encoder;
    private final MyModelMapper mapper;

    public UserService(UserRepository userRepository, PasswordEncoder encoder, MyModelMapper mapper) {
        this.repo = userRepository;
        this.encoder = encoder;
        this.mapper = mapper;
    }

    public UserResponse create(CreateUserRequest req){
        if (repo.existsByEmail(req.getEmail())) {
            throw new ConflictException("E-mail já cadastrado");
        }
        User user = mapper.mapTo(req, User.class);
        user.setPassword(encoder.encode(req.getPassword()));

        return mapper.mapTo(repo.save(user), UserResponse.class);
    }

    public UserResponse get(Long id){
        User user = repo.findById(id)
                .orElseThrow(() -> new NotFoundException("Usuário não encontrado"));
        return mapper.mapTo(user, UserResponse.class);
    }

    public List<UserResponse> list(){
        return mapper.toList(repo.findAll(), UserResponse.class);
    }

    public UserResponse update(Long id, UpdateUserRequest req){
        User user = repo.findById(id)
                .orElseThrow(() -> new NotFoundException("Usuário não encontrado"));

        mapper.mapTo(req, user.getClass());
        if (req.getName() != null) user.setName(req.getName());
        if (req.getEmail() != null) user.setEmail(req.getEmail());
        if (req.getRole() != null) user.setRole(req.getRole());
        if (req.getEnabled() != null) user.setEnabled(req.getEnabled());
        if (req.getLocked() != null) user.setLocked(req.getLocked());

        return mapper.mapTo(repo.save(user), UserResponse.class);
    }

    public void updatePassword(Long id, UpdatePasswordRequest req){
        User user = repo.findById(id)
                .orElseThrow(() -> new NotFoundException("Usuário não encontrado"));
        user.setPassword(encoder.encode(req.getNewPassword()));
        repo.save(user);
    }

    public void delete(Long id){
        if (!repo.existsById(id)) throw new NotFoundException("Usuário não encontrado");
        repo.deleteById(id);
    }
}
