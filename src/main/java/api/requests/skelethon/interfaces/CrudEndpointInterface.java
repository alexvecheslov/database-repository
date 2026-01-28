package api.requests.skelethon.interfaces;

import api.models.BaseModel;

import io.restassured.response.ValidatableResponse;

public interface CrudEndpointInterface {
    Object post(BaseModel model);
    Object get(long id);
    Object update(long id, BaseModel model);
    Object delete(long id);
}
