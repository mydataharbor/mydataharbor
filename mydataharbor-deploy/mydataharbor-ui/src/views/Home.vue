<template>
  <div>
    <el-container style="padding: 0 5%">
      <el-header>
        <el-menu mode="horizontal" router unique-opened :default-active="activeIndex">
          <el-menu-item index="/home">
            <i class="el-icon-menu"></i>
            <span slot="title">首页</span>
          </el-menu-item>
          <el-menu-item index="/cluster">
            <i class="el-icon-s-help"></i>
            <span slot="title">集群管理</span>
          </el-menu-item>

          <el-menu-item index="/plugin">
            <i class="el-icon-s-data"></i>
            <span slot="title">插件仓库</span>
          </el-menu-item>

        </el-menu>
      </el-header>
      <el-container style="height: 100%">

        <el-main>
          <!--首页home -->
          <DashBoard v-if="this.$router.currentRoute.path=='/home' || this.$router.currentRoute.path=='/'"></DashBoard>
          <router-view class="homeRouterView"/>
        </el-main>
      </el-container>
      <el-footer>


        <el-link style="float: right; color: #8492a6; font-size: 13px" href="https://www.mydataharbor.com"> &nbsp;&nbsp;官方网站 &nbsp;&nbsp;</el-link>

        <el-link style="float: right; color: #8492a6; font-size: 13px" href="/swagger-ui/#/"> &nbsp;&nbsp;swagger api文档 &nbsp;&nbsp;</el-link>

        <p style="float: right; color: #8492a6; font-size: 13px"> &nbsp;&nbsp; MyDataHarbor控制台版本：{{ consoleVersion }}  &nbsp;&nbsp;</p>

      </el-footer>
    </el-container>

  </div>
</template>

<script>
import DashBoard from '../components/dashboard/Dashboard'

export default {
  name: "Home",
  components: {DashBoard},
  data() {
    return {
      activeIndex: this.$router.currentRoute.path == "/" ? "/home" : this.$router.currentRoute.path,
      consoleVersion: ""
    }
  },
  mounted() {
    this.getRequest("/mydataharbor/dashboard/version").then(res=>{
      this.consoleVersion = res["console-version"];
    });
  },
  computed: {},
  methods: {}
}
</script>

<style scoped>
.homeRouterView {
  margin-top: 10px;
}

.homeHeader {
  background-color: #5f9ea0;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0px 15px;
  box-sizing: border-box;
}

.homeHeader .title {
  font-size: 30px;
  color: #f9ffff
}

.homeHeader {
  cursor: pointer;
}

.el-dropdown-link img {
  width: 38px;
  height: 38px;
  border-radius: 24px;
  margin-left: 8px;
}
</style>
