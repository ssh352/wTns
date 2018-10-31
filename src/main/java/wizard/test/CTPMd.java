package wizard.test;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import wizard.interfaces.MD;
import wizard.tools.CommonUtil;
import wizard.tools.StringUtils;
import xyz.redtorch.api.jctp.CThostFtdcDepthMarketDataField;
import xyz.redtorch.api.jctp.CThostFtdcForQuoteRspField;
import xyz.redtorch.api.jctp.CThostFtdcMdApi;
import xyz.redtorch.api.jctp.CThostFtdcMdSpi;
import xyz.redtorch.api.jctp.CThostFtdcReqUserLoginField;
import xyz.redtorch.api.jctp.CThostFtdcRspInfoField;
import xyz.redtorch.api.jctp.CThostFtdcRspUserLoginField;
import xyz.redtorch.api.jctp.CThostFtdcSpecificInstrumentField;
import xyz.redtorch.api.jctp.CThostFtdcUserLogoutField;

/**
 * @author sun0x00@gmail.com
 */
public class CTPMd extends CThostFtdcMdSpi implements MD {

	Logger log = LoggerFactory.getLogger(CTPMd.class);

	public String mdAddress;
	public String brokerID;
	public String userID;
	public String password;
	public String gatewayLogInfo;
	public String tradingDayStr;
	public String[] symbols;
	public CThostFtdcMdApi cThostFtdcMdApi;

	private boolean connecting = false;
	private boolean connected = false;
	private boolean loginned = false;


	CTPMd(String mdAddress, String brokerID, String userID, String password,
		  String gatewayLogInfo, String[] symbols) {
		this.mdAddress = mdAddress;
		this.brokerID = brokerID;
		this.userID = userID;
		this.password = password;
		this.gatewayLogInfo = gatewayLogInfo;
		this.symbols = symbols;
	}

	static{
		try {
			String envTmpDir = "/tmp";
			String tempLibPath = envTmpDir + File.separator + "xyz" + File.separator + "redtorch" + File.separator + "api"
					+ File.separator + "jctp" + File.separator + "lib";
			String[] libPartNames = {"jctpmdapiv6v3v11x64", "jctptraderapiv6v3v11x64", "thostmduserapi", "thosttraderapi"};
			for(String libPart : libPartNames) {
				CommonUtil.copyURLToFileForTmp(tempLibPath, MD.class.getResource("/assembly/lib" + libPart + ".so"));
			}
			for(String libPart : libPartNames){
				System.load(tempLibPath + File.separator + "lib" + libPart + ".so");
			}
		}catch (Exception e){
			e.printStackTrace();
			System.exit(-189);
		}
	}


	public synchronized boolean start() {
		if (alive() || connecting) {
			return true;
		}

		if (connected) {
			login();
			return true;
		}
		if (cThostFtdcMdApi != null) {
			cThostFtdcMdApi.RegisterSpi(null);
			cThostFtdcMdApi.Release();
			connected = false;
			loginned = false;

		}
		String envTmpDir = System.getProperty("java.io.tmpdir");
		String tempFilePath = envTmpDir + File.separator + "xyz" + File.separator + "redtorch" + File.separator + "api"
				+ File.separator + "jctp" + File.separator + "TEMP_CTP" + File.separator + "MD_";
		File tempFile = new File(tempFilePath);
		if (!tempFile.getParentFile().exists()) {
			try {
				FileUtils.forceMkdirParent(tempFile);
				log.info(gatewayLogInfo + "make tmp dir " + tempFile.getParentFile().getAbsolutePath());
			} catch (IOException e) {
				log.error(gatewayLogInfo + "make tmp dir failed " + tempFile.getParentFile().getAbsolutePath());
			}
		}
		log.info(gatewayLogInfo + "using " + tempFile.getParentFile().getAbsolutePath());

		cThostFtdcMdApi = CThostFtdcMdApi.CreateFtdcMdApi(tempFile.getAbsolutePath());
		cThostFtdcMdApi.RegisterSpi(this);
		cThostFtdcMdApi.RegisterFront(mdAddress);
		connecting = true;
		cThostFtdcMdApi.Init();
		// todo : why raw not join ?
		cThostFtdcMdApi.Join();
		//while(true){
		//	System.currentTimeMillis();
		//}
		return true;
	}

