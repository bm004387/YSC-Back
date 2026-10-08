package com.buc.ysc.user.vo.request;

public record PasswordChangeRequest(String currentPassword, String newPassword, String confirmPassword) {
}
