package io.harness.dl;

import static org.assertj.core.api.Assertions.assertThat;

import io.harness.OrchestrationMorphiaClasses;
import io.harness.app.VerificationServiceApplication;
import io.harness.mongo.HObjectFactory;
import org.junit.Test;
import org.mongodb.morphia.Morphia;
import org.mongodb.morphia.mapping.MappedClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;

public class MorphiaClassesTest {
  private static final Logger logger = LoggerFactory.getLogger(MorphiaClassesTest.class);

  @Test
  public void testSearchAndList() {
    Morphia morphia = new Morphia();
    morphia.getMapper().getOptions().setObjectFactory(new HObjectFactory());
    morphia.getMapper().getOptions().setMapSubPackages(true);
    morphia.mapPackage("software.wings");
    morphia.mapPackage("io.harness");

    Set<Class> classes = new HashSet();
    classes.addAll(VerificationServiceApplication.morphiaClasses);
    classes.add(software.wings.integration.dl.PageRequestTest.Dummy.class);
    classes.add(software.wings.integration.common.MongoDBTest.MongoEntity.class);
    classes.add(software.wings.core.queue.QueuableObject.class);

    classes.addAll(OrchestrationMorphiaClasses.classes);

    boolean success = true;
    for (MappedClass cls : morphia.getMapper().getMappedClasses()) {
      if (!classes.contains(cls.getClazz())) {
        logger.error(cls.getClazz().toString());
        success = false;
      }
    }

    assertThat(success).isTrue();
  }
}
