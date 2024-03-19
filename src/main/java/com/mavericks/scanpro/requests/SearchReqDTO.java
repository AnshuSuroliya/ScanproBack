package com.mavericks.scanpro.requests;

import lombok.Getter;
import lombok.Setter;

@Getter@Setter
public class SearchReqDTO {
    private String orderBy ="";
    private Long pageNumber =1L;
    private String name;
    private Integer CountToFetch;
}
