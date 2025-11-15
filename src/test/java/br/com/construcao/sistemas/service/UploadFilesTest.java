package br.com.construcao.sistemas.service;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import static org.junit.jupiter.api.Assertions.*;

class UploadFilesTest {

    @Test
    void testPutObject_ReturnsCorrectUrlFormat() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "foto.png",
                "image/png",
                "conteudo".getBytes()
        );

        UploadFiles upload = new UploadFiles(
                "meu-bucket",
                "us-east-1",
                "fakeKey",
                "fakeSecret"
        );

        String result = upload.putObject(file);

        assertNotNull(result);
        assertTrue(result.startsWith("https://meu-bucket.s3.us-east-1.amazonaws.com/foto.png_"));
    }
}