	public synchronized boolean stop() {
		if (!alive()) {
			return true;
		}

		if (cThostFtdcMdApi != null) {
			cThostFtdcMdApi.RegisterSpi(null);
			cThostFtdcMdApi.Release();
			connected = false;
			loginned = false;
			connecting = false;
		}
		return true;
	}

	public boolean alive() {
		return connected && loginned;
	}

	public String getTradingDay() {
		return tradingDayStr;
	}

	public void subscribe(String symbol) {
		if (alive()) {
			String[] symbolArray = new String[1];
			symbolArray[0] = symbol;
			cThostFtdcMdApi.SubscribeMarketData(symbolArray, 1);
		} else {
			log.warn(gatewayLogInfo + "can't subscribe, not connected yet");
		}
	}

	public void unSubscribe(String rtSymbol) {
		if (alive()) {
			String[] rtSymbolArray = new String[1];
			rtSymbolArray[0] = rtSymbol;
			cThostFtdcMdApi.UnSubscribeMarketData(rtSymbolArray, 1);
		} else {
			log.warn(gatewayLogInfo + "can't unsubscribe, not conneted yet");
		}
	}

	private void login() {
		if (StringUtils.isEmpty(brokerID) || StringUtils.isEmpty(userID) || StringUtils.isEmpty(password)) {
			log.error(gatewayLogInfo + "BrokerID UserID Password can't be empty");
			return;
		}
		CThostFtdcReqUserLoginField userLoginField = new CThostFtdcReqUserLoginField();
		userLoginField.setBrokerID(brokerID);
		userLoginField.setUserID(userID);
		userLoginField.setPassword(password);
		cThostFtdcMdApi.ReqUserLogin(userLoginField, 0);
	}

	public void OnFrontConnected() {
		log.info(gatewayLogInfo + "front machine is connected");
		connected = true;
		connecting = false;
		login();
	}

	public void OnFrontDisconnected(int nReason) {
		log.info(gatewayLogInfo + "front machine is disconnected, Reason:" + nReason);
		this.connected = false;
	}

	public void OnRspUserLogin(CThostFtdcRspUserLoginField pRspUserLogin, CThostFtdcRspInfoField pRspInfo,
			int nRequestID, boolean bIsLast) {
		if (pRspInfo.getErrorID() == 0) {
			log.info("{}OnRspUserLogin! TradingDay:{},SessionID:{},BrokerID:{},UserID:{}", gatewayLogInfo,
					pRspUserLogin.getTradingDay(), pRspUserLogin.getSessionID(), pRspUserLogin.getBrokerID(),
					pRspUserLogin.getUserID());
			this.loginned = true;
			tradingDayStr = pRspUserLogin.getTradingDay();
			log.info("{} get trading day {}", gatewayLogInfo, tradingDayStr);
			if (this.symbols != null && this.symbols.length > 0) {
				cThostFtdcMdApi.SubscribeMarketData(this.symbols, this.symbols.length);
			}
		} else {
			log.warn("{}md login error! ErrorID:{},ErrorMsg:{}", gatewayLogInfo, pRspInfo.getErrorID(),
					pRspInfo.getErrorMsg());
		}

	}

	public void OnHeartBeatWarning(int nTimeLapse) {
		log.warn(gatewayLogInfo + "md heartbeat nTimeLapse:" + nTimeLapse);
	}

	public void OnRspUserLogout(CThostFtdcUserLogoutField pUserLogout, CThostFtdcRspInfoField pRspInfo, int nRequestID,
			boolean bIsLast) {
		if (pRspInfo.getErrorID() != 0) {
			log.info("{} OnRspUserLogout!ErrorID:{}, ErrorMsg:{}", gatewayLogInfo, pRspInfo.getErrorID(),
					pRspInfo.getErrorMsg());
		} else {
			log.info("{} OnRspUserLogout!BrokerID:{}, UserID:{}", gatewayLogInfo, pUserLogout.getBrokerID(),
					pUserLogout.getUserID());

		}
		this.loginned = false;
	}

