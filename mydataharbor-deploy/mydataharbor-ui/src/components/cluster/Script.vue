<template>
  <div class="in-coder-panel">
    <!--数据源-->
    <div style="margin: 20px 0 20px 0">
      <el-row :gutter="12">
        <el-col :span="6">
          <div>
            输入源：<el-select class="code-mode-select" size="small"  @change="changeMode" value></el-select>
          </div>
        </el-col>
        <el-col :span="6">
          <div>
            输出源：<el-select class="code-mode-select" size="small"  @change="changeMode" value></el-select>
          </div>
        </el-col>
      </el-row>
    </div>
    <!--脚本输入-->
    <div>
      <textarea ref="textarea"></textarea>
    </div>
    <!--提交按钮-->
    <div style="margin-top: 20px">
      <el-button size="small" type="primary">提交执行</el-button>
    </div>
  </div>
</template>

<script>
// 引入全局实例
import _CodeMirror from 'codemirror'

// 核心样式
import 'codemirror/lib/codemirror.css'
// 引入主题后还需要在 options 中指定主题才会生效
import 'codemirror/theme/eclipse.css'
import 'codemirror/theme/3024-day.css'
import 'codemirror/theme/abcdef.css'

// 需要引入具体的语法高亮库才会有对应的语法高亮效果
// codemirror 官方其实支持通过 /addon/mode/loadmode.js 和 /mode/meta.js 来实现动态加载对应语法高亮库
// 但 vue 貌似没有无法在实例初始化后再动态加载对应 JS ，所以此处才把对应的 JS 提前引入
import 'codemirror/mode/groovy/groovy.js'


// 尝试获取全局实例
const CodeMirror = window.CodeMirror || _CodeMirror

export default {
  name: 'Script',
  props: {
    groupName:String,
    // 外部传入的内容，用于实现双向绑定
    value: String,
    // 外部传入的语法类型
    language: {
      type: String,
      default: null
    }
  },
  data () {
    return {
      // 内部真实的内容
      code: 'def cal(int a, int b){\n' +
        '  return a+b\n' +
        '}\n',
      // 默认的语法类型
      mode: 'groovy',
      // 编辑器实例
      coder: null,
      // 默认配置
      options: {
        // 缩进格式
        tabSize: 8,
        // 主题，对应主题库 JS 需要提前引入
        theme: 'abcdef',
        // 显示行号
        lineNumbers: true,
        line: true
      }
    }
  },
  mounted () {
    // 初始化
    this._initialize()
  },
  methods: {
    // 初始化
    _initialize () {
      // 初始化编辑器实例，传入需要被实例化的文本域对象和默认配置
      this.coder = CodeMirror.fromTextArea(this.$refs.textarea, this.options)
      // 编辑器赋值
      this.coder.setValue(this.value || this.code)

      // 支持双向绑定
      this.coder.on('change', (coder) => {
        this.code = coder.getValue()

        if (this.$emit) {
          this.$emit('input', this.code)
        }
      })

      // 尝试从父容器获取语法类型
      if (this.language) {
        // 获取具体的语法类型对象
        let modeObj = this._getLanguage(this.language)

        // 判断父容器传入的语法是否被支持
        if (modeObj) {
          this.mode = modeObj.label
        }
      }
    },
    changeMode(){

    }
  }
}
</script>

<style>

</style>
