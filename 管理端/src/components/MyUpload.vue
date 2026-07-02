<!--<template>-->
<!--  <div>-->
<!--    <el-upload-->
<!--        ref="upload"-->
<!--        :action="uploadUrl"-->
<!--        :list-type="listType"-->
<!--        :on-preview="handlePreview"-->
<!--        :file-list="fileList"-->
<!--        :limit="limit"-->
<!--        :accept="accept"-->
<!--        :headers="uploadHeaders"-->
<!--        :on-success="handleFileSuccess"-->
<!--        :on-exceed="handleExceed"-->
<!--        :on-remove="handleRemove">-->
<!--      <el-button size="small" type="primary"> {{-->
<!--          limit === 1 && fileList.length > 0 ? '点击替换' : '点击上传'-->
<!--        }}-->
<!--      </el-button>-->
<!--      <div slot="tip" class="el-upload__tip">{{ tip }}</div>-->
<!--    </el-upload>-->
<!--    <el-dialog v-model="dialogVisible" v-if="dialogVisible">-->
<!--      <div>-->
<!--        <img v-if="type==='image'||type==='imageCard'" width="100%" :src="previewFile.url" alt="">-->
<!--        <video v-if="type==='video'" width="100%" :src="previewFile.url" controls></video>-->
<!--        <audio v-if="type==='audio'" width="100%" :src="previewFile.url" controls></audio>-->
<!--      </div>-->
<!--    </el-dialog>-->
<!--  </div>-->
<!--</template>-->

<!--<script setup>-->
<!--import {ref, reactive, onMounted} from 'vue';-->
<!--import utils from "@/utils/tools.js";-->
<!--import {ElMessage, genFileId} from "element-plus";-->

<!--const props = defineProps({-->
<!--  /**-->
<!--   * 文件类型-->
<!--   */-->
<!--  type: {-->
<!--    type: String,-->
<!--    default: "file"-->
<!--  },-->
<!--  /**-->
<!--   * 文件列表-->
<!--   */-->
<!--  files: {-->
<!--    type: String,-->
<!--    default: ""-->
<!--  },-->
<!--  /**-->
<!--   * 提示信息-->
<!--   */-->
<!--  tip: {-->
<!--    type: String,-->
<!--    default: ""-->
<!--  },-->
<!--  /**-->
<!--   * 上传文件数量限制-->
<!--   */-->
<!--  limit: {-->
<!--    type: Number,-->
<!--    default: 100-->
<!--  }-->
<!--});-->
<!--//组件对象-->
<!--const upload = ref()-->
<!--//上传请求头，需要带token-->
<!--const uploadHeaders = reactive({-->
<!--  //设置token-->
<!--  token: utils.getToken()-->
<!--});-->
<!--//服务器上传的URL-->
<!--const uploadUrl = ref('');-->
<!--//上传文件列表-->
<!--const fileList = ref([]);-->
<!--//允许上传的文件类型-->
<!--const accept = ref('');-->
<!--//默认文件类型-->
<!--const listType = ref('picture-card');-->
<!--//预览弹框-->
<!--const dialogVisible = ref(false);-->
<!--//预览文件信息-->
<!--const previewFile = ref('');-->

<!--onMounted(() => {-->
<!--  //设置上传地址-->
<!--  uploadUrl.value = import.meta.env.VITE_APP_API_URL + "/file/upload";-->
<!--  load();-->
<!--});-->

