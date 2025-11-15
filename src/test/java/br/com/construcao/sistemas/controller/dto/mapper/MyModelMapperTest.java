package br.com.construcao.sistemas.controller.dto.mapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MyModelMapperTest {

    private MyModelMapper mapper;

    @BeforeEach
    void setup() {
        mapper = new MyModelMapper();
    }

    static class Source {
        private String name;
        private int age;

        public Source() {}
        public Source(String name, int age) { this.name = name; this.age = age; }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public int getAge() { return age; }
        public void setAge(int age) { this.age = age; }
    }

    static class Dest {
        private String name;
        private int age;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public int getAge() { return age; }
        public void setAge(int age) { this.age = age; }
    }

    @Test
    void testMapTo() {
        Source src = new Source("Diego", 30);

        Dest dest = mapper.mapTo(src, Dest.class);

        assertNotNull(dest);
        assertEquals(src.getName(), dest.getName());
        assertEquals(src.getAge(), dest.getAge());
    }

    @Test
    void testToList() {
        List<Source> sources = List.of(
                new Source("Alice", 25),
                new Source("Bob", 40)
        );

        List<Dest> destList = mapper.toList(sources, Dest.class);

        assertNotNull(destList);
        assertEquals(2, destList.size());
        assertEquals("Alice", destList.get(0).getName());
        assertEquals(25, destList.get(0).getAge());
        assertEquals("Bob", destList.get(1).getName());
        assertEquals(40, destList.get(1).getAge());
    }

    @Test
    void testToList_ListaVazia() {
        List<Source> sources = List.of();
        List<Dest> destList = mapper.toList(sources, Dest.class);

        assertNotNull(destList);
        assertTrue(destList.isEmpty());
    }

}