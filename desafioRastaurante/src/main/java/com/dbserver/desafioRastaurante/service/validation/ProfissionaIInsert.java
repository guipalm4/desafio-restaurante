package com.dbserver.desafioRastaurante.service.validation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.validation.Constraint;
import javax.validation.Payload;

@Constraint(validatedBy = ProfissionalInsertValidator.class)
@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface ProfissionaIInsert {
	
	String message() default "Erro de valida��o";

	Class<?>[] groups() default {};
	
	Class<? extends Payload>[] payload() default {};

}
