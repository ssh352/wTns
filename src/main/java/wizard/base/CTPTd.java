package wizard.base;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.openhft.chronicle.bytes.MethodReader;
import wizard.interfaces.CtpConstant;
import wizard.interfaces.RtConstant;
import wizard.interfaces.Strategy;
import wizard.interfaces.TD;
import wizard.test.Account;
import wizard.test.Contract;
import wizard.test.Order;
import wizard.test.Position;
import wizard.test.Trade;
import wizard.tools.CommonUtil;
import wizard.tools.StringUtils;
import xyz.redtorch.api.jctp.CThostFtdcInputOrderActionField;
import xyz.redtorch.api.jctp.CThostFtdcInputOrderField;
import xyz.redtorch.api.jctp.CThostFtdcInstrumentField;
import xyz.redtorch.api.jctp.CThostFtdcInvestorPositionField;
import xyz.redtorch.api.jctp.CThostFtdcOrderActionField;
import xyz.redtorch.api.jctp.CThostFtdcOrderField;
import xyz.redtorch.api.jctp.CThostFtdcQryInvestorPositionField;
import xyz.redtorch.api.jctp.CThostFtdcQryTradingAccountField;
import xyz.redtorch.api.jctp.CThostFtdcQueryMaxOrderVolumeField;
import xyz.redtorch.api.jctp.CThostFtdcReqAuthenticateField;
import xyz.redtorch.api.jctp.CThostFtdcReqUserLoginField;
import xyz.redtorch.api.jctp.CThostFtdcRspAuthenticateField;
import xyz.redtorch.api.jctp.CThostFtdcRspInfoField;
import xyz.redtorch.api.jctp.CThostFtdcRspUserLoginField;
import xyz.redtorch.api.jctp.CThostFtdcSettlementInfoConfirmField;
import xyz.redtorch.api.jctp.CThostFtdcTradeField;
import xyz.redtorch.api.jctp.CThostFtdcTraderApi;
import xyz.redtorch.api.jctp.CThostFtdcTraderSpi;
import xyz.redtorch.api.jctp.CThostFtdcTradingAccountField;
import xyz.redtorch.api.jctp.CThostFtdcUserLogoutField;
import xyz.redtorch.api.jctp.jctptraderapiv6v3v11x64Constants;

/**
 * @author whitelilis@gmail.com
 */
public class CTPTd extends CThostFtdcTraderSpi implements TD {

	private static Logger log = LoggerFactory.getLogger(CTPTd.class);
	// private String mdAddress;
	public String tdAddress;
	public String brokerID;
	public String userID;
	public String password;
	public String userProductInfo;
	public String authCode;
	public String tdName;
	public Board board;
	public Strategy writer;
	public MethodReader reader;
	// private String gatewayDisplayName;

	public HashMap<String, Position> positionMap = new HashMap<>();
	public HashMap<String, String> contractExchangeMap;
	public HashMap<String, Integer> contractSizeMap;
	public CThostFtdcTraderApi cThostFtdcTraderApi;
	public boolean connecting = false; // 避免重复调用
	public boolean connected = false; // 前置机连接状态
	public boolean logined = false; // 登陆状态
	public String tradingDayStr;

	public AtomicInteger reqID = new AtomicInteger(0); // 操作请求编号
	public AtomicInteger orderRef = new AtomicInteger(0); // 订单编号

	public boolean authStatus = false; // 验证状态
	public boolean loginFailed = false; // 是否已经使用错误的信息尝试登录过

	public int frontID = 0; // 前置机编号
	public int sessionID = 0; // 会话编号

	CTPTd(Board board, String tdAddress, String brokerID, String userID, String password, String authCode, String tdName) {
		this.tdAddress   = tdAddress;
		this.brokerID    = brokerID;
		this.userID      = userID;
		this.password    = password;
		this.authCode    = authCode;
		this.tdName = tdName;
		this.board       = board;
		board.addEngine(tdName, this);
		this.writer = Board.getStrategyWriter();
		this.reader = Board.getTdReader(this);
	}

