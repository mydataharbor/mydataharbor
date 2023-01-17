<template>
  <div>
    <!--表格数据-->
    <el-table :data="nodes" border class="table"
              ref="multipleTable" header-cell-class-name="table-header">
      <el-table-column prop="nodeName" label="节点名"  align="center" sortable></el-table-column>
      <el-table-column prop="ip" label="IP"></el-table-column>
      <el-table-column prop="port" label="端口"></el-table-column>
      <el-table-column prop="hostName" label="hostName"></el-table-column>
      <el-table-column label="运行时间" >
        <template #default="scope">
          <span>{{formatDuring(scope.row.startTime)}}</span>
        </template>
      </el-table-column>
      <el-table-column prop="taskNum" label="任务数" ></el-table-column>
      <el-table-column label="leader" >
        <template #default="scope">
          <el-tag v-if="scope.row.leader" type="success">true</el-tag>
          <el-tag v-else>false</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="javaVersion" label="javaVersion"></el-table-column>
      <el-table-column prop="version" label="version"></el-table-column>

      <el-table-column prop="osName" label="osName"></el-table-column>
      <el-table-column prop="osArch" label="osArch"></el-table-column>
    </el-table>
    <div class="pagination">
      <el-pagination background layout="sizes, total, prev, pager, next" :current-page="pageNo"
                     :page-sizes="[5,10,20,50]" :page-size="pageSize" :total="pageTotal"
                     @current-change="handlePageChange"></el-pagination>
    </div>
  </div>
</template>

<script>
export default {
  name: "Node",
  props: ['nodeList'],
  data() {
    return {
      nodes: this.nodeList,
      pageNo: 1,
      pageSize: 10,
      pageTotal: 0
    }
  },
  mounted() {
    this.init();
  },
  methods: {
    init() {
      //console.log(this.nodeList);
    },
    handlePageChange() {

    },
    //时间转换
    formatDuring(mss) {
      let time = new Date().getTime();
      mss = time - mss;
      let days = Math.floor(mss / (1000 * 60 * 60 * 24));
      let hours = Math.floor((mss % (1000 * 60 * 60 * 24)) / (1000 * 60 * 60));
      let minutes = Math.floor((mss % (1000 * 60 * 60)) / (1000 * 60));
      return days > 0 ? (days + "天" + hours + "小时" + minutes + "分钟") : hours + "小时" + minutes + "分钟";
    }
  }
}
</script>

<style scoped>

</style>
