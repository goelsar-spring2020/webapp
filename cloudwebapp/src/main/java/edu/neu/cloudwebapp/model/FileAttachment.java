package edu.neu.cloudwebapp.model;

import lombok.*;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FileAttachment {

    @Column(unique = true, columnDefinition = "VARCHAR(50)")
    private String fileId;
    private String fileName;
    private String url;
    private Date uploadDate;
}
