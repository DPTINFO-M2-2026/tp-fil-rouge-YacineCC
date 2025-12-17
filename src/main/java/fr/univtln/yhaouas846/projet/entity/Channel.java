package fr.univtln.yhaouas846.projet.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.time.LocalDateTime;
import java.util.Set;

@Entity
@Table(name = "channel")
public class Channel extends PanacheEntity {
    
    @NotBlank
    @Size(min = 1, max = 100)
    @Column(nullable = false, length = 100)
    public String name;
    
    @Size(max = 1024)
    @Column(length = 1024)
    public String description;
    
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    public ChannelType type;
    
    @NotNull
    @ManyToOne
    @JoinColumn(name = "guild_id", nullable = false)
    public Guild guild;
    
    @Column(name = "created_at", nullable = false)
    public LocalDateTime createdAt;
    
    @Column(name = "discord_id", unique = true)
    public String discordId;

    @Min(0)
    @Column(name = "position")
    public Integer position = 0;
    
    @Column(name = "is_nsfw")
    public boolean isNsfw = false;
    
    @OneToMany(mappedBy = "channel", cascade = CascadeType.ALL)
    @JsonIgnore
    public Set<Message> messages;
    
    @PrePersist
    void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
    
    public enum ChannelType {
        TEXT, VOICE, CATEGORY, NEWS, THREAD
    }
}