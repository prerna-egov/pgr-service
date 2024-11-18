package digit.web.models;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import digit.web.models.RequestHeader;
import digit.web.models.ServiceWrapper;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.validation.annotation.Validated;
import org.egov.common.contract.request.RequestInfo;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Data;
import lombok.Builder;

/**
 * Request object to fetch the report data
 */
@Schema(description = "Request object to fetch the report data")
@Validated
@jakarta.annotation.Generated(value = "org.egov.codegen.SpringBootCodegen", date = "2024-11-18T10:55:45.903607265+05:30[Asia/Kolkata]")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ServiceRequest {
    @JsonProperty("RequestInfo")
    @Valid
    private RequestInfo requestInfo = null;

    @JsonProperty("PGREntity")
    @NotNull
    @Valid
    private ServiceWrapper pgrEntity = null;


}
