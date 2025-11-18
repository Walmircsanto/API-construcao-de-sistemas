package br.com.construcao.sistemas.config.openapi;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Component
public class OpenApiConfig {

    @Bean
    public OpenAPI myOpenAPI() {
        final String securitySchemeName = "bearerAuth";

        Server devServer = new Server();
        devServer.setUrl("http://localhost:8080");
        devServer.setDescription("URL do servidor no ambiente de desenvolvimento");

        Contact contact = new Contact();
        contact.setEmail("");
        contact.setName("");
        contact.setUrl("");

//        License mitLicense = new License().name("MIT License").url("https://choosealicense.com/licenses/mit/");

        Info info = new Info()
                .title("Api Inove")
                .version("1.0")
                .contact(contact)
                .description("Esta API apresenta os endpoints do projeto Nexus.").termsOfService("");
//                .license(mitLicense);

        return new OpenAPI()
                .info(info);
//                .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
//                .components(new Components()
//                        .addSecuritySchemes(securitySchemeName,
//                                new SecurityScheme()
//                                        .name(securitySchemeName)
//                                        .type(SecurityScheme.Type.HTTP)
//                                        .scheme("bearer")
//                                        .bearerFormat("JWT")
//                        )
//                )
//                .servers(List.of(devServer));
    }

}
