<template>
  <div class="container">
    <div class="row">
      <div class="col-3">
        <div class="card" style="margin-top: 20px;">
          <div class="card-body">
            <img :src="$store.state.user.photo" alt="" style="width: 100%;">
          </div>
        </div>
      </div>
      <div class="col-9">
        <div class="card" style="margin-top: 20px;">
          <div class="card-header">
            <span style="font-size: 130%">我的Bot</span>
            <button type="button" class="btn btn-primary float-end" data-bs-toggle="modal"
                    data-bs-target="#add-bot-btn">
              创建Bot
            </button>

            <!-- Modal -->
            <div class="modal fade" id="add-bot-btn" tabindex="-1">
              <div class="modal-dialog modal-xl">
                <div class="modal-content">
                  <div class="modal-header">
                    <h5 class="modal-title">创建Bot</h5>
                    <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
                  </div>
                  <div class="modal-body">
                    <div class="mb-3">
                      <label for="add-bot-title" class="form-label">名称</label>
                      <input v-model="botadd.title" type="text" class="form-control" id="add-bot-title"
                             placeholder="请输入Bot名称">
                    </div>
                    <div class="mb-3">
                      <label for="add-bot-description" class="form-label">简介</label>
                      <textarea v-model="botadd.description" class="form-control" id="add-bot-description" rows="3"
                                placeholder="请输入Bot简介"></textarea>
                    </div>
                    <div class="mb-3">
                      <label for="add-bot-code" class="form-label">代码</label>
                      <VAceEditor
                          v-model:value="botadd.content"
                          @init="editorInit"
                          lang="c_cpp"
                          theme="textmate"
                          style="height: 300px"/>
                    </div>
                  </div>
                  <div class="modal-footer">
                    <div class="error-message">{{ botadd.resp_message }}</div>
                    <button type="button" class="btn btn-primary" @click="add_bot">创建</button>
                    <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">取消</button>
                  </div>
                </div>
              </div>
            </div>
          </div>
          <div class="card-body">
            <table class="table table-striped table-hover">
              <thead>
              <tr>
                <th>名称</th>
                <th>创建时间</th>
                <th>操作</th>
              </tr>
              </thead>
              <tbody>
              <tr v-for="bot in bots" :key="bot.id">
                <td>{{ bot.title }}</td>
                <td>{{ bot.createtime }}</td>
                <td>
                  <button type="button" class="btn btn-secondary" style="margin-right: 10px;" data-bs-toggle="modal"
                          :data-bs-target="'#look-bot-modal-' + bot.id">查看
                  </button>
                  <button type="button" class="btn btn-secondary" style="margin-right: 10px;" data-bs-toggle="modal"
                          :data-bs-target="'#update-bot-modal-' + bot.id">修改
                  </button>
                  <button type="button" class="btn btn-danger" @click="remove_bot(bot)">删除</button>

                  <!-- 修改 Bot 的模态框 -->
                  <div class="modal fade" :id="'update-bot-modal-' + bot.id" tabindex="-1">
                    <div class="modal-dialog modal-xl">
                      <div class="modal-content">
                        <div class="modal-header">
                          <h5 class="modal-title">创建Bot</h5>
                          <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
                        </div>
                        <div class="modal-body">
                          <div class="mb-3">
                            <label for="add_bot_title" class="form-label">名称</label>
                            <input v-model="bot.title" type="text" class="form-control" id="add_bot_title"
                                   placeholder="请输入Bot名称">
                          </div>
                          <div class="mb-3">
                            <label for="add_bot_description" class="form-label">简介</label>
                            <textarea v-model="bot.description" class="form-control" id="add_bot_description" rows="2"
                                      placeholder="请输入Bot简介"></textarea>
                          </div>
                          <div class="mb-3">
                            <label for="add_bot_code" class="form-label">代码</label>
                            <VAceEditor
                                v-model:value="bot.content"
                                @init="editorInit"
                                lang="c_cpp"
                                theme="textmate"
                                style="height: 400px;"/>
                          </div>
                        </div>
                        <div class="modal-footer">
                          <div class="error-message">{{ bot.resp_message }}</div>
                          <button type="button" class="btn btn-primary" @click="update_bot(bot)">保存修改</button>
                          <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">取消</button>
                        </div>
                      </div>
                    </div>
                  </div>

                  <!-- 修改 Bot 的模态框 -->
                  <div class="modal fade" :id="'look-bot-modal-' + bot.id" tabindex="-1">
                    <div class="modal-dialog modal-xl">
                      <div class="modal-content">
                        <div class="modal-header">
                          <h5 class="modal-title">查看Bot</h5>
                          <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
                        </div>
                        <div class="modal-body">
                          <div class="mb-3">
                            <label for="look_bot_title" class="form-label">名称</label>
                            <input v-model="bot.title" type="text" class="form-control" id="look_bot_title" disabled
                                   placeholder="请输入Bot名称">
                          </div>
                          <div class="mb-3">
                            <label for="look_bot_description" class="form-label">简介</label>
                            <textarea v-model="bot.description" class="form-control" id="look_bot_description" rows="2"
                                      disabled
                                      placeholder="请输入Bot简介"></textarea>
                          </div>
                          <div class="mb-3">
                            <label for="look_bot_code" class="form-label">代码</label>
                            <VAceEditor
                                readonly="true"
                                v-model:value="bot.content"
                                @init="editorInit"
                                lang="c_cpp"
                                theme="textmate"
                                style="height: 400px;"/>
                          </div>
                        </div>
                        <div class="modal-footer">
                          <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">取消</button>
                        </div>
                      </div>
                    </div>
                  </div>
                </td>
              </tr>
              </tbody>
            </table>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>


