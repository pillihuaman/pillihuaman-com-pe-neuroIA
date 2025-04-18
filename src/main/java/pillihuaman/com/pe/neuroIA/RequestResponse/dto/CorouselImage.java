package pillihuaman.com.pe.neuroIA.RequestResponse.dto;


import pillihuaman.com.pe.neuroIA.RequestResponse.RespProduct;

public class CorouselImage {
    private String imageSrc;
    private String imageAlt;

    private String imageCountainerToken;
    private String imagetoken;
    private String firstObject;
    private int index;
    private boolean indicators;
    private  String idDetail;
    private RespProduct respProduct;

    public RespProduct getRespProduct() {
        return respProduct;
    }

    public void setRespProduct(RespProduct respProduct) {
        respProduct = respProduct;
    }

    public String getIdDetail() {
        return idDetail;
    }

    public void setIdDetail(String idDetail) {
        this.idDetail = idDetail;
    }

    public boolean isIndicators() {
        return indicators;
    }

    public void setIndicators(boolean indicators) {
        this.indicators = indicators;
    }

    public String getImageSrc() {
        return imageSrc;
    }

    public void setImageSrc(String imageSrc) {
        this.imageSrc = imageSrc;
    }

    public String getImageAlt() {
        return imageAlt;
    }

    public void setImageAlt(String imageAlt) {
        this.imageAlt = imageAlt;
    }

    public String getImageCountainerToken() {
        return imageCountainerToken;
    }

    public void setImageCountainerToken(String imageCountainerToken) {
        this.imageCountainerToken = imageCountainerToken;
    }

    public String getImagetoken() {
        return imagetoken;
    }

    public void setImagetoken(String imagetoken) {
        this.imagetoken = imagetoken;
    }

    public String getFirstObject() {
        return firstObject;
    }

    public void setFirstObject(String firstObject) {
        this.firstObject = firstObject;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }
}
