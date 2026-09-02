package org.pirate.sportwebserver.service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.pirate.sportwebserver.dto.StravaToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import jakarta.annotation.PostConstruct;
import java.sql.Timestamp;

import org.pirate.sportwebserver.service.DbConnectionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

@Service
public class StravaService {
    private static final Logger log = LoggerFactory.getLogger(StravaService.class);

    @Value("${strava.client.id}")
    private String clientId;

    @Value("${strava.client.secret}")
    private String clientSecret;

    @Value("${strava.redirect.uri}")
    private String redirectUri;

    private final RestTemplate rest = new RestTemplate();

    // In-memory token store (simple). Replace with DB persistence if needed.
    private volatile StravaToken currentToken;

    @Autowired
    private DbConnectionService dbConnection;

    public String getAuthorizationUrl() {
        String scopes = "activity:read_all,profile:read_all";
        String url = String.format(
                "https://www.strava.com/oauth/authorize?client_id=%s&response_type=code&redirect_uri=%s&approval_prompt=auto&scope=%s",
                clientId, redirectUri, scopes);
        return url;
    }

    @SuppressWarnings("unchecked")
    public StravaToken exchangeCodeForToken(String code) {
        log.info("Exchanging code for Strava token");

        String url = "https://www.strava.com/oauth/token";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("client_id", clientId);
        body.add("client_secret", clientSecret);
        body.add("code", code);
        body.add("grant_type", "authorization_code");

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);

        Map<String, Object> resp = rest.postForObject(url, request, Map.class);
        if (resp == null) {
            throw new RuntimeException("Empty response from Strava token endpoint");
        }

