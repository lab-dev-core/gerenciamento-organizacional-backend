package com.gestaoformativa.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequest {

    @NotBlank(message = "O nome de usuário não pode estar em branco")
    private String username;

    @NotBlank(message = "A senha não pode estar em branco")
    private String password;
}