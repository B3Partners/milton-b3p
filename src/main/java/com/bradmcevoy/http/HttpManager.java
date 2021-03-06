package com.bradmcevoy.http;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class HttpManager {
    
    final OptionsHandler optionsHandler;
    final GetHandler getHandler;
    final PostHandler postHandler;
    final PropFindHandler propFindHandler;
    final MkColHandler mkColHandler;
    final MoveHandler moveFactory;
    final PutHandler putFactory;
    final DeleteHandler deleteHandler;
    final PropPatchHandler propPatchHandler;    
    final CopyHandler copyHandler;
    final HeadHandler headHandler;
    final LockHandler lockHandler;
    final UnlockHandler unlockHandler;
    
    public final Handler[] allHandlers;
    
    Map<Request.Method, Handler> methodFactoryMap = new ConcurrentHashMap<Request.Method, Handler>();
    
    final List<Filter> filters = new ArrayList<Filter>();
    final List<EventListener> eventListeners = new ArrayList<EventListener>();
    
    final ResourceFactory resourceFactory;
    
    private SessionAuthenticationHandler sessionAuthenticationHandler;
    
    public HttpManager(ResourceFactory resourceFactory) {
        if( resourceFactory == null ) throw new NullPointerException("resourceFactory cannot be null");
        this.resourceFactory = resourceFactory;
        
        optionsHandler = add( createOptionsHandler() );
        getHandler = add( createGetHandler() );
        postHandler = add( createPostHandler() );
        propFindHandler = add( createPropFindHandler() );
        mkColHandler = add( createMkColHandler() );
        moveFactory = add( createMoveFactory() );
        putFactory = add( createPutFactory() );
        deleteHandler = add( createDeleteHandler() );
        copyHandler = add( createCopyHandler() );
        headHandler = add( createHeadHandler() );
        propPatchHandler = add( createPropPatchHandler() );
        lockHandler = add(createLockHandler());
        unlockHandler = add(createUnlockHandler());
        allHandlers = new Handler[]{optionsHandler,getHandler,postHandler,propFindHandler,mkColHandler,moveFactory,putFactory,deleteHandler,propPatchHandler, lockHandler, unlockHandler};
        
        filters.add(createStandardFilter());
    }
    
    public Collection<Filter> getFilters() {
        ArrayList<Filter> col = new ArrayList<Filter>(filters);
        return col;
    }
    
    public ResourceFactory getResourceFactory() {
        return resourceFactory;
    }

    public SessionAuthenticationHandler getSessionAuthenticationHandler() {
        return sessionAuthenticationHandler;
    }

    public void setSessionAuthenticationHandler(SessionAuthenticationHandler sessionAuthenticationHandler) {
        this.sessionAuthenticationHandler = sessionAuthenticationHandler;
    }        

    /**
     * 
     * @param request
     * @return - if no SessionAuthenticationHandler has been set returns null. Otherwise,
     *  calls getSessionAuthentication on it and returns the result
     * 
     * 
     */
    public Auth getSessionAuthentication(Request request) {
        if( this.sessionAuthenticationHandler == null ) return null;
        return this.sessionAuthenticationHandler.getSessionAuthentication(request);
    }

    public String getSupportedLevels() {
        return resourceFactory.getSupportedLevels();
    }

    
    private <T extends Handler> T add(T h) {
        methodFactoryMap.put(h.method(),h);
        return h;
    }
    
    public void process(Request request, Response response) {
//        log.debug("process: " + request.getAbsoluteUrl() + "  " + request.getMethod());
        FilterChain chain = new FilterChain(this);
        chain.process(request,response);
    }
    
    
    
    protected Filter createStandardFilter() {
        return new StandardFilter();
    }
    
    
    protected OptionsHandler createOptionsHandler() {
        return new OptionsHandler(this);
    }
    
    protected GetHandler createGetHandler() {
        return new GetHandler(this);
    }
    
    protected PostHandler createPostHandler() {
        return new PostHandler(this);
    }
    
    protected DeleteHandler createDeleteHandler() {
        return new DeleteHandler(this);
    }
    
    protected PutHandler createPutFactory() {
        return new PutHandler(this);
    }
    
    protected MoveHandler createMoveFactory() {
        return new MoveHandler(this);
    }
    
    protected MkColHandler createMkColHandler() {
        return new MkColHandler(this);
    }
    
    protected PropFindHandler createPropFindHandler() {
        return new PropFindHandler(this);
    }
    
    protected CopyHandler createCopyHandler() {
        return new CopyHandler(this);
    }
    
    protected HeadHandler createHeadHandler() {
        return new HeadHandler(this);
    }

    protected PropPatchHandler createPropPatchHandler() {
        return new PropPatchHandler(this);
    }
    
    protected LockHandler createLockHandler() {
        return new LockHandler(this);
    }
    
    protected UnlockHandler createUnlockHandler() {
        return new UnlockHandler(this);
    }
    
    public static String decodeUrl(String s) {
        try {
            return URLDecoder.decode(s,"UTF-8");
        } catch (UnsupportedEncodingException ex) {
            throw new RuntimeException(ex);
        }
    }
    
    public void addFilter(int pos, Filter filter) {
        filters.add(pos,filter);
    }

    public void addEventListener(EventListener l) {
        eventListeners.add(l);
    }
    
    public void removeEventListener(EventListener l) {
        eventListeners.remove(l);
    }
    
    void onProcessResourceFinish(Request request, Response response, Resource resource, long duration) {
        for( EventListener l : eventListeners ) {
            l.onProcessResourceFinish(request, response, resource,duration);
        }
    }

    void onProcessResourceStart(Request request, Response response, Resource resource) {
        for( EventListener l : eventListeners ) {
            l.onProcessResourceStart(request, response, resource);
        }        
    }

    void onPost(Request request, Response response, Resource resource, Map<String, String> params, Map<String, FileItem> files) {
        for( EventListener l : eventListeners ) {
            l.onPost(request, response, resource, params, files);
        }   
    }

    void onGet(Request request, Response response, Resource resource, Map<String, String> params) {
        for( EventListener l : eventListeners ) {
            l.onGet(request, response, resource, params);
        }   
    }
    
}
