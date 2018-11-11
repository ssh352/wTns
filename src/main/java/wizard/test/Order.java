package wizard.test;

import java.io.Serializable;

/**
 * @author sun0x00@gmail.com
 */
public class Order implements Serializable{

	public static final long serialVersionUID = -8783647687127541104L;

	public String gatewayID; // 接口
	public String orderId;
	
	public String symbol; // 代码
	public String exchange; // 交易所代码
	public String rtSymbol; // 系统中的唯一代码,通常是 合约代码.交易所代码

	public double price; // 报单价格
	public int volume; // 报单总数量
	public int tradedVolume;
	public char status;
	public String msg;
	public String tradingDay;
	public String direction; // 报单方向
	public String offset; // 报单开平仓
	public String priceType; // 报单成交数量
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
