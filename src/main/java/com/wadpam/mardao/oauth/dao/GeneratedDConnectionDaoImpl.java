package com.wadpam.mardao.oauth.dao;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import net.sf.mardao.core.CursorPage;
import net.sf.mardao.core.filter.Filter;
import net.sf.mardao.core.geo.DLocation;
import net.sf.mardao.dao.AbstractDao;
import net.sf.mardao.dao.Supplier;
import com.wadpam.mardao.oauth.domain.DConnection;

/**
 * The DConnection domain-object specific finders and methods go in this POJO.
 * 
 * Generated on 2014-10-13T19:31:54.266+0200.
 * @author mardao DAO generator (net.sf.mardao.plugin.ProcessDomainMojo)
 */
public class GeneratedDConnectionDaoImpl
  extends AbstractDao<DConnection, java.lang.Long> {

  public GeneratedDConnectionDaoImpl(Supplier supplier) {
    super(new DConnectionMapper(supplier), supplier);
  }

// ----------------------- field finders -------------------------------
  /**
   * find-by method for unique field accessToken
   * @param accessToken the unique attribute
   * @return the unique DConnection for the specified accessToken
   */
  public DConnection findByAccessToken(Object userKeyKey, java.lang.String accessToken) {
    return queryUniqueByField(userKeyKey, DConnectionMapper.Field.ACCESSTOKEN.getFieldName(), accessToken);
  }

  /**
   * query-by method for field appArg0
   * @param appArg0 the specified attribute
   * @return an Iterable of DConnections for the specified appArg0
   */
  public Iterable<DConnection> queryByAppArg0(Object userKeyKey, java.lang.String appArg0) {
    return queryByField(userKeyKey, DConnectionMapper.Field.APPARG0.getFieldName(), appArg0);
  }

  /**
   * query-by method for field createdBy
   * @param createdBy the specified attribute
   * @return an Iterable of DConnections for the specified createdBy
   */
  public Iterable<DConnection> queryByCreatedBy(Object userKeyKey, java.lang.String createdBy) {
    return queryByField(userKeyKey, DConnectionMapper.Field.CREATEDBY.getFieldName(), createdBy);
  }

  /**
   * query-by method for field createdDate
   * @param createdDate the specified attribute
   * @return an Iterable of DConnections for the specified createdDate
   */
  public Iterable<DConnection> queryByCreatedDate(Object userKeyKey, java.util.Date createdDate) {
    return queryByField(userKeyKey, DConnectionMapper.Field.CREATEDDATE.getFieldName(), createdDate);
  }

  /**
   * query-by method for field displayName
   * @param displayName the specified attribute
   * @return an Iterable of DConnections for the specified displayName
   */
  public Iterable<DConnection> queryByDisplayName(Object userKeyKey, java.lang.String displayName) {
    return queryByField(userKeyKey, DConnectionMapper.Field.DISPLAYNAME.getFieldName(), displayName);
  }

  /**
   * query-by method for field expireTime
   * @param expireTime the specified attribute
   * @return an Iterable of DConnections for the specified expireTime
   */
  public Iterable<DConnection> queryByExpireTime(Object userKeyKey, java.util.Date expireTime) {
    return queryByField(userKeyKey, DConnectionMapper.Field.EXPIRETIME.getFieldName(), expireTime);
  }

  /**
   * query-by method for field imageUrl
   * @param imageUrl the specified attribute
   * @return an Iterable of DConnections for the specified imageUrl
   */
  public Iterable<DConnection> queryByImageUrl(Object userKeyKey, java.lang.String imageUrl) {
    return queryByField(userKeyKey, DConnectionMapper.Field.IMAGEURL.getFieldName(), imageUrl);
  }

  /**
   * query-by method for field profileUrl
   * @param profileUrl the specified attribute
   * @return an Iterable of DConnections for the specified profileUrl
   */
  public Iterable<DConnection> queryByProfileUrl(Object userKeyKey, java.lang.String profileUrl) {
    return queryByField(userKeyKey, DConnectionMapper.Field.PROFILEURL.getFieldName(), profileUrl);
  }

  /**
   * query-by method for field providerId
   * @param providerId the specified attribute
   * @return an Iterable of DConnections for the specified providerId
   */
  public Iterable<DConnection> queryByProviderId(Object userKeyKey, java.lang.String providerId) {
    return queryByField(userKeyKey, DConnectionMapper.Field.PROVIDERID.getFieldName(), providerId);
  }

  /**
   * query-by method for field providerUserId
   * @param providerUserId the specified attribute
   * @return an Iterable of DConnections for the specified providerUserId
   */
  public Iterable<DConnection> queryByProviderUserId(Object userKeyKey, java.lang.String providerUserId) {
    return queryByField(userKeyKey, DConnectionMapper.Field.PROVIDERUSERID.getFieldName(), providerUserId);
  }

  /**
   * find-by method for unique field refreshToken
   * @param refreshToken the unique attribute
   * @return the unique DConnection for the specified refreshToken
   */
  public DConnection findByRefreshToken(Object userKeyKey, java.lang.String refreshToken) {
    return queryUniqueByField(userKeyKey, DConnectionMapper.Field.REFRESHTOKEN.getFieldName(), refreshToken);
  }

  /**
   * query-by method for field secret
   * @param secret the specified attribute
   * @return an Iterable of DConnections for the specified secret
   */
  public Iterable<DConnection> queryBySecret(Object userKeyKey, java.lang.String secret) {
    return queryByField(userKeyKey, DConnectionMapper.Field.SECRET.getFieldName(), secret);
  }

  /**
   * query-by method for field updatedBy
   * @param updatedBy the specified attribute
   * @return an Iterable of DConnections for the specified updatedBy
   */
  public Iterable<DConnection> queryByUpdatedBy(Object userKeyKey, java.lang.String updatedBy) {
    return queryByField(userKeyKey, DConnectionMapper.Field.UPDATEDBY.getFieldName(), updatedBy);
  }

  /**
   * query-by method for field updatedDate
   * @param updatedDate the specified attribute
   * @return an Iterable of DConnections for the specified updatedDate
   */
  public Iterable<DConnection> queryByUpdatedDate(Object userKeyKey, java.util.Date updatedDate) {
    return queryByField(userKeyKey, DConnectionMapper.Field.UPDATEDDATE.getFieldName(), updatedDate);
  }

  /**
   * query-by method for field userRoles
   * @param userRoles the specified attribute
   * @return an Iterable of DConnections for the specified userRoles
   */
  public Iterable<DConnection> queryByUserRoles(Object userKeyKey, java.lang.String userRoles) {
    return queryByField(userKeyKey, DConnectionMapper.Field.USERROLES.getFieldName(), userRoles);
  }

}
