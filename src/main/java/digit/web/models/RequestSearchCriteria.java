package digit.web.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RequestSearchCriteria {
    @Schema(description = "Unique id for a tenant", required = true, minLength = 2, maxLength = 64)
    @NotNull
    @Size(min = 2, max = 64)
    private String tenantId;

    @Schema(description = "Allows search for service type - comma-separated list")
    private Set<String> serviceCode;

    @Schema(description = "Search by list of UUID")
    private Set<String> ids;

    @Schema(description = "Search by mobile number of service requester")
    private String mobileNo;

    @Schema(description = "Search by serviceRequestId of the complaint")
    private String serviceRequestId;

    @Schema(description = "Search by list of Application Status")
    private Set<String> applicationStatus;

    @JsonProperty("limit")
    private Integer limit;

    @JsonProperty("offset")
    private Integer offset;

    @JsonProperty("slaDeltaMaxLimit")
    private Long slaDeltaMaxLimit;

    @JsonProperty("slaDeltaMinLimit")
    private Long slaDeltaMinLimit;

    @JsonProperty("sortBy")
    private SortBy sortBy;

    @JsonProperty("sortOrder")
    private SortOrder sortOrder;

    public enum SortOrder {
        ASC,
        DESC
    }

    public enum SortBy {
        applicationStatus,
        serviceRequestId
    }

    public boolean isEmpty(){
        return (this.tenantId==null && this.serviceCode==null && this.mobileNo==null && this.serviceRequestId==null
                && this.applicationStatus==null && this.ids==null);
    }
}
