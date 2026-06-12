package com.disciplica.server.party;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.disciplica.shared.party.ChatMessageDto;
import com.disciplica.shared.party.PartyDto;
import com.disciplica.shared.party.PartyInviteDto;
import com.disciplica.shared.party.PartyMemberDto;

/**
 * Datenbankzugriff rund um Gruppen (Parties): Gruppen, Mitglieder,
 * Einladungen und Chat-Nachrichten.
 */
@Repository
public class PartyRepository {
    private final JdbcTemplate jdbcTemplate;

    /**
     * Erzeugt das Repository mit dem Datenbankzugriff.
     *
     * @param jdbcTemplate der Datenbankzugriff
     */
    public PartyRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * Legt eine neue Gruppe an und trägt den Ersteller als Leiter ein.
     *
     * @param leaderId die Kennung des Gruppenleiters
     * @param name     der Name der Gruppe
     * @return die angelegte Gruppe
     */
    public PartyDto create(UUID leaderId, String name) {
        UUID partyId = jdbcTemplate.queryForObject(
                "INSERT INTO parties (name, leader_id) VALUES (?, ?) RETURNING id",
                UUID.class, name, leaderId);
        jdbcTemplate.update("INSERT INTO party_members (party_id, user_id, role) VALUES (?, ?, 'LEADER')",
                partyId, leaderId);
        return findById(partyId).orElseThrow();
    }

    /**
     * Ermittelt die zuletzt beigetretene Gruppe eines Benutzers.
     *
     * @param userId die Kennung des Benutzers
     * @return die Gruppe oder ein leeres {@link Optional}, falls der Benutzer
     *         in keiner Gruppe ist
     */
    public Optional<PartyDto> findCurrentForUser(UUID userId) {
        return jdbcTemplate.query("""
                SELECT p.id, p.name, p.leader_id
                FROM parties p
                JOIN party_members pm ON pm.party_id = p.id
                WHERE pm.user_id = ?
                ORDER BY pm.joined_at DESC
                LIMIT 1
                """, (rs, rowNum) -> mapParty(rs), userId).stream().findFirst();
    }

    /**
     * Sucht eine Gruppe anhand ihrer Kennung.
     *
     * @param partyId die Kennung der Gruppe
     * @return die Gruppe oder ein leeres {@link Optional}, falls nicht
     *         vorhanden
     */
    public Optional<PartyDto> findById(UUID partyId) {
        return jdbcTemplate.query("""
                SELECT id, name, leader_id FROM parties WHERE id = ?
                """, (rs, rowNum) -> mapParty(rs), partyId).stream().findFirst();
    }