	static{
		try {
			String envTmpDir = "/tmp";
			String tempLibPath = envTmpDir + File.separator + "xyz" + File.separator + "redtorch" + File.separator + "api"
					+ File.separator + "jctp" + File.separator + "lib";
			String[] libPartNames = {"jctptraderapiv6v3v11x64", "thosttraderapi", "jctpmdapiv6v3v11x64", "thostmduserapi"};
			for(String libPart : libPartNames) {
				CommonUtil.copyURLToFileForTmp(tempLibPath, TD.class.getResource("/assembly/lib" + libPart + ".so"));
			}
			for(String libPart : libPartNames){
				System.load(tempLibPath + File.separator + "lib" + libPart + ".so");
			}
		}catch (Exception e){
			e.printStackTrace();
			System.exit(-189);
		}
	}

	/**
	 * 连接
	 */
	public synchronized boolean start() {
		if (alive() || connecting) {
			return true;
		}

		if (connected) {
			login();
			return true;
		}
		if (cThostFtdcTraderApi != null) {
			cThostFtdcTraderApi.RegisterSpi(null);
			cThostFtdcTraderApi.Release();
			connected = false;
			logined = false;

		}
		String envTmpDir = System.getProperty("java.io.tmpdir");
		String tempFilePath = envTmpDir + File.separator + "xyz" + File.separator + "redtorch" + File.separator + "api"
				+ File.separator + "jctp" + File.separator + "TEMP_CTP" + File.separator + "TD_"
				+ tdName + "_";
		File tempFile = new File(tempFilePath);
		if (!tempFile.getParentFile().exists()) {
			try {
				FileUtils.forceMkdirParent(tempFile);
				log.info(tdName + "创建临时文件夹" + tempFile.getParentFile().getAbsolutePath());
			} catch (IOException e) {
				log.error(tdName + "创建临时文件夹失败" + tempFile.getParentFile().getAbsolutePath(), e);
			}
		}
		log.info(tdName + "使用临时文件夹" + tempFile.getParentFile().getAbsolutePath());
		cThostFtdcTraderApi = CThostFtdcTraderApi.CreateFtdcTraderApi(tempFile.getAbsolutePath());
		cThostFtdcTraderApi.RegisterSpi(this);
		cThostFtdcTraderApi.RegisterFront(tdAddress);
		connecting = true;
		cThostFtdcTraderApi.Init();
		while (true) {
			reader.readOne();
		}
	}

	/**
	 * 关闭
	 */
	public synchronized boolean stop() {
		if (!alive()) {
			return true;
		}

		if (cThostFtdcTraderApi != null) {
			cThostFtdcTraderApi.RegisterSpi(null);
			cThostFtdcTraderApi.Release();
			connected = false;
			logined = false;
			connecting = false;
		}

		return true;
	}

	@Override
	public String getName() {
		return tdName;
	}

	/**
	 * 返回接口状态
	 *
	 * @return
	 */
	public boolean alive() {
		return connected && logined;
	}

	/**
	 * 获取交易日
	 *
	 * @return
	 */
	public String getTradingDayDay() {
		return tradingDayStr;
	}

	/**
	 * 查询账户
	 */
	public void queryAccount() {
		if (cThostFtdcTraderApi == null) {
			log.info("{}尚未初始化,无法查询账户", tdName);
			return;
		}
		CThostFtdcQryTradingAccountField cThostFtdcQryTradingAccountField = new CThostFtdcQryTradingAccountField();
		cThostFtdcTraderApi.ReqQryTradingAccount(cThostFtdcQryTradingAccountField, reqID.incrementAndGet());
	}

	/**
	 * 查询持仓
	 */
	public void queryPosition() {
		if (cThostFtdcTraderApi == null) {
			log.info("{}尚未初始化,无法查询持仓", tdName);
			return;
		}

		CThostFtdcQryInvestorPositionField cThostFtdcQryInvestorPositionField = new CThostFtdcQryInvestorPositionField();
		// log.info("查询持仓");
		cThostFtdcQryInvestorPositionField.setBrokerID(brokerID);
		cThostFtdcQryInvestorPositionField.setInvestorID(userID);
		cThostFtdcTraderApi.ReqQryInvestorPosition(cThostFtdcQryInvestorPositionField, reqID.incrementAndGet());
	}

