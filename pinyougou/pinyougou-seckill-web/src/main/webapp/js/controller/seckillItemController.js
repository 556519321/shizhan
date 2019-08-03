var app = new Vue({
    el:"#app",
    data:{
        //用户名
        username:"",
        //秒杀商品
        entity:{},
        //倒计时字符串
        timestring:""
    },
    methods: {
        submitOrder: function () {
            axios("seckillOrder/submitOrder.do?seckillGoodsId="+this.entity.id).then(function (response) {
                if(response.data.success){
                    alert("提交订单成功；请在1分钟内完成支付");
                    location.href = "pay.html?outTradeNo=" + response.data.message;
                } else {
                    alert(response.data.message);
                }
            });
        },
        //根据秒杀商品id获取秒杀商品
        findOne:function(){
            //获取地址栏中的商品id
            var seckillGoodsId = this.getParameterByName("id");
            axios.get("seckillGoods/findOne/"+seckillGoodsId+".do").then(function (response) {
                app.entity = response.data;

                //倒计时总秒数
                var allSeconds = Math.floor((new Date(response.data.endTime).getTime() - new Date().getTime()) / 1000);
                var task = setInterval(function () {
                    if (allSeconds > 0) {
                        allSeconds = allSeconds - 1;
                        //转换倒计时总秒数为 **天**:**:** 的格式并在页面展示
                        app.timestring = app.convertTimeString(allSeconds);
                    } else {
                        clearInterval(task);
                        alert("秒杀活动已结束。");
                    }
                }, 1000);
            });
        },
        //转化为格式化的时间字符串
        convertTimeString: function (allSeconds) {
            //天数
            var days = Math.floor(allSeconds / (60 * 60 * 24));
            //时
            var hours = Math.floor((allSeconds - days * 60 * 60 * 24) / (60 * 60));
            //分
            var minutes = Math.floor((allSeconds - days * 60 * 60 * 24 - hours * 60 * 60) / 60);
            //秒
            var seconds = allSeconds - days * 60 * 60 * 24 - hours * 60 * 60 - minutes * 60;

            var str = "";
            if (days > 0) {
                str = days + "天";
            }
            return str + hours + ":" + minutes + ":" + seconds;
        },
        //查询秒杀商品列表
        findList:function () {
            axios.get("seckillGoods/findList.do").then(function (response) {
                app.entityList = response.data;
            });
        },
        getUsername: function () {
            axios.get("seckillGoods/getUsername.do").then(function (response) {
                app.username=response.data.username;
            });
        },
        //根据参数名字获取参数
        getParameterByName: function (name) {
            return decodeURIComponent((new RegExp('[?|&]' + name + '=' + '([^&;]+?)(&|#|;|$)').exec(location.href) || [, ""])[1].replace(/\+/g, '%20')) || null
        }
    },
    created(){
        this.getUsername();
        this.findOne();
    }
});