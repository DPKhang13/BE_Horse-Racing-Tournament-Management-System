package com.group5.htms.util;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseCookie;

import java.time.Duration;
import java.util.Arrays;
import java.util.Optional;

public class CookieUtil {

    private CookieUtil() {
    }

    public static Optional<String> getCookieValue(HttpServletRequest request, String cookieName) {
        if (request.getCookies() == null) {
            return Optional.empty();
        }

        return Arrays.stream(request.getCookies())
                .filter(cookie -> cookieName.equals(cookie.getName()))
                .map(Cookie::getValue)
                .findFirst();
    }

    public static void addHttpOnlyCookie(
            HttpServletResponse response,
            String name,
            String value,
            long maxAgeMillis
    ) {
        ResponseCookie cookie = ResponseCookie.from(name, value)
                .httpOnly(true)
                .secure(false) // local dev false, deploy HTTPS thì đổi true
                .sameSite("Lax")
                .path("/")
                .maxAge(Duration.ofMillis(maxAgeMillis))
                .build();

        response.addHeader("Set-Cookie", cookie.toString());
    }

    public static void clearCookie(HttpServletResponse response, String name) {
        ResponseCookie cookie = ResponseCookie.from(name, "")
                .httpOnly(true)
                .secure(false) // local dev false, deploy HTTPS thì đổi true
                .sameSite("Lax")
                .path("/")
                .maxAge(0)
                .build();

        response.addHeader("Set-Cookie", cookie.toString());
    }
}