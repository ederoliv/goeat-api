package br.com.ederoliv.goeat_api.entities;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "support_messages")
public class SupportMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(nullable = false)
    private boolean fromSupport; // true se a mensagem for do suporte, false se for do parceiro

    @ManyToOne
    @JoinColumn(name = "support_id")
    @JsonBackReference
    private Support support;

    // Construtor útil para criar mensagens rapidamente
    public SupportMessage(String content, boolean fromSupport, Support support) {
        this.content = content;
        this.fromSupport = fromSupport;
        this.support = support;
        this.createdAt = LocalDateTime.now();
    }
}