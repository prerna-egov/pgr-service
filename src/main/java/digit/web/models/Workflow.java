package digit.web.models;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import digit.web.models.Document;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.ArrayList;
import java.util.List;

import org.springframework.validation.annotation.Validated;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Data;
import lombok.Builder;

/**
 * BPA application object to capture the details of land, land owners, and address of the land.
 */
@Schema(description = "BPA application object to capture the details of land, land owners, and address of the land.")
@Validated
@jakarta.annotation.Generated(value = "org.egov.codegen.SpringBootCodegen", date = "2024-11-18T10:55:45.903607265+05:30[Asia/Kolkata]")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Workflow {
    @JsonProperty("action")

    @Size(min = 1, max = 64)
    private String action = null;

    @JsonProperty("assignes")

    private List<String> assignes = null;

    @JsonProperty("comments")

    @Size(min = 1, max = 64)
    private String comments = null;

    @JsonProperty("verificationDocuments")
    @Valid
    private List<Document> verificationDocuments = null;


    public Workflow addAssignesItem(String assignesItem) {
        if (this.assignes == null) {
            this.assignes = new ArrayList<>();
        }
        this.assignes.add(assignesItem);
        return this;
    }

    public Workflow addVarificationDocumentsItem(Document varificationDocumentsItem) {
        if (this.verificationDocuments == null) {
            this.verificationDocuments = new ArrayList<>();
        }
        this.verificationDocuments.add(varificationDocumentsItem);
        return this;
    }

}
