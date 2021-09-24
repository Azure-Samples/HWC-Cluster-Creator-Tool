package com.microsoft.hdinsight;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.microsoft.hdinsight.config.HWCClusterConfig;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.Objects;

public class HWCClusterCreatorTest {

  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper(new YAMLFactory());

  @Test
  public void testParsingHWCClusterConfigYAML1() throws IOException {
    ClassLoader classLoader = getClass().getClassLoader();
    String configPath = "SampleConfig1.yaml";
    File file = new File(Objects.requireNonNull(classLoader.getResource(configPath)).getFile());
    OBJECT_MAPPER.readValue(file,
        HWCClusterConfig.class);
  }

  @Test
  public void testParsingHWCClusterConfigYAML2() throws IOException {
    ClassLoader classLoader = getClass().getClassLoader();
    String configPath = "SampleConfig2.yaml";
    File file = new File(Objects.requireNonNull(classLoader.getResource(configPath)).getFile());
    OBJECT_MAPPER.readValue(file,
      HWCClusterConfig.class);
  }
}