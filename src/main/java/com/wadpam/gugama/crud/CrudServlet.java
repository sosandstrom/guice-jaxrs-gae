/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.wadpam.gugama.crud;

import com.wadpam.gugama.oauth.web.OAuth2Filter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import net.sf.mardao.core.CursorPage;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author osandstrom
 */
public abstract class CrudServlet<T, ID extends Serializable> extends HttpServlet {
    
    public static final String CURSOR_STRING = "cursorString";
    public static final String DELETE = "DELETE";
    public static final String LOCATION = "Location";
    public static final String METHOD = "_method";
    public static final String MIME_JSON = "application/json";
    public static final String MIME_JSON_UTF8 = "application/json; charset=UTF-8";
    public static final String PAGE_SIZE = "pageSize";
    public static final String PATH_PREFIX = "path.prefix";
    public static final String POST = "POST";
    
    protected static final Logger LOGGER = LoggerFactory.getLogger(CrudServlet.class);
    public static final ObjectMapper MAPPER = new ObjectMapper();
    static {
        MAPPER.setSerializationInclusion(JsonSerialize.Inclusion.NON_NULL);
        MAPPER.getDeserializationConfig().set(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }
    
    protected static final ThreadLocal<HttpServletRequest> currentRequest = new ThreadLocal<HttpServletRequest>();
    protected static final ThreadLocal<HttpServletResponse> currentResponse = new ThreadLocal<HttpServletResponse>();
    
    protected final Class<ID> idClass;
    protected final Class<T> itemClass;
    
    /** example is /api/user */
    protected String pathPrefix = null;
    
    protected CrudServlet(Class<T> itemClass, Class<ID> idClass) {
        this.idClass = idClass;
        this.itemClass = itemClass;
    }

    protected CrudServlet(Class<T> itemClass, Class<ID> idClass, String pathPrefix) {
        this(itemClass, idClass);
        this.pathPrefix = pathPrefix;
    }

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        if (null == pathPrefix) {
            pathPrefix = config.getInitParameter(PATH_PREFIX);
        }
    }

    public static Cookie addCookie(HttpServletResponse response, String name, String value, String path, int maxAge) {
        Cookie c = new Cookie(name, value);
        c.setPath(path);
        c.setMaxAge(maxAge);
        response.addCookie(c);
        return c;
    }
    
    protected Cookie addCookie(String name, String value, String path, int maxAge) {
        return addCookie(currentResponse.get(), name, value, path, maxAge);
    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        setCurrent(request, response);
        final ID id = getId(request);
        
        if (null != id) {
            delete(request, id);
            writeResponse(response, 200, null);
        }
        else {
            writeResponse(response, 400, null);
        }
    }
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (DELETE.equals(request.getParameter(METHOD))) {
            doDelete(request, response);
            return;
        }
        if (POST.equals(request.getParameter(METHOD))) {
            doPost(request, response);
            return;
        }
        
        setCurrent(request, response);
        final ID id = getId(request);
        LOGGER.trace("GET {}", id);
        
        if (null != id) {
            T item = get(request, id);
            writeResponse(response, null != item ? 200 : 404, item);
        }
        else {
            final String pageSizeString = request.getParameter(PAGE_SIZE);
            int pageSize = 10;
            if (null != nullIfEmpty(pageSizeString)) {
                try {
                    pageSize = Integer.parseInt(pageSizeString);
                }
                catch (NumberFormatException nfe) {
                    response.sendError(400, pageSizeString);
                    return;
                }
            }
            final String cursorString = nullIfEmpty(request.getParameter(CURSOR_STRING));
            CursorPage<T> page = getPage(pageSize, cursorString);
            page.setRequestedPageSize(pageSize);
            writeResponse(response, 200, page);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (DELETE.equals(request.getParameter(METHOD))) {
            doDelete(request, response);
            return;
        }
        
        setCurrent(request, response);
        final ID id = getId(request);
        LOGGER.trace("GET {}", id);
        
        if (null != id) {
            
            final T item = parseBody(request, id);
            update(request, id, item);
            writeResponse(response, 200, null);
        }
        else {
            final T item = parseBody(request, null);
            final ID assignedId = create(request, item);
            if (null != assignedId) {
                response.setHeader(LOCATION, assignedId.toString());
            }
            writeResponse(response, 201, assignedId);
        }
    }
    
