package com.oop.web_project.annotations;


import com.oop.web_project.entities.CheckActivityTarget;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface ActivityCheckRequired {

    CheckActivityTarget checkActivityTarget();

}
