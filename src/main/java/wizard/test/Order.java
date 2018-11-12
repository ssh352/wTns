package wizard.test;

import java.io.Serializable;

/**
 * @author sun0x00@gmail.com
 */
public class Order implements Serializable{

	public static final long serialVersionUID = -8783647687127541104L;

	public String gatewayID;
	public String orderId;
	
	public String symbol;
	public String exchange;
	public String rtSymbol;

	public double price;
	public int volume;
	public int tradedVolume;
	public char status;
	public String msg;
	public String tradingDay;
	public String direction;
	public String offset;
	public String priceType;
	public String insertTime;
	public String cancelTime;
	public String activeTime;
	public String updateTime;

	@Override
	public String toString() {
		return "Order [gatewayID=" + gatewayID + ", symbol=" + symbol + ", exchange=" + exchange + ", rtSymbol="
				+ rtSymbol + ", price=" + price + ", volume=" + volume + ", direction=" + direction + ", offset="
				+ offset + ", priceType=" + priceType + "]";
	}

	
}
