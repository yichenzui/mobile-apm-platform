package com.monitor.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 修改密码请求DTO
 */
@Data
@Schema(description = "修改密码请求")
public class ChangePasswordDTO {

    @NotBlank(message = "原密码不能为空")
    @Schema(description = "原密码", example = "123456789")
    private String oldPassword;

    @NotBlank(message = "新密码不能为空")
    @Size(min = 6, max = 32, message = "新密码长度须为6-32位")
    @Schema(description = "新密码", example = "abc123456")
    private String newPassword;
}
