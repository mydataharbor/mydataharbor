<template>
  <div class="app-container mixin-components-container">
    <el-card class="box-card" style="padding: 5px">
      <el-tabs v-model="pluginTabActive" type="card">
        <el-tab-pane label="本地插件" name="localPlugin">
          <!--上传插件与搜索-->
          <div class="handle-box">
            <!--上传插件-->
            <div>
              <el-upload
                class="upload-demo"
                action="/mydataharbor/plugin/uploadPlugin"
                :on-success="handleSuccess"
                :on-error="handleError"
                :show-file-list="showFile">
                <el-button size="small" type="primary">上传插件</el-button>
                <div slot="tip" class="el-upload__tip">只能上传jar文件，且不超过500M</div>
              </el-upload>
            </div>

          </div>

          <el-row :gutter="20">
            <el-col :span="12">
              <el-col :span="24" v-for="(group,index) in tableData" :key="index" v-if="index%2==0">
                <el-card class="box-card">
                  <div slot="header" class="clearfix" style="height: 50px">
                    <img v-if="group.imageBase64!=null" :src="group.imageBase64" style="height: 50px"/>
                    {{ group.groupName }}
                  </div>

                  <el-collapse :value="group.plugins[0].pluginId">
                    <el-collapse-item v-for="(plugin,index) in group.plugins" :key="index" :title="plugin.pluginId" :name="plugin.pluginId">
                      <el-table :fit="true" v-fit-columns :data="plugin.repoPlugins" border class="table" row-key="pluginId"
                                ref="multipleTable" header-cell-class-name="table-header" >
                        <el-table-column prop="pluginId" label="插件id" ></el-table-column>
                        <el-table-column prop="version" label="版本" ></el-table-column>
                        <el-table-column show-overflow-tooltip label="依赖">
                          <template slot-scope="scop">
                            <p style=" color: #8492a6; font-size: 13px" v-for="dependency in scop.row.dependencies">【插件id:{{dependency.pluginId}},版本:{{dependency.pluginVersionSupport}},可选：{{dependency.optional}}】</p>
                          </template>
                        </el-table-column>
                        <el-table-column show-overflow-tooltip show-overflow-tooltip prop="pluginDescription" label="描述" ></el-table-column>

                        <el-table-column label="操作" align="center">
                          <template #default="scope">
                            <el-link
                              :href="'/mydataharbor/plugin/downloadPlugin?pluginId=' + scope.row.pluginId + '&version=' + scope.row.version"
                              type="primary" icon="el-icon-download" target="_blank">下载
                            </el-link>
                          </template>
                        </el-table-column>
                      </el-table>
                    </el-collapse-item>

                  </el-collapse>

                </el-card>
              </el-col>
            </el-col>
            <el-col :span="12">
              <el-col :span="24" v-for="(group,index) in tableData" :key="index" v-if="index%2==1">
                <el-card class="box-card">
                  <div slot="header" class="clearfix" style="height: 50px">
                    <img v-if="group.imageBase64!=null" :src="group.imageBase64" style="height: 50px"/>
                    {{ group.groupName }}
                  </div>

                  <el-collapse :value="group.plugins[0].pluginId">
                    <el-collapse-item v-for="(plugin,index) in group.plugins" :key="index" :title="plugin.pluginId" :name="plugin.pluginId">
                      <el-table :fit="true" v-fit-columns :data="plugin.repoPlugins" border class="table" row-key="pluginId"
                                ref="multipleTable" header-cell-class-name="table-header" >
                        <el-table-column prop="pluginId" label="插件id" ></el-table-column>
                        <el-table-column prop="version" label="版本"></el-table-column>
                        <el-table-column show-overflow-tooltip label="依赖">
                          <template slot-scope="scop">
                            <p style=" color: #8492a6; font-size: 13px" v-for="(dependency,index) in scop.row.dependencies" :key="index">【插件id:{{dependency.pluginId}},版本:{{dependency.pluginVersionSupport}},可选：{{dependency.optional}}】</p>
                          </template>
                        </el-table-column>
                        <el-table-column show-overflow-tooltip prop="pluginDescription" label="描述"></el-table-column>

                        <el-table-column label="操作" align="center">
                          <template #default="scope">
                            <el-link
                              :href="'/mydataharbor/plugin/downloadPlugin?pluginId=' + scope.row.pluginId + '&version=' + scope.row.version"
                              type="primary" icon="el-icon-download" target="_blank">下载
                            </el-link>
                          </template>
                        </el-table-column>
                      </el-table>
                    </el-collapse-item>

                  </el-collapse>

                </el-card>
              </el-col>
            </el-col>
          </el-row>


          <!--表格数据-->


        </el-tab-pane>

        <el-tab-pane label="插件市场" name="cloudPlugin">

          <div class="handle-box">

            <div>
              <el-form :inline="true" :model="repoForm" class="demo-form-inline">
                <el-form-item label="email">
                  <el-input v-model="repoForm.config.email" placeholder="email"></el-input>
                </el-form-item>
                <el-form-item label="token">
                  <el-input v-model="repoForm.config.token" placeholder="token" type="password"></el-input>
                </el-form-item>

                <el-form-item>
                  <el-button type="primary" @click="configPluginRepo">修改</el-button>

                </el-form-item>

                <el-form-item>
                  <a target="_blank" href="https://www.mydataharbor.com/user/info.html">获取账户Token</a>
                </el-form-item>


              </el-form>
            </div>

          </div>
          <el-row :gutter="20">
            <el-col :span="12">
              <el-col :span="24" v-for="(group,index) in cloudData" :key ="index" v-if="index%2==0">
                <el-card class="box-card">
                  <div slot="header" class="clearfix" style="height: 50px">
                    <img v-if="group.imageBase64!=null" :src="group.imageBase64" style="height: 50px"/>
                    {{ group.groupName }}
                  </div>

                  <el-collapse :value="group.plugins[0].pluginId">
                    <el-collapse-item v-for="(plugin,index) in group.plugins" :key="index" :title="plugin.pluginId" :name="plugin.pluginId">
                      <el-table v-fit-columns :data="plugin.repoPlugins" border class="table" row-key="pluginId"
                                ref="multipleTable" header-cell-class-name="table-header">
                        <el-table-column prop="pluginId" label="插件id" ></el-table-column>
                        <el-table-column prop="version" label="版本" ></el-table-column>
                        <el-table-column label="授权" >
                          <template #default="scope">
                            <el-tag v-if="scope.row.authed" size="small">已授权</el-tag>
                            <el-tag v-else size="small" type="warning">未授权</el-tag>
                          </template>
                        </el-table-column>
                        <el-table-column show-overflow-tooltip label="依赖">
                          <template slot-scope="scop">
                            <p v-for="(dependency,index) in scop.row.dependencies" :key="index">【插件id:{{dependency.pluginId}},版本:{{dependency.pluginVersionSupport}},可选：{{dependency.optional}}】</p>
                          </template>
                        </el-table-column>
                        <el-table-column show-overflow-tooltip prop="pluginDescription" label="描述"></el-table-column>

                        <el-table-column label="操作" align="center">
                          <template #default="scope">
                            <el-link
                              @click="downloadToLocal('/mydataharbor/plugin/downloadToLocal?pluginId=' + scope.row.pluginId + '&version=' + scope.row.version + '&repoType=MyDataHarbor-Reporsitory')"
                              type="primary" icon="el-icon-download">
                              下载到本地
                            </el-link>
                          </template>
                        </el-table-column>
                      </el-table>
                    </el-collapse-item>

                  </el-collapse>

                </el-card>
              </el-col>
            </el-col>
            <el-col :span="12">
              <el-col :span="24" v-for="(group,index) in cloudData" :key="index" v-if="index%2==1">
                <el-card class="box-card">
                  <div slot="header" class="clearfix" style="height: 50px">
                    <img v-if="group.imageBase64!=null" :src="group.imageBase64" style="height: 50px"/>
                    {{ group.groupName }}
                  </div>

                  <el-collapse :value="group.plugins[0].pluginId">
                    <el-collapse-item v-for="(plugin,index) in group.plugins" :key="index" :title="plugin.pluginId" :name="plugin.pluginId">
                      <el-table v-fit-columns :data="plugin.repoPlugins" border class="table" row-key="pluginId"
                                ref="multipleTable" header-cell-class-name="table-header">
                        <el-table-column prop="pluginId" label="插件id" ></el-table-column>
                        <el-table-column prop="version" label="版本" ></el-table-column>
                        <el-table-column label="授权">
                          <template #default="scope">
                            <el-tag v-if="scope.row.authed" size="small">已授权</el-tag>
                            <el-tag v-else size="small" type="warning">未授权</el-tag>
                          </template>
                        </el-table-column>
                        <el-table-column show-overflow-tooltip label="依赖">
                          <template slot-scope="scop">
                            <p v-for="(dependency,index) in scop.row.dependencies" :key="index">【插件id:{{dependency.pluginId}},版本:{{dependency.pluginVersionSupport}},可选：{{dependency.optional}}】</p>
                          </template>
                        </el-table-column>
                        <el-table-column show-overflow-tooltip prop="pluginDescription" label="描述"></el-table-column>

                        <el-table-column label="操作" align="center">
                          <template #default="scope">
                            <el-link
                              @click="downloadToLocal('/mydataharbor/plugin/downloadToLocal?pluginId=' + scope.row.pluginId + '&version=' + scope.row.version + '&repoType=MyDataHarbor-Reporsitory')"
                              type="primary" icon="el-icon-download">
                              下载到本地
                            </el-link>
                          </template>
                        </el-table-column>
                      </el-table>
                    </el-collapse-item>

                  </el-collapse>

                </el-card>
              </el-col>
            </el-col>

          </el-row>

        </el-tab-pane>

      </el-tabs>


    </el-card>
  </div>