	/**
	 * 发单
	 *
	 * @param order
	 * @return
	 */
	public void onOrder(Order order) {
		System.err.println("call on order");
		if (cThostFtdcTraderApi == null) {
			log.info("{}尚未初始化,无法发单", tdName);
			return;
		}
		CThostFtdcInputOrderField cThostFtdcInputOrderField = new CThostFtdcInputOrderField();
		orderRef.incrementAndGet();
		cThostFtdcInputOrderField.setInstrumentID(order.symbol);
		cThostFtdcInputOrderField.setLimitPrice(order.price);
		cThostFtdcInputOrderField.setVolumeTotalOriginal(order.volume);

		cThostFtdcInputOrderField.setOrderPriceType(
				CtpConstant.priceTypeMap.getOrDefault(order.priceType, Character.valueOf('\0')));
		cThostFtdcInputOrderField
				.setDirection(CtpConstant.directionMap.getOrDefault(order.direction, Character.valueOf('\0')));
		cThostFtdcInputOrderField.setCombOffsetFlag(
				String.valueOf(CtpConstant.offsetMap.getOrDefault(order.offset, Character.valueOf('\0'))));
		cThostFtdcInputOrderField.setOrderRef(orderRef.get() + "");
		cThostFtdcInputOrderField.setInvestorID(userID);
		cThostFtdcInputOrderField.setUserID(userID);
		cThostFtdcInputOrderField.setBrokerID(brokerID);

		cThostFtdcInputOrderField
				.setCombHedgeFlag(String.valueOf(jctptraderapiv6v3v11x64Constants.THOST_FTDC_HF_Speculation));
		cThostFtdcInputOrderField.setContingentCondition(jctptraderapiv6v3v11x64Constants.THOST_FTDC_CC_Immediately);
		cThostFtdcInputOrderField.setForceCloseReason(jctptraderapiv6v3v11x64Constants.THOST_FTDC_FCC_NotForceClose);
		cThostFtdcInputOrderField.setIsAutoSuspend(0);
		cThostFtdcInputOrderField.setTimeCondition(jctptraderapiv6v3v11x64Constants.THOST_FTDC_TC_GFD);
		cThostFtdcInputOrderField.setVolumeCondition(jctptraderapiv6v3v11x64Constants.THOST_FTDC_VC_AV);
		cThostFtdcInputOrderField.setMinVolume(1);

		// 判断FAK FOK市价单
		if (RtConstant.PRICETYPE_FAK.equals(order.priceType)) {
			cThostFtdcInputOrderField.setOrderPriceType(jctptraderapiv6v3v11x64Constants.THOST_FTDC_OPT_LimitPrice);
			cThostFtdcInputOrderField.setTimeCondition(jctptraderapiv6v3v11x64Constants.THOST_FTDC_TC_IOC);
			cThostFtdcInputOrderField.setVolumeCondition(jctptraderapiv6v3v11x64Constants.THOST_FTDC_VC_AV);
		} else if (RtConstant.PRICETYPE_FOK.equals(order.priceType)) {
			cThostFtdcInputOrderField.setOrderPriceType(jctptraderapiv6v3v11x64Constants.THOST_FTDC_OPT_LimitPrice);
			cThostFtdcInputOrderField.setTimeCondition(jctptraderapiv6v3v11x64Constants.THOST_FTDC_TC_IOC);
			cThostFtdcInputOrderField.setVolumeCondition(jctptraderapiv6v3v11x64Constants.THOST_FTDC_VC_CV);
		}

		// if("IH1805".equals(order.getSymbol())) {
		// System.out.println("T2T-OrderBefore-"+System.nanoTime());
		// }
		cThostFtdcTraderApi.ReqOrderInsert(cThostFtdcInputOrderField, reqID.incrementAndGet());
		// if("IH1805".equals(order.getSymbol())) {
		// System.out.println("T2T-Order-"+System.nanoTime());
		// }
		String rtOrderID = tdName + "." + orderRef.get();
		// todo : how to trasfer orderID ?
	}

