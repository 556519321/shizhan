var app = new Vue({
    el: "#app",
    data: {
        //当前登录用户名
        username: "",
        //交易号
        outTradeNo: "",
        //支付总金额
        totalFee: 0
    },
    methods: {
        //查询支付状态
        queryPayStatus: function (outTradeNo) {
            axios.get("pay/queryPayStatus.do?outTradeNo=" + outTradeNo+"&r="+Math.random()).then(function (response) {
                if(response.data.success) {
                    location.href = "paysuccess.html?totalFee="+app.totalFee;
                } else {
                    if("支付超时"==response.data.message) {
                        location.href = "paytimeout.html";
                    } else {
                        //支付失败页面
                        location.href = "payfail.html";
                    }
                }
            });
        },
        //生成二维码
        createNative: function () {
            //获取交易号
            this.outTradeNo = this.getParameterByName("outTradeNo");
            axios.get("pay/createNative.do?outTradeNo="+this.outTradeNo).then(function (response) {
                if ("SUCCESS" == response.data.result_code) {//创建支付地址成功
                    //计算总金额
                    app.totalFee = (response.data.total_fee / 100).toFixed(2);

                    //生成支付地址的二维码
                    var qr = new QRious({
                        element: document.getElementById("qrious"),
                        size: 250,
                        level: "M",
                        value: response.data.code_url
                    });

                    //查询支付状态
                    app.queryPayStatus(app.outTradeNo);
                } else {
                    alert("生成二维码失败！");
                }
            });
        },
        getUsername: function () {
            axios.get("seckillGoods/getUsername.do").then(function (response) {
                app.username = response.data.username;
            });
        },
        //根据参数名字获取参数
        getParameterByName: function (name) {
            return decodeURIComponent((new RegExp('[?|&]' + name + '=' + '([^&;]+?)(&|#|;|$)').exec(location.href) || [, ""])[1].replace(/\+/g, '%20')) || null
        }
    },
    created() {
        this.getUsername();
        //创建二维码
        this.createNative();
    }
});