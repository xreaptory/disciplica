package com.disciplica.server.user;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import com.disciplica.shared.user.AvatarProfileDto;
import com.disciplica.shared.user.UserProfile;

@Component
public class UserMapper {
    private final JdbcTemplate jdbcTemplate;

    public UserMapper(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public UserProfile toProfile(UserRow user) {
        AvatarProfileDto avatar = jdbcTemplate.query("""
                SELECT body_size, shirt_color, skin_color, hair_color, hair_bangs, hair_style, extra
                FROM avatar_profiles WHERE user_id = ?
                """, (rs, rowNum) -> new AvatarProfileDto(
                rs.getString("body_size"),
                rs.getString("shirt_color"),
                rs.getString("skin_color"),
                rs.getString("hair_color"),
                rs.getString("hair_bangs"),
                rs.getString("hair_style"),
                rs.getString("extra")
        ), user.id()).stream().findFirst().orElse(new AvatarProfileDto(
                "medium", "blue", "warm", "brown", "none", "short", "none"
        ));
        return new UserProfile(
                user.id(),
                user.username(),
                user.email(),
                user.level(),
                user.xp(),
                user.health(),
                user.gold(),
                avatar
        );
    }
}