	public void OnRspError(CThostFtdcRspInfoField pRspInfo, int nRequestID, boolean bIsLast) {
		log.info("{}md respError !ErrorID:{},ErrorMsg:{},RequestID:{},isLast{}", gatewayLogInfo, pRspInfo.getErrorID(),
				pRspInfo.getErrorMsg(), nRequestID, bIsLast);
	}

	public void OnRspSubMarketData(CThostFtdcSpecificInstrumentField pSpecificInstrument,
			CThostFtdcRspInfoField pRspInfo, int nRequestID, boolean bIsLast) {
		if (pRspInfo.getErrorID() == 0) {
			log.info(gatewayLogInfo + "OnRspSubMarketData! subscribe succeed: " + pSpecificInstrument.getInstrumentID());
		} else {
			log.warn(gatewayLogInfo + "OnRspSubMarketData! subscribe failed, ErrorID：" + pRspInfo.getErrorID() + "ErrorMsg:"
					+ pRspInfo.getErrorMsg());
		}
	}

	public void OnRspUnSubMarketData(CThostFtdcSpecificInstrumentField pSpecificInstrument,
			CThostFtdcRspInfoField pRspInfo, int nRequestID, boolean bIsLast) {
		if (pRspInfo.getErrorID() == 0) {
			log.info(gatewayLogInfo + "OnRspUnSubMarketData! succeed :" + pSpecificInstrument.getInstrumentID());
		} else {
			log.warn(gatewayLogInfo + "OnRspUnSubMarketData! failed, ErrorID：" + pRspInfo.getErrorID() + "ErrorMsg:"
					+ pRspInfo.getErrorMsg());
		}
	}