<script>
import $ from 'jquery'
import {useStore} from 'vuex'
import {reactive, ref} from 'vue'
import {Modal} from 'bootstrap/dist/js/bootstrap'
import {VAceEditor} from 'vue3-ace-editor';
import ace from 'ace-builds';


export default {
  components: {
    VAceEditor
  },
  setup() {
    ace.config.set(
        "basePath",
        "https://cdn.jsdelivr.net/npm/ace-builds@" + require('ace-builds').version + "/src-noconflict/")
    const store = useStore();
    let bots = ref([]);

    const botadd = reactive({
      title: "",
      description: "",
      content: "",
      resp_message: "",
    });

    const refresh_bots = () => {
      console.log("refresh")
      $.ajax({
        url: "http://127.0.0.1:8080/user/bot/getlist/",
        type: "get",
        headers: {
          Authorization: "voucher" + store.state.user.token,
        },
        success(resp) {
          bots.value = resp;
        }, error(resp) {
          console.log(resp)
          console.log("error")
        }
      })
    }

    refresh_bots();

    const add_bot = () => {
      botadd.resp_message = "";
      $.ajax({
        url: "http://127.0.0.1:8080/user/bot/add/",
        type: "post",
        data: {
          title: botadd.title,
          description: botadd.description,
          content: botadd.content,
        },
        headers: {
          Authorization: "voucher" + store.state.user.token,
        },
        success(resp) {
          if (resp.resp_message === "success") {
            botadd.title = "";
            botadd.description = "";
            botadd.content = "";
            Modal.getInstance("#add-bot-btn").hide();
            refresh_bots();
          } else {
            botadd.resp_message = resp.resp_message;
          }
        }
      })
    }

    const update_bot = (bot) => {
      botadd.resp_message = "";
      $.ajax({
        url: "http://127.0.0.1:8080/user/bot/update/",
        type: "post",
        data: {
          bot_id: bot.id,
          title: bot.title,
          description: bot.description,
          content: bot.content,
        },
        headers: {
          Authorization: "voucher" + store.state.user.token,
        },
        success(resp) {
          if (resp.resp_message === "success") {
            Modal.getInstance('#update-bot-modal-' + bot.id).hide();
            refresh_bots();
          } else {
            botadd.resp_message = resp.resp_message;
          }
        }
      })
    }

    const remove_bot = (bot) => {
      console.log("delete")
      $.ajax({
        url: "http://127.0.0.1:8080/user/bot/remove/",
        type: "post",
        data: {
          bot_id: bot.id,
        },
        headers: {
          Authorization: "voucher" + store.state.user.token,
        },
        success(resp) {
          console.log("success?");
          console.log(resp.resp_message);
          if (resp.resp_message === "success") {
            refresh_bots();
          } else {
            alert(resp.resp_message);
          }
        }, error(resp) {
          alert(resp.resp_message);
        }
      })
    }

    return {
      bots,
      botadd,
      add_bot,
      update_bot,
      remove_bot,
    }
  }
}
</script>

<style scoped>
.ace_editor {
  border: 1px solid lightgray;
  margin: auto;
  font-size: 18px;
}

div.error-message {
  color: red;
}
</style>
