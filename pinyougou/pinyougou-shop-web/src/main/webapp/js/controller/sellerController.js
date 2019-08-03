var app = new Vue({
    el: "#app",
    data:{
        entity:{}
    },
    methods:{
        add:function(){
            axios.post("seller/add.do", this.entity).then(function (response) {
                if(response.data.success){
                    //注册成功，跳转到登录页面
                    location.href = "shoplogin.html";
                } else {
                    alert(response.data.message);
                }
            });
        }
    }
});