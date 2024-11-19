package digit.repository;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import digit.repository.rowmapper.PGRRowMapper;
import digit.util.PGRUtil;
import digit.web.models.RequestSearchCriteria;
import digit.web.models.Service;
import digit.web.models.ServiceWrapper;
import digit.web.models.Workflow;
import lombok.extern.slf4j.Slf4j;
import org.egov.tracer.model.CustomException;
import org.egov.tracer.model.ServiceCallException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import digit.config.ServiceConstants.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static digit.config.ServiceConstants.*;

@Repository
@Slf4j
public class ServiceRequestRepository {

    private ObjectMapper mapper;

    private RestTemplate restTemplate;

    private final PGRQueryBuilder queryBuilder;

    private final JdbcTemplate jdbcTemplate;

    private final PGRUtil util;

    private final PGRRowMapper rowMapper;


    @Autowired
    public ServiceRequestRepository(ObjectMapper mapper, RestTemplate restTemplate, PGRQueryBuilder queryBuilder, JdbcTemplate jdbcTemplate, PGRUtil util, PGRRowMapper rowMapper) {
        this.mapper = mapper;
        this.restTemplate = restTemplate;
        this.queryBuilder = queryBuilder;
        this.jdbcTemplate = jdbcTemplate;
        this.util = util;
        this.rowMapper = rowMapper;
    }


    public Object fetchResult(StringBuilder uri, Object request) {
        mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        Object response = null;
        try {
            response = restTemplate.postForObject(uri.toString(), request, Map.class);
        }catch(HttpClientErrorException e) {
            log.error(EXTERNAL_SERVICE_EXCEPTION,e);
            throw new ServiceCallException(e.getResponseBodyAsString());
        }catch(Exception e) {
            log.error(SEARCHER_SERVICE_EXCEPTION,e);
        }

        return response;
    }

    public List<Service> getServices(RequestSearchCriteria criteria) {

        String tenantId = criteria.getTenantId();
        List<Object> preparedStmtList = new ArrayList<>();
        String query = queryBuilder.getPGRSearchQuery(criteria, preparedStmtList);
        try {
            query = util.replaceSchemaPlaceholder(query, tenantId);
        } catch (Exception e) {
            throw new CustomException("PGR_UPDATE_ERROR",
                    "TenantId length is not sufficient to replace query schema in a multi state instance");
        }
        List<Service> services =  jdbcTemplate.query(query, preparedStmtList.toArray(), rowMapper);
        return services;

    }

    public List<ServiceWrapper> getServiceWrappers(RequestSearchCriteria criteria){
        List<Service> services = getServices(criteria);
        List<String> serviceRequestids = services.stream().map(Service::getServiceRequestId).collect(Collectors.toList());
        Map<String, Workflow> idToWorkflowMap = new HashMap<>();
        List<ServiceWrapper> serviceWrappers = new ArrayList<>();

        for(Service service : services){
            ServiceWrapper serviceWrapper = ServiceWrapper.builder().service(service).workflow(idToWorkflowMap.get(service.getServiceRequestId())).build();
            serviceWrappers.add(serviceWrapper);
        }
        return serviceWrappers;
    }

    public Map<String, Integer> fetchDynamicData(String tenantId) {
        List<Object> preparedStmtListCompalintsResolved = new ArrayList<>();
        String query = queryBuilder.getResolvedComplaints(tenantId,preparedStmtListCompalintsResolved );
        try {
            query = util.replaceSchemaPlaceholder(query, tenantId);
        } catch (Exception e) {
            throw new CustomException("PGR_SEARCH_ERROR",
                    "TenantId length is not sufficient to replace query schema in a multi state instance");
        }
        int complaintsResolved = jdbcTemplate.queryForObject(query,preparedStmtListCompalintsResolved.toArray(),Integer.class);

        List<Object> preparedStmtListAverageResolutionTime = new ArrayList<>();
        query = queryBuilder.getAverageResolutionTime(tenantId, preparedStmtListAverageResolutionTime);
        try {
            query = util.replaceSchemaPlaceholder(query, tenantId);
        } catch (Exception e) {
            throw new CustomException("PGR_SEARCH_ERROR",
                    "TenantId length is not sufficient to replace query schema in a multi state instance");
        }
        Integer averageResolutionTime = jdbcTemplate.queryForObject(query, preparedStmtListAverageResolutionTime.toArray(),Integer.class);

        Map<String, Integer> dynamicData = new HashMap<String,Integer>();
        dynamicData.put(COMPLAINTS_RESOLVED, complaintsResolved);
        dynamicData.put(AVERAGE_RESOLUTION_TIME, averageResolutionTime == null ? 0 : averageResolutionTime);

        return dynamicData;
    }
}