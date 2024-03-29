package com.dbserver.desafioRastaurante.service.validation;

import java.util.ArrayList;
import java.util.List;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.springframework.beans.factory.annotation.Autowired;

import com.dbserver.desafioRastaurante.dto.VotoDTO;
import com.dbserver.desafioRastaurante.repository.VotoRepository;
import com.dbserver.desafioRastaurante.resource.exception.FieldMessage;
import com.dbserver.desafioRastaurante.util.Validator;

public class VotoInsertValidator implements ConstraintValidator<VotoInsert, VotoDTO> {

	@Autowired
	private VotoRepository votoRepository;

	@Override
	public void initialize(VotoInsert ann) {
	}

	@Override
	public boolean isValid(VotoDTO votoDTO, ConstraintValidatorContext context) {

		List<FieldMessage> list = new ArrayList<>();

		if (!Validator.has(votoDTO.getRestauranteDTO())) {
			list.add(new FieldMessage("restaurante", "N�o informado"));
		}

		if (!Validator.has(votoDTO.getRestauranteDTO())) {
			list.add(new FieldMessage("profissional", "N�o informado"));
		}

		for (FieldMessage e : list) {
			context.disableDefaultConstraintViolation();
			context.buildConstraintViolationWithTemplate(e.getMessage()).addPropertyNode(e.getFieldName())
					.addConstraintViolation();
		}
		return list.isEmpty();
	}

}
