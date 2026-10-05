package com.buc.ysc.security;

/**
 * @param userId
 * @param userNm
 * @param role
 */
public record UserSession(
        String userId,
        String userNm,
        String role
) {
}