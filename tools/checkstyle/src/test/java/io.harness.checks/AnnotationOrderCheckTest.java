package io.harness.checks;

import com.puppycrawl.tools.checkstyle.AbstractModuleTestSupport;
import com.puppycrawl.tools.checkstyle.DefaultConfiguration;
import com.puppycrawl.tools.checkstyle.TreeWalker;
import org.junit.Test;

import java.security.acl.Owner;

public class AnnotationOrderCheckTest extends AbstractModuleTestSupport {
  @Override
  protected String getPackageLocation() {
    return "io.harness.checks";
  }

  public DefaultConfiguration config() {
    DefaultConfiguration config = createModuleConfig(AnnotationOrderCheck.class);

    DefaultConfiguration twConf = createModuleConfig(TreeWalker.class);
    twConf.addChild(config);
    twConf.addAttribute("fileExtensions", "jv");

    return twConf;
  }

  @Test
  public void testIssues() throws Exception {
    final String[] expected = {"7:1: Annotation Value must be placed before annotation Builder.",
        "12:1: Annotation Data must be placed before annotation Builder.",
        "17:1: Annotation Data must be specified before any modifier.",
        "22:1: Annotation RandomNonTrackedAnnotation must be specified before any modifier.",
        "28:13: Annotation SchemaIgnore is incompatible with annotation Getter. "
            + "Lombok is not propagating the field annotations when creating getter methods. "
            + "This will result of @SchemaIgnore annotation to be noop. You cannot use them on the same field.",
        "33:3: Annotation Ignore is missing required predecesor Owner."};

    verify(config(), getPath("AnnotationOrderCheckIssues.jv"), expected);
  }

  @Test
  public void testFalsePositive() throws Exception {
    //    final String[] expected = {};
    //    verify(config(), getPath("WingsExceptionCheckNonIssues.jv"), expected);
  }
}