var app = new Vue({
    el:"#app",
    data:{
        //用户名
        username:"",
        //秒杀商品列表
        entityList:[]
    },
    methods: {
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
        }
    },
    created(){
        this.getUsername();
        this.findList();
    }
});