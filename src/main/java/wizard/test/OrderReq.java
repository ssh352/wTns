package wizard.test;

import java.io.Serializable;

/**
 * @author sun0x00@gmail.com
 */
public class OrderReq  implements Serializable{

	public static final long serialVersionUID = -8783647687127541104L;

	public String gatewayID; // 接口
	
	// 代码编号相关
	public String symbol; // 代码
	public String exchange; // 交易所代码
	public String rtSymbol; // 系统中的唯一代码,通常是 合约代码.交易所代码

	// 报单相关
	public double price; // 报单价格
	public int volume; // 报单总数量
	public String direction; // 报单方向
	public String offset; // 报单开平仓
	public String priceType; // 报单成交数量

	// IB预留
	public String productClass; // 合约类型
	public String currency; // 合约货币
	public String expiry; // 到期日
	public double strikePrice; // 行权价
	public String optionType; // 期权类型
	public String lastTradeDateOrContractMonth; // 合约月,IB专用
	public String multiplier; // 乘数,IB专用

	public String getMultiplier() {
		return multiplier;
	}
	public void setMultiplier(String multiplier) {
		this.multiplier = multiplier;
	}
	@Override
	public String toString() {
		return "OrderReq [gatewayID=" + gatewayID + ", symbol=" + symbol + ", exchange=" + exchange + ", rtSymbol="
				+ rtSymbol + ", price=" + price + ", volume=" + volume + ", direction=" + direction + ", offset="
				+ offset + ", priceType=" + priceType + ", productClass=" + productClass + ", currency=" + currency
				+ ", expiry=" + expiry + ", strikePrice=" + strikePrice + ", optionType=" + optionType
				+ ", lastTradeDateOrContractMonth=" + lastTradeDateOrContractMonth + ", multiplier=" + multiplier + "]";
	}

	
}
