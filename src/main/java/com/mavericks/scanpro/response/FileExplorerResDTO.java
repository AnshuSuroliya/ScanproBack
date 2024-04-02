package com.mavericks.scanpro.response;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.HashMap;

@Setter@Getter
public class FileExplorerResDTO {
    private ArrayList<HashMap<String,String>> files ;
}
