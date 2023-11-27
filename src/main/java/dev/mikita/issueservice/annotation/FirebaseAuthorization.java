package dev.mikita.issueservice.annotation;

import java.lang.annotation.*;

/**
 * The interface Firebase authorization.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface FirebaseAuthorization {
    /**
     * Roles string [ ].
     *
     * @return the string [ ]
     */
    String[] roles() default {};

    /**
     * Statuses string [ ].
     *
     * @return the string [ ]
     */
    String[] statuses() default {};
}