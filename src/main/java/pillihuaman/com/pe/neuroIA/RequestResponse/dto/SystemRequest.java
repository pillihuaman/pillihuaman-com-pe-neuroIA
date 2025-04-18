package pillihuaman.com.pe.neuroIA.RequestResponse.dto;

import lombok.AllArgsConstructor;


@AllArgsConstructor
public class SystemRequest {
    private String name;
    private String id; // Asegúrate de que MongoDB esté configurado correctamente.
    private String version;
    private String timezone;
    private String contactEmail;
    private String supportPhone;

    public SystemRequest() {
    }

    public SystemRequest(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getContactEmail() {
        return contactEmail;
    }

    public void setContactEmail(String contactEmail) {
        this.contactEmail = contactEmail;
    }

    public String getSupportPhone() {
        return supportPhone;
    }

    public void setSupportPhone(String supportPhone) {
        this.supportPhone = supportPhone;
    }

    public String getTimezone() {
        return timezone;
    }

    public void setTimezone(String timezone) {
        this.timezone = timezone;
    }

    public SystemRequest(String name, String version, String timezone) {
    }
}
