package com.ycx.common.config;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class DynamicConfigManager {
    private ConcurrentHashMap<String, ServiceDefinition> serviceDefinitionMap = new ConcurrentHashMap<>();

    private ConcurrentHashMap<String, Set<ServiceInstance>> serviceInstanceMap = new ConcurrentHashMap<>();

    private ConcurrentHashMap<String, Rule> ruleMap = new ConcurrentHashMap<>();

    private DynamicConfigManager() {

    }

    private static class SingletonHolder {
        private static final DynamicConfigManager INSTANCE = new DynamicConfigManager();
    }

    /***************** 	对服务定义缓存进行操作的系列方法 	***************/

    public static DynamicConfigManager getInstance() {
        return SingletonHolder.INSTANCE;
    }

    public void putServiceDefinition(String uniqueId,
                                     ServiceDefinition serviceDefinition) {

        serviceDefinitionMap.put(uniqueId, serviceDefinition);
        ;
    }

    public ServiceDefinition getServiceDefinition(String uniqueId) {
        return serviceDefinitionMap.get(uniqueId);
    }

    public void removeServiceDefinition(String uniqueId) {
        serviceDefinitionMap.remove(uniqueId);
    }

    public ConcurrentHashMap<String, ServiceDefinition> getServiceDefinitionMap() {
        return serviceDefinitionMap;
    }

    /***************** 	对服务实例缓存进行操作的系列方法 	***************/

    public Set<ServiceInstance> getServiceInstanceByUniqueId(String uniqueId) {
        return serviceInstanceMap.get(uniqueId);
    }

    public void addServiceInstance(String uniqueId, ServiceInstance serviceInstance) {
        Set<ServiceInstance> set = serviceInstanceMap.get(uniqueId);
        set.add(serviceInstance);
    }

    public void addServiceInstance(String uniqueId, Set<ServiceInstance> serviceInstanceSet) {
        serviceInstanceMap.put(uniqueId, serviceInstanceSet);
    }

    public void updateServiceInstance(String uniqueId, ServiceInstance serviceInstance) {
        Set<ServiceInstance> set = serviceInstanceMap.get(uniqueId);
        Iterator<ServiceInstance> it = set.iterator();
        while (it.hasNext()) {
            ServiceInstance is = it.next();
            if (is.getServiceInstanceId().equals(serviceInstance.getServiceInstanceId())) {
                it.remove();
                break;
            }
        }
        set.add(serviceInstance);
    }

    public void removeServiceInstance(String uniqueId, String serviceInstanceId) {
        Set<ServiceInstance> set = serviceInstanceMap.get(uniqueId);
        Iterator<ServiceInstance> it = set.iterator();
        while (it.hasNext()) {
            ServiceInstance is = it.next();
            if (is.getServiceInstanceId().equals(serviceInstanceId)) {
                it.remove();
                break;
            }
        }
    }

    public void removeServiceInstancesByUniqueId(String uniqueId) {
        serviceInstanceMap.remove(uniqueId);
    }


    /***************** 	对规则缓存进行操作的系列方法 	***************/

    public void putRule(String ruleId, Rule rule) {
        ruleMap.put(ruleId, rule);
    }

    public void putAllRule(List<Rule> ruleList) {
        Map<String, Rule> map = ruleList.stream()
                .collect(Collectors.toMap(Rule::getId, r -> r));
        ruleMap = new ConcurrentHashMap<>(map);
    }

    public Rule getRule(String ruleId) {
        return ruleMap.get(ruleId);
    }

    public void removeRule(String ruleId) {
        ruleMap.remove(ruleId);
    }

    public ConcurrentHashMap<String, Rule> getRuleMap() {
        return ruleMap;
    }
}
