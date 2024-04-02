package com.mavericks.scanpro.requests;

import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Getter@Setter
public class UpdateRepoAccess {
    String name ="";
    Set<String> emaillist;
}
