<template>
    <ContentFiled>
        <table class="table table-striped table-hover" style="text-align: center;">
            <thead>
            <tr>
                <th>A/蓝方</th>
                <th>B/红方</th>
                <th>对战结果</th>
                <th>对战时间</th>
                <th>操作</th>
            </tr>
            </thead>
            <tbody>
            <tr v-for="record in records" :key="record.record.id">
                <td>
                    <img :src="record.APhoto" alt="" class="record-user-photo">
                    <span class="record-user-username">{{ record.AUsername }}</span>
                </td>
                <td>
                    <img :src="record.BPhoto" alt="" class="record-user-photo">
                    <span class="record-user-username">{{ record.BUsername }}</span>
                </td>
                <td>
                    {{ record.Result }}
                </td>
                <td>{{ record.record.createtime }}</td>
                <td>
                    <button type="button" class="btn btn-secondary" @click="open_record_video(record.record.id)">查看录像
                    </button>
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
                    <a class="page-link" href="#" @click="pull_page_data(max_page)">尾页</a>
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
import router from "@/router/index";

export default {
    name: "RecordIndex",
    components: {ContentFiled},
    setup() {
        const store = new useStore();
        let records = ref([]);
        let current_page = 1;
        let total_records = 0;
        let pages = ref([]);
        let max_page = ref('')
        const update_pages = () => {
            let max_pages = parseInt(Math.ceil(total_records / 10));
            max_page.value = max_pages
            let new_pages = [];
            for (let i = current_page - 2; i <= current_page + 2; i++) {
                if (i >= 1 && i <= max_pages) {
                    new_pages.push({
                        number: i,
                        is_active: i === current_page ? "active" : "",
                        max_pages: max_pages
                    });
                }
            }
            pages.value = new_pages;
        }

        const stringTo2DMap = map => {
            let g = [];
            for (let i = 0, k = 0; i < 13; i++) {
                let line = [];
                for (let j = 0; j < 14; j++, k++) {
                    if (map[k] === '0') line.push(0);
                    else line.push(1);
                }
                g.push(line);
            }
            return g;
        }

        const open_record_video = recordId => {
            for (const record of records.value) {
                console.log(record.record.asteps)
                if (record.record.id === recordId) {
                    // 在跳转之前要存下 game 信息，因为这个 game 信息全局唯一，所以进入 PKIndex 的时候需要重置一下。
                    store.commit("updateIsRecord", true);

                    store.commit("updateRecordLoser", record.record.loser)
                    // 更新下每個人的操作，然后在 GameMap 中判断。
                    store.commit("updateSteps", {
                        asteps: record.record.asteps,
                        bsteps: record.record.bsteps
                    })

                    store.commit("updateGameMap", {
                        gamemap: stringTo2DMap(record.record.map)
                    });

                    store.commit("updatePlayGameInfo", {
                        blue_id: record.record.aid,
                        blue_sx: record.record.asx,
                        blue_sy: record.record.asy,
                        red_id: record.record.bid,
                        red_sx: record.record.bsx,
                        red_sy: record.record.bsy,
                    });
                    store.commit("updateLoser", {
                        loser: record.record.loser
                    })
                    router.push({name: 'video', params: {recordId}})
                    break;
                }
            }
        }

        const pull_page_data = page => {
            current_page = page;
            $.ajax({
                url: "http://127.0.0.1:8080/record/get/list/" + page,
                type: 'get',
                headers: {
                    Authorization: 'voucher' + store.state.user.token,
                }, success(resp) {
                    records.value = resp.records;
                    total_records = resp.recordsCount;
                    update_pages()
                }, error(resp) {
                    console.log(resp)
                }
            });
        };
        pull_page_data(1);
        return {
            current_page,
            records,
            open_record_video,
            pull_page_data,
            pages,
            max_page
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