</template>

<script>

export default {
  name: "Plugin",
  data() {
    return {
      query: {
        pageNo: 1,
        pageSize: 10,
        pluginId: '',
        version: '',
        des: ''
      },
      tableData: [],
      cloudData: [],
      pageTotal: 0,
      //是否显示上传进度
      showFile: true,
      pluginTabActive: "localPlugin",
      repoForm: {
        repoName: "",
        config: {
          email: "",
          token: ""
        }
      }
    }
  },
  mounted() {
    this.initData();
  },
  methods: {
    initData() {
      console.log("loading info……");
      this.getRequest("mydataharbor/plugin/listPlugins", this.query).then(res => {
        this.tableData = res.data["本地存储库"];
        this.cloudData = res.data["MyDataHarbor官方插件存储库"];
        console.log(res);
      })

      this.getRequest("mydataharbor/plugin/queryPluginRepoConfig", this.query).then(res => {
        this.repoForm.config = res.data["MyDataHarbor-Reporsitory"]["config"];
        console.log(res);
      })
    },

    downloadToLocal(url) {
      this.$message.warning("下载插件需要不少时间，请耐心等待！");
      this.setLoadingMsg("服务器带宽有限，努力下载中...")
      this.getRequest(url).then(res => {
        if (res.code == 0) {
          this.$message.info("下载成功！");
          this.initData();
        } else {
          this.$message.warning(res.msg);
        }
      })
    },

    configPluginRepo() {
      let formData = {};
      this.repoForm['repoName'] = "MyDataHarbor-Reporsitory";
      this.postRequest("mydataharbor/plugin/configPluginRepo", this.repoForm).then(res => {
        if (res.code == 0) {
          this.$message.info("Token修改成功！");
          this.initData();
        } else {
          this.$message.warning(res.msg);
        }
      });
    },

    clear() {
      this.initData();
    },

    //插件上传
    handleSuccess(response, fileList) {
      console.log("success");
      if (response.code == -1) {
        this.$message.error(response.msg);
        this.showFile = false;
      }

    },
    handleError(err, file, fileList) {
      console.log("error" + err);
      console.log(file);
      console.log(fileList);
    }
  }
}
</script>

<style scoped>
.handle-box {
  display: flex;
  justify-content: space-between;
  margin-bottom: 20px;
}

.handle-input {
  width: 150px;
  display: inline-block;
}

.table {
  width: 100%;
  font-size: 14px;
}

.mr10 {
  margin-right: 10px;
}
</style>
