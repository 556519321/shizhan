
var app = new Vue({
    el:"#app",
    data:{
        username:""
    },
    methods:{
        getUsername: function () {
            axios.get("user/getUsername.do").then(function (response) {
                app.username=response.data.username;
            });

        }
    },
    created(){
        this.getUsername();
    }
});