<template>
  <div>
    <!--提交任务-->
    <div style="margin-bottom: 10px">
      <el-button type="primary" size="small" @click="dialogFormVisible = true">提交任务</el-button>
      <el-button type="danger" size="small" @click="batchDelete()">批量删除</el-button>
    </div>
    <!--表格数据-->
    <el-table
      ref="multipleTable"
      :data="dataShow"
      border
      class="table"
      width="100%"
      header-cell-class-name="table-header"
      @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55"/>
      <el-table-column prop="taskId" label="任务ID" width="240" sortable/>
      <el-table-column :show-overflow-tooltip="true" label="任务名" width="120">
        <template #default="scope">
          <span v-if="scope.row.taskName != undefined">{{ scope.row.taskName }}</span>
          <span v-else>null</span>
        </template>
      </el-table-column>
      <el-table-column label="状态" width="100" prop="taskState">
        <template #default="scope">
          <el-tag :type="taskState[scope.row.taskState].color" size="small">{{ taskState[scope.row.taskState].zh }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column :show-overflow-tooltip="true" prop="pluginId" label="插件ID" width="250"/>
      <el-table-column :show-overflow-tooltip="true" prop="mydataharborCreatorClazz" label="创建器"/>
      <!--   <el-table-column prop="groupName" label="组名" width="100"></el-table-column>-->
      <el-table-column label="故障转移" width="120">
        <template #default="scope">
          <el-tag v-if="scope.row.enableRebalance" size="small">true</el-tag>
          <el-tag v-else size="small" type="warning">false</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="负载均衡" width="120">
        <template #default="scope">
          <el-tag v-if="scope.row.enableLoadBalance" size="small">true</el-tag>
          <el-tag v-else size="small" type="warning">false</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="totalNumberOfPipeline" label="管道数" width="80"/>
      <el-table-column label="创建时间" width="144" prop="createTime">
        <template slot-scope="scope">
          <span>{{ scope.row.createTime | parseTime('{y}-{m}-{d} {h}:{i}') }}</span>
        </template>
      </el-table-column>
      <el-table-column label="更新时间" width="144" prop="lastUpdateTime">
        <template slot-scope="scope">
          <span>{{ scope.row.lastUpdateTime | parseTime('{y}-{m}-{d} {h}:{i}') }}</span>
        </template>
      </el-table-column>
      <el-table-column :show-overflow-tooltip="true" label="tags" width="120">
        <template #default="scope">
          <span v-if="scope.row.tags != undefined">{{ scope.row.tags }}</span>
          <span v-else>null</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" fixed="right" width="480">
        <template #default="scope">
          <el-button-group>
            <el-button type="primary" @click="taskDetail(scope.row)" size="mini">详情</el-button>
            <el-button type="primary"  @click="taskUpdate(scope.row)" size="mini">修改</el-button>
            <el-button type="warning"  @click="taskRecreateUpdate(scope.row)" size="mini">重建</el-button>
            <el-button type="primary"  @click="taskFork(scope.row)" size="mini">复制</el-button>
            <el-button type="primary"  @click="taskStates(scope.row)" size="mini">状态管理</el-button>
            <el-button type="danger" @click="taskDelete(scope.row)" size="mini">删除</el-button>
          </el-button-group>
        </template>
      </el-table-column>
    </el-table>
    <!--分页-->
    <div class="pagination">
      <el-pagination
        :current-page="currentPage"
        :page-sizes="[5, 10, 20, 50, 100]"
        :page-size="pageSize"
        :total="pageTotal"
        background
        layout="sizes, total, prev, pager, next"
        @size-change="handleSizeChange"
        @current-change="handlePageChange"/>
    </div>
    <!--提交/fork任务弹框-->
    <el-dialog :title="dialogName" :visible.sync="dialogFormVisible" width="80%">
      <el-form :model="form" label-position="left" label-width="70px">
        <el-row>
          <el-col :span="8">
            <el-form-item label="组名">
              <el-input v-model="form.groupName" autocomplete="off" size="small" style="width: 80%" disabled/>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="任务名">
              <el-input
                v-model="form.taskName"
                placeholder="请输入任务名"
                autocomplete="off"
                size="small"
                style="width: 80%"/>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="管道数">
              <el-input
                v-model="form.totalNumberOfPipeline"
                autocomplete="off"
                size="small"
                style="width: 80%"/>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>

          <el-col :span="12">
            <el-form-item label="插件ID" prop="pluginId">
              <el-select
                v-model="form.pluginId"
                placeholder="请选择组件ID"
                size="small"
                style="width: 90%"
                @change="selectPluginId">
                <el-option
                  v-for="item in pluginInstallList"
                  :key="item.id"
                  :label="item.pluginId"
                  :value="item.pluginId"/>
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="创建器">
              <el-select
                v-model="form.mydataharborCreatorClazz"
                placeholder="请选择CreatorClazz"
                size="small"
                style="width: 90%"
                @change="creatorClazzChange">
                <el-option
                  v-for="item in clazzList"
                  :key="item.id"
                  :label="item.type + (item.clazz)"
                  :value="item.clazz"/>
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
         
          <el-col :span="12">
            <el-form-item label="故障转移">
              <el-select v-model="form.enableRebalance" placeholder="请选择是否设置故障转移" size="small" style="width: 90%">
                <el-option label="true" value="true"/>
                <el-option label="false" value="false"/>
              </el-select>
            </el-form-item>
            <el-alert title="值为false，节点停机，任务将不会自动转移！但是会有告警信息发出，适合不能重复运行的任务" type="info" show-icon style="margin-top: -20px"/>
          </el-col>
          <el-col :span="12">
            <el-form-item label="负载均衡">
              <el-select v-model="form.enableLoadBalance" placeholder="请选择是否设置负载均衡" size="small" style="width: 90%">
                <el-option label="true" value="true"/>
                <el-option label="false" value="false"/>
              </el-select>
            </el-form-item>
            <el-alert title="当集群内有节点加入时，开启负载均衡的任务有可能会被重新分配到其他节点（任务会被有中断的过程），以便分散压力，如果不希望任务随意被停止转移请设置为false" type="info" show-icon style="margin-top: -20px"/>
          </el-col>
        </el-row>
      </el-form>

      <el-collapse v-model="collapseActiveNames">
        <el-collapse-item title="settingContextConfig" name="settingConfig">
          <el-tabs v-model="defaultActiveModel">

            <el-tab-pane label="json" name="json">
              <vue-json-editor v-model="form.settingJsonConfig" :mode="'code'" lang="zh"/>
            </el-tab-pane>

            <el-tab-pane label="参数说明" name="form">

              <el-tree
                :data="settingConfigJsonTreeData"
                :props="defaultProps"
                :default-expand-all="true"
                :render-content="renderTreeContent"/>

            </el-tab-pane>

          </el-tabs>
        </el-collapse-item>
        <el-collapse-item title="taskConfig" name="taskConfig">

          <el-tabs v-model="taskConfigDefaultActiveModel">

            <el-tab-pane label="json" name="json">
              <vue-json-editor v-model="form.configJson" :mode="'code'" lang="zh"/>
            </el-tab-pane>

            <el-tab-pane label="参数说明" name="form">

              <el-tree
                :data="taskConfigJsonTreeData"
                :props="defaultProps"
                :default-expand-all="true"
                :render-content="renderTreeContent"/>

            </el-tab-pane>

          </el-tabs>

        </el-collapse-item>
        <el-collapse-item title="tagConfig" name="tagConfig">
          <vue-json-editor v-model="form.tags" :mode="'code'" lang="zh"/>
        </el-collapse-item>

      </el-collapse>

      <div slot="footer" class="dialog-footer">
        <el-button @click="cancel">取 消</el-button>
        <el-button type="primary" @click="submit">确 定</el-button>
      </div>
    </el-dialog>

    <!--任务详情-->
    <el-dialog :visible.sync="taskDetailVisible" title="任务详情" width="60%">
      <p>基本信息：</p>
      <el-table :data="taskDetailForm">
        <!--表格-->
        <el-table-column :show-overflow-tooltip="true" prop="taskId" label="任务ID"/>
        <el-table-column :show-overflow-tooltip="true" prop="taskName" label="任务名称"/>
        <el-table-column :show-overflow-tooltip="true" prop="pluginId" label="插件ID"/>
        <el-table-column prop="mydataharborCreatorClazz" label="创建器"/>
        <el-table-column prop="totalNumberOfPipeline" label="管道数"/>
        <el-table-column label="故障转移">
          <template #default="scope">
            <el-tag v-if="scope.row.enableRebalance" size="small">true</el-tag>
            <el-tag v-else size="small" type="warning">false</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="负载均衡">
          <template #default="scope">
            <el-tag v-if="scope.row.enableLoadBalance" size="small">true</el-tag>
            <el-tag v-else size="small" type="warning">false</el-tag>
          </template>
        </el-table-column>
      </el-table>
      <p>监控信息：</p>
      <el-table :data="taskMonitorInfos">
        <!--折叠-->
        <el-table-column :formatter="dateFormat" prop="lastRunTime" label="最近一次处理时间"/>
        <el-table-column prop="recordCount" label="拉取总数"/>
        <el-table-column prop="pollErrorCount" label="拉取异常次数"/>
        <el-table-column prop="protocolConvertSuccessCount" label="协议转换成功数"/>
        <el-table-column prop="protocolConvertErrorCount" label="协议转换失败数"/>
        <el-table-column prop="checkerSuccessCount" label="校验通过数"/>
        <el-table-column prop="checkerErrorCount" label="校验失败数"/>
        <el-table-column prop="dataConvertSuccessCount" label="数据转换成功数"/>
        <el-table-column prop="dataConvertErrorCount" label="数据转换失败数"/>
        <el-table-column prop="writeSuccessCount" label="写入成功数"/>
        <el-table-column prop="writeErrorCount" label="写入失败数"/>
      </el-table>
      <p>任务分配信息：</p>
      <el-table :data="taskAssignedInfo">
        <!--折叠-->
        <el-table-column type="expand">
          <template slot-scope="props">
            <p>管道信息：</p>
            <p v-for="(value, key) in props.row.pipelineStates" :key="key">{{ key }}：{{ value }}</p>
            <el-divider/>
            <p>写总计：</p>
            <p v-for="(value, key) in props.row.writeTotal" :key="key">{{ key }}：{{ value }}</p>
          </template>
        </el-table-column>
        <el-table-column prop="nodeName" label="节点" width="120"/>
        <el-table-column prop="taskNum" label="任务数" width="80"/>

        <el-table-column :show-overflow-tooltip="true" prop="createException" label="创建异常">
          <template #default="scope">
            <span v-if="scope.row.createException == null">无</span>
            <span v-else>{{ scope.row.createException }}</span>
          </template>
        </el-table-column>
      </el-table>

      <p>任务设置</p>
      <vue-json-editor v-model="detailSettingJsonConfig" :mode="'code'" lang="zh"/>
      <p>任务行为</p>
      <vue-json-editor v-model="detailConfigJson" :mode="'code'" lang="zh"/>
      <p>tags其它属性</p>
      <vue-json-editor v-model="detailTags" :mode="'code'" lang="zh"/>
      <div slot="footer" class="dialog-footer">
        <el-button @click="cancel">取 消</el-button>
      </div>
    </el-dialog>

    <!--任务修改弹框-->
    <el-dialog :visible.sync="dialogUpdateVisible" title="任务修改" width="42%">
      <el-form :model="updateForm" label-position="left">
        <el-row>
          <el-col :span="11">
            <el-form-item label="任务ID" label-width="70px">
              <el-input v-model="updateForm.taskId" autocomplete="off" style="width: 250px;" disabled/>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="管道数" label-width="70px" prop="pluginId">
              <el-input v-model="updateForm.totalNumberOfPipeline" autocomplete="off" style="width: 250px;"/>
            </el-form-item>
          </el-col>
          <el-col :span="11">
            <el-form-item label="任务名" label-width="70px" prop="pluginId">
              <el-input v-model="updateForm.taskName" autocomplete="off" style="width: 250px;"/>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="故障转移" label-width="70px" prop="version">
              <el-select v-model="updateForm.enableRebalance" placeholder="请选择是否故障转移" style="width: 250px">
                <el-option label="true" value="true"/>
                <el-option label="false" value="false"/>
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="负载均衡" label-width="70px" prop="version">
              <el-select v-model="updateForm.enableLoadBalance" placeholder="请选择是否负载均衡" style="width: 250px">
                <el-option label="true" value="true"/>
                <el-option label="false" value="false"/>
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="cancel">取 消</el-button>
        <el-button type="primary" @click="updateFormSubmit">确 定</el-button>
      </div>
    </el-dialog>

    <!--任务修改(重建)弹框-->
    <el-dialog :visible.sync="dialogRecreateUpdateVisible" title="重建任务（该操作会先销毁当前任务的所有管道实例，然后重新创建，很重的一种操作）" width="80%">
      <el-form :model="recreateUpdateForm" label-position="left">
        <el-row>
          <el-col :span="11">
            <el-form-item label="任务ID" label-width="70px">
              <el-input v-model="recreateUpdateForm.taskId" autocomplete="off" style="width: 250px;" disabled/>
            </el-form-item>
          </el-col>

        </el-row>
        <el-collapse v-model="collapseActiveNames">
        <el-collapse-item title="settingContextConfig" name="settingConfig">
          <el-tabs v-model="defaultActiveModel">

            <el-tab-pane label="json" name="json">
              <vue-json-editor v-model="recreateUpdateForm.settingJsonConfig" :mode="'code'" lang="zh"/>
            </el-tab-pane>

            <el-tab-pane label="参数说明" name="form">

              <el-tree
                :data="settingConfigJsonTreeData"
                :props="defaultProps"
                :default-expand-all="true"
                :render-content="renderTreeContent"/>

            </el-tab-pane>

          </el-tabs>
        </el-collapse-item>
        <el-collapse-item title="taskConfig" name="taskConfig">

          <el-tabs v-model="taskConfigDefaultActiveModel">

            <el-tab-pane label="json" name="json">
              <vue-json-editor v-model="recreateUpdateForm.configJson" :mode="'code'" lang="zh"/>
            </el-tab-pane>

            <el-tab-pane label="参数说明" name="form">

              <el-tree
                :data="taskConfigJsonTreeData"
                :props="defaultProps"
                :default-expand-all="true"
                :render-content="renderTreeContent"/>

            </el-tab-pane>

          </el-tabs>

        </el-collapse-item>
        <el-collapse-item title="tagConfig" name="tagConfig">
          <vue-json-editor v-model="recreateUpdateForm.tags" :mode="'code'" lang="zh"/>
        </el-collapse-item>

      </el-collapse>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="cancel">取 消</el-button>
        <el-button type="primary" @click="recreateUpdateFormSubmit">确 定</el-button>
      </div>
    </el-dialog>

    <!--任务状态弹框-->
    <el-dialog :visible.sync="dialogStateVisible" title="任务状态" width="23%">
      <el-form :model="stateForm" label-position="left">
        <el-form-item label="任务ID" label-width="70px">
          <el-input v-model="stateForm.taskId" autocomplete="off" style="width: 250px;" disabled/>
        </el-form-item>
        <el-form-item label="任务状态" label-width="70px" prop="version">
          <el-select v-model="stateForm.taskState" placeholder="请选择任务状态" style="width: 250px">
            <el-option v-for="(key, value) in states" :key="key" :label="key" :value="value"/>
          </el-select>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="cancel">取 消</el-button>
        <el-button type="primary" @click="updateStateFormSubmit">确 定</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import vueJsonEditor from 'vue-json-editor'

export default {
  inject: ['reload'],
  name: 'Tasks',
  components: {
    vueJsonEditor
  },
  // 父组件传值
  props: ['groupName'],
  data() {
    return {
      // 当前组任务
      taskList: [],
      // 当前组插件
      pluginInstallList: [],
      // 插件的类路径
      clazzList: [],
      // 对话框
      dialogFormVisible: false,
      taskDetailVisible: false,
      dialogUpdateVisible: false,
      dialogRecreateUpdateVisible: false,
      dialogStateVisible: false,
      // 提交任务数据
      form: {
        groupName: this.groupName,
        pluginId: '',
        taskName: '',
        mydataharborCreatorClazz: '',
        totalNumberOfPipeline: 1,
        enableRebalance: true,
        enableLoadBalance: true,
        settingJsonConfig: {},
        configJson: {},
        tags: {}
      },
      // 任务详情
      taskDetailForm: [],
      detailSettingJsonConfig: {},
      detailConfigJson: {},
      detailTags: {},
      taskAssignedInfo: [],

      // 状态
      taskState: {
        created: {
          zh: '创建',
          color: ''
        },
        started: {
          zh: '启动',
          color: 'success'
        },
        suspend: {
          zh: '暂停',
          color: 'info'
        },
        continued: {
          zh: '继续',
          color: 'warning'
        },
        over: {
          zh: '结束',
          color: 'over'
        }

      },
      states: { created: '创建', started: '启动', suspend: '暂停', continued: '继续', over: '结束' },
      // 对话框标题
      dialogName: '提交任务',
      // 修改数据
      updateForm: {
        enableRebalance: true,
        enableLoadBalance: true,
        taskId: '',
        taskName: '',
        totalNumberOfPipeline: 0
      },
      recreateUpdateForm: {
        taskId: '',
        configJson: {},
        settingJsonConfig: {},
        tags: {}
      },
      // 修改状态
      stateForm: {
        taskId: '',
        taskState: ''
      },

      /* 前端模拟分页*/
      // 所有页面的数据
      totalPage: [],
      // 每页显示数量
      pageSize: 10,
      // 共几页
      pageTotal: 1,
      // 当前显示的数据
      dataShow: [],
      // 默认当前显示第一页
      currentPage: 0,

      // 多选内容
      multipleSelection: [],

      collapseActiveNames: ['settingConfig', 'taskConfig', 'tagConfig'],
      defaultActiveModel: 'json',
      taskConfigDefaultActiveModel: 'json',

      settingConfigJsonTreeData: [],

      taskConfigJsonTreeData: [],

      defaultProps: {
        children: 'fieldInfos',
        label: 'fieldName'
      },

      taskMonitorInfos: []// 任务监控信息

    }
  },
  watch: {},
  mounted() {
    this.$nextTick(() => {
      this.$refs.main.scrollTop = this.$refs.content.scrollHeight
    })
  },
  mounted() {
    this.initData()
    this.getTasksByGroupName()
  },
  methods: {

    dateFormat(row, column) {
      if (row.lastRunTime != 0) {
        const date = new Date(row.lastRunTime)
        const year = date.getFullYear()
        const month = date.getMonth() + 1 < 10 ? `0${date.getMonth() + 1}` : date.getMonth() + 1
        const day = date.getDate() < 10 ? `0${date.getDate()}` : date.getDate()
        const hour = date.getHours() < 10 ? `0${date.getHours()}` : date.getHours()
        const minute = date.getMinutes() < 10 ? `0${date.getMinutes()}` : date.getMinutes()
        const second = date.getSeconds() < 10 ? `0${date.getSeconds()}` : date.getSeconds()
        return `${year}-${month}-${day} ${hour}:${minute}:${second}`
      }
      return '暂无'
    },

    compare(property) {
      return function(a, b) {
        var value1 = a[property]
        var value2 = b[property]
        return value2 - value1
      }
    },

    // 清空
    clearData() {
      this.form = {
        groupName: this.groupName,
        pluginId: '',
        mydataharborCreatorClazz: '',
        totalNumberOfPipeline: 1,
        enableRebalance: true,
        enableLoadBalance: true,
        settingJsonConfig: {},
        configJson: {}
      }
      this.taskDetailForm = []
      this.dialogName = '提交任务'
    },
    initData() {
      // 获取当前组的任务名称
      this.taskList = []
      this.multipleSelection = []

      // 获取任务
      this.postRequest('mydataharbor/task/listTaskByGroup?groupName=' + this.groupName).then(res => {
        if (res.code == 0) {
          const allTask = res.data
          for (const key in allTask) {
            this.taskList.push(allTask[key])
          }
          this.taskList.sort(this.compare('createTime'))
          // 分页
          this.initPageData()
        }else {
          this.$message.error(res.msg)
        }
      })
    },
    // 根据组名查询任务
    getTasksByGroupName() {
      this.getRequest('/mydataharbor/node/plugin?groupName=' + this.groupName).then(res => {
        this.pluginInstallList = res.data
        console.log(this.pluginInstallList)
      })
    },
    // 提交任务
    submit() {
      this.dialogFormVisible = false
      this.form.configJson = JSON.stringify(this.form.configJson)
      this.form.settingJsonConfig = JSON.stringify(this.form.settingJsonConfig)
      this.postRequest('mydataharbor/task/submit', this.form).then(res => {

        if (res.code == 0) {
          this.$message.info('任务提交成功！')
          this.initData()
          this.clearData()
        }else {
          this.$message.error(res.msg)
        }
      })
    },
    cancel() {
      this.dialogFormVisible = false
      this.taskDetailVisible = false
      this.dialogUpdateVisible = false
      this.dialogStateVisible = false
      this.dialogRecreateUpdateVisible = false
      this.clearData()
    },
    // 选择插件
    selectPluginId(value) {
      this.clazzList = []
      this.form.mydataharborCreatorClazz = ''
      this.pluginInstallList.forEach(plugin => {
        if (plugin.pluginId == value) {
          if (plugin.dataPipelineCreatorInfos != null) {
            plugin.dataPipelineCreatorInfos.forEach(dataSink => {
              if (dataSink.canCreatePipeline) { this.clazzList.push({ 'clazz': dataSink.clazz, 'type': dataSink.type }) }
            })
          }
        }
      })
    },
    // 选择创建器
    creatorClazzChange(val) {
      this.form.settingJsonConfig = {}
      this.form.configJson = {}
      if (val != '') {
        this.pluginInstallList.forEach((pluginInfo) => {
          if (pluginInfo.pluginId == this.form.pluginId) {
            pluginInfo.dataPipelineCreatorInfos.forEach((creatorInfo) => {
              if (creatorInfo.clazz == val) {
                creatorInfo.settingClassInfo.fieldName = 'root'
                creatorInfo.configClassInfo.fieldName = 'root'
                this.settingConfigJsonTreeData = [creatorInfo.settingClassInfo]
                this.taskConfigJsonTreeData = [creatorInfo.configClassInfo]
                for (const fieldInfo of creatorInfo.settingClassInfo.fieldInfos) {
                  this.fillField(this.form.settingJsonConfig, fieldInfo)
                }
                for (const fieldInfo of creatorInfo.configClassInfo.fieldInfos) {
                  this.fillField(this.form.configJson, fieldInfo)
                }
              }
            })
          }
        })
      }
    },
    // 任务详情
    taskDetail(task) {
      this.taskDetailForm = []
      this.taskAssignedInfo = []

      const row = JSON.parse(JSON.stringify(task))
      this.detailConfigJson = JSON.parse(task.configJson)
      this.detailTags = task.tags
      this.detailSettingJsonConfig = JSON.parse(task.settingJsonConfig)
      // 任务分配信息
      const assignedInfo = task.taskAssignedInfo.assignedInfoMap
      for (const rowKey in assignedInfo) {
        this.taskAssignedInfo.push(assignedInfo[rowKey])
      }

      this.getRequest('mydataharbor/task/getTaskMonitorInfo', { taskId: task.taskId }).then(res => {
        if (res.code == 0) {
          this.taskMonitorInfos = [res.data]
        }else {
          this.$message.error(res.msg)
        }
      })

      // console.log(this.taskAssignedInfo);
      this.taskDetailForm.push(row)
      // console.log(this.taskDetailForm);
      this.taskDetailVisible = true
    },
    // 任务修改弹框
    taskUpdate(task) {
      this.updateForm.taskId = task.taskId
      this.updateForm.taskName = task.taskName
      this.updateForm.totalNumberOfPipeline = task.totalNumberOfPipeline
      this.updateForm.enableRebalance = task.enableRebalance
      this.updateForm.enableLoadBalance = task.enableLoadBalance
      this.updateForm.tags = task.tags
      this.dialogUpdateVisible = true
    },
    // 任务修改
    updateFormSubmit() {
      this.postRequest('mydataharbor/task/editTask', this.updateForm).then(res => {
        if (res.code == 0) {
          this.$message.info('任务修改成功！')
          this.initData()
        }else {
          this.$message.error(res.msg)
        }
      })
      this.dialogUpdateVisible = false
    },
    // 任务重建修改弹框
    taskRecreateUpdate(task) {
      this.recreateUpdateForm.taskId = task.taskId
      this.recreateUpdateForm.configJson = JSON.parse(task.configJson)
      this.recreateUpdateForm.tags = task.tags
      this.recreateUpdateForm.settingJsonConfig = JSON.parse(task.settingJsonConfig)
      this.selectPluginId(task.pluginId)
      this.creatorClazzChange(task.mydataharborCreatorClazz)
      this.dialogRecreateUpdateVisible = true
    },
    //重建任务修改
    recreateUpdateFormSubmit(){
      this.recreateUpdateForm.configJson = JSON.stringify(this.recreateUpdateForm.configJson)
      this.recreateUpdateForm.settingJsonConfig = JSON.stringify(this.recreateUpdateForm.settingJsonConfig)
      this.postRequest('mydataharbor/task/recreateTask', this.recreateUpdateForm).then(res => {
        if (res.code == 0) {
          this.$message.info('任务重建成功！')
          this.initData()
        } else {
          this.$message.error(res.msg)
        }
      })
      this.dialogRecreateUpdateVisible = false
    },
    // 复制任务
    taskFork(task) {
      this.dialogName = '复制任务'
      this.form = JSON.parse(JSON.stringify(task))
      this.form.configJson = JSON.parse(task.configJson)
      this.form.settingJsonConfig = JSON.parse(task.settingJsonConfig)
      this.dialogFormVisible = true
    },
    // 任务状态弹框
    taskStates(task) {
      this.stateForm.taskId = task.taskId
      this.stateForm.taskState = task.taskState
      this.dialogStateVisible = true
    },
    // 删除任务
    taskDelete(task) {
      this.$confirm('是否永久删除该任务：' + task.taskId, '确认信息', {
        distinguishCancelAndClose: true,
        confirmButtonText: '删除',
        cancelButtonText: '取消',
        type: 'warning'
      })
        .then(() => {
          this.postRequest('/mydataharbor/task/deleteTask?taskId=' + task.taskId).then(res => {
            if (res.code == 0) {
              this.$message({
                type: 'info',
                message: '删除成功！'
              })
            } else {
              this.$message({
                type: 'warning',
                message: '删除失败！' + res.msg
              })
            }
          })
          this.initData()
        })
    },
    updateStateFormSubmit() {
      this.postRequest('mydataharbor/task/manageTaskState?taskId=' + this.stateForm.taskId + '&taskState=' + this.stateForm.taskState).then(res => {
        if (res.code == 0) {
          this.$message.info('任务状态修改成功！')
          this.initData()
        } else {
          this.$message.error(res.msg)
        }
      })
      this.dialogStateVisible = false
    },
    // 分页处理
    initPageData() {
      this.pageTotal = this.taskList.length
      for (let i = 0; i < this.pageTotal; i++) {
        // 每一页都是一个数组 形如 [['第一页的数据'],['第二页的数据'],['第三页数据']]
        // 根据每页显示数量 将后台的数据分割到 每一页,假设pageSize为5， 则第一页是1-5条，即slice(0,5)，第二页是6-10条，即slice(5,10)...
        this.totalPage[i + 1] = this.taskList.slice(this.pageSize * i, this.pageSize * (i + 1))
      }
      this.handlePageChange(1)
    },
    handlePageChange(pageNo) {
      this.currentPage = pageNo
      // 获取到数据后显示第一页内容
      this.dataShow = this.totalPage[this.currentPage]
    },
    handleSizeChange(size) {
      this.pageSize = size
      this.initPageData()
    },
    // 多选
    handleSelectionChange(val) {
      this.multipleSelection = val
    },
    // 删除多条数据
    batchDelete() {
      console.log(this.multipleSelection)
      if (this.multipleSelection.length == 0) {
        this.$message.info('当前未选择任务任务数据，请先勾选需要删除的数据！')
        return
      }
      let deleteCount = 0
      this.$confirm('是否永久删除已选的' + this.multipleSelection.length + '条数据', '确认信息', {
        distinguishCancelAndClose: true,
        confirmButtonText: '删除',
        cancelButtonText: '取消',
        type: 'warning'
      })
        .then(() => {
          this.multipleSelection.forEach(task => {
            // console.log(task.taskId)
            this.postRequest('/mydataharbor/task/deleteTask?taskId=' + task.taskId).then(res => {
              if (res.code == 0) {
                deleteCount = deleteCount + 1
                if (deleteCount == this.multipleSelection.length) {
                  this.$message({
                    type: 'info',
                    message: '成功删除' + deleteCount + '条数据！'
                  })
                }
              }else {
                this.$message.error(res.msg)
              }
            })
          })
          this.reload()
        })
    },
    renderTreeContent(h, { node, data, store }) {
      return (
        <span class='custom-tree-node'>
          <span>
            {data.fieldName} {data.title} 【类型:{data.clazzStr}】【{data['array'] == true ? '数组' : data['map'] == true ? 'map' : data['baseType'] == true ? '基础类型' : '对象'}】
            {data['enumeration'] == true ? '【枚举】候选值:' + JSON.stringify(data['candidateValue']) : ''}
            {data['require'] == true ? '【必须】' : '【非必须】'}
            {data['defaultValue'] != null && data['defaultValue'] != '' ? '【默认值：' + data['defaultValue'] + '】' : ''}
            {data['des']}
          </span>

        </span>)
    },
    fillField(json, fieldInfo) {
      if (fieldInfo.require) {
        if (fieldInfo.array) {
          json[fieldInfo.fieldName] = []
        } else if (fieldInfo.map) {
          json[fieldInfo.fieldName] = {}
        } else if (fieldInfo.baseType) {
          json[fieldInfo.fieldName] = fieldInfo.defaultValue
        } else {
          json[fieldInfo.fieldName] = {}
        }
        if (fieldInfo.fieldInfos != null && fieldInfo.fieldInfos.length > 0) {
          for (const subFieldInfo of fieldInfo.fieldInfos) {
            this.fillField(json[fieldInfo.fieldName], subFieldInfo)
          }
        }
      }
    }
  }
}
</script>

<style scoped>
/deep/ .el-col-24 {
  width: 50%;
  margin-left: -6%;
}

/deep/ .el-alert--info.is-light {
  background-color: #ffffff;
}

.demo-table-expand {
  font-size: 0;
}

.demo-table-expand label {
  width: 90px;
  color: #99a9bf;
}

.demo-table-expand .el-form-item {
  margin-right: 0;
  margin-bottom: 0;
  width: 50%;
}
</style>
