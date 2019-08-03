var app = new Vue({
    el:"#app",
    data:{
        username:""
    },
    methods: {
        getUsername: function () {
            axios.get("../login/getUsername.do").then(function (response) {
                app.username = response.data.username;
            });

        }
    },
    created(){
        //获取当前登录用户名
        this.getUsername();
    }
});