package com.dbserver.desafioRastaurante.entities;

import java.io.Serializable;
import java.util.Date;
import java.util.Objects;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import com.fasterxml.jackson.annotation.JsonFormat;

@Entity
public class Voto implements Serializable {

	private static final long serialVersionUID = -7322451342738257844L;

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Integer id;
	
	@ManyToOne
	private Restaurante restaurante;
	
	@ManyToOne
	@JoinColumn(name = "ID_PROFISSIONAL")
	private Profissional profissional;	

	@JsonFormat(pattern="dd/MM/yyyy")
	private Date dataVoto;

	public Voto(Integer id, Restaurante restaurante, Profissional profissional, Date dataVoto) {
		this.id = id;
		this.restaurante = restaurante;
		this.profissional = profissional;
		this.dataVoto = dataVoto;
	}

	public Voto() {
	
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Restaurante getRestaurante() {
		return restaurante;
	}

	public void setRestaurante(Restaurante restaurante) {
		this.restaurante = restaurante;
	}

	public Profissional getProfissional() {
		return profissional;
	}

	public void setProfissional(Profissional profissional) {
		this.profissional = profissional;
	}

	public Date getDataVoto() {
		return dataVoto;
	}

	public void setDataVoto(Date dataVoto) {
		this.dataVoto = dataVoto;
	}
	
	public Integer getIdRestauranteVoto(){
		return this.restaurante.getId();
	}
	

	@Override
	public int hashCode() {
		return Objects.hash(dataVoto, id, profissional, restaurante);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Voto other = (Voto) obj;
		return Objects.equals(dataVoto, other.dataVoto) && Objects.equals(id, other.id)
				&& Objects.equals(profissional, other.profissional) && Objects.equals(restaurante, other.restaurante);
	}
}