    /**
     * Prüft, ob ein Benutzer Mitglied einer Gruppe ist.
     *
     * @param partyId die Kennung der Gruppe
     * @param userId  die Kennung des Benutzers
     * @return {@code true}, wenn der Benutzer Mitglied ist
     */
    public boolean isMember(UUID partyId, UUID userId) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM party_members WHERE party_id = ? AND user_id = ?",
                Integer.class, partyId, userId);
        return count != null && count > 0;
    }

    /**
     * Prüft, ob ein Benutzer der Leiter einer Gruppe ist.
     *
     * @param partyId die Kennung der Gruppe
     * @param userId  die Kennung des Benutzers
     * @return {@code true}, wenn der Benutzer Leiter ist
     */
    public boolean isLeader(UUID partyId, UUID userId) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM party_members WHERE party_id = ? AND user_id = ? AND role = 'LEADER'",
                Integer.class, partyId, userId);
        return count != null && count > 0;
    }

    /**
     * Erstellt eine Einladung in eine Gruppe. Eine bereits bestehende
     * Einladung für dieselbe Person wird wieder auf „offen“ gesetzt.
     *
     * @param partyId         die Kennung der Gruppe
     * @param invitedUserId   die Kennung der eingeladenen Person
     * @param invitedByUserId die Kennung des einladenden Benutzers
     * @return die erstellte bzw. aufgefrischte Einladung
     */
    public PartyInviteDto invite(UUID partyId, UUID invitedUserId, UUID invitedByUserId) {
        return jdbcTemplate.queryForObject("""
                INSERT INTO party_invites (party_id, invited_user_id, invited_by_user_id)
                VALUES (?, ?, ?)
                ON CONFLICT (party_id, invited_user_id)
                DO UPDATE SET status = 'PENDING', invited_by_user_id = excluded.invited_by_user_id, responded_at = NULL
                RETURNING id, party_id, invited_user_id, status, created_at
                """, (rs, rowNum) -> mapInvite(rs), partyId, invitedUserId, invitedByUserId);
    }

    /**
     * Sucht eine Einladung, die an einen bestimmten Benutzer gerichtet ist.
     *
     * @param inviteId      die Kennung der Einladung
     * @param invitedUserId die Kennung der eingeladenen Person
     * @return die Einladung oder ein leeres {@link Optional}, falls nicht
     *         vorhanden
     */
    public Optional<PartyInviteDto> findInvite(UUID inviteId, UUID invitedUserId) {
        return jdbcTemplate.query("""
                SELECT id, party_id, invited_user_id, status, created_at
                FROM party_invites WHERE id = ? AND invited_user_id = ?
                """, (rs, rowNum) -> mapInvite(rs), inviteId, invitedUserId).stream().findFirst();
    }

    /**
     * Lädt alle offenen Einladungen, die an einen bestimmten Benutzer gerichtet
     * sind, neueste zuerst.
     *
     * @param invitedUserId die Kennung des eingeladenen Benutzers
     * @return die Liste der offenen Einladungen
     */
    public List<PartyInviteDto> findPendingInvitesForUser(UUID invitedUserId) {
        return jdbcTemplate.query("""
                SELECT id, party_id, invited_user_id, status, created_at
                FROM party_invites
                WHERE invited_user_id = ? AND status = 'PENDING'
                ORDER BY created_at DESC
                """, (rs, rowNum) -> mapInvite(rs), invitedUserId);
    }

    /**
     * Setzt den Status einer Einladung (z.&nbsp;B. angenommen oder abgelehnt).
     *
     * @param inviteId die Kennung der Einladung
     * @param status   der neue Status
     * @return die aktualisierte Einladung
     */
    public PartyInviteDto setInviteStatus(UUID inviteId, String status) {
        return jdbcTemplate.queryForObject("""
                UPDATE party_invites SET status = ?, responded_at = now()
                WHERE id = ?
                RETURNING id, party_id, invited_user_id, status, created_at
                """, (rs, rowNum) -> mapInvite(rs), status, inviteId);
    }

    /**
     * Fügt einen Benutzer als einfaches Mitglied zu einer Gruppe hinzu.
     *
     * @param partyId die Kennung der Gruppe
     * @param userId  die Kennung des Benutzers
     */
    public void addMember(UUID partyId, UUID userId) {
        jdbcTemplate.update("""
                INSERT INTO party_members (party_id, user_id, role)
                VALUES (?, ?, 'MEMBER')
                ON CONFLICT DO NOTHING
                """, partyId, userId);
    }

    /**
     * Lädt die letzten (höchstens 200) Chat-Nachrichten einer Gruppe,
     * älteste zuerst.
     *
     * @param partyId die Kennung der Gruppe
     * @return die Liste der Chat-Nachrichten
     */
    public List<ChatMessageDto> messages(UUID partyId) {
        return jdbcTemplate.query("""
                SELECT pm.id, pm.party_id, pm.sender_id, u.username, pm.message, pm.created_at
                FROM party_messages pm
                JOIN users u ON u.id = pm.sender_id
                WHERE pm.party_id = ?
                ORDER BY pm.created_at ASC
                LIMIT 200
                """, (rs, rowNum) -> mapMessage(rs), partyId);
    }

    /**
     * Speichert eine neue Chat-Nachricht und gibt sie inklusive Absendernamen
     * zurück.
     *
     * @param partyId  die Kennung der Gruppe
     * @param senderId die Kennung des Absenders
     * @param message  der Nachrichtentext
     * @return die gespeicherte Chat-Nachricht
     */
    public ChatMessageDto addMessage(UUID partyId, UUID senderId, String message) {
        return jdbcTemplate.queryForObject("""
                INSERT INTO party_messages (party_id, sender_id, message)
                VALUES (?, ?, ?)
                RETURNING id, party_id, sender_id, message, created_at
                """, (rs, rowNum) -> {
            String username = jdbcTemplate.queryForObject("SELECT username FROM users WHERE id = ?",
                    String.class, senderId);
            return new ChatMessageDto(
                    rs.getObject("id", UUID.class),
                    rs.getObject("party_id", UUID.class),
                    rs.getObject("sender_id", UUID.class),
                    username,
                    rs.getString("message"),
                    rs.getTimestamp("created_at").toInstant()
            );
        }, partyId, senderId, message);
    }

    /**
     * Wandelt eine Gruppen-Zeile in ein {@link PartyDto} um und lädt dabei die
     * Mitgliederliste nach.
     *
     * @param rs das Datenbankergebnis, positioniert auf der Gruppen-Zeile
     * @return die zusammengesetzte Gruppe
     * @throws SQLException bei einem Fehler beim Auslesen der Spalten
     */
    private PartyDto mapParty(ResultSet rs) throws SQLException {
        UUID partyId = rs.getObject("id", UUID.class);
        List<PartyMemberDto> members = jdbcTemplate.query("""
                SELECT pm.user_id, u.username, pm.role, pm.joined_at
                FROM party_members pm
                JOIN users u ON u.id = pm.user_id
                WHERE pm.party_id = ?
                ORDER BY pm.joined_at ASC
                """, (memberRs, rowNum) -> new PartyMemberDto(
                memberRs.getObject("user_id", UUID.class),
                memberRs.getString("username"),
                memberRs.getString("role"),
                memberRs.getTimestamp("joined_at").toInstant()
        ), partyId);
        return new PartyDto(
                partyId,
                rs.getString("name"),
                rs.getObject("leader_id", UUID.class),
                members
        );
    }

    /**
     * Wandelt eine Einladungs-Zeile in ein {@link PartyInviteDto} um und lädt
     * dabei den Gruppennamen nach.
     *
     * @param rs das Datenbankergebnis, positioniert auf der Einladungs-Zeile
     * @return die zusammengesetzte Einladung
     * @throws SQLException bei einem Fehler beim Auslesen der Spalten
     */
    private PartyInviteDto mapInvite(ResultSet rs) throws SQLException {
        UUID partyId = rs.getObject("party_id", UUID.class);
        String partyName = jdbcTemplate.queryForObject("SELECT name FROM parties WHERE id = ?", String.class, partyId);
        return new PartyInviteDto(
                rs.getObject("id", UUID.class),
                partyId,
                partyName,
                rs.getObject("invited_user_id", UUID.class),
                rs.getString("status"),
                rs.getTimestamp("created_at").toInstant()
        );
    }

    /**
     * Wandelt eine Nachrichten-Zeile in ein {@link ChatMessageDto} um.
     *
     * @param rs das Datenbankergebnis, positioniert auf der Nachrichten-Zeile
     * @return die gelesene Chat-Nachricht
     * @throws SQLException bei einem Fehler beim Auslesen der Spalten
     */
    private ChatMessageDto mapMessage(ResultSet rs) throws SQLException {
        return new ChatMessageDto(
                rs.getObject("id", UUID.class),
                rs.getObject("party_id", UUID.class),
                rs.getObject("sender_id", UUID.class),
                rs.getString("username"),
                rs.getString("message"),
                rs.getTimestamp("created_at").toInstant()
        );
    }
}
