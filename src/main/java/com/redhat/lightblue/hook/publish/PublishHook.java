package com.redhat.lightblue.hook.publish;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.redhat.lightblue.DataError;
import com.redhat.lightblue.Response;
import com.redhat.lightblue.config.LightblueFactory;
import com.redhat.lightblue.config.LightblueFactoryAware;
import com.redhat.lightblue.crud.InsertionRequest;
import com.redhat.lightblue.hook.publish.model.Event;
import com.redhat.lightblue.hook.publish.model.EventIdentity;
import com.redhat.lightblue.hook.publish.model.Trigger;
import com.redhat.lightblue.hooks.CRUDHook;
import com.redhat.lightblue.hooks.HookDoc;
import com.redhat.lightblue.metadata.EntityMetadata;
import com.redhat.lightblue.metadata.Field;
import com.redhat.lightblue.metadata.HookConfiguration;
import com.redhat.lightblue.query.QueryExpression;
import com.redhat.lightblue.util.Error;
import com.redhat.lightblue.util.Path;

public class PublishHook implements CRUDHook, LightblueFactoryAware {

    private final Logger LOGGER = LoggerFactory.getLogger(PublishHook.class);

    public static final String HOOK_NAME = "publishHook";
    public static final String ENTITY_NAME = "publish";
    public static final String ERR_MISSING_ID = HOOK_NAME + ":MissingID";

    private LightblueFactory lightblueFactory;

    @Override
    public String getName() {
        return HOOK_NAME;
    }

    @Override
    public void setLightblueFactory(LightblueFactory factory) {
        this.lightblueFactory = factory;
    }

    @Override
    public void processHook(EntityMetadata entityMetadata, HookConfiguration hookConfiguration, List<HookDoc> docs) {
        if (!(hookConfiguration instanceof PublishHookConfiguration)) {
            throw new IllegalArgumentException("Only instances of PublishHookConfiguration are supported.");
        }

        PublishHookConfiguration publishHookConfiguration = (PublishHookConfiguration) hookConfiguration;
        Map<String, List<Trigger>> operTriggers = publishHookConfiguration.getTriggers();

        for (HookDoc doc : docs) {
            Event event = new Event();

            event.setEntityName(doc.getEntityMetadata().getName());
            event.setVersionText(doc.getEntityMetadata().getVersion().getValue());

            event.setCreatedBy(HOOK_NAME);
            event.setCreationDate(new Date());

            event.setLastUpdatedBy(HOOK_NAME);
            event.setLastUpdateDate(new Date());

            event.setCRUDOperation(doc.getCRUDOperation().toString());
            List<Trigger> triggers = operTriggers.get(event.getCRUDOperation());

            //If triggers is null, then assume all operations trigger an event.
            if (triggers != null) {
                for (Trigger trigger : triggers) {
                    QueryExpression query = trigger.getQuery();
                    //TODO query json document
                    doc.getPreDoc();
                    doc.getPostDoc();
                }
            }

            for (Field f : doc.getEntityMetadata().getEntitySchema().getIdentityFields()) {
                Path p = f.getFullPath();
                JsonNode node = null;

                if (doc.getPreDoc() != null) {
                    node = doc.getPreDoc().get(p);
                }
                else if (doc.getPostDoc() != null) {
                    node = doc.getPostDoc().get(p);
                }
                else {
                    throw Error.get(ERR_MISSING_ID, "path:" + p.toString());
                }

                EventIdentity identity = new EventIdentity();
                identity.setFieldText(p.toString());
                identity.setValueText(node.asText());
                event.addIdentities(identity);
            }

            try {
                insert(ENTITY_NAME, event);
            } catch (ClassNotFoundException | IllegalAccessException | InvocationTargetException | NoSuchMethodException | InstantiationException | IOException e) {
                //TODO Better Handle Exception
                LOGGER.error("Unexpected error", e);
            }
        }
    }

    private void insert(String entityName, Object entity) throws ClassNotFoundException, IllegalAccessException, InvocationTargetException, IOException, NoSuchMethodException, InstantiationException {
        ObjectNode jsonNode = new ObjectNode(JsonNodeFactory.instance);
        jsonNode.put("entity", entityName);
        ArrayNode data = jsonNode.putArray("data");
        data.add(new ObjectMapper().valueToTree(entity));

        InsertionRequest ireq = InsertionRequest.fromJson(jsonNode);
        Response r = lightblueFactory.getMediator().insert(ireq);
        if (!r.getErrors().isEmpty()) {
            //TODO Better Handle Exception
            for (Error e : r.getErrors()) {
                LOGGER.error(e.toString());
            }
        }
        else if (!r.getDataErrors().isEmpty()) {
            //TODO Better Handle Exception
            for (DataError e : r.getDataErrors()) {
                LOGGER.error(e.toString());
            }
        }
    }

}
