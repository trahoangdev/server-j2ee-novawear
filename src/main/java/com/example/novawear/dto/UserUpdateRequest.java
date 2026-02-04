package com.example.novawear.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserUpdateRequest {

    @NotBlank
    @Size(min = 2, max = 50)
    private String username;

    @NotBlank
    @Email
    private String email;

    /** Để trống hoặc null = không đổi mật khẩu. Nếu có thì tối thiểu 6 ký tự (validate trong service). */
    private String password;

    /** ADMIN hoặc USER */
    private String role;

    /** Trạng thái hoạt động (true = hoạt động, false = khóa) */
    private Boolean active;
}
