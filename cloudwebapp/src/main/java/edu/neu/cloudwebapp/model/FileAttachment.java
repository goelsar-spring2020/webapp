package edu.neu.cloudwebapp.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.util.Date;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FileAttachment {

    @Column(unique = true, columnDefinition = "VARCHAR(50)", name = "file_id")
    private String id;
    private String file_name;
    private String url;
    private Date upload_date;
    @JsonIgnore
    private String md5Hash;
    @JsonIgnore
    private String filesize;
    @JsonIgnore
    private String contentType;
    @JsonIgnore
    private String hashcode;
}
