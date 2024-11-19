package digit.web.controllers;


import digit.service.PGRService;
import digit.util.ResponseInfoFactory;
import digit.web.models.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import org.egov.common.contract.response.ResponseInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestMapping;

import java.io.IOException;
import java.util.*;

import jakarta.validation.constraints.*;
import jakarta.validation.Valid;
import jakarta.servlet.http.HttpServletRequest;

import java.util.Optional;

import static digit.config.ServiceConstants.AVERAGE_RESOLUTION_TIME;
import static digit.config.ServiceConstants.COMPLAINTS_RESOLVED;

@jakarta.annotation.Generated(value = "org.egov.codegen.SpringBootCodegen", date = "2024-11-18T10:55:45.903607265+05:30[Asia/Kolkata]")
@Controller
@RequestMapping("pgr-service")
public class RequestApiController {

    private final ObjectMapper objectMapper;

    private final HttpServletRequest request;

    private final ResponseInfoFactory responseInfoFactory;
    private final PGRService pgrService;

    @Autowired
    public RequestApiController(ObjectMapper objectMapper, HttpServletRequest request, ResponseInfoFactory responseInfoFactory, PGRService pgrService) {
        this.objectMapper = objectMapper;
        this.request = request;
        this.responseInfoFactory = responseInfoFactory;
        this.pgrService = pgrService;
    }

