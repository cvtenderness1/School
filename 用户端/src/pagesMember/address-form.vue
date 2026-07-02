<script setup lang="ts">
import {
  getMemberAddressByIdAPI,
  postMemberAddressAPI,
  putMemberAddressByIdAPI,
} from '@/services/address'
import { onLoad } from '@dcloudio/uni-app'
import { ref } from 'vue'

// 表单数据
const form = ref({
  receiver: '', // 收货人
  contact: '', // 联系方式
  fullLocation: '', // 省市区(前端展示)
  provinceCode: '', // 省份编码(后端参数)
  cityCode: '', // 城市编码(后端参数)
  countyCode: '', // 区/县编码(后端参数)
  address: '', // 详细地址
  isDefault: 0, // 默认地址，1为是，0为否
  lat: 0, // 🔥 新增经纬度字段
  lng: 0,
})

// 获取页面参数
const query = defineProps<{
  id?: string
}>()

// 获取收货地址详情数据
const getMemberAddressByIdData = async () => {
  if (query.id) {
    const res = await getMemberAddressByIdAPI(query.id)
    Object.assign(form.value, res.data)
  }
}

// 页面加载
onLoad(() => {
  getMemberAddressByIdData()
})

// 动态设置标题
uni.setNavigationBarTitle({ title: query.id ? '修改地址' : '新建地址' })

// 收集所在地区
const onRegionChange: UniHelper.RegionPickerOnChange = (ev) => {
  form.value.fullLocation = ev.detail.value.join(' ')
  const [provinceCode, cityCode, countyCode] = ev.detail.code!
  Object.assign(form.value, { provinceCode, cityCode, countyCode })
}

// 收集是否默认收货地址
const onSwitchChange: UniHelper.SwitchOnChange = (ev) => {
  form.value.isDefault = ev.detail.value ? 1 : 0
}

// 定义校验规则
const rules: UniHelper.UniFormsRules = {
  receiver: {
    rules: [{ required: true, errorMessage: '请输入收货人姓名' }],
  },
  contact: {
    rules: [
      { required: true, errorMessage: '请输入联系方式' },
      { pattern: /^1[3-9]\d{9}$/, errorMessage: '手机号格式不正确' },
    ],
  },
  countyCode: {
    rules: [{ required: true, errorMessage: '请选择所在地区' }],
  },
  address: {
    rules: [{ required: true, errorMessage: '请填写详细地址' }],
  },
}

// 高德微信小程序地址解析
const autoResolveLocation = async (addressStr: string) => {
  try {
    // 用Promise包装uni.request，确保await生效
    const res: any = await new Promise((resolve, reject) => {
      uni.request({
        method: 'GET',
        url: 'https://restapi.amap.com/v3/geocode/geo',
        data: {
          key: '322145196e9b6676d0b58de41f6c840b',
          address: addressStr,
          output: 'json',
        },
        success: resolve,
        fail: reject,
      })
    })

    const data = res.data
    console.log('高德返回数据：', data)

    // 校验高德返回状态
    if (data.status === '1' && data.geocodes?.length > 0) {
      const locationStr = data.geocodes[0].location
      const [lng, lat] = locationStr.split(',').map(Number)
      form.value.lat = lat
      form.value.lng = lng
      console.log('解析成功，经纬度：', lat, lng)
    } else {
      console.log('地址解析失败，状态：', data.status, data.info)
    }
  } catch (err) {
    console.log('请求异常：', err)
  }
}

// 表单组件实例
const formRef = ref<UniHelper.UniFormsInstance>()

// 提交表单
const onSubmit = async () => {
  try {
    // 表单校验
    await formRef.value?.validate?.()

    // 拼接完整地址
    const fullAddr = form.value.fullLocation + ' ' + form.value.address
    console.log('要解析的地址：', fullAddr)

    //等待地址解析完成
    await autoResolveLocation(fullAddr)

    // 校验通过后再发送请求
    if (query.id) {
      await putMemberAddressByIdAPI(query.id, form.value)
    } else {
      await postMemberAddressAPI(form.value)
    }

    uni.showToast({ icon: 'success', title: query.id ? '修改成功' : '添加成功' })
    setTimeout(() => {
      uni.navigateBack()
    }, 400)
  } catch (error) {
    uni.showToast({ icon: 'error', title: '请填写完整信息' })
  }
}

