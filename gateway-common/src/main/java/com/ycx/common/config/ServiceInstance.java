package com.ycx.common.config;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Objects;

@Data
@NoArgsConstructor
public class ServiceInstance implements Serializable {

    private static final long serialVersionUID = -7559569289189228478L;

    protected String serviceInstanceId;

    protected String uniqueId;

    protected String ip;

    protected int port;

    protected String tags;

    protected Integer weight;

    protected long registerTime;

    protected boolean enable = true;

    protected String version;


    @Override
    public boolean equals(Object object) {
        if (object == null || getClass() != object.getClass()) return false;
        ServiceInstance that = (ServiceInstance) object;
        return Objects.equals(serviceInstanceId, that.serviceInstanceId);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(serviceInstanceId);
    }
}
