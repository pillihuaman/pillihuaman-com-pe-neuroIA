package pillihuaman.com.pe.neuroIA.RequestResponse.dto;
public class ReqImagen {
	private byte[] file;
	private String name;
	private int topFindCount;
	private int idProduct;

	public int getIdProduct() {
		return idProduct;
	}

	public void setIdProduct(int idProduct) {
		this.idProduct = idProduct;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getTopFindCount() {
		return topFindCount;
	}

	public void setTopFindCount(int topFindCount) {
		this.topFindCount = topFindCount;
	}

	public byte[] getFile() {
		return file;
	}

	public void setFile(byte[] file) {
		this.file = file;
	}

}
