<template>
    <ContentFiled>
        <table class="table table-striped table-hover" style="text-align: center;">
            <thead>
            <tr>
                <th>玩家</th>
                <th>天梯分</th>
                <th>对局总数</th>
            </tr>
            </thead>
            <tbody>
            <tr v-for="record in records" :key="record.id">
                <td>
                    <img :src="record.photo" alt="" class="record-user-photo">
                    &nbsp;
                    <span class="record-user-username">{{ record.username }}</span>
                </td>
                <td>
                    <span class="record-user-username">{{ record.rating }}</span>
                </td>
                <td>
                    {{ record.count }}
                </td>
            </tr>
            </tbody>
        </table>
        <nav aria-label="...">
            <ul class="pagination" style="float: right;">

                <li class="page-item">
                    <a class="page-link" href="#" @click="pull_page_data(1)">首页</a>
                </li>

                <li :class="'page-item ' +page.is_active" v-for="page in pages" :key="page.number"
                    @click="pull_page_data(page.number)">
                    <a class="page-link" href="#">{{ page.number }}</a>
                </li>
                <li class="page-item">
                    <a class="page-link" href="#" @click="pull_page_data(total_pages)">尾页</a>
                </li>
            </ul>
        </nav>
    </ContentFiled>
</template>


<script>
import ContentFiled from "@/components/ContentFiled";
import $ from 'jquery'
import {useStore} from "vuex";
import {ref} from "vue";

export default {
    name: "RanklistIndex",
    components: {ContentFiled},
    setup() {
        const store = new useStore();
        let records = ref([]);
        let current_page = 1;
        let total_pages = ref('');
        let pages = ref([]);
        const update_pages = () => {
            let new_pages = [];
            for (let i = current_page - 2; i <= current_page + 2; i++) {
                if (i >= 1 && i <= total_pages.value) {
                    new_pages.push({
                        number: i,
                        is_active: i === current_page ? "active" : "",
                        max_pages: total_pages.value
                    });
                }
            }
            pages.value = new_pages;
        }

        const pull_page_data = page => {
            current_page = page;
            $.ajax({
                url: "http://127.0.0.1:8080/ranklist/list/" + page,
                type: 'get',
                headers: {
                    Authorization: 'voucher' + store.state.user.token,
                }, success(resp) {
                    console.log(resp.records)
                    records.value = resp.records;
                    total_pages.value = resp.total_pages;
                    update_pages()
                }, error(resp) {
                    console.log("error")
                    console.log(resp)
                }
            });
        };
        pull_page_data(1);
        return {
            current_page,
            records,
            pull_page_data,
            pages,
            total_pages,
        }
    }
}
</script>

<style scoped>
img.record-user-photo {
    width: 4vh;
    border-radius: 50%;
}
</style>