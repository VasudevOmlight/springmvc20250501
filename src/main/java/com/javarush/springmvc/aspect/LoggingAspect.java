package com.javarush.springmvc.aspect;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

import java.util.logging.Logger;

// AOP для логування дій із товарами.
// призначення – перехоплювати виконання певних методів ( методи сервісного шару, як налаштовано у pointcut)
// і виконувати додаткову логіку – логування інформації про виклик методу та його аргументи (до виконання - @Before)
// та про результат виконання (після виконання - @AfterReturning).
@Aspect
@Component
public class LoggingAspect {

    private static final Logger logger = Logger.getLogger(LoggingAspect.class.getName());

    @Before("execution(* com.javarush.springmvc.service.ProductService.*(..))")
    public void logBefore(JoinPoint joinPoint) {
        logger.info("Executing: " + joinPoint.getSignature().getName() + " with arguments: " + joinPoint.getArgs());
    }

    @AfterReturning(pointcut = "execution(* com.javarush.springmvc.service.ProductService.*(..))", returning = "result")
    public void logAfterReturning(JoinPoint joinPoint, Object result) {
        logger.info("Completed: " + joinPoint.getSignature().getName() + " with result: " + result);
    }
}