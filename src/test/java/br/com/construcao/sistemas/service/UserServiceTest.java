package br.com.construcao.sistemas.service;

import br.com.construcao.sistemas.controller.dto.mapper.MyModelMapper;
import br.com.construcao.sistemas.controller.dto.request.login.UpdatePasswordRequest;
import br.com.construcao.sistemas.controller.dto.request.login.UpdateUserRequest;
import br.com.construcao.sistemas.controller.dto.request.user.CreateUserRequest;
import br.com.construcao.sistemas.controller.dto.response.image.ImageResponse;
import br.com.construcao.sistemas.controller.dto.response.user.UserResponse;
import br.com.construcao.sistemas.controller.exceptions.BadRequestException;
import br.com.construcao.sistemas.controller.exceptions.ConflictException;
import br.com.construcao.sistemas.controller.exceptions.NotFoundException;
import br.com.construcao.sistemas.exception.UnauthorizedException;
import br.com.construcao.sistemas.model.Image;
import br.com.construcao.sistemas.model.User;
import br.com.construcao.sistemas.model.enums.AuthProvider;
import br.com.construcao.sistemas.model.enums.OwnerType;
import br.com.construcao.sistemas.model.enums.Role;
import br.com.construcao.sistemas.repository.ImageRepository;
import br.com.construcao.sistemas.repository.UserRepository;
import br.com.construcao.sistemas.util.helpers.PasswordGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository repo;
    @Mock
    private ImageRepository imageRepo;
    @Mock
    private UploadFiles uploadFiles;
    @Mock
    private PasswordEncoder encoder;
    @Mock
    private PasswordGenerator generator;
    @Mock
    private EmailService emailService;
    @Mock
    private MyModelMapper mapper;
    @Mock
    private MultipartFile file;

    @InjectMocks
    private UserService service;

    private User user;

    @BeforeEach
    void setup() {
        user = new User();
        user.setId(1L);
        user.setName("João");
        user.setEmail("joao@test.com");
        user.setPassword("encoded");
        user.setRole(Role.SECURITY);
        user.setProvider(AuthProvider.LOCAL);
    }

    @Test
    void testCreate_SucessoSemImagem() throws Exception {
        CreateUserRequest req = new CreateUserRequest();
        req.setEmail("JOAO@Test.com ");
        req.setPassword("123456");
        req.setProvisionalPassword(false);

        when(repo.existsByEmail("joao@test.com")).thenReturn(false);
        when(mapper.mapTo(req, User.class)).thenReturn(user);
        when(encoder.encode("123456")).thenReturn("encoded");
        when(repo.save(any())).thenReturn(user);
        when(imageRepo.findFirstByUser_IdAndOwnerType(1L, OwnerType.USER))
                .thenReturn(Optional.empty());
        when(mapper.mapTo(any(User.class), eq(UserResponse.class)))
                .thenReturn(new UserResponse());

        UserResponse resp = service.create(req, null);

        assertNotNull(resp);
        verify(emailService, never()).sendProvisionalPassword(any(), any(), any(), any());
    }

    @Test
    void testCreate_EmailDuplicado_DeveLancarConflict() {
        CreateUserRequest req = new CreateUserRequest();
        req.setEmail("teste@teste.com");

        when(repo.existsByEmail("teste@teste.com")).thenReturn(true);

        assertThrows(ConflictException.class, () -> service.create(req, null));
    }

    @Test
    void testCreate_GeraSenhaProvisoria_EnviaEmail() throws Exception {
        CreateUserRequest req = new CreateUserRequest();
        req.setEmail("novo@test.com");
        req.setPassword(null); // <- força gerar senha

        when(repo.existsByEmail("novo@test.com")).thenReturn(false);
        when(mapper.mapTo(req, User.class)).thenReturn(user);
        when(generator.generate(10)).thenReturn("senhaGerada");
        when(encoder.encode("senhaGerada")).thenReturn("encoded");
        when(repo.save(any())).thenReturn(user);
        when(mapper.mapTo(any(User.class), eq(UserResponse.class)))
                .thenReturn(new UserResponse());
        when(imageRepo.findFirstByUser_IdAndOwnerType(anyLong(), eq(OwnerType.USER)))
                .thenReturn(Optional.empty());

        service.create(req, null);

        verify(emailService).sendProvisionalPassword(
                eq("novo@test.com"),
                eq("João"),
                eq("senhaGerada"),
                any()
        );
    }

    @Test
    void testCreate_ComImagem_DeveSalvarImagem() throws Exception {
        CreateUserRequest req = new CreateUserRequest();
        req.setEmail("a@a.com");

        when(repo.existsByEmail("a@a.com")).thenReturn(false);
        when(mapper.mapTo(req, User.class)).thenReturn(user);
        when(repo.save(any())).thenReturn(user);
        when(file.isEmpty()).thenReturn(false);
        when(uploadFiles.putObject(file)).thenReturn("url.png");
        when(imageRepo.save(any())).thenReturn(new Image());
        when(mapper.mapTo(any(User.class), eq(UserResponse.class)))
                .thenReturn(new UserResponse());
        when(imageRepo.findFirstByUser_IdAndOwnerType(anyLong(), eq(OwnerType.USER)))
                .thenReturn(Optional.empty());

        UserResponse resp = service.create(req, file);

        assertNotNull(resp);
        verify(imageRepo).deleteByUser_IdAndOwnerType(1L, OwnerType.USER);
        verify(imageRepo).save(any(Image.class));
    }

    @Test
    void testGet_Sucesso() {
        when(repo.findById(1L)).thenReturn(Optional.of(user));
        when(mapper.mapTo(user, UserResponse.class)).thenReturn(new UserResponse());

        UserResponse resp = service.get(1L);

        assertNotNull(resp);
    }

    @Test
    void testGet_NotFound() {
        when(repo.findById(1L)).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> service.get(1L));
    }

    @Test
    void testDelete_Sucesso() {
        when(repo.existsById(1L)).thenReturn(true);

        service.delete(1L);

        verify(repo).deleteById(1L);
    }

    @Test
    void testDelete_NotFound() {
        when(repo.existsById(1L)).thenReturn(false);

        assertThrows(NotFoundException.class, () -> service.delete(1L));
    }

    @Test
    void testList_DeveRetornarPaginaDeUsuariosComFoto() {
        User u = new User();
        u.setId(10L);

        UserResponse mapped = new UserResponse();
        mapped.setId(10L);

        when(repo.findAll(any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(u)));

        when(mapper.mapTo(any(User.class), eq(UserResponse.class)))
                .thenReturn(mapped);

        when(imageRepo.findFirstByUser_IdAndOwnerType(10L, OwnerType.USER))
                .thenReturn(Optional.of(Image.builder().url("url.png").build()));

        Page<UserResponse> page = service.list(PageRequest.of(0, 10));

        assertEquals(1, page.getTotalElements());
        assertEquals("url.png", page.getContent().get(0).getProfileImageUrl());
    }

    @Test
    void testUpdatePassword_SenhaAtualIncorreta() {
        User u = new User();
        u.setPassword("encoded");

        when(repo.findById(1L)).thenReturn(Optional.of(u));
        when(encoder.matches("123", "encoded")).thenReturn(false);

        UpdatePasswordRequest req = new UpdatePasswordRequest();
        req.setCurrentPassword("123");
        req.setNewPassword("novaSenha123");

        assertThrows(UnauthorizedException.class,
                () -> service.updatePassword(1L, req)
        );
    }

    @Test
    void testUpdatePassword_SenhaNovaCurta() {
        User u = new User();
        when(repo.findById(1L)).thenReturn(Optional.of(u));

        UpdatePasswordRequest req = new UpdatePasswordRequest();
        req.setNewPassword("123");

        assertThrows(BadRequestException.class,
                () -> service.updatePassword(1L, req)
        );
    }

    @Test
    void testUpdatePassword_Sucesso() {
        User u = new User();
        when(repo.findById(1L)).thenReturn(Optional.of(u));
        when(encoder.matches("123456", u.getPassword())).thenReturn(true);
        when(encoder.encode("novaSenha")).thenReturn("encodedNova");

        UpdatePasswordRequest req = new UpdatePasswordRequest();
        req.setCurrentPassword("123456");
        req.setNewPassword("novaSenha");

        service.updatePassword(1L, req);

        assertEquals("encodedNova", u.getPassword());
        verify(repo).save(u);
    }

    @Test
    void testUpdate_UsuarioNaoEncontrado() {
        UpdateUserRequest req = new UpdateUserRequest();
        when(repo.findById(1L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> service.update(1L, req, null)
        );
    }

    @Test
    void testUpdate_ApenasAtualizarNome() throws Exception{
        User u = new User();
        u.setId(1L);
        u.setName("Antigo Nome");
        u.setEmail("email@mail.com");

        UpdateUserRequest req = new UpdateUserRequest();
        req.setName("Novo Nome");

        when(repo.findById(1L)).thenReturn(Optional.of(u));
        when(repo.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(mapper.mapTo(any(User.class), eq(UserResponse.class))).thenReturn(new UserResponse());

        service.update(1L, req, null);

        assertEquals("Novo Nome", u.getName());
    }

    @Test
    void testUpdate_EmailJaExiste() {
        User u = new User();
        u.setId(1L);
        u.setEmail("old@mail.com");

        UpdateUserRequest req = new UpdateUserRequest();
        req.setEmail("novo@mail.com");

        when(repo.findById(1L)).thenReturn(Optional.of(u));
        when(repo.existsByEmail("novo@mail.com")).thenReturn(true);

        assertThrows(ConflictException.class,
                () -> service.update(1L, req, null)
        );
    }

    @Test
    void testUpdate_AtualizarEmailComSucesso() throws Exception {
        User u = new User();
        u.setId(1L);
        u.setEmail("old@mail.com");

        UpdateUserRequest req = new UpdateUserRequest();
        req.setEmail("new@mail.com");

        when(repo.findById(1L)).thenReturn(Optional.of(u));
        when(repo.existsByEmail("new@mail.com")).thenReturn(false);
        when(repo.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(mapper.mapTo(any(User.class), eq(UserResponse.class))).thenReturn(new UserResponse());

        service.update(1L, req, null);

        assertEquals("new@mail.com", u.getEmail());
    }

    @Test
    void testUpdate_AtualizarCamposSimples() throws Exception {
        User u = new User();
        u.setId(1L);

        UpdateUserRequest req = new UpdateUserRequest();
        req.setRole(Role.ADMIN);
        req.setEnabled(false);
        req.setLocked(true);

        when(repo.findById(1L)).thenReturn(Optional.of(u));
        when(repo.save(any())).thenReturn(u);
        when(mapper.mapTo(any(User.class), eq(UserResponse.class)))
                .thenReturn(new UserResponse());

        service.update(1L, req, null);

        assertEquals(Role.ADMIN, u.getRole());
    }

    @Test
    void testClearFcmToken_Sucesso() {
        User u = new User();
        u.setFcmToken("abc");

        when(repo.findById(1L)).thenReturn(Optional.of(u));

        service.clearFcmToken(1L);

        assertNull(u.getFcmToken());
        assertNotNull(u.getFcmTokenUpdatedAt());
        verify(repo).save(u);
    }

    @Test
    void testSetProfileImage_ArquivoInvalido() {
        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(true);

        assertThrows(BadRequestException.class,
                () -> service.setProfileImage(1L, file)
        );
    }

    @Test
    void testSetProfileImage_UsuarioNaoEncontrado() {
        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(false);

        when(repo.findById(1L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> service.setProfileImage(1L, file)
        );
    }

    @Test
    void testSetProfileImage_Sucesso() throws Exception {
        User u = new User();
        u.setId(1L);

        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(false);
        when(file.getContentType()).thenReturn("image/png");
        when(file.getSize()).thenReturn(123L);

        when(repo.findById(1L)).thenReturn(Optional.of(u));
        when(uploadFiles.putObject(file)).thenReturn("url.png");

        Image saved = Image.builder()
                .id(50L)
                .url("url.png")
                .contentType("image/png")
                .sizeBytes(123L)
                .user(u)
                .ownerType(OwnerType.USER)
                .build();

        when(imageRepo.save(any(Image.class))).thenReturn(saved);

        ImageResponse resp = service.setProfileImage(1L, file);

        assertEquals(50L, resp.getId());
        assertEquals("url.png", resp.getUrl());
        verify(imageRepo).deleteByUser_IdAndOwnerType(1L, OwnerType.USER);
    }






}