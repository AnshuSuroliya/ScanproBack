package com.mavericks.scanpro.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Set;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name="users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private  Long id;

    @Column(nullable = false)
    private String fullname;

    @Column(nullable = false,unique = true)
    private String email;

    @Column(nullable = false)
    @JsonIgnore
    private Boolean emailVerified;

    @Column(nullable = false)
    @JsonIgnore
    private String password;

    @Column(nullable = false)
    private String role;

    @OneToOne(mappedBy = "user")
    @JsonIgnore
    private VerificationTokens VerificationToken;

    @OneToOne(mappedBy = "user")
    @JsonIgnore
    private ResetTokens ResetToken;

    @OneToOne
    @JsonIgnore
    private GitCreds gitCreds;


    @ManyToMany
    @JsonIgnore
    @JoinTable(
            name = "user_repo",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "repo_id"))
    Set<Repository> repoaccess;

}
