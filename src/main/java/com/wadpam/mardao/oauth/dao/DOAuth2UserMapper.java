package com.wadpam.mardao.oauth.dao;

import net.sf.mardao.dao.Mapper;
import net.sf.mardao.dao.Supplier;
import com.wadpam.mardao.oauth.domain.DOAuth2User;

/**
 * The DOAuth2User domain-object specific mapping methods go here.
 *
 * Generated on 2014-09-24T18:11:56.297+0200.
 * @author mardao DAO generator (net.sf.mardao.plugin.ProcessDomainMojo)
 */
public class DOAuth2UserMapper
  implements Mapper<DOAuth2User, Long> {

  private final Supplier supplier;

  public enum Field {
    ID("id"),
    CREATEDBY("createdBy"),
    CREATEDDATE("createdDate"),
    DISPLAYNAME("displayName"),
    EMAIL("email"),
    PROFILELINK("profileLink"),
    ROLES("roles"),
    THUMBNAILURL("thumbnailUrl"),
    UPDATEDBY("updatedBy"),
    UPDATEDDATE("updatedDate");

    private final String fieldName;

    Field(String fieldName) {
      this.fieldName = fieldName;
    }

    public String getFieldName() {
      return fieldName;
    }
  }

  public DOAuth2UserMapper(Supplier supplier) {
    this.supplier = supplier;
  }

  @Override
  public Long fromKey(Object key) {
    return supplier.toLongKey(key);
  }

  @Override
  public DOAuth2User fromReadValue(Object value) {
    final DOAuth2User entity = new DOAuth2User();

    // set primary key:
    entity.setId(supplier.getLong(value, Field.ID.getFieldName()));

    // set all fields:
    entity.setCreatedBy(supplier.getString(value, Field.CREATEDBY.getFieldName()));
    entity.setCreatedDate(supplier.getDate(value, Field.CREATEDDATE.getFieldName()));
    entity.setDisplayName(supplier.getString(value, Field.DISPLAYNAME.getFieldName()));
    entity.setEmail(supplier.getString(value, Field.EMAIL.getFieldName()));
    entity.setProfileLink(supplier.getString(value, Field.PROFILELINK.getFieldName()));
    entity.setRoles(supplier.getCollection(value, Field.ROLES.getFieldName()));
    entity.setThumbnailUrl(supplier.getString(value, Field.THUMBNAILURL.getFieldName()));
    entity.setUpdatedBy(supplier.getString(value, Field.UPDATEDBY.getFieldName()));
    entity.setUpdatedDate(supplier.getDate(value, Field.UPDATEDDATE.getFieldName()));
    return entity;
  }

  @Override
  public Long getId(DOAuth2User entity) {
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
  public Object toWriteValue(DOAuth2User entity) {
    final Long id = getId(entity);
    final Object key = toKey(id);
    final Object value = supplier.createWriteValue(key);

    // set the primary key:
    supplier.setLong(value, Field.ID.getFieldName(), entity.getId());

    // set all fields:
    supplier.setString(value, Field.CREATEDBY.getFieldName(), entity.getCreatedBy());
    supplier.setDate(value, Field.CREATEDDATE.getFieldName(), entity.getCreatedDate());
    supplier.setString(value, Field.DISPLAYNAME.getFieldName(), entity.getDisplayName());
    supplier.setString(value, Field.EMAIL.getFieldName(), entity.getEmail());
    supplier.setString(value, Field.PROFILELINK.getFieldName(), entity.getProfileLink());
    supplier.setCollection(value, Field.ROLES.getFieldName(), entity.getRoles());
    supplier.setString(value, Field.THUMBNAILURL.getFieldName(), entity.getThumbnailUrl());
    supplier.setString(value, Field.UPDATEDBY.getFieldName(), entity.getUpdatedBy());
    supplier.setDate(value, Field.UPDATEDDATE.getFieldName(), entity.getUpdatedDate());
    return value;
  }

}
