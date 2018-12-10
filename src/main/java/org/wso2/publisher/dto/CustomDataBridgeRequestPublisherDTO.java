package org.wso2.publisher.dto;

import org.wso2.carbon.apimgt.usage.publisher.DataPublisherUtil;

/**
 * Custom DataBridge Publisher DTO.
 */
public class CustomDataBridgeRequestPublisherDTO extends CustomRequestPublisherDTO {
    public CustomDataBridgeRequestPublisherDTO (CustomRequestPublisherDTO requestPublisherDTO){
        setConsumerKey(requestPublisherDTO.getConsumerKey());
        setContext(requestPublisherDTO.getContext());
        setApiVersion(requestPublisherDTO.getApiVersion());
        setApi(requestPublisherDTO.getApi());
        setResourcePath(requestPublisherDTO.getResourcePath());
        setResourceTemplate(requestPublisherDTO.getResourceTemplate());
        setMethod(requestPublisherDTO.getMethod());
        setVersion(requestPublisherDTO.getVersion());
        setUsername(requestPublisherDTO.getUsername());
        setTenantDomain(requestPublisherDTO.getTenantDomain());
        setHostName(DataPublisherUtil.getHostAddress());
        setApiPublisher(requestPublisherDTO.getApiPublisher());
        setApplicationName(requestPublisherDTO.getApplicationName());
        setApplicationId(requestPublisherDTO.getApplicationId());
        setClientIp(requestPublisherDTO.getClientIp());
        setApplicationOwner(requestPublisherDTO.getApplicationOwner());
        setResProg(requestPublisherDTO.getResProg());
    }

    public Object createPayload(){
        return new Object[]{getConsumerKey(), getContext(), getApiVersion(), getApi(), getResourcePath(),
                getResourceTemplate(), getMethod(), getVersion(),
                getUsername(), getTenantDomain(), getHostName(), getApiPublisher(), getApplicationName(),
                getApplicationId(), getClientIp(), getApplicationOwner(),
                getResProg()};
    }

    public Object createMetaData() {
        return null;
    }
}