// #ifdef H5 || APP-PLUS
const onCityChange: UniHelper.UniDataPickerOnChange = (ev) => {
  const [province, city, county] = ev.detail.value
  Object.assign(form.value, {
    provinceCode: province.value,
    cityCode: city.value,
    countyCode: county.value,
  })
}
// #endif
</script>

<template>
  <view class="content">
    <uni-forms :rules="rules" :model="form" ref="formRef">
      <!-- 表单内容 -->
      <uni-forms-item name="receiver" class="form-item">
        <text class="label">收货人</text>
        <input class="input" placeholder="请填写收货人姓名" v-model="form.receiver" />
      </uni-forms-item>
      <uni-forms-item name="contact" class="form-item">
        <text class="label">手机号码</text>
        <input class="input" placeholder="请填写收货人手机号码" :maxlength="11" v-model="form.contact" />
      </uni-forms-item>
      <uni-forms-item name="countyCode" class="form-item">
        <text class="label">所在地区</text>
        <!-- #ifdef MP-WEIXIN -->
        <picker @change="onRegionChange" class="picker" mode="region" :value="form.fullLocation.split(' ')">
          <view v-if="form.fullLocation">{{ form.fullLocation }}</view>
          <view v-else class="placeholder">请选择省/市/区(县)</view>
        </picker>
        <!-- #endif -->

        <!-- #ifdef H5 || APP-PLUS -->
        <uni-data-picker placeholder="请选择地址" popup-title="请选择城市" collection="opendb-city-china"
          field="code as value, name as text" orderby="value asc" :step-searh="true" self-field="code"
          parent-field="parent_code" @change="onCityChange" :clear-icon="false" v-model="form.countyCode" />
        <!-- #endif -->
      </uni-forms-item>
      <uni-forms-item name="address" class="form-item">
        <text class="label">详细地址</text>
        <input class="input" placeholder="街道、楼牌号等信息" v-model="form.address" />
      </uni-forms-item>
      <view class="form-item">
        <label class="label">设为默认地址</label>
        <switch @change="onSwitchChange" class="switch" color="#27ba9b" :checked="form.isDefault === 1" />
      </view>
    </uni-forms>
  </view>
  <!-- 提交按钮 -->
  <button @tap="onSubmit" class="button">保存并使用</button>
</template>

<style lang="scss">
// 深度选择器修改 uni-data-picker 组件样式
:deep(.selected-area) {
  flex: 0 1 auto;
  height: auto;
}

page {
  background-color: #fff9e6;
  /* 暖黄背景 */
}

.content {
  margin: 20rpx 20rpx 0;
  padding: 0 20rpx;
  border-radius: 10rpx;
  background-color: #fff;
  /* 卡片白色 */

  .form-item,
  .uni-forms-item {
    display: flex;
    align-items: center;
    min-height: 96rpx;
    padding: 25rpx 10rpx;
    background-color: #fff;
    font-size: 28rpx;
    border-bottom: 1rpx solid #f5e8c8;
    /* 浅黄分割线 */
    position: relative;
    margin-bottom: 0;

    // 调整 uni-forms 样式
    .uni-forms-item__content {
      display: flex;
    }

    .uni-forms-item__error {
      margin-left: 200rpx;
      color: #f5a623;
      /* 错误提示暖黄色 */
    }

    &:last-child {
      border: none;
    }

    .label {
      width: 200rpx;
      color: #8c6c35;
      /* 标签文字暖黄色 */
    }

    .input {
      flex: 1;
      display: block;
      height: 46rpx;
      color: #6b4e24;
    }

    .switch {
      position: absolute;
      right: -20rpx;
      transform: scale(0.8);
    }

    .picker {
      flex: 1;
      color: #6b4e24;
    }

    .placeholder {
      color: #c8a870;
      /* 占位符浅黄 */
    }
  }
}

.button {
  height: 80rpx;
  margin: 30rpx 20rpx;
  color: #fff;
  border-radius: 80rpx;
  font-size: 30rpx;
  background-color: #f9c950;
  /* 按钮主色：暖黄色 */
}
</style>
