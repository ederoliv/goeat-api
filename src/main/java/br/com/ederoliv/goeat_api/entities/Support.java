package br.com.ederoliv.goeat_api.entities;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "supports")
public class Support {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SupportStatus status = SupportStatus.ABERTO;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;

    @OneToMany(mappedBy = "support", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<SupportMessage> messages = new ArrayList<>();

    @ManyToOne
    @JoinColumn(name = "partner_id")
    @JsonBackReference
    private Partner partner;


    public void addMessage(String content, boolean fromSupport) {
        SupportMessage message = new SupportMessage(content, fromSupport, this);
        this.messages.add(message);
        this.updatedAt = LocalDateTime.now();
    }
}