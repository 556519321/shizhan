var app = new Vue({
    el:"#app",
    data:{
        //用户名
        username:"",
        //交易号
        outTradeNo:"",
        //支付总金额
        total_fee:0
    },
    methods:{
        //查询支付状态
        queryPayStatus: function(outTradeNo){
            axios.get("pay/queryPayStatus.do?outTradeNo=" + outTradeNo).then(function (response) {
                if(response.data.success){
                    location.href = "paysuccess.html?total_fee=" + app.total_fee;
                } else {
                    if ("支付超时" == response.data.message) {
                        //支付超时重新生成二维码
                        app.createNative();
                    } else {
                        location.href = "payfail.html";
                    }
                }
            });
        },
        //生成二维码
        createNative:function(){
            //获取支付日志id
            this.outTradeNo = this.getParameterByName("outTradeNo");
            axios.get("pay/createNative.do?outTradeNo=" + this.outTradeNo).then(function (response) {
                if ("SUCCESS" == response.data.result_code) {
                    //统一下单成功，二维码链接地址已经返回
                    app.total_fee = (response.data.total_fee / 100).toFixed(2);
                    //生成二维码
                    var qr = new QRious({
                        element: document.getElementById("qrious"),
                        size:250,
                        level:"M",
                        value:response.data.code_url
                    });

                    //查询支付状态
                    app.queryPayStatus(app.outTradeNo);

                } else {
                    alert("生成二维码失败！");
                }
            });
        },
        //查询用户名
        getUsername: function () {
            axios.get("cart/getUsername.do").then(function (response) {
                app.username = response.data.username;
            });

        },
        //根据参数名字获取参数
        getParameterByName: function (name) {
            return decodeURIComponent((new RegExp('[?|&]' + name + '=' + '([^&;]+?)(&|#|;|$)').exec(location.href) || [, ""])[1].replace(/\+/g, '%20')) || null
        }
    },
    created(){
        //获取用户名
        this.getUsername();
        //生成二维码
        this.createNative();
    }
});