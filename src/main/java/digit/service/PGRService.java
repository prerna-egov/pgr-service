package digit.service;

import digit.config.Configuration;
import digit.kafka.Producer;
import digit.repository.ServiceRequestRepository;
import digit.util.MdmsUtil;
import digit.validator.Validator;
import digit.web.models.RequestSearchCriteria;
import digit.web.models.ServiceRequest;
import digit.web.models.ServiceWrapper;
import org.egov.common.contract.request.RequestInfo;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.*;

@Service
public class PGRService {

    private final MdmsUtil mdmsUtil;
    private final Validator validator;
    private final EnrichmentService enrichmentService;
    private final Producer producer;
    private final Configuration config;
    private final ServiceRequestRepository repository;

    public PGRService(MdmsUtil mdmsUtil, Validator validator, EnrichmentService enrichmentService, Producer producer, Configuration config, ServiceRequestRepository repository) {
        this.mdmsUtil = mdmsUtil;
        this.validator = validator;
        this.enrichmentService = enrichmentService;
        this.producer = producer;
        this.config = config;
        this.repository = repository;
    }

    public ServiceRequest create(ServiceRequest request){
        Object mdmsData = mdmsUtil.getServiceDefFromMdms(request);
        validator.validateCreate(request, mdmsData);
        enrichmentService.enrichCreateRequest(request);
//        workflowService.updateWorkflowStatus(request);
        producer.push(config.getCreateTopic(),request);
        return request;
    }

    public List<ServiceWrapper> search(RequestInfo requestInfo, RequestSearchCriteria criteria){
        validator.validateSearchParam(requestInfo, criteria);

        enrichmentService.enrichSearchRequest(requestInfo, criteria);

        if(criteria.isEmpty())
            return new ArrayList<>();

        if(criteria.getMobileNo()!=null)
            return new ArrayList<>();


        List<ServiceWrapper> serviceWrappers = repository.getServiceWrappers(criteria);

        if(CollectionUtils.isEmpty(serviceWrappers))
            return new ArrayList<>();;

            //TODO
//        userService.enrichUsers(serviceWrappers);
//        List<ServiceWrapper> enrichedServiceWrappers = workflowService.enrichWorkflow(requestInfo,serviceWrappers);
//        Map<Long, List<ServiceWrapper>> sortedWrappers = new TreeMap<>(Collections.reverseOrder());
//        for(ServiceWrapper svc : enrichedServiceWrappers){
//            if(sortedWrappers.containsKey(svc.getService().getAuditDetails().getCreatedTime())){
//                sortedWrappers.get(svc.getService().getAuditDetails().getCreatedTime()).add(svc);
//            }else{
//                List<ServiceWrapper> serviceWrapperList = new ArrayList<>();
//                serviceWrapperList.add(svc);
//                sortedWrappers.put(svc.getService().getAuditDetails().getCreatedTime(), serviceWrapperList);
//            }
//        }
//        List<ServiceWrapper> sortedServiceWrappers = new ArrayList<>();
//        for(Long createdTimeDesc : sortedWrappers.keySet()){
//            sortedServiceWrappers.addAll(sortedWrappers.get(createdTimeDesc));
//        }
        return serviceWrappers;
    }

    public Map<String, Integer> getDynamicData(String tenantId) {

        Map<String,Integer> dynamicData = repository.fetchDynamicData(tenantId);

        return dynamicData;
    }

    public int getComplaintTypes() {

        return Integer.valueOf(config.getComplaintTypes());
    }
}
