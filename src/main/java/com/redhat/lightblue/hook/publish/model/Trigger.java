package com.redhat.lightblue.hook.publish.model;

import com.redhat.lightblue.query.QueryExpression;

public class Trigger {

    private String name;
    private String action;
    private QueryExpression query;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public QueryExpression getQuery() {
        return query;
    }

    public void setQuery(QueryExpression query) {
        this.query = query;
    }

}
