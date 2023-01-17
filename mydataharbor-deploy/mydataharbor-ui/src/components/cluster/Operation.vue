<template>
  <div>
      <el-tag>当前分组： {{groupName}}</el-tag>

      <el-button type="danger" size="small" style="margin-left:20px" @click="deleteGroup()">删除该分组</el-button>
  </div>
</template>

<script type="text/javascript">

export default {
  inject: ['reload'],
  name: "operation",
  //父组件传值
  props: ['groupName'],
  data() {
    return {

    }
  },
  methods: {
    deleteGroup() {
      this.$confirm('此操作将永久删除【' + this.groupName + '】集群, 是否继续?', '确认提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        }).then(() => {
          this.getRequest("/mydataharbor/node/deleteGroup?groupName=" + this.groupName).then(res=>{
            console.log(res);
            if (res.code == 0) {
              this.$message({
                type: 'success',
                message: '删除成功!'
              });
              // 跳转到
              this.reload();
            } else {
              this.$message({
                type: 'error',
                message: '删除失败，' + res.msg
              });
            }
          });
        }).catch(() => {
          this.$message({
            type: 'info',
            message: '已取消删除!'
          });
        });

    }
  }
}
</script>

<style scoped>

</style>
