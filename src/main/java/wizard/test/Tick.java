package wizard.test;

import net.openhft.chronicle.wire.AbstractMarshallable;
import xyz.redtorch.api.jctp.CThostFtdcDepthMarketDataField;


/**
 * @author sun0x00@gmail.com
 */
public class Tick extends AbstractMarshallable {

	public static final long serialVersionUID = -2066668386737336931L;

	public String from; // 接口

	// 代码相关
	public String symbol; // 代码
	public String exchange; // 交易所代码
	public String rtSymbol; // 系统中的唯一代码,通常是 合约代码.交易所代码

    public String tradingDay;       // 交易日
    public String actionDay;        // 业务发生日
    public String updateTime;       // 时间(HHMMSSmmm)

	public Integer status; // 状态

	// 成交数据
	public Double lastPrice = 0d; // 最新成交价
	public Integer lastVolume = 0; // 最新成交量
	public Integer volume = 0; // 今天总成交量
	public Double openInterest = 0d; // 持仓量

	public double preOpenInterest = 0L;// 昨持仓
	public Double preClosePrice = 0d; // 前收盘价
	public Double preSettlePrice = 0d; // 昨结算

	// 常规行情
	public Double openPrice = 0d; // 今日开盘价
	public Double highestPrice = 0d; // 今日最高价
	public Double lowestPrice = 0d; // 今日最低价

	public Double upperLimit = 0d; // 涨停价
	public Double lowerLimit = 0d; // 跌停价

	public Double bidPrice1 = 0d;
	public Double bidPrice2 = 0d;
	public Double bidPrice3 = 0d;
	public Double bidPrice4 = 0d;
	public Double bidPrice5 = 0d;
	public Double bidPrice6 = 0d;
	public Double bidPrice7 = 0d;
	public Double bidPrice8 = 0d;
	public Double bidPrice9 = 0d;
	public Double bidPrice10 = 0d;

	public Double askPrice1 = 0d;
	public Double askPrice2 = 0d;
	public Double askPrice3 = 0d;
	public Double askPrice4 = 0d;
	public Double askPrice5 = 0d;
	public Double askPrice6 = 0d;
	public Double askPrice7 = 0d;
	public Double askPrice8 = 0d;
	public Double askPrice9 = 0d;
	public Double askPrice10 = 0d;

	public Integer bidVolume1 = 0;
	public Integer bidVolume2 = 0;
	public Integer bidVolume3 = 0;
	public Integer bidVolume4 = 0;
	public Integer bidVolume5 = 0;
	public Integer bidVolume6 = 0;
	public Integer bidVolume7 = 0;
	public Integer bidVolume8 = 0;
	public Integer bidVolume9 = 0;
	public Integer bidVolume10 = 0;

	public Integer askVolume1 = 0;
	public Integer askVolume2 = 0;
	public Integer askVolume3 = 0;
	public Integer askVolume4 = 0;
	public Integer askVolume5 = 0;
	public Integer askVolume6 = 0;
	public Integer askVolume7 = 0;
	public Integer askVolume8 = 0;
	public Integer askVolume9 = 0;
	public Integer askVolume10 = 0;
	
	
	// CThostFtdcDepthMarketDataField  is a pointer in c++, and will never 'new', always clear and set, so we must 'NEW'
	// an object to save it.
	public Tick(String from, CThostFtdcDepthMarketDataField tickP) {
		this.from = from;
		this.symbol = tickP.getInstrumentID();
		this.exchange = tickP.getExchangeID();
		this.rtSymbol = symbol + "." + exchange;
		this.tradingDay = tickP.getTradingDay();
		this.actionDay = tickP.getActionDay();
		this.updateTime = tickP.getUpdateTime();
		this.status = 0;
		this.lastPrice = tickP.getLastPrice();
		this.lastVolume = 0;
		this.volume = tickP.getVolume();
		this.openInterest = tickP.getOpenInterest();
		this.preOpenInterest = tickP.getPreOpenInterest();
		this.preClosePrice = tickP.getPreClosePrice();
		this.preSettlePrice = tickP.getPreSettlementPrice();
		this.openPrice = tickP.getOpenPrice();
		this.highestPrice = tickP.getHighestPrice();
		this.lowestPrice = tickP.getLowestPrice();
		this.upperLimit = tickP.getUpperLimitPrice();
		this.lowerLimit = tickP.getLowerLimitPrice();
		this.bidPrice1 = tickP.getBidPrice1();
		this.bidPrice2 = tickP.getBidPrice2();
		this.bidPrice3 = tickP.getBidPrice3();
		this.bidPrice4 = tickP.getBidPrice4();
		this.bidPrice5 = tickP.getBidPrice5();
		this.askPrice1 = tickP.getAskPrice1();
		this.askPrice2 = tickP.getAskPrice2();
		this.askPrice3 = tickP.getAskPrice3();
		this.askPrice4 = tickP.getAskPrice4();
		this.askPrice5 = tickP.getAskPrice5();
		this.bidVolume1 = tickP.getBidVolume1();
		this.bidVolume2 = tickP.getBidVolume2();
		this.bidVolume3 = tickP.getBidVolume3();
		this.bidVolume4 = tickP.getBidVolume4();
		this.bidVolume5 = tickP.getBidVolume5();
		this.askVolume1 = tickP.getAskVolume1();
		this.askVolume2 = tickP.getAskVolume2();
		this.askVolume3 = tickP.getAskVolume3();
		this.askVolume4 = tickP.getAskVolume4();
		this.askVolume5 = tickP.getAskVolume5();
	}

