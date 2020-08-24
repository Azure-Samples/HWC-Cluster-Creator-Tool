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
  public void testParsingHWCClusterConfigYAML() throws IOException {
    ClassLoader classLoader = getClass().getClassLoader();
    String configPath = "SampleConfig.yaml";
    File file = new File(Objects.requireNonNull(classLoader.getResource(configPath)).getFile());
    OBJECT_MAPPER.readValue(file,
        HWCClusterConfig.class);
  }
}