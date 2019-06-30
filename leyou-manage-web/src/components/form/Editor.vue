<template>
  <div>
    <quilleditor v-model="content"
                 ref="myTextEditor"
                 :options="editorOption"
                 @change="onChange">
      <div id="toolbar" slot="toolbar">
        <select class="ql-size">
          <option value="small"></option>
          <!-- Note a missing, thus falsy value, is used to reset to default -->
          <option selected></option>
          <option value="large"></option>
          <option value="huge"></option>
        </select>
        <!-- Add subscript and superscript buttons -->
        <span class="ql-formats"><button class="ql-script" value="sub"></button></span>
        <span class="ql-formats"><button class="ql-script" value="super"></button></span>
        <span class="ql-formats"><button type="button" class="ql-bold"></button></span>
        <span class="ql-formats"><button type="button" class="ql-italic"></button></span>
        <span class="ql-formats"><button type="button" class="ql-blockquote"></button></span>
        <span class="ql-formats"><button type="button" class="ql-list" value="ordered"></button></span>
        <span class="ql-formats"><button type="button" class="ql-list" value="bullet"></button></span>
        <span class="ql-formats"><button type="button" class="ql-link"></button></span>
        <span class="ql-formats">
        <button type="button" @click="imgClick" style="outline:none">
        <svg viewBox="0 0 18 18"> <rect class="ql-stroke" height="10" width="12" x="3" y="4"></rect> <circle
          class="ql-fill" cx="6" cy="7" r="1"></circle> <polyline class="ql-even ql-fill"
                                                                  points="5 12 5 11 7 9 8 10 11 7 13 9 13 12 5 12"></polyline> </svg>
        </button>
      </span>
        <span class="ql-formats"><button type="button" class="ql-video"></button></span>
      </div>
    </quilleditor>
  </div>
</template>
<script>
  import 'quill/dist/quill.core.css'
  import 'quill/dist/quill.snow.css'
  import 'quill/dist/quill.bubble.css'

  import {quillEditor} from 'vue-quill-editor'
  import fileUtil from './fileUtil';
  export default {
    name: "v-editor",
    props: {
      value: {
        type: String
      },
      /*上传图片的地址*/
      url: {
        type: String,
        default: '/'
      },
      /*上传图片的file控件name*/
      fileName: {
        type: String,
        default: 'file'
      },
      maxUploadSize:{
        type:Number,
        default: 1024 * 1024 * 500
      },
      needSignature: {
        type: Boolean,
        default: false
      }
    },
    data() {
      return {
        content: '',
        editorOption: {
          modules: {
            toolbar: '#toolbar'
          }
        },
      }
    },
    methods: {
      onChange() {
        this.$emit('input', this.content)
      },
      /*选择上传图片切换*/
      async onFileChange(e) {
        const fileInput = e.target;
        if (fileInput.files.length === 0) {
          return
        }
        this.editor.focus();
        if (fileInput.files[0].size > this.maxUploadSize) {
          this.$alert('图片不能大于500KB', '图片尺寸过大', {
            confirmButtonText: '确定',
            type: 'warning',
          })
        }
        let formData = new FormData;
        let filename = fileInput.files[0].name;
        let uploadUrl = this.url;
        // 是否需要签名
        if(this.needSignature){
          // 上传到阿里，进行签名，把接收到的url作为签名接口地址
          const resp = await this.$http.loadData(this.url);
          // 判断接口返回的签名时间是否超时
          if (resp.expire < new Date().getTime()) {
            this.$message.error('请求超时!');
            return;
          }
          // 修改文件名为随机文件名
          filename = fileUtil.getFileName(filename, resp.dir);
          // 修改上传文件路径
          uploadUrl = resp.host;
          // 准备请求参数
          formData.append("key", filename);
          formData.append("policy", resp.policy);
          formData.append("OSSAccessKeyId", resp.accessId);
          formData.append("success_action_status", '200');
          formData.append("signature", resp.signature);
        }
        formData.append(this.fileName, fileInput.files[0]);
        this.$http.post(uploadUrl, formData)
          .then(resp => {
            if (resp.status == '200') {
              const fileUrl = resp.data || uploadUrl + "/" + filename;
              this.editor.insertEmbed(this.editor.getSelection().index, 'image', fileUrl);
            }
          }).catch(error => {
            console.log(error);
            this.$message.error("上传失败，请重试");
        })
      },
      /*点击上传图片按钮*/
      imgClick() {
        if (!this.url) {
          console.log('no editor uploadUrl');
          return;
        }
        /*内存创建input file*/
        let input = document.createElement('input');
        input.type = 'file';
        input.name = this.fileName;
        input.accept = 'image/jpeg,image/png,image/jpg,image/gif';
        input.onchange = this.onFileChange;
        input.click()
      }
    },
    computed: {
      editor() {
        return this.$refs.myTextEditor.quill
      }
    },
    components: {
      'quilleditor': quillEditor
    },
    mounted() {
      this.content = this.value
    },
    watch: {
      'value'(newVal, oldVal) {
        if (this.editor) {
          if (newVal !== this.content) {
            this.content = newVal
          }
        }
      },
    }
  }

</script>
