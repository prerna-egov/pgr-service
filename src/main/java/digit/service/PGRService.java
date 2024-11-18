package digit.service;

import digit.config.Configuration;
import digit.kafka.Producer;
import digit.util.MdmsUtil;
import digit.validator.Validator;
import digit.web.models.ServiceRequest;
import org.springframework.stereotype.Service;

@Service
public class PGRService {

    private final MdmsUtil mdmsUtil;
    private final Validator validator;
    private final EnrichmentService enrichmentService;
    private final Producer producer;
    private final Configuration config;

    public PGRService(MdmsUtil mdmsUtil, Validator validator, EnrichmentService enrichmentService, Producer producer, Configuration config) {
        this.mdmsUtil = mdmsUtil;
        this.validator = validator;
        this.enrichmentService = enrichmentService;
        this.producer = producer;
        this.config = config;
    }

    public ServiceRequest create(ServiceRequest request){
        Object mdmsData = mdmsUtil.getServiceDefFromMdms(request);
        validator.validateCreate(request, mdmsData);
        enrichmentService.enrichCreateRequest(request);
//        workflowService.updateWorkflowStatus(request);
        producer.push(config.getCreateTopic(),request);
        return request;
    }
}
