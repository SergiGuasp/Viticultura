package model;

import java.math.BigDecimal;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import utils.TipoVid;

@Entity
@Table(name= "vid")
public class Vid {
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(name = "id", unique = true, nullable = true)
	private int id;
	@Column(name = "tipo_vid", nullable = true)
	private TipoVid vid;
	@Column(name = "cantidad", nullable = true)
	private int cantidad;
	@Column(name = "price", nullable = true)
	private double price;
	
	public Vid() {}
		
	public Vid(TipoVid vid, int cantidad, double price) {
		this.vid = vid;
		this.cantidad = cantidad;
		this.price = price;
	}
	public int getId() {
		return this.id;
	}
	public TipoVid getVid() {
		return vid;
	}
	public int getCantidad() {
		return cantidad;
	}
	
	public double getPrice() {
		return price;
	}
	
	
	public void setPrice(double price2) {
		this.price = price2;
	}

	@Override
	public String toString() {
	    return "Vid [vid=" + (vid == TipoVid.BLANCA ? "blanca" : "negra") + ", cantidad=" + cantidad + "]";
	}

}
