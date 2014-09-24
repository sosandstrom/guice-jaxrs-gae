package com.wadpam.mardao.oauth.dao;

import net.sf.mardao.dao.Mapper;
import net.sf.mardao.dao.Supplier;
import com.wadpam.mardao.oauth.domain.DFactory;

/**
 * The DFactory domain-object specific mapping methods go here.
 *
 * Generated on 2014-09-24T18:11:56.297+0200.
 * @author mardao DAO generator (net.sf.mardao.plugin.ProcessDomainMojo)
 */
public class DFactoryMapper
  implements Mapper<DFactory, String> {

  private final Supplier supplier;

  public enum Field {
    ID("id"),
    BASEURL("baseUrl"),
    CLIENTID("clientId"),
    CLIENTSECRET("clientSecret"),
    CREATEDBY("createdBy"),
    CREATEDDATE("createdDate"),
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

  public DFactoryMapper(Supplier supplier) {
    this.supplier = supplier;
  }

  @Override
  public String fromKey(Object key) {
    return supplier.toStringKey(key);
  }

  @Override
  public DFactory fromReadValue(Object value) {
    final DFactory entity = new DFactory();

    // set primary key:
    entity.setId(supplier.getString(value, Field.ID.getFieldName()));

    // set all fields:
    entity.setBaseUrl(supplier.getString(value, Field.BASEURL.getFieldName()));
    entity.setClientId(supplier.getString(value, Field.CLIENTID.getFieldName()));
    entity.setClientSecret(supplier.getString(value, Field.CLIENTSECRET.getFieldName()));
    entity.setCreatedBy(supplier.getString(value, Field.CREATEDBY.getFieldName()));
    entity.setCreatedDate(supplier.getDate(value, Field.CREATEDDATE.getFieldName()));
    entity.setUpdatedBy(supplier.getString(value, Field.UPDATEDBY.getFieldName()));
    entity.setUpdatedDate(supplier.getDate(value, Field.UPDATEDDATE.getFieldName()));
    return entity;
  }

  @Override
  public String getId(DFactory entity) {
    return entity != null ? entity.getId() : null;
  }

  @Override
  public String getKind() {
    return String.class.getSimpleName();
  }

  @Override
  public Object toKey(String id) {
    return supplier.toKey(String.class.getSimpleName(), id);
  }

  @Override
  public Object toWriteValue(DFactory entity) {
    final String id = getId(entity);
    final Object key = toKey(id);
    final Object value = supplier.createWriteValue(key);

    // set the primary key:
    supplier.setString(value, Field.ID.getFieldName(), entity.getId());

    // set all fields:
    supplier.setString(value, Field.BASEURL.getFieldName(), entity.getBaseUrl());
    supplier.setString(value, Field.CLIENTID.getFieldName(), entity.getClientId());
    supplier.setString(value, Field.CLIENTSECRET.getFieldName(), entity.getClientSecret());
    supplier.setString(value, Field.CREATEDBY.getFieldName(), entity.getCreatedBy());
    supplier.setDate(value, Field.CREATEDDATE.getFieldName(), entity.getCreatedDate());
    supplier.setString(value, Field.UPDATEDBY.getFieldName(), entity.getUpdatedBy());
    supplier.setDate(value, Field.UPDATEDDATE.getFieldName(), entity.getUpdatedDate());
    return value;
  }

}