	// 撤单
	public void onCancelOrder(Order cancelOrder) {
		System.err.println("call on cancel order");
		if (cThostFtdcTraderApi == null) {
			log.info("{}尚未初始化,无法撤单", tdName);
			return;
		}
		CThostFtdcInputOrderActionField cThostFtdcInputOrderActionField = new CThostFtdcInputOrderActionField();

		cThostFtdcInputOrderActionField.setInstrumentID(cancelOrder.symbol);
		cThostFtdcInputOrderActionField.setExchangeID(cancelOrder.exchange);
		cThostFtdcInputOrderActionField.setOrderRef(cancelOrder.orderId);
		cThostFtdcInputOrderActionField.setFrontID(frontID);
		cThostFtdcInputOrderActionField.setSessionID(sessionID);

		cThostFtdcInputOrderActionField.setActionFlag(jctptraderapiv6v3v11x64Constants.THOST_FTDC_AF_Delete);
		cThostFtdcInputOrderActionField.setBrokerID(brokerID);
		cThostFtdcInputOrderActionField.setInvestorID(userID);

		cThostFtdcTraderApi.ReqOrderAction(cThostFtdcInputOrderActionField, reqID.incrementAndGet());
	}

	private void login() {
		if (loginFailed) {
			log.warn(tdName + "交易接口登录曾发生错误,不再登录,以防被锁");
		}

		if (StringUtils.isEmpty(brokerID) || StringUtils.isEmpty(userID) || StringUtils.isEmpty(password)) {
			log.error(tdName + "BrokerID UserID Password不允许为空");
			return;
		}

		if (!StringUtils.isEmpty(authCode) && !authStatus) {
			// 验证
			CThostFtdcReqAuthenticateField authenticateField = new CThostFtdcReqAuthenticateField();
			authenticateField.setAuthCode(authCode);
			authenticateField.setUserID(userID);
			authenticateField.setBrokerID(brokerID);
			authenticateField.setUserProductInfo(userProductInfo);
			cThostFtdcTraderApi.ReqAuthenticate(authenticateField, reqID.incrementAndGet());
		} else {
			// 登录
			CThostFtdcReqUserLoginField userLoginField = new CThostFtdcReqUserLoginField();
			userLoginField.setBrokerID(brokerID);
			userLoginField.setUserID(userID);
			userLoginField.setPassword(password);
			cThostFtdcTraderApi.ReqUserLogin(userLoginField, 0);
		}
	}

	// 前置机联机回报
	public void OnFrontConnected() {
		log.info(tdName + "交易接口前置机已连接");
		// 修改前置机连接状态为true
		connected = true;
		connecting = false;
		login();
	}

	// 前置机断开回报
	public void OnFrontDisconnected(int nReason) {
		log.info(tdName + "交易接口前置机已断开,Reason:" + nReason);
		this.connected = false;
	}

	// 登录回报
	public void OnRspUserLogin(CThostFtdcRspUserLoginField pRspUserLogin, CThostFtdcRspInfoField pRspInfo,
							   int nRequestID, boolean bIsLast) {
		if (pRspInfo.getErrorID() == 0) {
			log.info("{} 交易接口登录成功! TradingDay:{},SessionID:{},BrokerID:{},UserID:{}", tdName,
					pRspUserLogin.getTradingDay(), pRspUserLogin.getSessionID(), pRspUserLogin.getBrokerID(),
					pRspUserLogin.getUserID());
			this.sessionID = pRspUserLogin.getSessionID();
			this.frontID = pRspUserLogin.getFrontID();
			// 修改登录状态为true
			this.logined = true;
			tradingDayStr = pRspUserLogin.getTradingDay();
			log.info("{}交易接口获取到的交易日为{}", tdName, tradingDayStr);

			// 确认结算单
			CThostFtdcSettlementInfoConfirmField settlementInfoConfirmField = new CThostFtdcSettlementInfoConfirmField();
			settlementInfoConfirmField.setBrokerID(brokerID);
			settlementInfoConfirmField.setInvestorID(userID);
			cThostFtdcTraderApi.ReqSettlementInfoConfirm(settlementInfoConfirmField, reqID.incrementAndGet());

		} else {
			log.warn("{}交易接口登录回报错误! ErrorID:{},ErrorMsg:{}", tdName, pRspInfo.getErrorID(),
					pRspInfo.getErrorMsg());
			loginFailed = true;
		}

	}

