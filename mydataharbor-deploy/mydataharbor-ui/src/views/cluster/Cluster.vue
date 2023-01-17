<template>
  <div class="app-container mixin-components-container">
    <el-card class="box-card" style="padding: 5px">
      <el-tabs tab-position="left" v-model="selectGroup" @tab-click="groupChange">
        <el-tab-pane :label="item.groupName" :name="item.groupName" v-for="(item) in cluster" v-bind:key="item.id" :lazy="true">
          <el-tabs active-name="first" style="margin-left: 10px" v-model="selectOption" @tab-click="optionChange">
            <!--节点管理-->
            <el-tab-pane label="节点" name="节点" :lazy="true">
              <Node :node-list="item.nodeInfos"></Node>
            </el-tab-pane>
            <!--插件管理-->
            <el-tab-pane label="插件" name="插件" :lazy="true">
              <Plugins :group-name="item.groupName" ></Plugins>
            </el-tab-pane>
            <!--任务管理-->
            <el-tab-pane label="任务" name="任务" :lazy="true">
              <Tasks :group-name="item.groupName"></Tasks>
            </el-tab-pane>

            <!--操作-->
            <el-tab-pane label="操作" name="操作" :lazy="true">
              <Operation :group-name="item.groupName"></Operation>
            </el-tab-pane>
          </el-tabs>
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<script>
import Node from "../../components/cluster/Node";
import Plugins from "../../components/cluster/Plugins";
import Tasks from "../../components/cluster/Tasks"
import Script from "../../components/cluster/Script"
import Operation from "../../components/cluster/Operation.vue"

export default {
  name: "Cluster",
  components: {Tasks, Plugins, Node, Script, Operation},
  data() {
    return {
      cluster: {},
      selectGroup: "",
      selectOption: "节点"
    }
  },
  mounted() {
    this.initData();
  },
  methods: {
    initData() {
      this.getRequest("mydataharbor/node/groupList").then(res => {
        //console.log(res);
        if (res.code == 0) {
          this.cluster = res.data;
          let query = this.$router.history.current.query;
          //对象的拷贝
          let newQuery = JSON.parse(JSON.stringify(query));
          if (newQuery.selectOption != null) {
            this.selectOption = newQuery.selectOption;
          }
          this.selectGroup = newQuery.selectGroup;
          var firstKey = "";
          var count = 0;
          var containSelectGroup = false;
          for (var key in this.cluster) {
            if (count == 0) {
              firstKey = key;
            }
            count++
            if (this.selectGroup != null && this.selectGroup == key) {
              containSelectGroup = true;
            }
          }
          if (!containSelectGroup) {
            this.selectGroup = firstKey;
          }
        }
      })
    },

    groupChange(tab, event) {
      let query = this.$router.history.current.query;
      let path = this.$router.history.current.path;
      //对象的拷贝
      let newQuery = JSON.parse(JSON.stringify(query));
      // 地址栏的参数值赋值
      newQuery.selectGroup = tab.name;
      this.$router.push({path, query: newQuery});
    },

    optionChange(tab, event) {
      let query = this.$router.history.current.query;
      let path = this.$router.history.current.path;
      //对象的拷贝
      let newQuery = JSON.parse(JSON.stringify(query));
      // 地址栏的参数值赋值
      newQuery.selectOption = tab.name;
      this.$router.push({path, query: newQuery});
    }
  }
}
</script>

<style scoped>
/deep/ .el-tabs--left .el-tabs__item.is-left {
  text-align: left;
}
</style>
