package com.redhat.lightblue.hook.publish;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.redhat.lightblue.hook.publish.model.Trigger;
import com.redhat.lightblue.hooks.CRUDHook;
import com.redhat.lightblue.metadata.HookConfiguration;
import com.redhat.lightblue.metadata.parser.HookConfigurationParser;
import com.redhat.lightblue.metadata.parser.MetadataParser;

public class PublishHookConfigurationParser<T> implements HookConfigurationParser<T> {

    private static final String PROPERTY_TIGGERS = "triggers";
    private static final String PROPERTY_NAME = "name";
    private static final String PROPERTY_ACTION = "action";
    private static final String PROPERTY_QUERY = "query";

    @Override
    public String getName() {
        return PublishHook.HOOK_NAME;
    }

    @Override
    public CRUDHook getCRUDHook() {
        return new PublishHook();
    }

    @Override
    public void convert(MetadataParser<T> parser, T emptyNode, HookConfiguration object) {
        // TODO Auto-generated method stub

    }

    @Override
    public HookConfiguration parse(String name, MetadataParser<T> parser, T node) {
        List<T> triggersNode = parser.getObjectList(node, PROPERTY_TIGGERS);
        Map<String, List<Trigger>> triggers = new HashMap<>();
        if (triggersNode != null) {
            for (T triggerNode : triggersNode) {
                Trigger trigger = new Trigger();
                trigger.setName(parser.getRequiredStringProperty(triggerNode, PROPERTY_NAME));
                trigger.setAction(parser.getRequiredStringProperty(triggerNode, PROPERTY_ACTION));
                trigger.setQuery(parser.getQuery(triggerNode, PROPERTY_QUERY));

                if (triggers.get(trigger.getAction()) == null) {
                    triggers.put(trigger.getAction(), new ArrayList<Trigger>());
                }

                triggers.get(trigger.getAction()).add(trigger);
            }
        }

        return new PublishHookConfiguration(triggers);
    }
}