    @RequestMapping(value = "/request/_count", method = RequestMethod.POST)
    public ResponseEntity<ServiceResponse> requestCountPost(@NotNull @Parameter(in = ParameterIn.QUERY, description = "Unique id for a tenant.", required = true, schema = @Schema()) @Valid @RequestParam(value = "tenantId", required = true) String tenantId, @Parameter(in = ParameterIn.QUERY, description = "Allows search for service type - comma separated list", schema = @Schema()) @Valid @RequestParam(value = "serviceCode", required = false) List<String> serviceCode, @Parameter(in = ParameterIn.QUERY, description = "Search by list of UUID", schema = @Schema()) @Valid @RequestParam(value = "ids", required = false) List<String> ids, @Parameter(in = ParameterIn.QUERY, description = "Search by mobile number of service requester", schema = @Schema()) @Valid @RequestParam(value = "mobileNo", required = false) String mobileNo, @Parameter(in = ParameterIn.QUERY, description = "Search by serviceRequestId of the complaint", schema = @Schema()) @Valid @RequestParam(value = "serviceRequestId", required = false) String serviceRequestId, @Parameter(in = ParameterIn.QUERY, description = "Search by list of Application Status", schema = @Schema()) @Valid @RequestParam(value = "applicationStatus", required = false) List<String> applicationStatus) {
        String accept = request.getHeader("Accept");
        if (accept != null && accept.contains("application/json")) {
            try {
                return new ResponseEntity<ServiceResponse>(objectMapper.readValue("{  \"responseInfo\" : \"{}\",  \"PGREntities\" : [ \"{}\", \"{}\" ]}", ServiceResponse.class), HttpStatus.NOT_IMPLEMENTED);
            } catch (IOException e) {
                return new ResponseEntity<ServiceResponse>(HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }

        return new ResponseEntity<ServiceResponse>(HttpStatus.NOT_IMPLEMENTED);
    }

    @RequestMapping(value = "/request/_create", method = RequestMethod.POST)
    public ResponseEntity<ServiceResponse> requestCreatePost(@Parameter(in = ParameterIn.DEFAULT, description = "Request schema.", required = true, schema = @Schema()) @Valid @RequestBody ServiceRequest body) {
            ServiceRequest enrichedReq = pgrService.create(body);
            ResponseInfo responseInfo = responseInfoFactory.createResponseInfoFromRequestInfo(body.getRequestInfo(), true);
            ServiceWrapper serviceWrapper = ServiceWrapper.builder().service(enrichedReq.getPgrEntity().getService()).workflow(enrichedReq.getPgrEntity().getWorkflow()).build();
            ServiceResponse response = ServiceResponse.builder().responseInfo(responseInfo).pgREntities(Collections.singletonList(serviceWrapper)).build();
            return new ResponseEntity<>(response, HttpStatus.OK);

    }

    @RequestMapping(value = "/request/_search", method = RequestMethod.POST)
    public ResponseEntity<ServiceResponse> requestSearchPost(
            @Valid @RequestBody RequestInfoWrapper requestInfoWrapper,
            @Valid RequestSearchCriteria requestSearchCriteria
//            @NotNull @Parameter(in = ParameterIn.QUERY, description = "Tenant ID", required = true, schema = @Schema())  @Valid @RequestParam(value = "tenantId", required = true) String tenantId,
//            @Parameter(in = ParameterIn.QUERY, description = "Service type (comma-separated)", schema = @Schema()) @Valid @RequestParam(value = "serviceCode", required = false) List<String> serviceCode,
//            @Parameter(in = ParameterIn.QUERY, description = "List of UUIDs", schema = @Schema()) @Valid @RequestParam(value = "ids", required = false) List<String> ids,
//            @Parameter(in = ParameterIn.QUERY, description = "Requester's mobile number", schema = @Schema()) @Valid @RequestParam(value = "mobileNo", required = false) String mobileNo,
//            @Parameter(in = ParameterIn.QUERY, description = "Complaint serviceRequestId", schema = @Schema()) @Valid @RequestParam(value = "serviceRequestId", required = false) String serviceRequestId,
//            @Parameter(in = ParameterIn.QUERY, description = "Application Status list", schema = @Schema()) @Valid @RequestParam(value = "applicationStatus", required = false) List<String> applicationStatus
    ) {
            try {
                String tenantId = requestSearchCriteria.getTenantId();
                ResponseInfo responseInfo = responseInfoFactory.createResponseInfoFromRequestInfo(requestInfoWrapper.getRequestInfo(), true);
                List<ServiceWrapper> serviceWrappers = pgrService.search(requestInfoWrapper.getRequestInfo(), requestSearchCriteria);
                Map<String,Integer> dynamicData = pgrService.getDynamicData(tenantId);

                int complaintsResolved = dynamicData.get(COMPLAINTS_RESOLVED);
                int averageResolutionTime = dynamicData.get(AVERAGE_RESOLUTION_TIME);
                int complaintTypes = pgrService.getComplaintTypes();

                ServiceResponse response = ServiceResponse.builder()
                        .responseInfo(responseInfo)
                        .pgREntities(serviceWrappers)
                        .complaintsResolved(complaintsResolved)
                        .averageResolutionTime(averageResolutionTime)
                        .complaintTypes(complaintTypes)
                        .build();

                return new ResponseEntity<>(response, HttpStatus.OK);
            } catch (Exception e) {
                return new ResponseEntity<ServiceResponse>(HttpStatus.INTERNAL_SERVER_ERROR);
            }

    }

    @RequestMapping(value = "/request/_update", method = RequestMethod.POST)
    public ResponseEntity<ServiceResponse> requestUpdatePost(@Parameter(in = ParameterIn.DEFAULT, description = "Request schema.", required = true, schema = @Schema()) @Valid @RequestBody ServiceRequest body) {
        ServiceRequest enrichedReq = pgrService.update(body);
        ResponseInfo responseInfo = responseInfoFactory.createResponseInfoFromRequestInfo(body.getRequestInfo(), true);
        ServiceWrapper serviceWrapper = ServiceWrapper.builder().service(enrichedReq.getPgrEntity().getService()).workflow(enrichedReq.getPgrEntity().getWorkflow()).build();
        ServiceResponse response = ServiceResponse.builder().responseInfo(responseInfo).pgREntities(Collections.singletonList(serviceWrapper)).build();
        return new ResponseEntity<>(response, HttpStatus.OK);

    }

}
