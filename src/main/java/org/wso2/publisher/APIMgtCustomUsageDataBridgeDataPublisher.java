package org.wso2.publisher;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.usage.publisher.APIMgtUsageDataBridgeDataPublisher;
import org.wso2.carbon.apimgt.usage.publisher.DataPublisherUtil;
import org.wso2.carbon.databridge.agent.DataPublisher;
import org.wso2.carbon.databridge.agent.exception.DataEndpointAgentConfigurationException;
import org.wso2.carbon.databridge.agent.exception.DataEndpointAuthenticationException;
import org.wso2.carbon.databridge.agent.exception.DataEndpointConfigurationException;
import org.wso2.carbon.databridge.agent.exception.DataEndpointException;
import org.wso2.carbon.databridge.commons.exception.TransportException;
import org.wso2.publisher.dto.CustomDataBridgeRequestPublisherDTO;
import org.wso2.publisher.dto.CustomRequestPublisherDTO;

/**
 * Custom APIMgtUsageDataBridgeDataPublisher implementation.
 */
public class APIMgtCustomUsageDataBridgeDataPublisher extends APIMgtUsageDataBridgeDataPublisher {
    private static final Log log   = LogFactory.getLog(APIMgtCustomUsageDataBridgeDataPublisher.class);

    private DataPublisher dataPublisher;
    private static DataPublisher dataPublisherStatics;

    public void init(){
        try {
            if(log.isDebugEnabled()){
                log.debug("Initializing APIMgtCustomUsageDataBridgeDataPublisher");
            }

            this.dataPublisher = getDataPublisher();

        }catch (Exception e){
            log.error("Error initializing APIMgtCustomUsageDataBridgeDataPublisher", e);
        }
    }

    private static DataPublisher getDataPublisher() {

        //If a DataPublisher had not been registered for the tenant.
        if (dataPublisherStatics == null
                && DataPublisherUtil.getApiManagerAnalyticsConfiguration().getDasReceiverUrlGroups() != null) {

            String serverUser = DataPublisherUtil.getApiManagerAnalyticsConfiguration().getDasReceiverServerUser();
            String serverPassword = DataPublisherUtil.getApiManagerAnalyticsConfiguration()
                    .getDasReceiverServerPassword();
            String serverURL = DataPublisherUtil.getApiManagerAnalyticsConfiguration().getDasReceiverUrlGroups();
            String serverAuthURL = DataPublisherUtil.getApiManagerAnalyticsConfiguration()
                    .getDasReceiverAuthUrlGroups();

            try {
                //Create new DataPublisher for the tenant.
                synchronized (APIMgtCustomUsageDataBridgeDataPublisher.class) {

                    if (dataPublisherStatics == null) {
                        dataPublisherStatics = new DataPublisher(null, serverURL, serverAuthURL, serverUser,
                                serverPassword);
                    }
                }
            }  catch (DataEndpointConfigurationException e) {
                log.error("Error while creating data publisher", e);
            } catch (DataEndpointException e) {
                log.error("Error while creating data publisher", e);
            } catch (DataEndpointAgentConfigurationException e) {
                log.error("Error while creating data publisher", e);
            } catch (TransportException e) {
                log.error("Error while creating data publisher", e);
            } catch (DataEndpointAuthenticationException e) {
                log.error("Error while creating data publisher", e);
            }
        }

        return dataPublisherStatics;
    }

    public void publishEvent(CustomRequestPublisherDTO requestPublisherDTO) {
        CustomDataBridgeRequestPublisherDTO dataBridgeRequestPublisherDTO = new CustomDataBridgeRequestPublisherDTO(requestPublisherDTO);
        try {

            /*String streamID= DataPublisherUtil.getApiManagerAnalyticsConfiguration().getRequestStreamName()+":"
                    +DataPublisherUtil.getApiManagerAnalyticsConfiguration().getRequestStreamVersion();*/
            //Publish Request Data
            dataPublisher.tryPublish("org.wso2.apimgt.custom.statistics:1.0.0", System.currentTimeMillis(),
                    (Object[]) dataBridgeRequestPublisherDTO.createMetaData(), null,
                    (Object[]) dataBridgeRequestPublisherDTO.createPayload());
        } catch(Exception e){
            log.error("Error while publishing Request event", e);
        }

    }
}