	public void OnRtnDepthMarketData(CThostFtdcDepthMarketDataField pDepthMarketData) {
	    log.info("md start get tick");
	    // todo : why new Thread every tick ?
		if (pDepthMarketData != null) {

			// // T2T Test
			// if("IH1805".equals(pDepthMarketData.getInstrumentID())) {
			// System.out.println("T2T-Tick-"+System.nanoTime());
			// }
			String symbol = pDepthMarketData.getInstrumentID();

			// TODO 大商所时间修正
			Long updateTime = Long.valueOf(pDepthMarketData.getUpdateTime().replaceAll(":", ""));
			Long updateMillisec = (long) pDepthMarketData.getUpdateMillisec();
			Long actionDay = Long.valueOf(pDepthMarketData.getActionDay());

			String updateDateTimeWithMS = (actionDay * 100 * 100 * 100 * 1000 + updateTime * 1000 + updateMillisec)
					+ "";

			/*  todo : parse date time
			DateTime dateTime;
			try {
				dateTime = RtConstant.DT_FORMAT_WITH_MS_INT_FORMATTER.parseDateTime(updateDateTimeWithMS);
			} catch (Exception e) {
				log.error("{}解析日期发生异常", gatewayLogInfo, e);
				return;
			}
			String actionTime = dateTime.toString(RtConstant.T_FORMAT_WITH_MS_INT_FORMATTER);

			*/

			String dateTime = "whatDataTime";
			String actionTime = "whatActionTime";

			// change above


			String exchange = "needExchange";
			String rtSymbol = symbol + "." + exchange;
			String tradingDay = tradingDayStr;
			String actionDayStr = pDepthMarketData.getActionDay();
			Integer status = 0;
			Double lastPrice = pDepthMarketData.getLastPrice();
			Integer lastVolume = 0;
			Integer volume = pDepthMarketData.getVolume();
			Double openInterest = pDepthMarketData.getOpenInterest();
			Long preOpenInterest = 0L;
			Double preClosePrice = pDepthMarketData.getPreClosePrice();
			Double preSettlePrice = pDepthMarketData.getPreSettlementPrice();
			Double openPrice = pDepthMarketData.getOpenPrice();
			Double highPrice = pDepthMarketData.getHighestPrice();
			Double lowPrice = pDepthMarketData.getLowestPrice();
			Double upperLimit = pDepthMarketData.getUpperLimitPrice();
			Double lowerLimit = pDepthMarketData.getLowerLimitPrice();
			Double bidPrice1 = pDepthMarketData.getBidPrice1();
			Double bidPrice2 = pDepthMarketData.getBidPrice2();
			Double bidPrice3 = pDepthMarketData.getBidPrice3();
			Double bidPrice4 = pDepthMarketData.getBidPrice4();
			Double bidPrice5 = pDepthMarketData.getBidPrice5();
			Double bidPrice6 = 0d;
			Double bidPrice7 = 0d;
			Double bidPrice8 = 0d;
			Double bidPrice9 = 0d;
			Double bidPrice10 = 0d;
			Double askPrice1 = pDepthMarketData.getAskPrice1();
			Double askPrice2 = pDepthMarketData.getAskPrice2();
			Double askPrice3 = pDepthMarketData.getAskPrice3();
			Double askPrice4 = pDepthMarketData.getAskPrice4();
			Double askPrice5 = pDepthMarketData.getAskPrice5();
			Double askPrice6 = 0d;
			Double askPrice7 = 0d;
			Double askPrice8 = 0d;
			Double askPrice9 = 0d;
			Double askPrice10 = 0d;
			Integer bidVolume1 = pDepthMarketData.getBidVolume1();
			Integer bidVolume2 = pDepthMarketData.getBidVolume2();
			Integer bidVolume3 = pDepthMarketData.getBidVolume3();
			Integer bidVolume4 = pDepthMarketData.getBidVolume4();
			Integer bidVolume5 = pDepthMarketData.getBidVolume5();
			Integer bidVolume6 = 0;
			Integer bidVolume7 = 0;
			Integer bidVolume8 = 0;
			Integer bidVolume9 = 0;
			Integer bidVolume10 = 0;
			Integer askVolume1 = pDepthMarketData.getAskVolume1();
			Integer askVolume2 = pDepthMarketData.getAskVolume2();
			Integer askVolume3 = pDepthMarketData.getAskVolume3();
			Integer askVolume4 = pDepthMarketData.getAskVolume4();
			Integer askVolume5 = pDepthMarketData.getAskVolume5();
			Integer askVolume6 = 0;
			Integer askVolume7 = 0;
			Integer askVolume8 = 0;
			Integer askVolume9 = 0;
			Integer askVolume10 = 0;

			// todo : data exchange with other
			log.info("one ce");
			/*
			ctpGateway.emitTick(gatewayID, symbol, exchange, rtSymbol, tradingDay, actionDayStr, actionTime, dateTime,
					status, lastPrice, lastVolume, volume, openInterest, preOpenInterest, preClosePrice, preSettlePrice,
					openPrice, highPrice, lowPrice, upperLimit, lowerLimit, bidPrice1, bidPrice2, bidPrice3, bidPrice4,
					bidPrice5, bidPrice6, bidPrice7, bidPrice8, bidPrice9, bidPrice10, askPrice1, askPrice2, askPrice3,
					askPrice4, askPrice5, askPrice6, askPrice7, askPrice8, askPrice9, askPrice10, bidVolume1,
					bidVolume2, bidVolume3, bidVolume4, bidVolume5, bidVolume6, bidVolume7, bidVolume8, bidVolume9,
					bidVolume10, askVolume1, askVolume2, askVolume3, askVolume4, askVolume5, askVolume6, askVolume7,
					askVolume8, askVolume9, askVolume10);

			*/
		} else {
			log.warn("{}OnRtnDepthMarketData! get empty ticks", gatewayLogInfo);
		}
	}

	public void OnRspSubForQuoteRsp(CThostFtdcSpecificInstrumentField pSpecificInstrument,
			CThostFtdcRspInfoField pRspInfo, int nRequestID, boolean bIsLast) {
		log.info("{}OnRspSubForQuoteRsp!", gatewayLogInfo);
	}

	public void OnRspUnSubForQuoteRsp(CThostFtdcSpecificInstrumentField pSpecificInstrument,
			CThostFtdcRspInfoField pRspInfo, int nRequestID, boolean bIsLast) {
		log.info("{}OnRspUnSubForQuoteRsp!", gatewayLogInfo);
	}

	public void OnRtnForQuoteRsp(CThostFtdcForQuoteRspField pForQuoteRsp) {
		log.info("{}OnRspUnSubForQuoteRsp!", gatewayLogInfo);
	}

	// todo : what to consider more
	// 1, heartbeat
	// 2, cpu not idle
	// 3, how to get exchange
	// 4, comment using english
	// 5, test auto reconnect
}