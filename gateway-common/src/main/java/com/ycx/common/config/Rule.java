package com.ycx.common.config;

import lombok.Data;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Data
public class Rule implements Comparable<Rule> {


    private String id;

    private String name;

    private String protocol;

    private Integer order;

    private Set<FilterConfig> filterConfigs = new HashSet<>();

    public Rule() {
    }

    public Rule(String id, String name, String protocol, Integer order, Set<FilterConfig> filterConfigs) {
        this.id = id;
        this.name = name;
        this.protocol = protocol;
        this.order = order;
        this.filterConfigs = filterConfigs;
    }

    public boolean addFilterConfig(FilterConfig filterConfig) {
        return filterConfigs.add(filterConfig);
    }

    public FilterConfig getFilterConfig(String id) {
        for (FilterConfig filterConfig : filterConfigs) {
            if (filterConfig.getId().equalsIgnoreCase(id)) {
                return filterConfig;
            }
        }
        return null;
    }

    public boolean hasId(String id) {
        for (FilterConfig filterConfig : filterConfigs) {
            if (filterConfig.getId().equalsIgnoreCase(id)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public int compareTo(Rule o) {
        int compare = Integer.compare(getOrder(), o.getOrder());
        if (compare == 0) {
            return getId().compareTo(o.getId());
        }
        return compare;
    }

    @Override
    public boolean equals(Object object) {
        if (object == null || getClass() != object.getClass()) return false;
        Rule rule = (Rule) object;
        return Objects.equals(id, rule.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Data
    public static class FilterConfig {
        private String id;
        private String config;

        @Override
        public boolean equals(Object object) {
            if (this == object) return true;
            if (object == null || getClass() != object.getClass()) return false;
            FilterConfig that = (FilterConfig) object;
            return Objects.equals(id, that.id);
        }

        @Override
        public int hashCode() {
            return Objects.hash(id);
        }
    }
}
