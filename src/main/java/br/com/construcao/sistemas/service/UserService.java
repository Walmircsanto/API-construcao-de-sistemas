package br.com.construcao.sistemas.service;


import br.com.construcao.sistemas.controller.dto.mapper.MyModelMapper;
import br.com.construcao.sistemas.controller.dto.request.login.UpdatePasswordRequest;
import br.com.construcao.sistemas.controller.dto.request.login.UpdateUserRequest;
import br.com.construcao.sistemas.controller.dto.request.user.CreateUserRequest;
import br.com.construcao.sistemas.controller.dto.response.emergency.EmergencyContactResponse;
import br.com.construcao.sistemas.controller.dto.response.image.ImageResponse;
import br.com.construcao.sistemas.controller.dto.response.user.UserResponse;
import br.com.construcao.sistemas.controller.exceptions.BadRequestException;
import br.com.construcao.sistemas.controller.exceptions.ConflictException;
import br.com.construcao.sistemas.controller.exceptions.NotFoundException;
import br.com.construcao.sistemas.exception.InternalServerErrorException;
import br.com.construcao.sistemas.exception.UnauthorizedException;
import br.com.construcao.sistemas.model.Image;
import br.com.construcao.sistemas.model.User;
import br.com.construcao.sistemas.model.enums.AuthProvider;
import br.com.construcao.sistemas.model.enums.OwnerType;
import br.com.construcao.sistemas.model.enums.Role;
import br.com.construcao.sistemas.repository.ImageRepository;
import br.com.construcao.sistemas.repository.UserRepository;
import br.com.construcao.sistemas.util.helpers.PasswordGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.lang.Nullable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository repo;
    private final ImageRepository imageRepo;
    private final UploadFiles uploadFiles;
    private final PasswordEncoder encoder;
    private final PasswordGenerator generator;
    private final EmailService emailService;
    private final MyModelMapper mapper;

    @Transactional
    public UserResponse create(CreateUserRequest req, @Nullable MultipartFile file) throws IOException {
        String email = req.getEmail().trim().toLowerCase();
        if (repo.existsByEmail(email)) throw new ConflictException("E-mail já cadastrado");

        User user = mapper.mapTo(req, User.class);
        user.setEmail(email);

        boolean mustGenerateProvisional = (req.getPassword() == null || req.getPassword().isBlank());
        boolean markProvisional = Boolean.TRUE.equals(req.getProvisionalPassword()) || mustGenerateProvisional;

        String rawPassword = mustGenerateProvisional ? generator.generate(10) : req.getPassword();
        user.setPassword(encoder.encode(rawPassword));
        user.setProvisionalPassword(markProvisional);
        user.setProvisionalPasswordExpiresAt(markProvisional ? Instant.now().plus(Duration.ofDays(7)) : null);

        if (user.getRole() == null) user.setRole(Role.SECURITY);
        if (user.getProvider() == null) user.setProvider(AuthProvider.LOCAL);

        user = repo.save(user);

        if (file != null && !file.isEmpty()) {
            imageRepo.deleteByUser_IdAndOwnerType(user.getId(), OwnerType.USER);
            saveProfileImage(user, file);
        }

        if (markProvisional) {
            emailService.sendProvisionalPassword(
                    user.getEmail(), user.getName(), rawPassword, user.getProvisionalPasswordExpiresAt()
            );
        }

        return enrichWithProfileImage(mapper.mapTo(user, UserResponse.class), user.getId());
    }

    public UserResponse get(Long id){
        User user = repo.findById(id).orElseThrow(() -> new NotFoundException("Usuário não encontrado"));
        return enrichWithProfileImage(mapper.mapTo(user, UserResponse.class), id);
    }

    public Page<UserResponse> listAllByRole(Role role, Pageable pageable){
        return repo.findAllByRole(role, pageable)
                .map(u -> enrichWithProfileImage(mapper.mapTo(u, UserResponse.class), u.getId()));
    }

    @Transactional
    public UserResponse update(Long id, UpdateUserRequest req, @Nullable MultipartFile file) throws IOException {
        User user = repo.findById(id).orElseThrow(() -> new NotFoundException("Usuário não encontrado"));

        if (req.getName() != null) user.setName(req.getName());

        if (req.getEmail() != null) {
            String newEmail = req.getEmail().trim().toLowerCase();
            if (!newEmail.equalsIgnoreCase(user.getEmail()) && repo.existsByEmail(newEmail)) {
                throw new ConflictException("E-mail já cadastrado");
            }
            user.setEmail(newEmail);
        }

        if (req.getRole() != null) user.setRole(req.getRole());
        if (req.getEnabled() != null) user.setEnabled(req.getEnabled());
        if (req.getLocked() != null) user.setLocked(req.getLocked());

        user = repo.save(user);

        if (file != null && !file.isEmpty()) {
            imageRepo.deleteByUser_IdAndOwnerType(user.getId(), OwnerType.USER);
            String url = uploadFiles.putObject(file);
            if (url == null) throw new InternalServerErrorException("Falha ao salvar no bucket");

            Image img = Image.builder()
                    .user(user)
                    .ownerType(OwnerType.USER)
                    .url(url)
                    .contentType(file.getContentType())
                    .sizeBytes(file.getSize())
                    .build();
            imageRepo.save(img);
        }

        UserResponse resp = mapper.mapTo(user, UserResponse.class);
        imageRepo.findFirstByUser_IdAndOwnerType(user.getId(), OwnerType.USER)
                .ifPresent(img -> resp.setProfileImageUrl(img.getUrl()));
        return resp;
    }

    @Transactional
    public void updatePassword(Long id, UpdatePasswordRequest req) {
        User user = repo.findById(id)
                .orElseThrow(() -> new NotFoundException("Usuário não encontrado"));

        if (!encoder.matches(req.getCurrentPassword(), user.getPassword())) {
            throw new UnauthorizedException("Senha atual inválida");
        }

        if (!req.getNewPassword().equals(req.getConfirmNewPassword())) {
            throw new BadRequestException("Confirmação de senha não confere");
        }

        if (encoder.matches(req.getNewPassword(), user.getPassword())) {
            throw new BadRequestException("Nova senha deve ser diferente da atual");
        }

        user.setPassword(encoder.encode(req.getNewPassword()));
        user.setProvisionalPassword(false);
        user.setProvisionalPasswordExpiresAt(null);
        user.setLastPasswordChangeAt(Instant.now());

        repo.save(user);
    }


    public void delete(Long id){
        if (!repo.existsById(id)) throw new NotFoundException("Usuário não encontrado");
        repo.deleteById(id);
    }

    @Transactional
    public void clearFcmToken(Long userId) {
        User u = repo.findById(userId).orElseThrow(() -> new NotFoundException("Usuário não encontrado"));
        u.setFcmToken(null);
        u.setFcmTokenUpdatedAt(Instant.now());
        repo.save(u);
    }

    @Transactional
    public void updateFcmToken(Long userId, String token) {
        if (token == null || token.isBlank()) throw new BadRequestException("Token FCM inválido");
        User u = repo.findById(userId).orElseThrow(() -> new NotFoundException("Usuário não encontrado"));
        u.setFcmToken(token);
        u.setFcmTokenUpdatedAt(Instant.now());
        repo.save(u);
    }

    @Transactional
    public ImageResponse setProfileImage(Long userId, MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) throw new BadRequestException("Arquivo de imagem ausente");

        User user = repo.findById(userId).orElseThrow(() -> new NotFoundException("Usuário não encontrado"));

        imageRepo.deleteByUser_IdAndOwnerType(user.getId(), OwnerType.USER);

        Image img = saveProfileImage(user, file);
        return new ImageResponse(img.getId(), img.getUrl(), img.getContentType(), img.getSizeBytes());
    }

    private Image saveProfileImage(User user, MultipartFile file) throws IOException {
        String url = uploadFiles.putObject(file);
        if (url == null) throw new InternalServerErrorException("Falha ao salvar no bucket");

        Image img = Image.builder()
                .user(user)
                .ownerType(OwnerType.USER)
                .url(url)
                .contentType(file.getContentType())
                .sizeBytes(file.getSize())
                .build();
        return imageRepo.save(img);
    }

    private UserResponse enrichWithProfileImage(UserResponse resp, Long userId) {
        imageRepo.findFirstByUser_IdAndOwnerType(userId, OwnerType.USER)
                .ifPresent(img -> resp.setProfileImageUrl(img.getUrl()));
        return resp;
    }
}