	// 心跳警告
	public void OnHeartBeatWarning(int nTimeLapse) {
		log.warn(tdName + "交易接口心跳警告 nTimeLapse:" + nTimeLapse);
	}

	// 登出回报
	public void OnRspUserLogout(CThostFtdcUserLogoutField pUserLogout, CThostFtdcRspInfoField pRspInfo, int nRequestID,
								boolean bIsLast) {
		if (pRspInfo.getErrorID() != 0) {
			log.info("{}OnRspUserLogout!ErrorID:{},ErrorMsg:{}", tdName, pRspInfo.getErrorID(),
					pRspInfo.getErrorMsg());
		} else {
			log.info("{}OnRspUserLogout!BrokerID:{},UserID:{}", tdName, pUserLogout.getBrokerID(),
					pUserLogout.getUserID());

		}
		this.logined = false;
	}

	// 错误回报
	public void OnRspError(CThostFtdcRspInfoField pRspInfo, int nRequestID, boolean bIsLast) {
		log.error("{}交易接口错误回报!ErrorID:{},ErrorMsg:{},RequestID:{},isLast:{}", tdName, pRspInfo.getErrorID(),
				pRspInfo.getErrorMsg(), nRequestID, bIsLast);

	}

	// 验证客户端回报
	public void OnRspAuthenticate(CThostFtdcRspAuthenticateField pRspAuthenticateField, CThostFtdcRspInfoField pRspInfo,
								  int nRequestID, boolean bIsLast) {

		if (pRspInfo.getErrorID() == 0) {
			authStatus = true;
			log.info(tdName + "交易接口客户端验证成功");

			login();

		} else {
			log.warn("{}交易接口客户端验证失败! ErrorID:{},ErrorMsg:{}", tdName, pRspInfo.getErrorID(),
					pRspInfo.getErrorMsg());
		}

	}

	// 发单错误（柜台）
	public void OnRspOrderInsert(CThostFtdcInputOrderField pInputOrder, CThostFtdcRspInfoField pRspInfo, int nRequestID,
								 boolean bIsLast) {

		String symbol = pInputOrder.getInstrumentID();
		String exchange = CtpConstant.exchangeMapReverse.get(pInputOrder.getExchangeID());
		String rtSymbol = symbol + "." + exchange;
		String orderID = pInputOrder.getOrderRef();
		String rtOrderID = tdName + "." + orderID;
		String direction = CtpConstant.directionMapReverse.getOrDefault(pInputOrder.getDirection(),
				RtConstant.DIRECTION_UNKNOWN);
		String offset = CtpConstant.offsetMapReverse.getOrDefault(pInputOrder.getCombOffsetFlag(),
				RtConstant.OFFSET_UNKNOWN);
		double price = pInputOrder.getLimitPrice();
		int totalVolume = pInputOrder.getVolumeTotalOriginal();
		int tradedVolume = 0;
		String status = RtConstant.STATUS_REJECTED;
		String tradingDay = tradingDayStr;
		String orderDate = null;
		String orderTime = null;
		String cancelTime = null;
		String activeTime = null;
		String updateTime = null;


		// 发送委托事件
		log.error("{}交易接口发单错误回报(柜台)! ErrorID:{},ErrorMsg:{}", tdName, pRspInfo.getErrorID(),
				pRspInfo.getErrorMsg());

	}


	// 撤单错误回报（柜台）
	public void OnRspOrderAction(CThostFtdcInputOrderActionField pInputOrderAction, CThostFtdcRspInfoField pRspInfo,
								 int nRequestID, boolean bIsLast) {

		log.error("{}交易接口撤单错误（柜台）! ErrorID:{},ErrorMsg:{}", tdName, pRspInfo.getErrorID(),
				pRspInfo.getErrorMsg());
	}

