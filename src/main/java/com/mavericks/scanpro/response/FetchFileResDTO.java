package com.mavericks.scanpro.response;

import com.mavericks.scanpro.entities.Address;
import com.mavericks.scanpro.entities.Scanned_files;
import lombok.Getter;
import lombok.Setter;

@Setter@Getter
public class FetchFileResDTO {
    private String Name;

    private String Sha;

    private String content;

    private Scanned_files scannedData;

    private Address address;

}
