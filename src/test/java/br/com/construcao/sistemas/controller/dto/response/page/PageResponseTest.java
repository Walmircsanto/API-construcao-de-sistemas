package br.com.construcao.sistemas.controller.dto.response.page;

import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PageResponseTest {

    @Test
    void testOf_ComResultados() {
        List<String> content = List.of("A", "B", "C");
        Page<String> page = new PageImpl<>(
                content,
                PageRequest.of(2, 3),
                15
        );

        PageResponse<String> resp = PageResponse.of(page);

        assertEquals(content, resp.getItems());
        assertEquals(2, resp.getPage());
        assertEquals(3, resp.getSize());
        assertEquals(15, resp.getTotalElements());
        assertEquals(5, resp.getTotalPages());
        assertFalse(resp.isFirst());
        assertFalse(resp.isLast());
    }

    @Test
    void testOf_PrimeiraPagina() {
        List<Integer> content = List.of(10, 20);

        Page<Integer> page = new PageImpl<>(
                content,
                PageRequest.of(0, 2),
                6
        );

        PageResponse<Integer> resp = PageResponse.of(page);

        assertTrue(resp.isFirst());
        assertFalse(resp.isLast());
        assertEquals(3, resp.getTotalPages());
    }

    @Test
    void testOf_UltimaPagina() {
        List<String> content = List.of("X", "Y");

        Page<String> page = new PageImpl<>(
                content,
                PageRequest.of(2, 2),
                6
        );

        PageResponse<String> resp = PageResponse.of(page);

        assertFalse(resp.isFirst());
        assertTrue(resp.isLast());
    }


    @Test
    void testOf_TiposGenericos() {
        record MyObj(int id, String name) {}

        List<MyObj> content = List.of(
                new MyObj(1, "A"),
                new MyObj(2, "B")
        );

        Page<MyObj> page = new PageImpl<>(
                content,
                PageRequest.of(1, 2),
                6
        );

        PageResponse<MyObj> resp = PageResponse.of(page);

        assertEquals(2, resp.getItems().size());
        assertEquals("A", resp.getItems().get(0).name());
        assertEquals("B", resp.getItems().get(1).name());
        assertEquals(1, resp.getPage());
        assertEquals(2, resp.getSize());
    }
}