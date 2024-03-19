package com.mavericks.scanpro.entities;


import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Set;

@Setter
@Getter
@NoArgsConstructor
@Entity
public class Repository {
    @Id
    private  Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = true)
    private String description;

    @Column(unique = false,nullable = false)
    private Long owner;


    @ManyToMany(mappedBy = "repoaccess")
    Set<User> AccessUser;

}
