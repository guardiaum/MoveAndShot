package com.johnymoreira.pojo;

import android.location.Address;
import com.google.android.gms.maps.model.LatLng;
import java.io.Serializable;

/***
 * created by johnymoreira
 *
 * POJO para representação de ponto de interesse
 */
public class PontoDeInteresse implements Serializable {
    private static final long serialVersionUID = 1;
    private Address endereco;
    private int id;
    private String imgPrincipal;
    private String nomePOI;
    private LatLng ponto;
    private String type;

    /**
     * Construtor sobrecarregado de ponto de interesse
     * @param id int identificador do ponto de interesse
     * @param nomePOI String nome do ponto de interesse
     * @param ponto {@link LatLng} coordenada geográfica do ponto de interesse
     */
    public PontoDeInteresse(int id, String nomePOI, LatLng ponto) {
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
    public PontoDeInteresse(int id, String nomePOI, String type, Address endereco, String imgPrincipal) {
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
    public PontoDeInteresse(int id, String nomePOI, String type, Address endereco, LatLng ponto, String imgPrincipal) {
        this.id = id;
        this.nomePOI = nomePOI;
        this.type = type;
        this.endereco = endereco;
        this.ponto = ponto;
        this.imgPrincipal = imgPrincipal;
    }

    // getters and setters
    public int getId() {
        return this.id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNomePOI() {
        return this.nomePOI;
    }

    public void setNomePOI(String nomePOI) {
        this.nomePOI = nomePOI;
    }

    public String getType() {
        return this.type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Address getEndereco() {
        return this.endereco;
    }

    public void setEndereco(Address endereco) {
        this.endereco = endereco;
    }

    public String getImgPrincipal() {
        return this.imgPrincipal;
    }

    public void setImgPrincipal(String imgPrincipal) {
        this.imgPrincipal = imgPrincipal;
    }

    public LatLng getPonto() {
        return this.ponto;
    }

    public void setPonto(LatLng ponto) {
        this.ponto = ponto;
    }
}