        StravaToken token = mapToToken(resp);
        this.currentToken = token;
        // persist token to DB
        try {
            saveTokenToDb(token);
        } catch (Exception e) {
            log.warn("Failed to persist Strava token to DB", e);
        }
        log.info("Obtained Strava token, expires at {}", token.getExpiresAt());
        return token;
    }

    @PostConstruct
    private void init() {
        // Load token from DB on startup
        try {
            StravaToken token = loadTokenFromDb();
            if (token != null) {
                this.currentToken = token;
                log.info("Loaded Strava token from DB, athleteId={}, expiresAt={}", token.getAthleteId(), token.getExpiresAt());
            } else {
                log.info("No Strava token found in DB on startup");
            }
        } catch (Exception e) {
            log.warn("Error loading Strava token from DB on startup", e);
        }
    }

    private StravaToken mapToToken(Map<String, Object> resp) {
        StravaToken token = new StravaToken();
        token.setAccessToken((String) resp.get("access_token"));
        token.setRefreshToken((String) resp.get("refresh_token"));
        Number expiresAt = (Number) resp.get("expires_at");
        if (expiresAt != null) token.setExpiresAt(Instant.ofEpochSecond(expiresAt.longValue()));
        Object athlete = resp.get("athlete");
        if (athlete == null) 
        {
			log.info("No athlete info in Strava token response, cop old ID");
			token.setAthleteId(currentToken != null ? currentToken.getAthleteId() : null);
		} 
        else if (athlete instanceof Map) {
            Map<String, Object> a = (Map<String, Object>) athlete;
            Object id = a.get("id");
            if (id instanceof Number) token.setAthleteId(((Number) id).longValue());
        }
        return token;
    }

    @SuppressWarnings("unchecked")
    public StravaToken refreshAccessToken() {
        if (currentToken == null || currentToken.getRefreshToken() == null) {
            throw new IllegalStateException("No refresh token available. Authenticate first.");
        }

        String url = "https://www.strava.com/oauth/token";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("client_id", clientId);
        body.add("client_secret", clientSecret);
        body.add("grant_type", "refresh_token");
        body.add("refresh_token", currentToken.getRefreshToken());

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);

        Map<String, Object> resp = rest.postForObject(url, request, Map.class);
        if (resp == null) {
            throw new RuntimeException("Empty response from Strava token refresh endpoint");
        }
        StravaToken token = mapToToken(resp);
        this.currentToken = token;
        try {
            saveTokenToDb(token);
        } catch (Exception e) {
            log.warn("Failed to persist refreshed Strava token to DB", e);
        }
        log.info("Refreshed Strava token, expires at {}", token.getExpiresAt());
        return token;
    }

    private void saveTokenToDb(StravaToken token) throws Exception {
        if (token == null) return;
        if (token.getAthleteId() == null) {
            log.warn("Cannot persist Strava token without athleteId");
            return;
        }

        // Convert expiresAt to SQL Timestamp if present
        Timestamp expiresAtTs = null;
        if (token.getExpiresAt() != null) {
            expiresAtTs = Timestamp.from(token.getExpiresAt());
        }

        // Use MySQL-style upsert (ON DUPLICATE KEY) assuming UNIQUE constraint on athlete_id
        String sql = "INSERT INTO STRAVA_TOKENS (ATHLETE_ID,ACCESS_TOKEN, REFRESH_TOKEN,EXPIRES_AT, RAW_RESPONSE, REVOKED) VALUES (?, ?, ?, ?, ?, 0) "
                + "ON DUPLICATE KEY UPDATE ACCESS_TOKEN = VALUES(ACCESS_TOKEN), REFRESH_TOKEN = VALUES(REFRESH_TOKEN), EXPIRES_AT = VALUES(EXPIRES_AT), RAW_RESPONSE = VALUES(RAW_RESPONSE), REVOKED = 0, UPDATED_AT = CURRENT_TIMESTAMP(3)";

        // raw_response left null (could be enhanced to store full JSON)
        dbConnection.executeUpdateWithParams(sql, token.getAthleteId(), token.getAccessToken(), token.getRefreshToken(), expiresAtTs, null);
        log.info("Strava token persisted for athlete {}", token.getAthleteId());
    }

    private StravaToken loadTokenFromDb() throws Exception {
        // Load most recently updated non-revoked token
        String sql = "SELECT * FROM STRAVA_TOKENS WHERE REVOKED = 0 ORDER BY UPDATED_AT DESC LIMIT 1";
        List<Map<String, Object>> rows = dbConnection.executeQuery(sql);
        if (rows == null || rows.isEmpty()) return null;
        Map<String, Object> row = rows.get(0);
        StravaToken token = new StravaToken();
        if (row.get("ATHLETE_ID") != null) token.setAthleteId(((Number) row.get("ATHLETE_ID")).longValue());
        if (row.get("ACCESS_TOKEN") != null) token.setAccessToken((String) row.get("ACCESS_TOKEN"));
        if (row.get("REFRESH_TOKEN") != null) token.setRefreshToken((String) row.get("REFRESH_TOKEN"));
        if (row.get("EXPIRES_AT") != null) {
            Object o = row.get("EXPIRES_AT");
            if (o instanceof java.sql.Timestamp) 
            {
                token.setExpiresAt(((java.sql.Timestamp) o).toInstant());
            }
            else if (o instanceof Number) 
            {
                token.setExpiresAt(Instant.ofEpochSecond(((Number) o).longValue()));
            }
            else if (o instanceof LocalDateTime) 
			{
            	//tour.tourdaten.datum =  ((LocalDateTime) row.get("DATUM")).toString();
				token.setExpiresAt(Instant.ofEpochSecond(((LocalDateTime) o).toEpochSecond(java.time.ZoneOffset.UTC)));
			}
            else if (o instanceof String) 
            {
                try {
                    token.setExpiresAt(Instant.parse((String) o));
                } catch (Exception e) {
                    log.warn("Could not parse EXPIRES_AT value: {}", o);
                }
            }
        }
        return token;
    }

    public List<Map<String, Object>> getActivities(int page, int perPage) {
        if (currentToken == null || currentToken.getAccessToken() == null) {
            throw new IllegalStateException("No access token available. Authenticate first.");
        }
        try {
            String url = String.format("https://www.strava.com/api/v3/athlete/activities?page=%d&per_page=%d", page,
                    perPage);
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(currentToken.getAccessToken());
            headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

            org.springframework.http.HttpEntity<Void> entity = new org.springframework.http.HttpEntity<>(headers);
            org.springframework.http.ResponseEntity<List> response = rest.exchange(url, org.springframework.http.HttpMethod.GET,
                    entity, List.class);
            List<Map<String, Object>> resp = response.getBody();
            if (resp == null) return Collections.emptyList();
            return resp;
        } catch (Exception e) {
            log.error("Failed to fetch activities", e);
            throw new RuntimeException("Failed to fetch activities", e);
        }
    }

    public StravaToken getCurrentToken() {
        return currentToken;
    }

}
