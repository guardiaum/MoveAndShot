package com.johnymoreira.pojo;

import java.io.Serializable;

import com.google.android.gms.maps.model.LatLng;

import android.location.Address;

/***
 * created by johnymoreira
 *
 * POJO para representação de ponto de interesse
 */
public class PontoDeInteresse implements Serializable{

	private static final long serialVersionUID = 1L;
	private int id;
	private String nomePOI;
	private String type;
	private Address endereco;
	private LatLng ponto;
	private String imgPrincipal;

    //construtor default
    public PontoDeInteresse() {
		super();
	}

	/**
	 * Construtor sobrecarregado de ponto de interesse
	 * @param id int identificador do ponto de interesse
	 * @param nomePOI String nome do ponto de interesse
	 * @param ponto {@link LatLng} coordenada geográfica do ponto de interesse
	 */
	public PontoDeInteresse(int id, String nomePOI, LatLng ponto) {
		super();
		this.id = id;
		this.nomePOI = nomePOI;
		this.ponto = ponto;
	}

	/**
	 * Construtor sobrecarregado
	 *
 	 * @param id int identificador do ponto de interesse
	 * @param nomePOI String nome do ponto de  interesse
	 * @param type String tipo do ponto de interesse
	 * @param endereco {@link Address} endereço do ponto de interesse
	 * @param imgPrincipal URI da imagem no servidor
	 */
	public PontoDeInteresse(int id, String nomePOI, String type,
			Address endereco, String imgPrincipal) {
		super();
		this.id = id;
		this.nomePOI = nomePOI;
		this.type = type;
		this.endereco = endereco;
		this.imgPrincipal = imgPrincipal;
	}

	/**
	 * Construtor sobrecarregado
	 *
	 * @param id int identificador do ponto de interesse
	 * @param nomePOI String nome do ponto de interesse
	 * @param type String tipo do ponto de interesse
	 * @param endereco {@link Address} endereço físico do ponto de interesse
	 * @param ponto {@link LatLng} coordenadas geográficas do ponto de interesse
	 * @param imgPrincipal String URI da imagem no servidor
	 */
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

	//getters and setters

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
