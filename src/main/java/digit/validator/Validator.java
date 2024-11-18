package digit.validator;

import com.jayway.jsonpath.JsonPath;
import digit.config.Configuration;
import digit.util.HRMSUtil;
import digit.util.MdmsUtil;
import digit.web.models.ServiceRequest;
import org.egov.common.contract.request.RequestInfo;
import org.egov.common.contract.request.User;
import org.egov.tracer.model.CustomException;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static digit.config.ServiceConstants.*;

@Component
public class Validator {

    private final MdmsUtil mdmsUtil;
    private final Configuration config;
    private final HRMSUtil hrmsUtil;

    public Validator(MdmsUtil mdmsUtil, Configuration config, HRMSUtil hrmsUtil) {
        this.mdmsUtil = mdmsUtil;
        this.config = config;
        this.hrmsUtil = hrmsUtil;
    }


    public void validateCreate(ServiceRequest request, Object mdmsData){
        Map<String,String> errorMap = new HashMap<>();
        validateUserData(request,errorMap);
        validateSource(request.getPgrEntity().getService().getSource());
        validateMDMS(request, mdmsData);
        validateDepartment(request, mdmsData);
        if(!errorMap.isEmpty())
            throw new CustomException(errorMap);
    }

    private void validateUserData(ServiceRequest request,Map<String, String> errorMap){

        RequestInfo requestInfo = request.getRequestInfo();
        String accountId = request.getPgrEntity().getService().getAccountId();


        if(requestInfo.getUserInfo().getType().equalsIgnoreCase(USERTYPE_EMPLOYEE)){
            User citizen = request.getPgrEntity().getService().getCitizen();
            if(citizen == null)
                errorMap.put("INVALID_REQUEST","Citizen object cannot be null");
            else if(citizen.getMobileNumber()==null || citizen.getName()==null)
                errorMap.put("INVALID_REQUEST","Name and Mobile Number is mandatory in citizen object");
        }

    }

    private void validateMDMS(ServiceRequest request, Object mdmsData){

        String serviceCode = request.getPgrEntity().getService().getServiceCode();
        String jsonPath = MDMS_SERVICEDEF_SEARCH.replace("{SERVICEDEF}",serviceCode);

        List<Object> res = null;

        try{
            res = JsonPath.read(mdmsData,jsonPath);
        }
        catch (Exception e){
            throw new CustomException("JSONPATH_ERROR","Failed to parse mdms response");
        }

        if(CollectionUtils.isEmpty(res))
            throw new CustomException("INVALID_SERVICECODE","The service code: "+serviceCode+" is not present in MDMS");


    }

    private void validateSource(String source){

        List<String> allowedSourceStr = Arrays.asList(config.getAllowedSource().split(","));

        if(!allowedSourceStr.contains(source))
            throw new CustomException("INVALID_SOURCE","The source: "+source+" is not valid");

    }

    private void validateDepartment(ServiceRequest request, Object mdmsData){

        String serviceCode = request.getPgrEntity().getService().getServiceCode();
        List<String> assignes = request.getPgrEntity().getWorkflow().getAssignes();

        if(CollectionUtils.isEmpty(assignes))
            return;

        List<String> departments = hrmsUtil.getDepartment(assignes, request.getRequestInfo());

        String jsonPath = MDMS_DEPARTMENT_SEARCH.replace("{SERVICEDEF}",serviceCode);

        List<String> res = null;
        String departmentFromMDMS;

        try{
            res = JsonPath.read(mdmsData,jsonPath);
        }
        catch (Exception e){
            throw new CustomException("JSONPATH_ERROR","Failed to parse mdms response for department");
        }

        if(CollectionUtils.isEmpty(res))
            throw new CustomException("PARSING_ERROR","Failed to fetch department from mdms data for serviceCode: "+serviceCode);
        else departmentFromMDMS = res.get(0);

        Map<String, String> errorMap = new HashMap<>();

        if(!departments.contains(departmentFromMDMS))
            errorMap.put("INVALID_ASSIGNMENT","The application cannot be assigned to employee of department: "+departments.toString());


        if(!errorMap.isEmpty())
            throw new CustomException(errorMap);

    }
}
