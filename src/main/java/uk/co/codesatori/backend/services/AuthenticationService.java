package uk.co.codesatori.backend.services;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;

import static java.util.Collections.emptyList;

public class AuthenticationService {
  static final long EXPIRATIONTIME = 864_000_00;
  static final String SIGNINGKEY = "codeSatoriSigningKey";
  static final String BEARER_PREFIX = "Bearer";
  static final String AUTH_HEADER = "Authorisation";

  // Adds the JWT token after successful authentication, containing the username,
  // to authorization header field.
  static public void addJWTToken(HttpServletResponse response, String username) {
    String JwtToken = Jwts.builder().setSubject(username)
        .setExpiration(new Date(System.currentTimeMillis() + EXPIRATIONTIME))
        .signWith(SignatureAlgorithm.HS512, SIGNINGKEY)
        .compact();
    response.addHeader(AUTH_HEADER, BEARER_PREFIX + " " + JwtToken);
    response.addHeader("Access-Control-Expose-Headers", AUTH_HEADER);
  }

  // Tries to get the username from the JWT in the authorization header, and then
  // returns a valid authentication token for that user if found.
  static public Authentication getAuthentication(HttpServletRequest request) {
    String token = request.getHeader(AUTH_HEADER);
    if (token != null) {
      String user = Jwts.parser()
          .setSigningKey(SIGNINGKEY)
          .parseClaimsJws(token.replace(BEARER_PREFIX, ""))
          .getBody()
          .getSubject();

      if (user != null) {
        return new UsernamePasswordAuthenticationToken(user, null, emptyList());
      } else {
        throw new RuntimeException("Authentication failed");
      }
    }
    return null;
  }
}