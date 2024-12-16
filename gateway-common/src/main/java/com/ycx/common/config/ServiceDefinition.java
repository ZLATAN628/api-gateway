package com.ycx.common.config;

import lombok.*;

import java.io.Serializable;
import java.util.Map;
import java.util.Objects;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ServiceDefinition implements Serializable {

    private String uniqueId;

    private String serviceId;

    private String version;

    private String protocol;

    private String patternPath;

    private String envType;

    private boolean enable = true;

    private Map<String, ServiceInvoker> invokerMap;

    @Override
    public boolean equals(Object object) {
        if (object == null || getClass() != object.getClass()) return false;
        ServiceDefinition that = (ServiceDefinition) object;
        return Objects.equals(uniqueId, that.uniqueId);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(uniqueId);
    }
}
