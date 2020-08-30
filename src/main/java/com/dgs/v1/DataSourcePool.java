package com.dgs.v1;

public interface DataSourcePool<P, I> {
    public P getPool();
    public I getConnection();
    public void healthCheck();
}