	public void OnRspQueryMaxOrderVolume(CThostFtdcQueryMaxOrderVolumeField pQueryMaxOrderVolume,
										 CThostFtdcRspInfoField pRspInfo, int nRequestID, boolean bIsLast) {
	}

	// 确认结算信息回报
	public void OnRspSettlementInfoConfirm(CThostFtdcSettlementInfoConfirmField pSettlementInfoConfirm,
										   CThostFtdcRspInfoField pRspInfo, int nRequestID, boolean bIsLast) {

		if (pRspInfo.getErrorID() == 0) {
			log.warn("{}交易接口结算信息确认完成!", tdName);
		} else {
			log.error("{}交易接口结算信息确认出错! ErrorID:{},ErrorMsg:{}", tdName, pRspInfo.getErrorID(),
					pRspInfo.getErrorMsg());
		}

		// 查询所有合约
		/*  // todo : here bug, will terminated out c++
		CThostFtdcQryInstrumentField cThostFtdcQryInstrumentField = new CThostFtdcQryInstrumentField();
		cThostFtdcTraderApi.ReqQryInstrument(cThostFtdcQryInstrumentField, reqID.incrementAndGet());
		*/

	}

	// 持仓查询回报
	public void OnRspQryInvestorPosition(CThostFtdcInvestorPositionField pInvestorPosition,
										 CThostFtdcRspInfoField pRspInfo, int nRequestID, boolean bIsLast) {

		if (pInvestorPosition == null || StringUtils.isEmpty(pInvestorPosition.getInstrumentID())) {
			return;
		}
		String symbol = pInvestorPosition.getInstrumentID();
		String rtSymbol = symbol;
		if (contractExchangeMap.containsKey(symbol)) {
			rtSymbol = symbol + "." + contractExchangeMap.get(symbol);
		}

		/*
		// 获取持仓缓存
		String posName = tdName + symbol + pInvestorPosition.getPosiDirection();

		Position position;
		if (positionMap.containsKey(posName)) {
			position = positionMap.get(posName);
		} else {
			position = new Position();
			positionMap.put(posName, position);
			position.setSymbol(symbol);
			position.setRtSymbol(rtSymbol);
			position.setDirection(
					CtpConstant.posiDirectionMapReverse.getOrDefault(pInvestorPosition.getPosiDirection(), ""));
			position.setRtPositionName(gatewayID + rtSymbol + pInvestorPosition.getPosiDirection());
		}
		// 针对上期所持仓的今昨分条返回（有昨仓、无今仓）,读取昨仓数据
		if (pInvestorPosition.getYdPosition() > 0 && pInvestorPosition.getTodayPosition() == 0) {
			position.setYdPosition(pInvestorPosition.getPosition());
		}
		// 计算成本
		Integer size = contractSizeMap.get(symbol);
		Double cost = position.getPrice() * position.getPosition() * size;

		// 汇总总仓
		position.setPosition(position.getPosition() + pInvestorPosition.getPosition());
		position.setPositionProfit(position.getPositionProfit() + pInvestorPosition.getPositionProfit());

		// 计算持仓均价
		if (position.getPosition() != 0 && contractSizeMap.containsKey(symbol)) {
			position.setPrice((cost + pInvestorPosition.getPositionCost()) / (position.getPosition() * size));
		}

		if (RtConstant.DIRECTION_LONG.equals(position.getDirection())) {
			position.setFrozen(pInvestorPosition.getLongFrozen());
		} else {
			position.setFrozen(pInvestorPosition.getShortFrozen());
		}

		// 回报结束
		if (bIsLast) {
			for (Position tmpPosition : positionMap.values()) {
				// 发送持仓事件
				ctpGateway.emitPositon(tmpPosition);
			}

			// 清空缓存
			positionMap = new HashMap<>();
		}
		*/
	}

