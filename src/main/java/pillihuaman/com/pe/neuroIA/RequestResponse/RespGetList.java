package pillihuaman.com.pe.neuroIA.RequestResponse;

import java.util.List;



public class RespGetList<T> {

	private Integer count;
	private Long total;
	private List<T> items;

	public Integer getCount() {
		return count;
	}

	public void setCount(Integer count) {
		this.count = count;
	}

	public Long getTotal() {
		return total;
	}

	public void setTotal(Long total) {
		this.total = total;
	}

	public List<T> getItems() {
		return items;
	}

	public void setItems(List<T> items) {
		this.items = items;
	}

	public RespGetList(List<T> items) {
		this.items = items;
		this.count = items.size();
	}
	
	public RespGetList() {
		
	}
}
