package com.br.comunicacao.productapi.modules.jwt.service;


import com.br.comunicacao.productapi.config.exception.AuthenticationException;
import com.br.comunicacao.productapi.modules.jwt.dto.JwtResponse;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import static org.springframework.util.ObjectUtils.isEmpty;

@Service
public class JwtService {

    @Value("${app-config.secrets.api-secret}")
    private String apiSecret;

    private static String EMPTY_SPACE = " ";
    private static Integer TOKEN_INDEX = 1;

    public void validateAuthorization(String token){
        var accessToken = extractToken(token);
        try {

            var claims = Jwts
                    .parserBuilder()
                    .setSigningKey(Keys.hmacShaKeyFor(apiSecret.getBytes()))
                    .build()
                    .parseClaimsJws(accessToken)
                    .getBody();
            var user = JwtResponse.getUser(claims);
            if (isEmpty(user) || isEmpty(user.getId())){
                throw new AuthenticationException("The user is not valid.");
            }
        }catch (Exception e) {
            e.printStackTrace();
            throw new AuthenticationException("Error while trying to process the Access token.");
        }
    }

    private String extractToken(String token){
        if (isEmpty(token)) {
            throw new AuthenticationException("The access token was not informed.");
        }

        if (token.contains(EMPTY_SPACE)){
            token = token.split(EMPTY_SPACE)[TOKEN_INDEX];
        }

        return token;
    }

}