	// 账户查询回报
	public void OnRspQryTradingAccount(CThostFtdcTradingAccountField pTradingAccount, CThostFtdcRspInfoField pRspInfo,
									   int nRequestID, boolean bIsLast) {
		Account account = new Account();
		account.setAccountID(pTradingAccount.getAccountID());
		account.setAvailable(pTradingAccount.getAvailable());
		account.setCloseProfit(pTradingAccount.getCloseProfit());
		account.setCommission(pTradingAccount.getCommission());
		account.setGatewayID(tdName);
		account.setMargin(pTradingAccount.getCurrMargin());
		account.setPositionProfit(pTradingAccount.getPositionProfit());
		account.setPreBalance(pTradingAccount.getPreBalance());
		account.setRtAccountID(tdName + pTradingAccount.getAccountID());

		double balance = pTradingAccount.getPreBalance() - pTradingAccount.getPreCredit()
				- pTradingAccount.getPreMortgage() + pTradingAccount.getMortgage() - pTradingAccount.getWithdraw()
				+ pTradingAccount.getDeposit() + pTradingAccount.getCloseProfit() + pTradingAccount.getPositionProfit()
				+ pTradingAccount.getCashIn() - pTradingAccount.getCommission();

		account.setBalance(balance);
	}


	// 合约查询回报
	public void OnRspQryInstrument(CThostFtdcInstrumentField pInstrument, CThostFtdcRspInfoField pRspInfo,
								   int nRequestID, boolean bIsLast) {
		Contract contract = new Contract();
		contract.setGatewayID(tdName);
		contract.setSymbol(pInstrument.getInstrumentID());
		contract.setExchange(CtpConstant.exchangeMapReverse.get(pInstrument.getExchangeID()));
		contract.setRtSymbol(contract.getSymbol() + "." + contract.getExchange());
		contract.setName(pInstrument.getInstrumentName());

		contract.setSize(pInstrument.getVolumeMultiple());
		contract.setPriceTick(pInstrument.getPriceTick());
		contract.setStrikePrice(pInstrument.getStrikePrice());
		contract.setProductClass(CtpConstant.productClassMapReverse.getOrDefault(pInstrument.getProductClass(),
				RtConstant.PRODUCT_UNKNOWN));
		contract.setExpiryDate(pInstrument.getExpireDate());
		// 针对商品期权
		contract.setUnderlyingSymbol(pInstrument.getUnderlyingInstrID());
		// contract.setUnderlyingSymbol(pInstrument.getUnderlyingInstrID()+pInstrument.getExpireDate().substring(2,
		// pInstrument.getExpireDate().length()-2));

		if (RtConstant.PRODUCT_OPTION.equals(contract.getProductClass())) {
			if (pInstrument.getOptionsType() == '1') {
				contract.setOptionType(RtConstant.OPTION_CALL);
			} else if (pInstrument.getOptionsType() == '2') {
				contract.setOptionType(RtConstant.OPTION_PUT);
			}
		}
		contractExchangeMap.put(contract.getSymbol(), contract.getExchange());
		contractSizeMap.put(contract.getSymbol(), contract.getSize());

		if (bIsLast) {
			log.info(tdName + "交易接口合约信息获取完成!");
		}
	}

	// 委托回报
	public void OnRtnOrder(CThostFtdcOrderField pOrder) {
		System.err.println("in OnRtn");
		Order order = new Order();
		String newRef = pOrder.getOrderRef().replace(" ", "");
		// 更新最大报单编号
		orderRef = new AtomicInteger(Math.max(orderRef.get(), Integer.valueOf(newRef)));

		order.symbol = pOrder.getInstrumentID();
		order.exchange = CtpConstant.exchangeMapReverse.get(pOrder.getExchangeID());
		/*
		 * CTP的报单号一致性维护需要基于frontID, sessionID, orderID三个字段
		 * 但在本接口设计中,已经考虑了CTP的OrderRef的自增性,避免重复 唯一可能出现OrderRef重复的情况是多处登录并在非常接近的时间内（几乎同时发单
		 */
		order.orderId = pOrder.getOrderRef();
		order.direction = CtpConstant.directionMapReverse.get(pOrder.getDirection());
		order.offset = CtpConstant.offsetMapReverse.get(pOrder.getCombOffsetFlag().toCharArray()[0]);
		order.volume = pOrder.getVolumeTotalOriginal();
		order.tradedVolume = pOrder.getVolumeTraded();
		order.status = pOrder.getOrderStatus();
		order.msg = pOrder.getStatusMsg();
		order.tradingDay = pOrder.getTradingDay();
		order.insertTime = pOrder.getInsertTime();
		order.cancelTime = pOrder.getCancelTime();
		order.activeTime = pOrder.getActiveTime();
		order.updateTime = pOrder.getUpdateTime();
		writer.onReturnOrder(order);
	}

