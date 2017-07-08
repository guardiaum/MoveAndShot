package com.johnymoreira.pojo;

import java.io.Serializable;

import com.google.android.gms.maps.model.LatLng;

import android.location.Address;

public class PontoDeInteresse implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private int id;
	private String nomePOI;
	private String type;
	private Address endereco;
	private LatLng ponto;
	private String imgPrincipal;
	
	public PontoDeInteresse() {
		super();
	}

	public PontoDeInteresse(int id, String nomePOI, String type,
			Address endereco, String imgPrincipal) {
		super();
		this.id = id;
		this.nomePOI = nomePOI;
		this.type = type;
		this.endereco = endereco;
		this.imgPrincipal = imgPrincipal;
	}
	
	public PontoDeInteresse(int id, String nomePOI, String type,
			Address endereco, LatLng ponto, String imgPrincipal) {
		super();
		this.id = id;
		this.nomePOI = nomePOI;
		this.type = type;
		this.endereco = endereco;
		this.ponto = ponto;
		this.imgPrincipal = imgPrincipal;
	}
	public PontoDeInteresse(int id, String nomePOI, LatLng ponto) {
		super();
		this.id = id;
		this.nomePOI = nomePOI;
		this.ponto = ponto;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getNomePOI() {
		return nomePOI;
	}

	public void setNomePOI(String nomePOI) {
		this.nomePOI = nomePOI;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public Address getEndereco() {
		return endereco;
	}

	public void setEndereco(Address endereco) {
		this.endereco = endereco;
	}

	public String getImgPrincipal() {
		return imgPrincipal;
	}

	public void setImgPrincipal(String imgPrincipal) {
		this.imgPrincipal = imgPrincipal;
	}

	public LatLng getPonto() {
		return ponto;
	}

	public void setPonto(LatLng ponto) {
		this.ponto = ponto;
	}
	
}
