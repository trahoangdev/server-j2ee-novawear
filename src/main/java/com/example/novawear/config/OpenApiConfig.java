package com.example.novawear.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    private static final String SECURITY_SCHEME_NAME = "bearerAuth";
    private static final String SECURITY_SCHEME = "bearer";
    private static final String BEARER_FORMAT = "JWT";

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(apiInfo())
                .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME_NAME))
                .components(new Components()
                        .addSecuritySchemes(SECURITY_SCHEME_NAME,
                                new SecurityScheme()
                                        .name(SECURITY_SCHEME_NAME)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme(SECURITY_SCHEME)
                                        .bearerFormat(BEARER_FORMAT)
                                        .description("Nhập JWT token trả về từ POST /api/auth/login")));
    }

    private Info apiInfo() {
        return new Info()
                .title("NovaWear API")
                .description("REST API backend cho hệ thống thương mại điện tử NovaWear (đồ án J2EE). " +
                        "Bao gồm: Auth (JWT), Danh mục, Sản phẩm, Giỏ hàng, Đơn hàng, Đánh giá, và trang quản trị Admin.")
                .version("1.0.0")
                .contact(new Contact()
                        .name("NovaWear")
                        .url("https://github.com/trahoangdev/server-j2ee-novawear"))
                .license(new License().name("Unlicense").url("https://unlicense.org/"));
    }
}
