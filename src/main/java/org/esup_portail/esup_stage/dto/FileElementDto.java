package org.esup_portail.esup_stage.dto;

import lombok.Builder;
import lombok.Data;
import java.util.Date;

@Data @Builder
public class FileElementDto {
    private String id;
    private String name;
    private String path;
    private boolean isFolder;
    private Long size;
    private Date lastModified;
    private String extension;
    private String parent;
}