	// 成交回报
	public void OnRtnTrade(CThostFtdcTradeField pTrade) {

		Trade trade = new Trade();
		trade.setGatewayID(tdName);
		trade.setSymbol(pTrade.getInstrumentID());
		trade.setExchange(CtpConstant.exchangeMapReverse.get(pTrade.getExchangeID()));
		trade.setRtSymbol(trade.getSymbol() + "." + trade.getExchange());

		trade.setTradeID(pTrade.getTradeID());
		trade.setRtTradeID(tdName + "." + trade.getTradeID());

		trade.setOrderID(pTrade.getOrderRef());
		trade.setRtOrderID(tdName + "." + pTrade.getOrderRef());

		// 方向
		trade.setDirection(CtpConstant.directionMapReverse.getOrDefault(pTrade.getDirection(), ""));

		// 开平
		trade.setOffset(CtpConstant.offsetMapReverse.getOrDefault(pTrade.getOffsetFlag(), ""));

		trade.setPrice(pTrade.getPrice());
		trade.setVolume(pTrade.getVolume());
		trade.setTradeTime(pTrade.getTradeTime());
		trade.setTradeDate(pTrade.getTradeDate());

		writer.onReturnTrade(trade);
	}

	// 发单错误回报（交易所）
	public void OnErrRtnOrderInsert(CThostFtdcInputOrderField pInputOrder, CThostFtdcRspInfoField pRspInfo) {

		String symbol = pInputOrder.getInstrumentID();
		String exchange = CtpConstant.exchangeMapReverse.get(pInputOrder.getExchangeID());
		String rtSymbol = symbol + "." + exchange;
		String orderID = pInputOrder.getOrderRef();
		String rtOrderID = tdName + "." + orderID;
		String direction = CtpConstant.directionMapReverse.get(pInputOrder.getDirection());
		String offset = CtpConstant.offsetMapReverse.get(pInputOrder.getCombOffsetFlag().toCharArray()[0]);
		double price = pInputOrder.getLimitPrice();
		int totalVolume = pInputOrder.getVolumeTotalOriginal();
		int tradedVolume = 0;
		String status = RtConstant.STATUS_REJECTED;
		String tradingDay = tradingDayStr;
		String orderDate = null;
		String orderTime = null;
		String cancelTime = null;
		String activeTime = null;
		String updateTime = null;

		// todo : handle errorRtnOrder
		log.error("{}交易接口发单错误回报（交易所）! ErrorID:{},ErrorMsg:{}", tdName, pRspInfo.getErrorID(),
				pRspInfo.getErrorMsg());

	}

	// 撤单错误回报（交易所）
	public void OnErrRtnOrderAction(CThostFtdcOrderActionField pOrderAction, CThostFtdcRspInfoField pRspInfo) {
		log.error("{}交易接口撤单错误回报（交易所）! ErrorID:{},ErrorMsg:{}", tdName, pRspInfo.getErrorID(),
				pRspInfo.getErrorMsg());
	}

	public static void main(String[] args) throws InterruptedException {
		final String tdName = args[0];
		String ctpTdAddress = "tcp://" + args[1];
		Board board = new Board();
		CTPTd ctpTd = new CTPTd(board, ctpTdAddress, "9999", "125268", "140706",
				"", tdName);
		ctpTd.start();
	}

}