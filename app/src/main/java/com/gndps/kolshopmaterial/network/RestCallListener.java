package com.gndps.kolshopmaterial.network;

public interface RestCallListener<T> {

    public void onRestCallSuccess(T result, T requestId);

    public void onRestCallFail(T result, T requestId);
}
