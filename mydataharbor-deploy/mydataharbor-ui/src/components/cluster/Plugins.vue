<template>
  <div>
    <!--安装插件-->
    <div style="margin-bottom: 10px">
      <el-button type="primary" size="small" @click="dialogFormVisible = true">安装插件</el-button>
    </div>
    <!--表格数据-->
    <el-table :data="plugins" border class="table"
              ref="multipleTable" header-cell-class-name="table-header">
      <el-table-column prop="pluginId" label="插件ID"  sortable></el-table-column>
      <el-table-column prop="pluginDescription" label="描述" ></el-table-column>
      <!--<el-table-column prop="pluginClass" label="类路径" width="400"></el-table-column>-->
      <el-table-column prop="version" label="版本" ></el-table-column>
      <el-table-column  label="依赖" >
        <template slot-scope="scop">
          <p v-for="dependency in scop.row.dependencies">【插件id:{{dependency.pluginId}},版本:{{dependency.pluginVersionSupport}},可选：{{dependency.optional}}】</p>
        </template>
      </el-table-column>
      <el-table-column prop="provider" label="提供者" ></el-table-column>

      <el-table-column label="操作" align="center">
        <template #default="scope">
          <el-link @click="unInstallPlugin(scope.row.pluginId)" type="primary" icon="el-icon-close">卸载</el-link>
        </template>
      </el-table-column>
    </el-table>

    <!--插件安装弹框-->
    <el-dialog title="插件安装" :visible.sync="dialogFormVisible" width="25%">
      <el-form :model="form" :rules="rules" ref="form" label-position="left">
        <el-form-item label="组名" hidden>
          <el-input v-model="form.groupName" autocomplete="off" disabled></el-input>
        </el-form-item>
        <el-form-item label="插件ID" prop="pluginId">
          <el-select filterable v-model="form.pluginId" placeholder="请选择插件id">
            <el-option-group
              v-for="group in allPlugins"
              :key="group.groupName"
              :label="group.groupName">
              <el-option
                v-for="item in group.plugins"
                :key="item.pluginId"
                :label="item.pluginId"
                :value="item.pluginId">
              </el-option>
            </el-option-group>
          </el-select>
        </el-form-item>

        <el-form-item label="插件版本" prop="version">
          <el-select v-model="form.version" filterable placeholder="请选择版本">
            <el-option v-for="item in allVersion" v-bind:key="item.version" :label="item.version"
                       :value="item.version">

              <span style="float: left">{{ item.version }}</span>
              <span style="float: right; color: #8492a6; font-size: 13px">{{ item.pluginDescription }}</span>

            </el-option>
          </el-select>
        </el-form-item>

        <el-form-item label="同步安装">
          <el-switch v-model="form.sync"></el-switch>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="cancel">取 消</el-button>
        <el-button type="primary" @click="submit">确 定</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
export default {
  name: "Plugins",
  //父组件传值
  props: ['groupName'],
  data() {
    return {
      plugins: [],
      pageNo: 1,
      pageSize: 10,
      pageTotal: 0,
      dialogFormVisible: false,
      form: {
        groupName: this.groupName,
        pluginId: '',
        version: '',
        sync: false  //是否同步上传
      },
      //插件列表
      pluginInstallList: [],
      pluginInfoMap: {},
      //form校验
      rules: {
        pluginId: [
          {required: true, message: "插件ID不能为空", trigger: "blur"}
        ],
        version: [
          {required: true, message: "版本不能为空", trigger: "blur"}
        ],
      },
      allPlugins: [],
      allVersion: []
    }
  },
  watch: {
    "form.pluginId": function (value) {

      this.form.version = "";

      this.allPlugins.forEach(item => {
        item.plugins.forEach(item2 => {
          if (item2.pluginId == value) {
            this.allVersion = item2.repoPlugins;
          }
        })
      });
    }
  },
  mounted() {
    this.initData();
    this.initPlugins();
  },
  methods: {
    //清空
    clearData() {
      this.form = {
        groupName: this.groupName,
        pluginId: '',
        version: '',
        sync: false
      }
    },
    //初始化下拉框数据
    initData() {
      this.getRequest("mydataharbor/plugin/listPlugins").then(res => {
        this.allPlugins = res.data["本地存储库"];
        console.log(this.allPlugins)

      })
    },
    //初始化插件列表
    initPlugins() {
      this.getRequest("mydataharbor/node/groupList").then(res => {
        this.plugins = res.data[this.groupName].installedPlugins;
        console.log(this.plugins)
      })
    },
    //卸载插件
    unInstallPlugin(pluginId) {
      console.log("uninstall" + pluginId);
      this.postRequest("/mydataharbor/node/uninstallPlugin?groupName=" + this.groupName + "&pluginId=" + pluginId).then(res => {
        if (res.code == 0) {
          this.$message.info("插件卸载成功！");
          this.initPlugins();
        }
      })
    },
    //安装插件弹框按钮事件
    submit() {
      //安装插件请求
      this.$refs.form.validate(valid => {
        if (valid) {
          let url = "mydataharbor/node/installPlugin?groupName=" + this.form.groupName +
            "&pluginId=" + this.form.pluginId + "&version=" + this.form.version + "&sync=" + this.form.sync;
          this.postRequest(url).then(res => {
            if (res.code == 0) {
              this.$message.info("插件安装成功！");
              this.initPlugins();
            } else {
              this.$message.error(res.msg);
            }
          });
          this.dialogFormVisible = false;
          this.clearData();
        } else {
          return false;
        }
      });
    },
    cancel() {
      this.dialogFormVisible = false;
      this.clearData();
    },
    //分页处理
    handlePageChange() {

    }
  }
}
</script>

<style scoped>

</style>
