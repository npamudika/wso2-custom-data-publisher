package org.wso2.publisher.dto;

import org.wso2.carbon.apimgt.usage.publisher.dto.PublisherDTO;

/**
 * Custom Request Publisher DTO.
 */
public class CustomRequestPublisherDTO extends PublisherDTO {
    public String getResProg() {
        return resProg;
    }

    public void setResProg(String resProg) {
        this.resProg = resProg;
    }

    private String resProg;
}
