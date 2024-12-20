package com.ycx.gateway.config.center.api;

import com.ycx.common.config.Rule;

import java.util.List;

@FunctionalInterface
public interface RulesChangeListener {
    void onRulesChange(List<Rule> rules);
}
