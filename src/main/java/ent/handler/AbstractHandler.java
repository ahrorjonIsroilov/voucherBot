package ent.handler;

import ent.service.BaseService;

public abstract class AbstractHandler<S extends BaseService> {

    protected final S service;

    protected AbstractHandler(S service) {
        this.service = service;
    }
}
