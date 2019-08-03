var app = new Vue({
    el: "#app",
    data: {
        //密码
        password: "",
        entity: {"username": "", "password": "", "phone": ""},
        //验证码
        smsCode: ""
    },
    methods: {
        //注册
        register: function () {
            if (this.entity.username == "") {
                alert("请输入用户名");
                return;
            }
            if (this.entity.password == "") {
                alert("请输入密码");
                return;
            }
            //判断两次密码是否一致
            if (this.password != this.entity.password) {
                alert("两次输入的密码不一致;请重新输入");
                return;
            }

            if (this.entity.phone == "") {
                alert("请输入手机号");
                return;
            }

            axios.post("user/add.do?smsCode=" + this.smsCode, this.entity).then(function (response) {
                if (response.data.success) {
                    alert(response.data.message);
                } else {
                    alert(response.data.message);
                }
            });

        },
        //发送验证码
        sendSmsCode: function () {

            if (this.entity.phone == null || this.entity.phone == "") {
                alert("请输入手机号");
                return;
            }

            axios.get("user/sendSmsCode.do?phone=" + this.entity.phone).then(function (response) {
                alert(response.data.message);
            });
        }
    },
    created() {
        //
    }
});

