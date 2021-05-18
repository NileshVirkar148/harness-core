package io.harness.annotations.retry;

import static io.harness.annotations.dev.HarnessTeam.PL;

import io.harness.annotations.dev.OwnedBy;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.lang.reflect.Method;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

@Singleton
@NoArgsConstructor
@Slf4j
@OwnedBy(PL)
public class RetryOnExceptionInterceptor implements MethodInterceptor {
  @Inject MethodExecutionHelper methodExecutionHelper;

  @Override
  public Object invoke(MethodInvocation methodInvocation) throws Throwable {
    Method method = methodInvocation.getMethod();
    log.info("Retryable method invocation started: {}", method.getName());
    RetryOnException retryConfig = method.getDeclaredAnnotation(RetryOnException.class);
    int retryAttempts = retryConfig.retryCount();
    long sleepInterval = retryConfig.sleepDurationInMilliseconds();
    Class<? extends Throwable>[] retryOnExceptions = retryConfig.retryOn();
    IMethodWrapper<Object> task = new IMethodWrapper<Object>() {
      @Override
      public Object execute() throws Throwable {
        return methodInvocation.proceed();
      }
    };
    return methodExecutionHelper.execute(task, retryAttempts, sleepInterval, retryOnExceptions);
  }
}
