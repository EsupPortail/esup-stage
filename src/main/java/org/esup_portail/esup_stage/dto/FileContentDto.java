package org.esup_portail.esup_stage.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
 public class FileContentDto {
     private String fileName;
     private String content;
     private long totalLines;
     private int page;
     private int pageSize;
 }