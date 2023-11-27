package dev.mikita.issueservice.aspect;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseToken;
import dev.mikita.issueservice.annotation.FirebaseAuthorization;
import jakarta.security.auth.message.AuthException;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

/**
 * The type Firebase authorization aspect.
 */
@Aspect
@Component
public class FirebaseAuthorizationAspect {
    private final FirebaseAuth firebaseAuth;

    /**
     * Instantiates a new Firebase authorization aspect.
     *
     * @param firebaseAuth the firebase auth
     */
    @Autowired
    public FirebaseAuthorizationAspect(FirebaseAuth firebaseAuth) {
        this.firebaseAuth = firebaseAuth;
    }

    /**
     * Firebase authorization pointcut.
     */
    @Pointcut("@annotation(dev.mikita.issueservice.annotation.FirebaseAuthorization)")
    public void firebaseAuthorizationPointcut() {}

    /**
     * Authorize object.
     *
     * @param joinPoint the join point
     * @return the object
     * @throws Throwable the throwable
     */
    @Around("firebaseAuthorizationPointcut()")
    public Object authorize(ProceedingJoinPoint joinPoint) throws Throwable {
        FirebaseAuthorization annotation = getFirebaseAuthorizationAnnotation(joinPoint);
        if (annotation != null) {
            String token = getTokenFromRequestContext();
            if (token != null) {
                FirebaseToken firebaseToken = firebaseAuth.verifyIdToken(token);
                List<String> roles = Arrays.asList(annotation.roles());
                List<String> statuses = Arrays.asList(annotation.statuses());

                if (!hasRole(firebaseToken, roles) || !hasStatus(firebaseToken, statuses)) {
                    throw new AuthException("Unauthorized");
                } else {
                    ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
                    assert attributes != null;
                    HttpServletRequest request = attributes.getRequest();

                    request.setAttribute("firebaseToken", firebaseToken);

                    return joinPoint.proceed();
                }
            } else {
                throw new AuthException("Unauthorized");
            }
        }

        return joinPoint.proceed();
    }

    private FirebaseAuthorization getFirebaseAuthorizationAnnotation(ProceedingJoinPoint joinPoint) {
        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        Method method = methodSignature.getMethod();
        return AnnotationUtils.findAnnotation(method, FirebaseAuthorization.class);
    }

    private String getTokenFromRequestContext() {
        ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (requestAttributes != null) {
            HttpServletRequest request = requestAttributes.getRequest();
            String authorizationHeader = request.getHeader("Authorization");
            if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
                return authorizationHeader.substring(7);
            }
        }
        return null;
    }

    private boolean hasRole(FirebaseToken firebaseToken, List<String> roles) {
        if (roles.isEmpty()) {
            return true;
        }

        String role = (String) firebaseToken.getClaims().get("role");
        return roles.contains(role);
    }

    private boolean hasStatus(FirebaseToken firebaseToken, List<String> statuses) {
        if (statuses.isEmpty()) {
            return true;
        }

        String status = (String) firebaseToken.getClaims().get("status");
        return statuses.contains(status);
    }
}