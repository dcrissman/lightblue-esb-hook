package com.redhat.lightblue.hook.publish;

import java.util.List;
import java.util.Map;

import com.redhat.lightblue.hook.publish.model.Trigger;
import com.redhat.lightblue.metadata.HookConfiguration;

public class PublishHookConfiguration implements HookConfiguration {

    private static final long serialVersionUID = -2297815875083279355L;

    private final Map<String, List<Trigger>> triggers;

    public Map<String, List<Trigger>> getTriggers() {
        return triggers;
    }

    public PublishHookConfiguration(Map<String, List<Trigger>> triggers) {
        this.triggers = triggers;
    }

}
