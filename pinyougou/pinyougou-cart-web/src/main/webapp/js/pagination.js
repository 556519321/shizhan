var pageComponent = Vue.extend({
    template: `<nav aria-label="Page navigation">
        <ul class="pagination">
            <li :class="{\'disabled\':current==1}">
                <a href="javascript:;" @click="goPage(current==1?1:current-1)" aria-label="Previous">
                    <span aria-hidden="true">&laquo;</span>
                </a>
            </li>
            <li v-for="page in showPageBtn" :class="{\'active\':page==current}">
                <a href="javascript:;" v-if="page" @click="goPage(page)">{{page}}</a>
                <a href="javascript:;" v-else>···</a>
            </li>
            <li :class="{\'disabled\':current==pages}">
                <a href="javascript:;" @click="goPage(current==pages?pages:current+1)" aria-label="Next">
                    <span aria-hidden="true">&raquo;</span>
                </a>
            </li>
            <li> <a style="border: 0; background: none;color: #777"> 共 {{total}} 条；跳转至<input @keyup.enter="goPage(pagex)" v-model.number="pagex" style="width: 40px; padding-left:3px;" ></a></li>
        </ul>
    </nav>`, props: {total: {type: Number, default: 0}, pagesize: {type: Number, default: 10}, current: {type: Number, default: 1}}, data() {
        return {pages:0, pagex:1}
    }, computed: {
        showPageBtn() {
            //总页数
            this.pages = Math.floor((this.total+this.pagesize-1)/this.pagesize);
            this.pagex = this.current;

            let pageNum = this.pages;
            let index = this.current;
            let arr = [];
            if (pageNum <= 5) {
                for (let i = 1; i <= pageNum; i++) {
                    arr.push(i)
                }
                return arr
            }
            if (index <= 2) return [1, 2, 3, 0, pageNum];
            if (index >= pageNum - 1) return [1, 0, pageNum - 2, pageNum - 1, pageNum];
            if (index === 3) return [1, 2, 3, 4, 0, pageNum];
            if (index === pageNum - 2) return [1, 0, pageNum - 3, pageNum - 2, pageNum - 1, pageNum];
            return [1, 0, index - 1, index, index + 1, 0, pageNum];
        }
    }, methods: {
        goPage(page) {
            if (0 < page && page <= this.pages) {
                //console.log(page);
                //this.current = page;
                this.$emit('navpage', page);
            } else {
                console.log('Already in the current page');
            }
        }
    }
});
Vue.component('navigation', pageComponent);