	@Override
	public String toString() {
		return "Tick [gatewayID=" + from + ", symbol=" + symbol + ", exchange=" + exchange + ", rtSymbol="
				+ rtSymbol + ", tradingDay=" + tradingDay + ", actionDay=" + actionDay + ", updateTime=" + updateTime
				+ ", dateTime=" + "NNNNNNNNNNNNNN" + ", status=" + status + ", lastPrice=" + lastPrice + ", lastVolume="
				+ lastVolume + ", volume=" + volume + ", openInterest=" + openInterest + ", preOpenInterest="
				+ preOpenInterest + ", preClosePrice=" + preClosePrice + ", preSettlePrice=" + preSettlePrice
				+ ", openPrice=" + openPrice + ", highestPrice=" + highestPrice + ", lowestPrice=" + lowestPrice + ", upperLimit="
				+ upperLimit + ", lowerLimit=" + lowerLimit + ", bidPrice1=" + bidPrice1 + ", bidPrice2=" + bidPrice2
				+ ", bidPrice3=" + bidPrice3 + ", bidPrice4=" + bidPrice4 + ", bidPrice5=" + bidPrice5 + ", bidPrice6="
				+ bidPrice6 + ", bidPrice7=" + bidPrice7 + ", bidPrice8=" + bidPrice8 + ", bidPrice9=" + bidPrice9
				+ ", bidPrice10=" + bidPrice10 + ", askPrice1=" + askPrice1 + ", askPrice2=" + askPrice2
				+ ", askPrice3=" + askPrice3 + ", askPrice4=" + askPrice4 + ", askPrice5=" + askPrice5 + ", askPrice6="
				+ askPrice6 + ", askPrice7=" + askPrice7 + ", askPrice8=" + askPrice8 + ", askPrice9=" + askPrice9
				+ ", askPrice10=" + askPrice10 + ", bidVolume1=" + bidVolume1 + ", bidVolume2=" + bidVolume2
				+ ", bidVolume3=" + bidVolume3 + ", bidVolume4=" + bidVolume4 + ", bidVolume5=" + bidVolume5
				+ ", bidVolume6=" + bidVolume6 + ", bidVolume7=" + bidVolume7 + ", bidVolume8=" + bidVolume8
				+ ", bidVolume9=" + bidVolume9 + ", bidVolume10=" + bidVolume10 + ", askVolume1=" + askVolume1
				+ ", askVolume2=" + askVolume2 + ", askVolume3=" + askVolume3 + ", askVolume4=" + askVolume4
				+ ", askVolume5=" + askVolume5 + ", askVolume6=" + askVolume6 + ", askVolume7=" + askVolume7
				+ ", askVolume8=" + askVolume8 + ", askVolume9=" + askVolume9 + ", askVolume10=" + askVolume10 + "]";
	}
	
}
