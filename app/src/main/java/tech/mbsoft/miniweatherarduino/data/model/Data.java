package tech.mbsoft.miniweatherarduino.data.model;


import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import androidx.annotation.NonNull;

public class Data {

    @SerializedName("soil_temperature")
    @Expose
    private String soilTemperature;
    @SerializedName("Temperature")
    @Expose
    private String temperature;
    @SerializedName("pressure")
    @Expose
    private String pressure;
    @SerializedName("Altitude")
    @Expose
    private String altitude;
    @SerializedName("Humadity")
    @Expose
    private String humadity;
    @SerializedName("Moisture")
    @Expose
    private String moisture;
    @SerializedName("Gas")
    @Expose
    private String gas;
    @SerializedName("Atmosphere")
    @Expose
    private String atmosphere;
    @SerializedName("uv_index")
    @Expose
    private String uvIndex;

    public String getSoilTemperature() {
        return soilTemperature;
    }

    public void setSoilTemperature(String soilTemperature) {
        this.soilTemperature = soilTemperature;
    }

    public String getTemperature() {
        return temperature;
    }

    public void setTemperature(String temperature) {
        this.temperature = temperature;
    }

    public String getPressure() {
        return pressure;
    }

    public void setPressure(String pressure) {
        this.pressure = pressure;
    }

    public String getAltitude() {
        return altitude;
    }

    public void setAltitude(String altitude) {
        this.altitude = altitude;
    }

    public String getHumadity() {
        return humadity;
    }

    public void setHumadity(String humadity) {
        this.humadity = humadity;
    }

    public String getMoisture() {
        return moisture;
    }

    public void setMoisture(String moisture) {
        this.moisture = moisture;
    }

    public String getGas() {
        return gas;
    }

    public void setGas(String gas) {
        this.gas = gas;
    }

    public String getAtmosphere() {
        return atmosphere;
    }

    public void setAtmosphere(String atmosphere) {
        this.atmosphere = atmosphere;
    }

    public String getUvIndex() {
        return uvIndex;
    }

    public void setUvIndex(String uvIndex) {
        this.uvIndex = uvIndex;
    }

    @NonNull
    @Override
    public String toString() {
        return " Altitude:"+getAltitude()
                +" Atomosphere:" + getAtmosphere()
                +" Gas:"+getGas()
                +" Humadity:"+getHumadity()
                +" Moisture:"+getMoisture()
                +" Pressure:"+getPressure()
                +" SoilTemperature:"+getSoilTemperature()
                +" Temperature:"+getTemperature()
                +" UvIndex:"+getUvIndex();
    }
}

