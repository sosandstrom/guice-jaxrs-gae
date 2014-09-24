package com.wadpam.mardao.oauth.dao;

import net.sf.mardao.dao.Mapper;
import net.sf.mardao.dao.Supplier;
import com.wadpam.mardao.oauth.domain.DConnection;

/**
 * The DConnection domain-object specific mapping methods go here.
 *
 * Generated on 2014-09-24T18:11:56.297+0200.
 * @author mardao DAO generator (net.sf.mardao.plugin.ProcessDomainMojo)
 */
public class DConnectionMapper
  implements Mapper<DConnection, Long> {

  private final Supplier supplier;

  public enum Field {
    ID("id"),
    ACCESSTOKEN("accessToken"),
    APPARG0("appArg0"),
    CREATEDBY("createdBy"),
    CREATEDDATE("createdDate"),
    DISPLAYNAME("displayName"),
    EXPIRETIME("expireTime"),
    IMAGEURL("imageUrl"),
    PROFILEURL("profileUrl"),
    PROVIDERID("providerId"),
    PROVIDERUSERID("providerUserId"),
    REFRESHTOKEN("refreshToken"),
    SECRET("secret"),
    UPDATEDBY("updatedBy"),
    UPDATEDDATE("updatedDate"),
    USERROLES("userRoles");

    private final String fieldName;

    Field(String fieldName) {
      this.fieldName = fieldName;
    }

    public String getFieldName() {
      return fieldName;
    }
  }

  public DConnectionMapper(Supplier supplier) {
    this.supplier = supplier;
  }

  @Override
  public Long fromKey(Object key) {
    return supplier.toLongKey(key);
  }

  @Override
  public DConnection fromReadValue(Object value) {
    final DConnection entity = new DConnection();

    // set primary key:
    entity.setId(supplier.getLong(value, Field.ID.getFieldName()));

    // set all fields:
    entity.setAccessToken(supplier.getString(value, Field.ACCESSTOKEN.getFieldName()));
    entity.setAppArg0(supplier.getString(value, Field.APPARG0.getFieldName()));
    entity.setCreatedBy(supplier.getString(value, Field.CREATEDBY.getFieldName()));
    entity.setCreatedDate(supplier.getDate(value, Field.CREATEDDATE.getFieldName()));
    entity.setDisplayName(supplier.getString(value, Field.DISPLAYNAME.getFieldName()));
    entity.setExpireTime(supplier.getDate(value, Field.EXPIRETIME.getFieldName()));
    entity.setImageUrl(supplier.getString(value, Field.IMAGEURL.getFieldName()));
    entity.setProfileUrl(supplier.getString(value, Field.PROFILEURL.getFieldName()));
    entity.setProviderId(supplier.getString(value, Field.PROVIDERID.getFieldName()));
    entity.setProviderUserId(supplier.getString(value, Field.PROVIDERUSERID.getFieldName()));
    entity.setRefreshToken(supplier.getString(value, Field.REFRESHTOKEN.getFieldName()));
    entity.setSecret(supplier.getString(value, Field.SECRET.getFieldName()));
    entity.setUpdatedBy(supplier.getString(value, Field.UPDATEDBY.getFieldName()));
    entity.setUpdatedDate(supplier.getDate(value, Field.UPDATEDDATE.getFieldName()));
    entity.setUserRoles(supplier.getString(value, Field.USERROLES.getFieldName()));
    return entity;
  }

  @Override
  public Long getId(DConnection entity) {
    return entity != null ? entity.getId() : null;
  }

  @Override
  public String getKind() {
    return Long.class.getSimpleName();
  }

  @Override
  public Object toKey(Long id) {
    return supplier.toKey(Long.class.getSimpleName(), id);
  }

  @Override
  public Object toWriteValue(DConnection entity) {
    final Long id = getId(entity);
    final Object key = toKey(id);
    final Object value = supplier.createWriteValue(key);

    // set the primary key:
    supplier.setLong(value, Field.ID.getFieldName(), entity.getId());

    // set all fields:
    supplier.setString(value, Field.ACCESSTOKEN.getFieldName(), entity.getAccessToken());
    supplier.setString(value, Field.APPARG0.getFieldName(), entity.getAppArg0());
    supplier.setString(value, Field.CREATEDBY.getFieldName(), entity.getCreatedBy());
    supplier.setDate(value, Field.CREATEDDATE.getFieldName(), entity.getCreatedDate());
    supplier.setString(value, Field.DISPLAYNAME.getFieldName(), entity.getDisplayName());
    supplier.setDate(value, Field.EXPIRETIME.getFieldName(), entity.getExpireTime());
    supplier.setString(value, Field.IMAGEURL.getFieldName(), entity.getImageUrl());
    supplier.setString(value, Field.PROFILEURL.getFieldName(), entity.getProfileUrl());
    supplier.setString(value, Field.PROVIDERID.getFieldName(), entity.getProviderId());
    supplier.setString(value, Field.PROVIDERUSERID.getFieldName(), entity.getProviderUserId());
    supplier.setString(value, Field.REFRESHTOKEN.getFieldName(), entity.getRefreshToken());
    supplier.setString(value, Field.SECRET.getFieldName(), entity.getSecret());
    supplier.setString(value, Field.UPDATEDBY.getFieldName(), entity.getUpdatedBy());
    supplier.setDate(value, Field.UPDATEDDATE.getFieldName(), entity.getUpdatedDate());
    supplier.setString(value, Field.USERROLES.getFieldName(), entity.getUserRoles());
    return value;
  }

}
