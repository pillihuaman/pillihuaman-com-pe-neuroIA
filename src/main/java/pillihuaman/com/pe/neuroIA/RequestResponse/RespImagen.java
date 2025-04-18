package pillihuaman.com.pe.neuroIA.RequestResponse;
import org.bson.types.ObjectId;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Date;


public class RespImagen {
	private int idProduct;
	private Timestamp createDate;
	private String description;
	private Date expirationDate;
	private BigDecimal idImagen;
	private BigDecimal idPrice;
	private BigDecimal idSystem;
	private BigDecimal idType;
	private BigDecimal idUser;
	private String name;
	private String status;
	private Timestamp updateDate;
	private String userCreate;
	private String userModify;
	private ObjectId _id;
	public int getIdProduct() {
		return idProduct;
	}
	public void setIdProduct(int idProduct) {
		this.idProduct = idProduct;
	}
	public Timestamp getCreateDate() {
		return createDate;
	}
	public void setCreateDate(Timestamp createDate) {
		this.createDate = createDate;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public Date getExpirationDate() {
		return expirationDate;
	}
	public void setExpirationDate(Date expirationDate) {
		this.expirationDate = expirationDate;
	}
	public BigDecimal getIdImagen() {
		return idImagen;
	}
	public void setIdImagen(BigDecimal idImagen) {
		this.idImagen = idImagen;
	}
	public BigDecimal getIdPrice() {
		return idPrice;
	}
	public void setIdPrice(BigDecimal idPrice) {
		this.idPrice = idPrice;
	}
	public BigDecimal getIdSystem() {
		return idSystem;
	}
	public void setIdSystem(BigDecimal idSystem) {
		this.idSystem = idSystem;
	}
	public BigDecimal getIdType() {
		return idType;
	}
	public void setIdType(BigDecimal idType) {
		this.idType = idType;
	}
	public BigDecimal getIdUser() {
		return idUser;
	}
	public void setIdUser(BigDecimal idUser) {
		this.idUser = idUser;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public Timestamp getUpdateDate() {
		return updateDate;
	}
	public void setUpdateDate(Timestamp updateDate) {
		this.updateDate = updateDate;
	}
	public String getUserCreate() {
		return userCreate;
	}
	public void setUserCreate(String userCreate) {
		this.userCreate = userCreate;
	}
	public String getUserModify() {
		return userModify;
	}
	public void setUserModify(String userModify) {
		this.userModify = userModify;
	}

	public ObjectId get_id() {
		return _id;
	}

	public void set_id(ObjectId _id) {
		this._id = _id;
	}
}


