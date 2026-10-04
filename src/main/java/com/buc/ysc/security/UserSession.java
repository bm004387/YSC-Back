package com.buc.ysc.security;

/**
 * @param userId
 * @param userName
 * @param role
 */
public record UserSession(
        String userId,
        String userName,
        String role
) {
}