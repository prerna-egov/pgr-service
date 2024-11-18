package digit.web.models;

import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import digit.web.models.Service;
import digit.web.models.Workflow;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.validation.annotation.Validated;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Data;
import lombok.Builder;

/**
 * Top level wrapper object containing the Service and Workflow objects
 */
@Schema(description = "Top level wrapper object containing the Service and Workflow objects")
@Validated
@jakarta.annotation.Generated(value = "org.egov.codegen.SpringBootCodegen", date = "2024-11-18T10:55:45.903607265+05:30[Asia/Kolkata]")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PGREntity   {
        @JsonProperty("service")
          @NotNull

          @Valid
                private Service service = null;

        @JsonProperty("workflow")

          @Valid
                private Workflow workflow = null;


}
