package com.pinyougou.pay.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.wxpay.sdk.WXPayUtil;
import com.pinyougou.common.util.HttpClient;
import com.pinyougou.pay.service.PayService;
import org.springframework.beans.factory.annotation.Value;

import java.util.HashMap;
import java.util.Map;

@Service
public class PayServiceImpl implements PayService {

    @Value("${appid}")
    private String appid;
    @Value("${partner}")
    private String partner;
    @Value("${partnerkey}")
    private String partnerkey;
    @Value("${notifyurl}")
    private String notifyurl;

    @Override
    public Map<String, Object> createNative(String outTradeNo, String total_fee) {
        Map<String, Object> resultMap = new HashMap<>();

        try {
            //- 设置请求的参数
            Map<String, String> paramMap = new HashMap<>();
            //公众账号ID
            paramMap.put("appid", appid);
            //商户号
            paramMap.put("mch_id", partner);
            //随机字符串
            paramMap.put("nonce_str", WXPayUtil.generateNonceStr());
            //签名；在转换的时候动态生成
            //paramMap.put("sign", null);
            //商品描述
            paramMap.put("body", "108品优购");
            //商户订单号
            paramMap.put("out_trade_no", outTradeNo);
            //标价金额
            paramMap.put("total_fee", total_fee);
            //终端IP
            paramMap.put("spbill_create_ip", "127.0.0.1");
            //通知地址
            paramMap.put("notify_url", notifyurl);
            //交易类型
            paramMap.put("trade_type", "NATIVE");

            //生成签名和发送的xml内容
            String signedXml = WXPayUtil.generateSignedXml(paramMap, partnerkey);

            System.out.println("发送 统一下单 请求的参数为：" + signedXml);

            //- 发送请求
            HttpClient httpClient = new HttpClient("https://api.mch.weixin.qq.com/pay/unifiedorder");
            httpClient.setHttps(true);
            httpClient.setXmlParam(signedXml);
            httpClient.post();

            String content = httpClient.getContent();
            System.out.println("发送 统一下单 请求的返回结果为：" + content);

            //将返回xml转换为map对象
            Map<String, String> map = WXPayUtil.xmlToMap(content);

            resultMap.put("outTradeNo", outTradeNo);
            resultMap.put("total_fee", total_fee);
            resultMap.put("result_code", map.get("result_code"));
            resultMap.put("code_url", map.get("code_url"));

            //- 处理结果
        } catch (Exception e) {
            e.printStackTrace();
        }
        return resultMap;
    }

    @Override
    public Map<String, String> queryPayStatus(String outTradeNo) {
        try {
            //- 设置请求的参数
            Map<String, String> paramMap = new HashMap<>();
            //公众账号ID
            paramMap.put("appid", appid);
            //商户号
            paramMap.put("mch_id", partner);
            //随机字符串
            paramMap.put("nonce_str", WXPayUtil.generateNonceStr());
            //签名；在转换的时候动态生成
            //paramMap.put("sign", null);
            //商品描述
            //商户订单号
            paramMap.put("out_trade_no", outTradeNo);

            //生成签名和发送的xml内容
            String signedXml = WXPayUtil.generateSignedXml(paramMap, partnerkey);

            System.out.println("发送 查询订单 请求的参数为：" + signedXml);

            //- 发送请求
            HttpClient httpClient = new HttpClient("https://api.mch.weixin.qq.com/pay/orderquery");
            httpClient.setHttps(true);
            httpClient.setXmlParam(signedXml);
            httpClient.post();

            String content = httpClient.getContent();
            System.out.println("发送 查询订单 请求的返回结果为：" + content);

            //将返回xml转换为map对象
            return WXPayUtil.xmlToMap(content);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;

    }

    @Override
    public Map<String, String> closeOrder(String outTradeNo) {
        try {
            //- 设置请求的参数
            Map<String, String> paramMap = new HashMap<>();
            //公众账号ID
            paramMap.put("appid", appid);
            //商户号
            paramMap.put("mch_id", partner);
            //随机字符串
            paramMap.put("nonce_str", WXPayUtil.generateNonceStr());
            //签名；在转换的时候动态生成
            //paramMap.put("sign", null);
            //商品描述
            //商户订单号
            paramMap.put("out_trade_no", outTradeNo);

            //生成签名和发送的xml内容
            String signedXml = WXPayUtil.generateSignedXml(paramMap, partnerkey);

            System.out.println("发送 关闭订单 请求的参数为：" + signedXml);

            //- 发送请求
            HttpClient httpClient = new HttpClient("https://api.mch.weixin.qq.com/pay/closeorder");
            httpClient.setHttps(true);
            httpClient.setXmlParam(signedXml);
            httpClient.post();

            String content = httpClient.getContent();
            System.out.println("发送 关闭订单 请求的返回结果为：" + content);

            //将返回xml转换为map对象
            return WXPayUtil.xmlToMap(content);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new HashMap<>();
    }
}
