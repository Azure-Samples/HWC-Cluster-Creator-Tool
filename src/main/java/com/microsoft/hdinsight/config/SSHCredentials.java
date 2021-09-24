package com.microsoft.hdinsight.config;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(
  use = JsonTypeInfo.Id.NAME,
  include = JsonTypeInfo.As.PROPERTY,
  property = "type")
@JsonSubTypes({
  @JsonSubTypes.Type(value = SSHWithKeys.class, name = "keys"),
  @JsonSubTypes.Type(value = SSHWithPassword.class, name = "password")
})
public interface SSHCredentials {
}