    protected ID create(HttpServletRequest request, T item) {
        LOGGER.debug("create({})", item);
        return null;
    }
    
    protected void delete(HttpServletRequest request, ID id) {
        LOGGER.debug("delete({})", id);
    }
    
    protected T get(HttpServletRequest request, ID id) {
        LOGGER.debug("get({})", id);
        return null;
    }
    
    protected CursorPage<T> getPage(int pageSize, String cursorString) {
        LOGGER.debug("getPage({},{})", pageSize, cursorString);
        CursorPage<T> page = new CursorPage<>();
        
        return page;
    }
    
    public static String getFileString(final String pathPrefix, String requestURI) {
        int beginIndex = requestURI.indexOf(pathPrefix);
        if (beginIndex < 0) {
            return null;
        }
        
        String tail = requestURI.substring(beginIndex + pathPrefix.length());
        
        if (pathPrefix.endsWith("/")) {
            return nullIfEmpty(tail);
        }
        
        if (tail.startsWith("/")) {
            return nullIfEmpty(tail.substring(1));
        }
        
        return nullIfEmpty(tail);
    }

    public ID getId(HttpServletRequest request) {
        final String fileString = getFileString(pathPrefix, request.getRequestURI());
        if (null == fileString) {
            return null;
        }
        if (Long.class.equals(idClass)) {
            if ("me".equals(fileString)) {
                return (ID) request.getAttribute(OAuth2Filter.NAME_USER_ID);
            }

            return (ID) Long.valueOf(fileString);
        }
        return (ID) fileString;
    }
    
    public static String getParameter(HttpServletRequest request, String name, String defaultValue) {
        final String value = request.getParameter(name);
        if (null == value) {
            return defaultValue;
        }
        return value;
    }
    
    public static String nullIfEmpty(String s) {
        if (null == s) {
            return null;
        }
        if (0 == s.length()) {
            return null;
        }
        return s;
    }
    
    public T parseBody(HttpServletRequest request, ID id) throws IOException {
        T item = null;
        if (POST.equals(request.getMethod())) {
            item = MAPPER.readValue(request.getInputStream(), itemClass);
        }
        else {
            final Map<String,Object> simpleMap = new HashMap<>();
            for (Iterator it = request.getParameterMap().entrySet().iterator(); it.hasNext();) {
                Entry<String,String[]> e = (Entry<String,String[]>) it.next();
                if (1 == e.getValue().length) {
                    simpleMap.put(e.getKey(), e.getValue()[0]);
                }
                else {
                    simpleMap.put(e.getKey(), e.getValue());
                }
            }
            item = MAPPER.convertValue(simpleMap, itemClass);
        }
        
        return item;
    }
    
    private void setCurrent(HttpServletRequest request, HttpServletResponse response) {
        currentRequest.set(request);
        currentResponse.set(response);
    }
    
    protected void update(HttpServletRequest request, ID id, T item) {
        LOGGER.debug("update({},{})", id, item);
        
    }
    
    public static void writeResponse(HttpServletResponse response, int statusCode, Object entity) throws IOException {
        response.setStatus(statusCode);
        if (HttpServletResponse.SC_MOVED_TEMPORARILY == statusCode && entity instanceof String) {
            response.setHeader(LOCATION, (String) entity);
        }
        else if (null != entity) {
            response.setContentType(MIME_JSON_UTF8);
            final PrintWriter writer = response.getWriter();
            MAPPER.writeValue(writer, entity);
            writer.flush();
            writer.close();
        }
    }
}
