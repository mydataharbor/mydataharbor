<template>
  <div>
    <!--安装插件-->
    <div style="margin-bottom: 10px">
      <el-button type="primary" size="small" @click="dialogFormVisible = true">安装/升级插件</el-button>
      <el-button type="success" size="small" @click="savePluginOrder" :disabled="!orderChanged" :loading="loading">保存顺序</el-button>
    </div>
    
    <!-- 添加拖拽提示 -->
    <div class="drag-tip" v-if="plugins.length > 1">
      <i class="el-icon-info"></i>
      <span>提示：您可以通过拖拽 <i class="el-icon-rank"></i> 图标来调整插件的顺序</span>
    </div>
    
    <!--表格数据-->
    <el-table 
      :data="plugins" 
      border 
      class="table"
      ref="multipleTable" 
      header-cell-class-name="table-header"
      row-key="pluginId"
      :row-class-name="tableRowClassName">
      <el-table-column width="50">
        <template slot-scope="scope">
          <el-button 
            type="text" 
            icon="el-icon-rank" 
            class="drag-handle"
            @mousedown.native.prevent="handleDragStart($event, scope.$index)">
          </el-button>
        </template>
      </el-table-column>
      <el-table-column prop="pluginId" label="插件ID" sortable></el-table-column>
      <el-table-column prop="pluginDescription" label="描述"></el-table-column>
      <!--<el-table-column prop="pluginClass" label="类路径" width="400"></el-table-column>-->
      <el-table-column prop="version" label="版本"></el-table-column>
      <el-table-column label="依赖">
        <template slot-scope="scop">
          <p v-for="(dependency, index) in scop.row.dependencies" :key="index">
            【插件id:{{dependency.pluginId}},版本:{{dependency.pluginVersionSupport}},可选：{{dependency.optional}}】
          </p>
        </template>
      </el-table-column>
      <el-table-column prop="provider" label="提供者"></el-table-column>

      <el-table-column label="操作" align="center">
        <template #default="scope">
          <el-link @click="unInstallPlugin(scope.row.pluginId)" type="primary" icon="el-icon-close">卸载</el-link>
        </template>
      </el-table-column>
    </el-table>

    <!--插件安装弹框-->
    <el-dialog title="插件安装/升级" :visible.sync="dialogFormVisible" width="25%">
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
      originalPlugins: [], // 用于存储原始插件顺序
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
      allVersion: [],
      dragging: false,
      dragRow: null,
      dragIndex: -1,
      dropIndex: -1,
      orderChanged: false,
      loading: false,
      rowHeight: 0,
      startMouseY: 0,
      startScrollTop: 0
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
    
    // 添加拖拽相关的事件监听
    document.addEventListener('mousemove', this.handleDragMove);
    document.addEventListener('mouseup', this.handleDragEnd);
    
    // 测试事件绑定是否正常
    this.$nextTick(() => {
      console.log('Vue mounted, testing event binding');
      const dragHandles = document.querySelectorAll('.drag-handle');
      console.log('Found drag handles:', dragHandles.length);
      
      // 添加一个简单的点击事件测试
      if (dragHandles.length > 0) {
        dragHandles.forEach(handle => {
          handle.addEventListener('click', () => {
            console.log('Drag handle clicked');
          });
        });
      }
    });
  },
  beforeDestroy() {
    // 移除事件监听
    document.removeEventListener('mousemove', this.handleDragMove);
    document.removeEventListener('mouseup', this.handleDragEnd);
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
    // 表格行样式
    tableRowClassName({row, rowIndex}) {
      if (this.dragging && rowIndex === this.dropIndex) {
        return 'drop-over-row';
      }
      return '';
    },
    // 开始拖拽
    handleDragStart(event, index) {
      this.dragging = true;
      this.dragIndex = index;
      this.dragRow = this.plugins[index];
      
      // 创建拖拽时的视觉效果
      const table = this.$refs.multipleTable.$el;
      const tableRect = table.getBoundingClientRect();
      const rows = table.querySelectorAll('.el-table__body tr');
      this.rowHeight = rows[0].offsetHeight;
      
      // 设置鼠标样式
      document.body.style.cursor = 'move';
      
      // 记录鼠标初始位置
      this.startMouseY = event.clientY;
      this.startScrollTop = table.querySelector('.el-table__body-wrapper').scrollTop;
    },
    // 拖拽移动
    handleDragMove(event) {
      if (!this.dragging) return;
      
      const table = this.$refs.multipleTable.$el;
      const tableBody = table.querySelector('.el-table__body-wrapper');
      const tableRect = tableBody.getBoundingClientRect();
      
      // 计算鼠标在表格中的相对位置
      const mouseY = event.clientY - tableRect.top + tableBody.scrollTop;
      
      // 计算当前鼠标所在的行索引
      let index = Math.floor(mouseY / this.rowHeight);
      
      // 边界检查
      if (index < 0) index = 0;
      if (index >= this.plugins.length) index = this.plugins.length - 1;
      
      if (this.dropIndex !== index) {
        this.dropIndex = index;
        this.$forceUpdate(); // 强制更新视图
      }
      
      // 自动滚动
      const scrollThreshold = 40;
      if (event.clientY - tableRect.top < scrollThreshold) {
        // 向上滚动
        tableBody.scrollTop -= 10;
      } else if (tableRect.bottom - event.clientY < scrollThreshold) {
        // 向下滚动
        tableBody.scrollTop += 10;
      }
    },
    // 结束拖拽
    handleDragEnd() {
      if (!this.dragging) return;
      
      // 如果有有效的拖放位置
      if (this.dragIndex !== -1 && this.dropIndex !== -1 && this.dragIndex !== this.dropIndex) {
        // 移动数组元素
        const row = this.plugins.splice(this.dragIndex, 1)[0];
        this.plugins.splice(this.dropIndex, 0, row);
        this.orderChanged = true;
      }
      
      // 重置拖拽状态
      this.dragging = false;
      this.dragRow = null;
      this.dragIndex = -1;
      this.dropIndex = -1;
      document.body.style.cursor = '';
      
      this.$forceUpdate(); // 强制更新视图
    },
    // 保存插件顺序
    savePluginOrder() {
      if (!this.orderChanged) return;
      
      this.loading = true;
      const pluginIds = this.plugins.map(plugin => plugin.pluginId);
      
      console.log('Saving plugin order:', pluginIds);
      
      // 使用正确的参数传递方式
      this.postRequest('mydataharbor/node/adjustPluginOrder?groupName=' + this.groupName + '&pluginIds='+pluginIds.join(',')).then(res => {
        if (res.code === 0) {
          this.$message({
            message: '插件顺序调整成功',
            type: 'success',
            duration: 3000
          });
          this.orderChanged = false;
          this.originalPlugins = JSON.parse(JSON.stringify(this.plugins)); // 更新原始插件列表
        } else {
          this.$message({
            message: '插件顺序调整失败: ' + res.msg,
            type: 'error',
            duration: 5000
          });
          // 恢复原始顺序
          this.plugins = JSON.parse(JSON.stringify(this.originalPlugins));
        }
        this.loading = false;
      }).catch(err => {
        this.$message({
          message: '插件顺序调整失败: ' + (err.message || '未知错误'),
          type: 'error',
          duration: 5000
        });
        this.loading = false;
        // 恢复原始顺序
        this.plugins = JSON.parse(JSON.stringify(this.originalPlugins));
      });
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
        // 保存原始插件顺序，用于取消拖拽时恢复
        this.originalPlugins = JSON.parse(JSON.stringify(this.plugins));
        this.orderChanged = false;
        console.log(this.plugins);
      })
    },
    
    // 表格行样式
    tableRowClassName({row, rowIndex}) {
      if (this.dragging) {
        if (rowIndex === this.dropIndex) {
          return 'drop-over-row';
        } else if (rowIndex === this.dragIndex) {
          return 'dragging-row';
        }
      }
      return '';
    },
    
    // 开始拖拽
    handleDragStart(event, index) {
      console.log('Drag start', index);
      
      // 获取表格行高度，用于计算拖拽位置
      const table = this.$refs.multipleTable.$el;
      const rows = table.querySelectorAll('.el-table__body tr');
      if (rows.length === 0) return;
      
      this.rowHeight = rows[0].offsetHeight;
      this.dragging = true;
      this.dragIndex = index;
      this.dropIndex = index;
      this.dragRow = JSON.parse(JSON.stringify(this.plugins[index]));
      
      // 记录鼠标初始位置和滚动位置
      this.startMouseY = event.clientY;
      const tableBody = table.querySelector('.el-table__body-wrapper');
      this.startScrollTop = tableBody ? tableBody.scrollTop : 0;
      
      // 设置鼠标样式
      document.body.style.cursor = 'move';
      
      // 添加拖拽样式到表格
      table.classList.add('dragging');
      
      // 显示拖拽提示
      this.$message({
        message: '正在拖动插件，松开鼠标完成排序',
        type: 'info',
        duration: 2000
      });
    },
    
    // 拖拽移动
    handleDragMove(event) {
      if (!this.dragging) return;
      
      const table = this.$refs.multipleTable.$el;
      const tableBody = table.querySelector('.el-table__body-wrapper');
      if (!tableBody) return;
      
      const tableRect = tableBody.getBoundingClientRect();
      
      // 计算鼠标在表格中的相对位置
      const mouseY = event.clientY - tableRect.top + tableBody.scrollTop;
      
      // 计算当前鼠标所在的行索引
      let index = Math.floor(mouseY / this.rowHeight);
      
      // 边界检查
      if (index < 0) index = 0;
      if (index >= this.plugins.length) index = this.plugins.length - 1;
      
      if (this.dropIndex !== index) {
        console.log('Drop index changed', index);
        this.dropIndex = index;
        this.$forceUpdate(); // 强制更新视图
      }
      
      // 自动滚动
      const scrollThreshold = 40;
      if (event.clientY - tableRect.top < scrollThreshold) {
        // 向上滚动
        tableBody.scrollTop -= 5;
      } else if (tableRect.bottom - event.clientY < scrollThreshold) {
        // 向下滚动
        tableBody.scrollTop += 5;
      }
      
      // 阻止默认事件和冒泡
      event.preventDefault();
      event.stopPropagation();
    },
    
    // 结束拖拽
    handleDragEnd(event) {
      if (!this.dragging) return;
      
      console.log('Drag end', this.dragIndex, this.dropIndex);
      
      // 如果有有效的拖放位置
      if (this.dragIndex !== -1 && this.dropIndex !== -1 && this.dragIndex !== this.dropIndex) {
        // 移动数组元素
        const row = this.plugins.splice(this.dragIndex, 1)[0];
        this.plugins.splice(this.dropIndex, 0, row);
        this.orderChanged = true;
        
        // 显示成功提示
        this.$message({
          message: '插件顺序已调整，点击"保存顺序"按钮保存更改',
          type: 'success',
          duration: 3000
        });
      }
      
      // 重置拖拽状态
      this.dragging = false;
      this.dragRow = null;
      this.dragIndex = -1;
      this.dropIndex = -1;
      document.body.style.cursor = '';
      
      // 移除拖拽样式
      const table = this.$refs.multipleTable.$el;
      if (table) {
        table.classList.remove('dragging');
      }
      
      this.$forceUpdate(); // 强制更新视图
      
      // 阻止默认事件和冒泡
      if (event) {
        event.preventDefault();
        event.stopPropagation();
      }
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
.drag-handle {
  cursor: move;
  color: #909399;
}

.drag-handle:hover {
  color: #409EFF;
}

/* 拖拽时的行样式 */
/deep/ .el-table__body tr.drop-over-row td {
  background-color: #ecf5ff !important;
  border-top: 2px dashed #409EFF;
}

/* 拖拽时的行样式 */
/deep/ .el-table__body tr.dragging-row {
  background-color: #f0f9eb;
  opacity: 0.5;
}

/* 拖拽时的表格样式 */
/deep/ .dragging .el-table__body {
  cursor: move !important;
}

/* 添加一个明显的提示，表明可以拖动 */
.drag-tip {
  margin-bottom: 10px;
  padding: 8px;
  background-color: #f0f9eb;
  border-radius: 4px;
  color: #67c23a;
  display: flex;
  align-items: center;
}

.drag-tip i {
  margin-right: 5px;
}
</style>
