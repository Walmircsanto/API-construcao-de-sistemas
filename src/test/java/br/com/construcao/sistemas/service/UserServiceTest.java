package br.com.construcao.sistemas.service;

import br.com.construcao.sistemas.controller.dto.mapper.MyModelMapper;
import br.com.construcao.sistemas.controller.dto.request.login.UpdatePasswordRequest;
import br.com.construcao.sistemas.controller.dto.request.login.UpdateUserRequest;
import br.com.construcao.sistemas.controller.dto.request.user.CreateUserRequest;
import br.com.construcao.sistemas.controller.dto.response.user.UserResponse;
import br.com.construcao.sistemas.controller.exceptions.ConflictException;
import br.com.construcao.sistemas.controller.exceptions.NotFoundException;
import br.com.construcao.sistemas.model.User;
import br.com.construcao.sistemas.model.enums.Role;
import br.com.construcao.sistemas.repository.UserRepository;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository repo;

    @Mock
    private PasswordEncoder encoder;

    @Mock
    private MyModelMapper mapper;

    @InjectMocks
    private UserService service;

    private User baseUser(Long id, String name, String email, String encodedPwd,
                          Role role, boolean enabled, boolean locked) {
        return new User(
                id, name, email, encodedPwd,
                role, null, enabled, locked,
                0, Instant.now()
        );
    }

    @Nested
    @DisplayName("create(CreateUserRequest)")
    class CreateTests {

        @Test
        @DisplayName("Deve persistir e retornar UserResponse quando e-mail não existe")
        void create_shouldPersist_andReturnResponse_whenEmailIsFree() {
            // Given
            CreateUserRequest req = new CreateUserRequest();
            req.setName("Alice");
            req.setEmail("alice@mail.com");
            req.setPassword("pl@in");
            req.setRole(Role.ADMIN);

            when(repo.existsByEmail("alice@mail.com")).thenReturn(false);

            User toPersist = new User();
            toPersist.setName("Alice");
            toPersist.setEmail("alice@mail.com");
            toPersist.setRole(Role.ADMIN);

            when(mapper.mapTo(req, User.class)).thenReturn(toPersist);
            when(encoder.encode("pl@in")).thenReturn("{enc}pl@in");

            User saved = baseUser(10L, "Alice", "alice@mail.com", "{enc}pl@in", Role.ADMIN, true, false);
            when(repo.save(toPersist)).thenReturn(saved);

            UserResponse mapped = new UserResponse(10L, "Alice", "alice@mail.com", Role.ADMIN, true, false);
            when(mapper.mapTo(saved, UserResponse.class)).thenReturn(mapped);

            // When
            UserResponse resp = service.create(req);

            // Then
            assertNotNull(resp);
            assertEquals(10L, resp.getId());
            assertEquals("Alice", resp.getName());
            assertEquals("alice@mail.com", resp.getEmail());
            assertEquals(Role.ADMIN, resp.getRole());
            assertTrue(resp.isEnabled());
            assertFalse(resp.isLocked());

            verify(repo).existsByEmail("alice@mail.com");
            verify(mapper).mapTo(req, User.class);
            verify(encoder).encode("pl@in");
            verify(repo).save(toPersist);
            verify(mapper).mapTo(saved, UserResponse.class);
            verifyNoMoreInteractions(repo, encoder, mapper);

            assertEquals("{enc}pl@in", toPersist.getPassword());
        }

        @Test
        @DisplayName("Deve lançar ConflictException quando e-mail já existe")
        void create_shouldThrowConflict_whenEmailAlreadyExists() {
            // Given
            CreateUserRequest req = new CreateUserRequest();
            req.setName("Alice");
            req.setEmail("alice@mail.com");
            req.setPassword("pl@in");
            req.setRole(Role.SECURITY);

            when(repo.existsByEmail("alice@mail.com")).thenReturn(true);

            // When / Then
            ConflictException ex = assertThrows(ConflictException.class, () -> service.create(req));
            assertEquals("E-mail já cadastrado", ex.getMessage());

            verify(repo).existsByEmail("alice@mail.com");
            verifyNoMoreInteractions(repo, encoder, mapper);
        }
    }

    @Nested
    @DisplayName("get(Long)")
    class GetTests {

        @Test
        @DisplayName("Deve retornar UserResponse quando encontrar o usuário")
        void get_shouldReturnUserResponse_whenFound() {
            // Given
            User found = baseUser(10L, "Alice", "alice@mail.com", "{enc}", Role.ADMIN, true, false);
            when(repo.findById(10L)).thenReturn(Optional.of(found));

            UserResponse mapped = new UserResponse(10L, "Alice", "alice@mail.com", Role.ADMIN, true, false);
            when(mapper.mapTo(found, UserResponse.class)).thenReturn(mapped);

            // When
            UserResponse resp = service.get(10L);

            // Then
            assertNotNull(resp);
            assertEquals(10L, resp.getId());
            assertEquals("Alice", resp.getName());
            assertEquals(Role.ADMIN, resp.getRole());

            verify(repo).findById(10L);
            verify(mapper).mapTo(found, UserResponse.class);
            verifyNoMoreInteractions(repo, mapper);
        }

        @Test
        @DisplayName("Deve lançar NotFoundException quando usuário não existir")
        void get_shouldThrowNotFound_whenAbsent() {
            // Given
            when(repo.findById(999L)).thenReturn(Optional.empty());

            // When / Then
            NotFoundException ex = assertThrows(NotFoundException.class, () -> service.get(999L));
            assertEquals("Usuário não encontrado", ex.getMessage());

            verify(repo).findById(999L);
            verifyNoMoreInteractions(repo);
            verifyNoInteractions(mapper);
        }
    }

    @Nested
    @DisplayName("list()")
    class ListTests {

        @Test
        @DisplayName("Deve retornar lista mapeada")
        void list_shouldReturnMappedList() {
            // Given
            var u1 = baseUser(10L, "Alice", "alice@mail.com", "{enc}", Role.ADMIN, true, false);
            var u2 = baseUser(11L, "Bob", "bob@mail.com", "{enc}", Role.SECURITY, true, false);

            when(repo.findAll()).thenReturn(List.of(u1, u2));

            var mapped = List.of(
                    new UserResponse(10L, "Alice", "alice@mail.com", Role.ADMIN, true, false),
                    new UserResponse(11L, "Bob", "bob@mail.com", Role.SECURITY, true, false)
            );
            when(mapper.toList(List.of(u1, u2), UserResponse.class)).thenReturn(mapped);

            // When
            List<UserResponse> resp = service.list();

            // Then
            assertNotNull(resp);
            assertEquals(2, resp.size());
            assertEquals(10L, resp.get(0).getId());
            assertEquals(Role.ADMIN, resp.get(0).getRole());
            assertEquals(Role.SECURITY, resp.get(1).getRole());

            verify(repo).findAll();
            verify(mapper).toList(List.of(u1, u2), UserResponse.class);
            verifyNoMoreInteractions(repo, mapper);
        }

        @Test
        @DisplayName("Deve retornar lista vazia quando não houver usuários")
        void list_shouldReturnEmptyList_whenNoUsers() {
            // Given
            when(repo.findAll()).thenReturn(List.of());
            when(mapper.toList(List.of(), UserResponse.class)).thenReturn(List.of());

            // When
            List<UserResponse> resp = service.list();

            // Then
            assertNotNull(resp);
            assertTrue(resp.isEmpty());

            verify(repo).findAll();
            verify(mapper).toList(List.of(), UserResponse.class);
            verifyNoMoreInteractions(repo, mapper);
        }
    }

    @Nested
    @DisplayName("update(Long, UpdateUserRequest)")
    class UpdateTests {

        @Test
        @DisplayName("Deve aplicar alterações e retornar UserResponse quando encontrado")
        void update_shouldApplyChanges_andReturnResponse_whenFound() {
            // Given
            User existing = baseUser(10L, "Alice", "alice@mail.com", "{enc}old", Role.SECURITY, true, false);
            when(repo.findById(10L)).thenReturn(Optional.of(existing));

            UpdateUserRequest req = new UpdateUserRequest();
            req.setName("Alice Nova");
            req.setEmail("alice.nova@mail.com");
            req.setRole(Role.ADMIN);
            req.setEnabled(false);
            req.setLocked(true);

            when(mapper.mapTo(eq(req), any())).thenReturn(existing);

            User saved = baseUser(10L, "Alice Nova", "alice.nova@mail.com", "{enc}old", Role.ADMIN, false, true);
            when(repo.save(existing)).thenReturn(saved);

            UserResponse mapped = new UserResponse(10L, "Alice Nova", "alice.nova@mail.com", Role.ADMIN, false, true);
            when(mapper.mapTo(saved, UserResponse.class)).thenReturn(mapped);

            // When
            UserResponse resp = service.update(10L, req);

            // Then
            assertNotNull(resp);
            assertEquals(10L, resp.getId());
            assertEquals("Alice Nova", resp.getName());
            assertEquals("alice.nova@mail.com", resp.getEmail());
            assertEquals(Role.ADMIN, resp.getRole());
            assertFalse(resp.isEnabled());
            assertTrue(resp.isLocked());

            assertEquals("Alice Nova", existing.getName());
            assertEquals("alice.nova@mail.com", existing.getEmail());
            assertEquals(Role.ADMIN, existing.getRole());
            assertFalse(existing.isEnabled());
            assertTrue(existing.isLocked());

            verify(repo).findById(10L);
            verify(mapper).mapTo(eq(req), any());
            verify(repo).save(existing);
            verify(mapper).mapTo(saved, UserResponse.class);
            verifyNoMoreInteractions(repo, mapper);
        }

        @Test
        @DisplayName("Não deve alterar campos quando request tem todos os valores nulos")
        void update_shouldNotChangeFields_whenRequestHasAllNulls() {
            // Given
            User existing = baseUser(10L, "Alice", "alice@mail.com", "{enc}old", Role.SECURITY, true, false);
            when(repo.findById(10L)).thenReturn(Optional.of(existing));

            UpdateUserRequest emptyReq = new UpdateUserRequest(); // tudo null
            when(mapper.mapTo(eq(emptyReq), any())).thenReturn(existing);

            when(repo.save(existing)).thenReturn(existing);

            UserResponse mapped = new UserResponse(10L, "Alice", "alice@mail.com", Role.SECURITY, true, false);
            when(mapper.mapTo(existing, UserResponse.class)).thenReturn(mapped);

            // When
            UserResponse resp = service.update(10L, emptyReq);

            // Then
            assertNotNull(resp);
            assertEquals("Alice", existing.getName());
            assertEquals("alice@mail.com", existing.getEmail());
            assertEquals(Role.SECURITY, existing.getRole());
            assertTrue(existing.isEnabled());
            assertFalse(existing.isLocked());

            verify(repo).findById(10L);
            verify(mapper).mapTo(eq(emptyReq), any());
            verify(repo).save(existing);
            verify(mapper).mapTo(existing, UserResponse.class);
            verifyNoMoreInteractions(repo, mapper);
        }

        @Test
        @DisplayName("Deve lançar NotFoundException quando usuário não existir")
        void update_shouldThrowNotFound_whenUserDoesNotExist() {
            // Given
            UpdateUserRequest req = new UpdateUserRequest();
            req.setName("X");

            when(repo.findById(999L)).thenReturn(Optional.empty());

            // When / Then
            NotFoundException ex = assertThrows(NotFoundException.class, () -> service.update(999L, req));
            assertEquals("Usuário não encontrado", ex.getMessage());

            verify(repo).findById(999L);
            verifyNoMoreInteractions(repo);
            verifyNoInteractions(mapper);
        }
    }

    @Nested
    @DisplayName("updatePassword(Long, UpdatePasswordRequest)")
    class UpdatePasswordTests {

        @Test
        @DisplayName("Deve codificar e persistir a nova senha quando encontrado")
        void updatePassword_shouldEncodeAndPersist_whenFound() {
            // Given
            User existing = baseUser(10L, "Alice", "alice@mail.com", "{enc}old", Role.ADMIN, true, false);
            when(repo.findById(10L)).thenReturn(Optional.of(existing));

            UpdatePasswordRequest req = new UpdatePasswordRequest();
            req.setNewPassword("n3wPass!");

            when(encoder.encode("n3wPass!")).thenReturn("{enc}n3w");
            when(repo.save(existing)).thenReturn(existing);

            // When
            service.updatePassword(10L, req);

            // Then
            assertEquals("{enc}n3w", existing.getPassword());

            verify(repo).findById(10L);
            verify(encoder).encode("n3wPass!");
            verify(repo).save(existing);
            verifyNoMoreInteractions(repo, encoder);
        }

        @Test
        @DisplayName("Deve lançar NotFoundException quando usuário não existir")
        void updatePassword_shouldThrowNotFound_whenUserDoesNotExist() {
            // Given
            UpdatePasswordRequest req = new UpdatePasswordRequest();
            req.setNewPassword("x");

            when(repo.findById(999L)).thenReturn(Optional.empty());

            // When / Then
            NotFoundException ex = assertThrows(NotFoundException.class, () -> service.updatePassword(999L, req));
            assertEquals("Usuário não encontrado", ex.getMessage());

            verify(repo).findById(999L);
            verifyNoMoreInteractions(repo);
            verifyNoInteractions(encoder);
        }
    }

    @Nested
    @DisplayName("delete(Long)")
    class DeleteTests {

        @Test
        @DisplayName("Deve remover quando existir")
        void delete_shouldRemove_whenExists() {
            // Given
            when(repo.existsById(10L)).thenReturn(true);

            // When
            service.delete(10L);

            // Then
            verify(repo).existsById(10L);
            verify(repo).deleteById(10L);
            verifyNoMoreInteractions(repo);
        }

        @Test
        @DisplayName("Deve lançar NotFoundException quando não existir")
        void delete_shouldThrowNotFound_whenNotExists() {
            // Given
            when(repo.existsById(999L)).thenReturn(false);

            // When / Then
            NotFoundException ex = assertThrows(NotFoundException.class, () -> service.delete(999L));
            assertEquals("Usuário não encontrado", ex.getMessage());

            verify(repo).existsById(999L);
            verifyNoMoreInteractions(repo);
        }
    }
}
