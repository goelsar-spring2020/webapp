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
@ToString
@Table(name = "files")
public class FileAttachment {

    @Id
    @Column(nullable = false, unique = true, columnDefinition = "VARCHAR(50)")
    private String fileId;
    @Column(nullable = false)
    private String fileName;
    @Column(nullable = false)
    private String url;
    @Column(nullable = false)
    private Date uploadDate;
}
