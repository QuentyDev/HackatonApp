package gdg.hackatonapp.entity;

import com.google.gson.annotations.Expose;

import gdg.hackatonapp.entity.anotation.Ignore;

/**
 * Created by Kenji on 14/10/2015.
 */
public class User{
    @Expose
    private long id;
    @Expose
    private String nombres;
    @Expose
    private String gcm_id;
    @Expose
    private String email;
    @Expose
    private String nroTelefono;
    @Expose
    private double latitud;
    @Expose
    private double longitud;

    private String id_facebook;
    private String id_google;

    public User() {
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getNombres() {
        return nombres;
    }

    public void setNombres(String nombres) {
        this.nombres = nombres;
    }

    public String getGcm_id() {
        return gcm_id;
    }

    public void setGcm_id(String gcm_id) {
        this.gcm_id = gcm_id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getNroTelefono() {
        return nroTelefono;
    }

    public void setNroTelefono(String nroTelefono) {
        this.nroTelefono = nroTelefono;
    }

    public double getLatitud() {
        return latitud;
    }

    public void setLatitud(double latitud) {
        this.latitud = latitud;
    }

    public double getLongitud() {
        return longitud;
    }

    public void setLongitud(double longitud) {
        this.longitud = longitud;
    }

    public String getId_facebook() {
        return id_facebook;
    }

    public void setId_facebook(String id_facebook) {
        this.id_facebook = id_facebook;
    }

    public String getId_google() {
        return id_google;
    }

    public void setId_google(String id_google) {
        this.id_google = id_google;
    }
}