<!--//加载-->
<!--function load() {-->
<!--  if (props.files) {-->
<!--    //对文件按照，分割-->
<!--    let files = props.files.split(",");-->
<!--    for (let file of files) {-->
<!--      //切割文件名-->
<!--      let strings = file.split("/");-->
<!--      fileList.value.push({-->
<!--        name: strings[strings.length - 1],-->
<!--        url: file-->
<!--      });-->
<!--    }-->
<!--  }-->
<!--  switch (props.type) {-->
<!--      /**-->
<!--       * 图片卡片类型-->
<!--       */-->
<!--    case "imageCard":-->
<!--      listType.value = "picture-card";-->
<!--      accept.value = "image/*";-->
<!--      break;-->
<!--      /**-->
<!--       *图片-->
<!--       */-->
<!--    case "image":-->
<!--      listType.value = "picture";-->
<!--      accept.value = "image/*";-->
<!--      break;-->
<!--      /**-->
<!--       * 视频-->
<!--       */-->
<!--    case "video":-->
<!--      accept.value = "video/*";-->
<!--      listType.value = "text";-->
<!--      break;-->
<!--      /**-->
<!--       * 音频-->
<!--       */-->
<!--    case "audio":-->
<!--      accept.value = "audio/*";-->
<!--      listType.value = "text";-->
<!--      break;-->
<!--      /**-->
<!--       * 文件 附件-->
<!--       */-->
<!--    case "file":-->
<!--      listType.value = "text";-->
<!--      break;-->
<!--  }-->
<!--}-->

<!--//回调父组件，设置文件数据-->
<!--const emit = defineEmits(['setFiles']);-->

<!--/**-->
<!-- * 通知父组件文件改变-->
<!-- */-->
<!--function setFiles() {-->
<!--  let files = fileList.value.map((item) => {-->
<!--    return item.url;-->
<!--  });-->
<!--  emit("setFiles", files.join(","));-->
<!--}-->

<!--/**-->
<!-- * 文件上传成功后的处理-->
<!-- * @param response-->
<!-- * @param file-->
<!-- * @param fileListRes-->
<!-- */-->
<!--function handleFileSuccess(response, file, fileListRes) {-->
<!--  //添加到文件对象-->
<!--  fileList.value.push({-->
<!--    name: response.data.name,-->
<!--    url: response.data.url-->
<!--  });-->
<!--  //通知父组件文件改变-->
<!--  setFiles();-->
<!--}-->

<!--/**-->
<!-- * 删除文件-->
<!-- * @param file-->
<!-- * @param fileListRes-->
<!-- */-->
<!--function handleRemove(file, fileListRes) {-->
<!--  fileList.value = fileListRes;-->
<!--  //通知父组件文件改变-->
<!--  setFiles();-->
<!--}-->

<!--/**-->
<!-- * 预览文件-->
<!-- * @param file-->
<!-- */-->
<!--function handlePreview(file) {-->
<!--  //设置预览对象-->
<!--  previewFile.value = file;-->
<!--  if (props.type === "file") {-->
<!--    //文件类型直接下载-->
<!--    downloadFile();-->
<!--    return;-->
<!--  }-->
<!--  //打开预览弹框-->
<!--  dialogVisible.value = true;-->
<!--}-->

<!--/**-->
<!-- * 处理文件超出限制的情况-->
<!-- * @param files-->
<!-- */-->
<!--function handleExceed(files) {-->
<!--  //如果只有一个就进行替换-->
<!--  if (props.limit === 1) {-->
<!--    // 这个方法会移除数组的最后一个元素，并返回被移除的元素。如果数组为空，则返回 undefine-->
<!--    fileList.value.pop()-->
<!--    upload.value.clearFiles()-->
<!--    const file = files[0];-->
<!--    file.uid = genFileId()-->
<!--    upload.value.handleStart(file)-->
<!--    upload.value.submit()-->
<!--  } else {-->
<!--    ElMessage.warning(`最多只允许上传${props.limit}张图片`);-->
<!--  }-->
<!--}-->

<!--/**-->
<!-- * 下载文件-->
<!-- */-->
<!--function downloadFile() {-->
<!--  const link = document.createElement('a');-->
<!--  link.style.display = 'none';-->
<!--  document.body.appendChild(link);-->
<!--  link.href = previewFile.value.url;-->
<!--  console.log(previewFile.value);-->
<!--  link.setAttribute('download', previewFile.value.name); // 你可以自定义下载时的文件名-->
<!--  link.click();-->
<!--  link.remove();-->
<!--}-->
<!--</script>-->


<!--<style>-->
<!--</style>-->
