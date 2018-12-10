package org.wso2.handlers.analytics;

import org.apache.axis2.Constants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.MessageContext;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.rest.AbstractHandler;
import org.apache.synapse.rest.RESTConstants;
import org.wso2.carbon.apimgt.gateway.APIMgtGatewayConstants;
import org.wso2.carbon.apimgt.gateway.handlers.security.AuthenticationContext;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.APIManagerAnalyticsConfiguration;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.gateway.handlers.security.APISecurityUtils;
import org.wso2.carbon.apimgt.usage.publisher.DataPublisherUtil;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;
import org.wso2.publisher.APIMgtCustomUsageDataBridgeDataPublisher;
import org.wso2.publisher.dto.CustomRequestPublisherDTO;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Custom Handler implementation.
 */
public class APIMgtCustomHandler extends AbstractHandler {
    private static final Log log = LogFactory.getLog(APIMgtCustomHandler.class);
    public static final Pattern resourcePattern = Pattern.compile("^/.+?/.+?([/?].+)$");

    protected volatile APIMgtCustomUsageDataBridgeDataPublisher publisher;

    public boolean handleRequest(MessageContext mc) {

        boolean enabled = getApiManagerAnalyticsConfiguration().isAnalyticsEnabled();
        boolean skipEventReceiverConnection = getApiManagerAnalyticsConfiguration().
                isSkipEventReceiverConnection();

        if (!enabled || skipEventReceiverConnection) {
            return true;
        }

        /*setting global analytic enabled status. Which use at by the by bam mediator in
        synapse to enable or disable destination based stat publishing*/
        mc.setProperty("isStatEnabled", Boolean.toString(enabled));

        String publisherClass = "org.wso2.publisher.APIMgtCustomUsageDataBridgeDataPublisher";
        try {
            if (publisher == null) {
                // The publisher initializes in the first request only
                synchronized (this) {
                    if (publisher == null) {
                        try {
                            log.debug("Instantiating Data Publisher");

                            APIMgtCustomUsageDataBridgeDataPublisher tempPublisher =
                                    (APIMgtCustomUsageDataBridgeDataPublisher) APIUtil.getClassForName(publisherClass).newInstance();
                            tempPublisher.init();
                            publisher = tempPublisher;
                        } catch (ClassNotFoundException e) {
                            log.error("Class not found " + publisherClass, e);
                        } catch (InstantiationException e) {
                            log.error("Error instantiating " + publisherClass, e);
                        } catch (IllegalAccessException e) {
                            log.error("Illegal access to " + publisherClass, e);
                        }
                    }
                }
            }

            AuthenticationContext authContext = APISecurityUtils.getAuthenticationContext(mc);
            String consumerKey = "";
            String username = "";
            String applicationName = "";
            String applicationId = "";
            String applicationOwner = "";
            if (authContext != null) {
                consumerKey = authContext.getConsumerKey();
                username = authContext.getUsername();
                applicationName = authContext.getApplicationName();
                applicationId = authContext.getApplicationId();
                applicationOwner = authContext.getSubscriber();
            }
            String hostName = DataPublisherUtil.getHostAddress();
            org.apache.axis2.context.MessageContext axis2MsgContext =
                    ((Axis2MessageContext) mc).getAxis2MessageContext();
            String context = (String) mc.getProperty(RESTConstants.REST_API_CONTEXT);
            String apiVersion = (String) mc.getProperty(RESTConstants.SYNAPSE_REST_API);
            String fullRequestPath = (String) mc.getProperty(RESTConstants.REST_FULL_REQUEST_PATH);
            String apiPublisher = (String) mc.getProperty(APIMgtGatewayConstants.API_PUBLISHER);

            String tenantDomain = MultitenantUtils.getTenantDomainFromRequestURL(fullRequestPath);
            if (apiPublisher == null) {
                apiPublisher = APIUtil.getAPIProviderFromRESTAPI(apiVersion, tenantDomain);
            }

            String api = APIUtil.getAPINamefromRESTAPI(apiVersion);
            String version = (String) mc.getProperty(RESTConstants.SYNAPSE_REST_API_VERSION);
            String resource = extractResource(mc);
            String resourceTemplate = (String) mc.getProperty(APIConstants.API_ELECTED_RESOURCE);

            String method = (String) (axis2MsgContext.getProperty(Constants.Configuration.HTTP_METHOD));
            String clientIp = DataPublisherUtil.getClientIp(axis2MsgContext);
            CustomRequestPublisherDTO requestPublisherDTO = new CustomRequestPublisherDTO();
            requestPublisherDTO.setConsumerKey(consumerKey);
            requestPublisherDTO.setContext(context);
            requestPublisherDTO.setApiVersion(apiVersion);
            requestPublisherDTO.setApi(api);
            requestPublisherDTO.setVersion(version);
            requestPublisherDTO.setResourcePath(resource);
            requestPublisherDTO.setResourceTemplate(resourceTemplate);
            requestPublisherDTO.setMethod(method);
            requestPublisherDTO.setUsername(username);
            requestPublisherDTO.setTenantDomain(MultitenantUtils.getTenantDomain(apiPublisher));
            requestPublisherDTO.setHostName(hostName);
            requestPublisherDTO.setApiPublisher(apiPublisher);
            requestPublisherDTO.setApplicationName(applicationName);
            requestPublisherDTO.setApplicationId(applicationId);
            requestPublisherDTO.setClientIp(clientIp);
            requestPublisherDTO.setApplicationOwner(applicationOwner);
            requestPublisherDTO.setResProg("RESPROG");
            publisher.publishEvent(requestPublisherDTO);
        } catch (Exception e) {
            log.error("Cannot publish event. " + e.getMessage(), e);
        }
        return true;
    }

    protected APIManagerAnalyticsConfiguration getApiManagerAnalyticsConfiguration() {
        return DataPublisherUtil.getApiManagerAnalyticsConfiguration();
    }

    public boolean handleResponse(MessageContext mc) {
        return true;
    }

    private String extractResource(MessageContext mc) {
        String resource = "/";
        Matcher matcher = resourcePattern.matcher((String) mc.getProperty(RESTConstants.REST_FULL_REQUEST_PATH));
        if (matcher.find()) {
            resource = matcher.group(1);
        }
        return resource;
    }
}
