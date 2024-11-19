package digit.service;

import digit.config.Configuration;
import digit.repository.IdGenRepository;
import digit.util.PGRUtil;
import digit.web.models.*;
import org.egov.common.contract.idgen.IdResponse;
import org.egov.common.contract.request.RequestInfo;
//import org.springframework.stereotype.Service;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.egov.common.contract.models.Workflow;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static digit.config.ServiceConstants.*;

@org.springframework.stereotype.Service
public class EnrichmentService {

    private final PGRUtil pgrUtil;
    private final UserService userService;
    private final Configuration config;
    private final IdGenRepository idGenRepository;

    @Autowired
    public EnrichmentService(PGRUtil pgrUtil, UserService userService, Configuration config, IdGenRepository idGenRepository) {
        this.pgrUtil = pgrUtil;
        this.userService = userService;
        this.config = config;
        this.idGenRepository = idGenRepository;
    }

    public void enrichCreateRequest(ServiceRequest serviceRequest){

        RequestInfo requestInfo = serviceRequest.getRequestInfo();
        Service service = serviceRequest.getPgrEntity().getService();
        Workflow workflow = serviceRequest.getPgrEntity().getWorkflow();
        String tenantId = service.getTenantId();

        // Enrich accountId of the logged in citizen
        if(requestInfo.getUserInfo().getType().equalsIgnoreCase(USERTYPE_CITIZEN))
            serviceRequest.getPgrEntity().getService().setAccountId(requestInfo.getUserInfo().getUuid());

//        userService.callUserService(serviceRequest);


        AuditDetails auditDetails = pgrUtil.getAuditDetails(requestInfo.getUserInfo().getUuid(), service,true);

        service.setAuditDetails(auditDetails);
        service.setId(UUID.randomUUID().toString());
        service.getAddress().setId(UUID.randomUUID().toString());
        service.getAddress().setTenantId(tenantId);
//        service.setActive(true);

        if(workflow.getDocuments()!=null){
            workflow.getDocuments().forEach(document -> {
                document.setId(UUID.randomUUID().toString());
            });
        }

        if(StringUtils.isEmpty(service.getAccountId()))
            service.setAccountId(service.getCitizen().getUuid());

        List<String> customIds = getIdList(requestInfo,tenantId,config.getServiceRequestIdGenName(),config.getServiceRequestIdGenFormat(),1);

        service.setServiceRequestId(customIds.get(0));


    }

    public void enrichUpdateRequest(ServiceRequest serviceRequest){

        RequestInfo requestInfo = serviceRequest.getRequestInfo();
        Service service = serviceRequest.getPgrEntity().getService();
        AuditDetails auditDetails = pgrUtil.getAuditDetails(requestInfo.getUserInfo().getUuid(), service,false);

        service.setAuditDetails(auditDetails);

        //TODO
//        userService.callUserService(serviceRequest);
    }

    private List<String> getIdList(RequestInfo requestInfo, String tenantId, String idKey,
                                   String idformat, int count) {
        List<IdResponse> idResponses = idGenRepository.getId(requestInfo, tenantId, idKey, idformat, count).getIdResponses();

        if (CollectionUtils.isEmpty(idResponses))
            throw new CustomException("IDGEN ERROR", "No ids returned from idgen Service");

        return idResponses.stream()
                .map(IdResponse::getId).collect(Collectors.toList());
    }

    public void enrichSearchRequest(RequestInfo requestInfo, RequestSearchCriteria criteria) {
        if(criteria.isEmpty() && requestInfo.getUserInfo().getType().equalsIgnoreCase(USERTYPE_CITIZEN)){
            String citizenMobileNumber = requestInfo.getUserInfo().getUserName();
            criteria.setMobileNo(citizenMobileNumber);
        }

        String tenantId = (criteria.getTenantId()!=null) ? criteria.getTenantId() : requestInfo.getUserInfo().getTenantId();

        //TODO
//        if(criteria.getMobileNo()!=null){
//            userService.enrichUserIds(tenantId, criteria);
//        }

        if(criteria.getLimit()==null)
            criteria.setLimit(config.getDefaultLimit());

        if(criteria.getOffset()==null)
            criteria.setOffset(config.getDefaultOffset());

        if(criteria.getLimit()!=null && criteria.getLimit() > config.getMaxLimit())
            criteria.setLimit(config.getMaxLimit());
    }

}
