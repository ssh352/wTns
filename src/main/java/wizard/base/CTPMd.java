package wizard.base;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.openhft.chronicle.bytes.MethodReader;
import wizard.interfaces.MD;
import wizard.interfaces.Strategy;
import wizard.test.Tick;
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
	public String mdName;
	public String tradingDayStr;
	public String[] symbols;
	public CThostFtdcMdApi cThostFtdcMdApi;
	public Board board;
	public Strategy writer;
	public MethodReader reader;

	private boolean connecting = false;
	private boolean connected = false;
	private boolean loginned = false;


	public CTPMd(Board board, String mdAddress, String brokerID, String userID, String password,
		  String mdName, String[] symbols) {
		this.mdAddress = mdAddress;
		this.brokerID = brokerID;
		this.userID = userID;
		this.password = password;
		this.mdName = mdName;
		this.symbols = symbols;
		this.board = board;
		board.addEngine(mdName, this);
		this.writer = Board.getStrategyWriter();
		this.reader = Board.getMdReader(this);
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
				log.info(mdName + "make tmp dir " + tempFile.getParentFile().getAbsolutePath());
			} catch (IOException e) {
				log.error(mdName + "make tmp dir failed " + tempFile.getParentFile().getAbsolutePath());
			}
		}
		log.info(mdName + "using " + tempFile.getParentFile().getAbsolutePath());

		cThostFtdcMdApi = CThostFtdcMdApi.CreateFtdcMdApi(tempFile.getAbsolutePath());
		cThostFtdcMdApi.RegisterSpi(this);
		cThostFtdcMdApi.RegisterFront(mdAddress);
		connecting = true;
		cThostFtdcMdApi.Init();
		// todo : why raw not join ?
		//cThostFtdcMdApi.Join();
		while(true){
			this.reader.readOne();
		}
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

	public void onSub(String symbol) {
		if (alive()) {
			String[] symbolArray = new String[1];
			symbolArray[0] = symbol;
			cThostFtdcMdApi.SubscribeMarketData(symbolArray, 1);
		} else {
			log.warn(mdName + "can't subscribe, not connected yet");
		}
	}

	public void onUnsub(String rtSymbol) {
		if (alive()) {
			String[] rtSymbolArray = new String[1];
			rtSymbolArray[0] = rtSymbol;
			cThostFtdcMdApi.UnSubscribeMarketData(rtSymbolArray, 1);
		} else {
			log.warn(mdName + "can't unsubscribe, not conneted yet");
		}
	}

	private void login() {
		if (StringUtils.isEmpty(brokerID) || StringUtils.isEmpty(userID) || StringUtils.isEmpty(password)) {
			log.error(mdName + "BrokerID UserID Password can't be empty");
			return;
		}
		CThostFtdcReqUserLoginField userLoginField = new CThostFtdcReqUserLoginField();
		userLoginField.setBrokerID(brokerID);
		userLoginField.setUserID(userID);
		userLoginField.setPassword(password);
		cThostFtdcMdApi.ReqUserLogin(userLoginField, 0);
	}

	public void OnFrontConnected() {
		log.info(mdName + "front machine is connected");
		connected = true;
		connecting = false;
		login();
	}

	public void OnFrontDisconnected(int nReason) {
		log.info(mdName + "front machine is disconnected, Reason:" + nReason);
		this.connected = false;
	}

	public void OnRspUserLogin(CThostFtdcRspUserLoginField pRspUserLogin, CThostFtdcRspInfoField pRspInfo,
			int nRequestID, boolean bIsLast) {
		if (pRspInfo.getErrorID() == 0) {
			log.info("{}OnRspUserLogin! TradingDay:{},SessionID:{},BrokerID:{},UserID:{}", mdName,
					pRspUserLogin.getTradingDay(), pRspUserLogin.getSessionID(), pRspUserLogin.getBrokerID(),
					pRspUserLogin.getUserID());
			this.loginned = true;
			tradingDayStr = pRspUserLogin.getTradingDay();
			log.info("{} get trading day {}", mdName, tradingDayStr);
			if (this.symbols != null && this.symbols.length > 0) {
				cThostFtdcMdApi.SubscribeMarketData(this.symbols, this.symbols.length);
			}
		} else {
			log.warn("{}md login error! ErrorID:{},ErrorMsg:{}", mdName, pRspInfo.getErrorID(),
					pRspInfo.getErrorMsg());
		}

	}

	public void OnHeartBeatWarning(int nTimeLapse) {
		log.warn(mdName + "md heartbeat nTimeLapse:" + nTimeLapse);
	}

	public void OnRspUserLogout(CThostFtdcUserLogoutField pUserLogout, CThostFtdcRspInfoField pRspInfo, int nRequestID,
			boolean bIsLast) {
		if (pRspInfo.getErrorID() != 0) {
			log.info("{} OnRspUserLogout!ErrorID:{}, ErrorMsg:{}", mdName, pRspInfo.getErrorID(),
					pRspInfo.getErrorMsg());
		} else {
			log.info("{} OnRspUserLogout!BrokerID:{}, UserID:{}", mdName, pUserLogout.getBrokerID(),
					pUserLogout.getUserID());

		}
		this.loginned = false;
	}

	public void OnRspError(CThostFtdcRspInfoField pRspInfo, int nRequestID, boolean bIsLast) {
		log.info("{}md respError !ErrorID:{},ErrorMsg:{},RequestID:{},isLast{}", mdName, pRspInfo.getErrorID(),
				pRspInfo.getErrorMsg(), nRequestID, bIsLast);
	}

	public void OnRspSubMarketData(CThostFtdcSpecificInstrumentField pSpecificInstrument,
			CThostFtdcRspInfoField pRspInfo, int nRequestID, boolean bIsLast) {
		if (pRspInfo.getErrorID() == 0) {
			log.info(mdName + "OnRspSubMarketData! subscribe succeed: " + pSpecificInstrument.getInstrumentID());
		} else {
			log.warn(mdName + "OnRspSubMarketData! subscribe failed, ErrorID：" + pRspInfo.getErrorID() + "ErrorMsg:"
					+ pRspInfo.getErrorMsg());
		}
	}

	public void OnRspUnSubMarketData(CThostFtdcSpecificInstrumentField pSpecificInstrument,
			CThostFtdcRspInfoField pRspInfo, int nRequestID, boolean bIsLast) {
		if (pRspInfo.getErrorID() == 0) {
			log.info(mdName + "OnRspUnSubMarketData! succeed :" + pSpecificInstrument.getInstrumentID());
		} else {
			log.warn(mdName + "OnRspUnSubMarketData! failed, ErrorID：" + pRspInfo.getErrorID() + "ErrorMsg:"
					+ pRspInfo.getErrorMsg());
		}
	}

	public void OnRtnDepthMarketData(CThostFtdcDepthMarketDataField pDepthMarketData) {
	    log.info("md start get tick");
	    // todo : why new Thread every tick ?
		if (pDepthMarketData != null) {

			// TODO 大商所时间修正
			Long updateTime = Long.valueOf(pDepthMarketData.getUpdateTime().replaceAll(":", ""));
			Long updateMillisec = (long) pDepthMarketData.getUpdateMillisec();
			Long actionDay = Long.valueOf(pDepthMarketData.getActionDay());

			//  todo : parse date time
			writer.onTick(new Tick(mdName, pDepthMarketData));
		} else {
			log.warn("{}OnRtnDepthMarketData! get empty ticks", mdName);
		}
	}

	public void OnRspSubForQuoteRsp(CThostFtdcSpecificInstrumentField pSpecificInstrument,
			CThostFtdcRspInfoField pRspInfo, int nRequestID, boolean bIsLast) {
		log.info("{}OnRspSubForQuoteRsp!", mdName);
	}

	public void OnRspUnSubForQuoteRsp(CThostFtdcSpecificInstrumentField pSpecificInstrument,
			CThostFtdcRspInfoField pRspInfo, int nRequestID, boolean bIsLast) {
		log.info("{}OnRspUnSubForQuoteRsp!", mdName);
	}

	public void OnRtnForQuoteRsp(CThostFtdcForQuoteRspField pForQuoteRsp) {
		log.info("{}OnRspUnSubForQuoteRsp!", mdName);
	}

	// todo : what to consider more
	// 1, heartbeat
	// 2, cpu not idle
	// 3, how to get exchange
	// 4, comment using english
	// 5, test auto reconnect
	public static void main(String[] args) {
		final String mdName = args[0];
		String ctpMdAddress = "tcp://" + args[1];
		String[] symbols = {"kk"};
		symbols[0] = args[2];

		Board board = new Board();
		CTPMd ctpMd = new CTPMd(board, ctpMdAddress, "9999", "125268", "140706",
				mdName, symbols);
		ctpMd.start();
	}
}