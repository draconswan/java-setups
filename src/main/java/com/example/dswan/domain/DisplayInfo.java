package com.example.dswan.domain;

import lombok.Data;

import java.io.Serializable;

@Data
public class DisplayInfo implements Serializable {
    private String infoKey;
    private String infoJson;
}
