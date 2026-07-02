<template>
  <div class="page-container">
    <div class="search-section">
      <el-form :inline="true" :model="data" class="custom-search-form">
        <el-form-item label="店铺名称">
          <el-input
              v-model="data.merchantName"
              placeholder="店铺查询"
              :prefix-icon="Search"
              clearable
          />
        </el-form-item>
        <el-form-item label="营业状态">
          <el-select v-model="data.status" placeholder="全部状态" style="width: 120px">
            <el-option label="营业中" :value="1" />
            <el-option label="已停业" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item label="店铺分类">
          <el-select
              v-model="data.categoryName"
              placeholder="选择/搜索分类"
              style="width:160px"
              filterable
              clearable
          >
            <el-option
                v-for="item in categoryList"
                :key="item.category_id"
                :label="item.category_name"
                :value="item.category_name"
            />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" class="btn-orange" @click="load">查询</el-button>
          <el-button @click="reset">重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <div class="operation-section">
      <el-button type="primary" class="btn-orange-gradient" @click="HandAdd">
        <el-icon><Shop /></el-icon> 新增商家
      </el-button>
      <el-button type="primary" @click="openGoodDialog">
        <el-icon><Food /></el-icon> 新增商品
      </el-button>
      <el-button type="danger" @click="HandDelete">
        <el-icon><Delete /></el-icon> 批量删除
      </el-button>

      <!-- 👇 这里新增：店铺营销设置按钮 -->
      <el-button type="primary" @click="openMerchantSetting">
        <el-icon><Setting /></el-icon> 店铺营销设置
      </el-button>

      <div class="data-info">
        共 <span class="highlight">{{ data.total }}</span> 家店铺
      </div>
    </div>

    <el-table
        :data="data.tabledata"
        class="custom-table"
        stripe
        @selection-change="handleSelectionChange"
    >
      <el-table-column type="selection" width="55" />
      <el-table-column prop="merchantId" label="ID" width="60" />
      <el-table-column label="封面" width="80">
        <template #default="scope">
          <el-image
              style="width:50px;height:50px;border-radius:8px"
              :src="scope.row.coverImg"
              fit="cover"
          />
        </template>
      </el-table-column>
      <el-table-column prop="merchantName" label="店铺名称" width="160" />
      <el-table-column prop="address" label="地址" min-width="100" show-overflow-tooltip />
      <el-table-column label="评分" width="170">
        <template #default="scope">
          <el-rate v-model="scope.row.score" disabled show-score text-color="#ff9900" score-template="{value}"/>
        </template>
      </el-table-column>
      <el-table-column label="状态" width="100">
        <template #default="scope">
          <el-tag type="success" v-if="scope.row.status===1">营业中</el-tag>
          <el-tag type="danger" v-if="scope.row.status===0">已停业</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="入驻时间" width="100" />

      <el-table-column label="操作" width="320" fixed="right" align="center">
        <template #default="scope">
          <el-button link type="primary" class="text-orange" @click="HandleEdit(scope.row)">
            <el-icon><EditPen /></el-icon> 编辑
          </el-button>
          <el-button link type="success" @click="viewGoods(scope.row.merchantId)">
            <el-icon><View /></el-icon> 查看商品
          </el-button>
          <el-button link type="danger" @click="deletesave(scope.row.merchantId)">
            <el-icon><Delete /></el-icon> 删除
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <div style="margin-top:15px">
      <el-pagination
          @size-change="load"
          @current-change="load"
          v-model:current-page="data.pagenum"
          v-model:page-size="data.pagesize"
          layout="total, sizes, prev, pager, next, jumper"
          :total="data.total"
      />
    </div>

    <!-- 商家弹窗 -->
    <el-dialog v-model="data.dialogFormVisible" title="商家信息" width="550" destroy-on-close>
      <el-form ref="formRef" :rules="data.rules" :model="data.form">
        <el-form-item label="店铺封面" label-width="80px">
          <el-upload
              class="avatar-uploader"
              action="http://localhost:1000/files/upload"
              :show-file-list="false"
              :on-success="handleCoverSuccess"
          >
            <img v-if="data.form.coverImg" :src="data.form.coverImg" class="avatar" />
            <el-icon v-else class="avatar-uploader-icon"><Plus /></el-icon>
          </el-upload>
        </el-form-item>

        <el-form-item prop="merchantName" label="店铺名" label-width="80px">
          <el-input v-model="data.form.merchantName" placeholder="请输入店铺名称" />
        </el-form-item>
        <el-form-item prop="categoryName" label="分类名称" label-width="80px">
          <el-input v-model="data.form.categoryName" placeholder="如：奶茶、快餐、水果"/>
        </el-form-item>
        <el-form-item label="地址" label-width="80px">
          <el-input v-model="data.form.address" placeholder="店铺地址" style="width: 280px;" />
          <el-button type="primary" @click="openMap" style="margin-left: 5px;">
            <el-icon><Location /></el-icon> 地图选点
          </el-button>
        </el-form-item>

        <el-form-item label="经纬度" label-width="80px">
          <el-input v-model="data.form.lat" placeholder="纬度" style="width: 120px; margin-right: 10px" />
          <el-input v-model="data.form.lng" placeholder="经度" style="width: 120px" />
        </el-form-item>
        <el-form-item label="商家描述" label-width="80px">
          <el-input
              v-model="data.form.alt"
              type="textarea"
              rows="2"
              placeholder="请输入商家简介/描述"
          />
        </el-form-item>
        <el-form-item label="状态" label-width="80px">
          <el-radio-group v-model="data.form.status">
            <el-radio :label="1">营业</el-radio>
            <el-radio :label="0">停业</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="data.dialogFormVisible=false">取消</el-button>
        <el-button type="primary" @click="save">保存</el-button>
      </template>
    </el-dialog>

    <!-- 新增商品弹窗 -->
    <el-dialog v-model="goodDialogVisible" title="新增商品" width="580px" append-to-body>
      <el-form ref="goodFormRef" :model="goodForm" :rules="goodRules" label-width="80px">
        <el-form-item label="所属商家" prop="merchantId">
          <el-select v-model="goodForm.merchantId" placeholder="请选择商家" @change="loadMerchantCategory" style="width:100%">
            <el-option v-for="item in data.tabledata" :key="item.merchantId" :label="item.merchantName" :value="item.merchantId" />
          </el-select>
        </el-form-item>
        <el-form-item label="商品分类" prop="categoryId">
          <el-select v-model="goodForm.categoryId" placeholder="请选择分类" style="width:100%">
            <el-option v-for="item in merchantCategoryList" :key="item.categoryId" :label="item.categoryName" :value="item.categoryId" />
          </el-select>
        </el-form-item>
        <el-form-item label="商品名称" prop="goodsName">
          <el-input v-model="goodForm.goodsName" placeholder="请输入商品名称" />
        </el-form-item>
        <el-form-item label="原价" prop="price">
          <el-input v-model="goodForm.price" type="number" placeholder="请输入原价" />
        </el-form-item>
        <el-form-item label="折扣价" prop="discountPrice">
          <el-input v-model="goodForm.discountPrice" type="number" placeholder="请输入折扣价" />
        </el-form-item>

        <el-form-item label="商品图片" prop="img">
          <el-upload
              class="avatar-uploader"
              action="http://localhost:1000/files/upload"
              :show-file-list="false"
              :on-success="handleGoodImgSuccess"
          >
            <img v-if="goodForm.img" :src="goodForm.img" class="avatar" />
            <el-icon v-else class="avatar-uploader-icon"><Plus /></el-icon>
          </el-upload>
        </el-form-item>
        <el-form-item label="商品描述" label-width="80px">
          <el-input
              v-model="goodForm.desc"
              type="textarea"
              rows="2"
              placeholder="请输入商品描述/简介"
          />
        </el-form-item>
        <el-form-item label="库存" prop="stock">
          <el-input v-model="goodForm.stock" type="number" placeholder="库存数量" />
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="goodForm.status">
            <el-radio :label="1">上架</el-radio>
            <el-radio :label="0">下架</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="goodDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="addGood">保存商品</el-button>
      </template>
    </el-dialog>

    <!-- 编辑商品弹窗 -->
    <el-dialog v-model="editGoodDialogVisible" title="编辑商品" width="580px" append-to-body>
      <el-form ref="editGoodFormRef" :model="editGoodForm" :rules="goodRules" label-width="80px">
        <el-form-item label="商品名称" prop="goodsName">
          <el-input v-model="editGoodForm.goodsName" placeholder="请输入商品名称" />
        </el-form-item>
        <el-form-item label="原价" prop="price">
          <el-input v-model="editGoodForm.price" type="number" placeholder="请输入原价" />
        </el-form-item>
        <el-form-item label="折扣价" prop="discountPrice">
          <el-input v-model="editGoodForm.discountPrice" type="number" placeholder="请输入折扣价" />
        </el-form-item>

        <el-form-item label="商品图片" prop="img">
          <el-upload
              class="avatar-uploader"
              action="http://localhost:1000/files/upload"
              :show-file-list="false"
              :on-success="handleEditGoodImgSuccess"
          >
            <img v-if="editGoodForm.img" :src="editGoodForm.img" class="avatar" />
            <el-icon v-else class="avatar-uploader-icon"><Plus /></el-icon>
          </el-upload>
        </el-form-item>
        <el-form-item label="商品描述" label-width="80px">
          <el-input
              v-model="editGoodForm.desc"
              type="textarea"
              rows="2"
              placeholder="请输入商品描述/简介"
          />
        </el-form-item>
        <el-form-item label="库存" prop="stock">
          <el-input v-model="editGoodForm.stock" type="number" placeholder="库存数量" />
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="editGoodForm.status">
            <el-radio :label="1">上架</el-radio>
            <el-radio :label="0">下架</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editGoodDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="updateGood">保存修改</el-button>
      </template>
    </el-dialog>

    <!-- 查看商品弹窗 -->
    <el-dialog v-model="viewGoodDialogVisible" title="店铺商品列表" width="700px" append-to-body>
      <div style="margin-bottom:12px;">
        <el-select v-model="selectedDesc" placeholder="按商品描述分类" clearable @change="filterGoods" style="width:200px">
          <el-option label="全部" :value="null" />
          <el-option
              v-for="d in descList"
              :key="d"
              :label="d"
              :value="d"
          />
        </el-select>
      </div>

      <el-table :data="filteredGoodList" stripe border size="default">
        <el-table-column label="图片" width="80">
          <template #default="scope">
            <el-image style="width:50px;height:50px;border-radius:6px" :src="scope.row.img" fit="cover" />
          </template>
        </el-table-column>
        <el-table-column prop="goodsName" label="商品名称" />
        <el-table-column prop="price" label="原价" />
        <el-table-column prop="discountPrice" label="折扣价" />
        <el-table-column prop="stock" label="库存" />
        <el-table-column label="状态" width="100">
          <template #default="scope">
            <el-tag type="success" v-if="scope.row.status===1">上架</el-tag>
            <el-tag type="info" v-else>下架</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="100" align="center">
          <template #default="scope">
            <el-button link type="primary" @click="openEditGoodDialog(scope.row)">编辑</el-button>
            <el-button link type="success" @click="openSkuDialog(scope.row)">SKU 管理</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-dialog>

    <!-- ================== SKU 规格管理弹窗 ================== -->
    <el-dialog v-model="skuDialogVisible" title="商品规格与SKU管理" width="900px" append-to-body>
      <div v-if="currentGoodsId">
        <h3 style="margin-bottom:12px">商品：{{ currentGoodsName }}</h3>
        <div style="margin-bottom:20px">
          <el-button size="small" @click="addSpec">+ 添加规格（如：颜色、尺寸、口味）</el-button>
        </div>

        <div v-for="(spec, idx) in specList" :key="idx" style="margin-bottom:15px;border:1px solid #eee;padding:10px;border-radius:8px">
          <el-input v-model="spec.name" placeholder="规格名称：例如 颜色/尺寸/口味" style="width:160px;margin-right:10px" />
          <el-input v-model="spec.itemsStr" placeholder="规格值：例如 红色,蓝色,XL,XXL" style="width:280px;margin-right:10px" />
          <el-button size="small" type="danger" @click="delSpec(idx)">删除</el-button>
        </div>

        <div style="margin:15px 0">
          <el-button size="small" type="primary" @click="buildSku">生成 SKU 组合</el-button>
        </div>

        <el-table :data="skuList" border size="small" style="width:100%">
          <el-table-column label="SKU 组合" prop="skuNameArr" />
          <el-table-column label="图片" width="80">
            <template #default="scope">
              <el-upload
                  action="http://localhost:1000/files/upload"
                  :show-file-list="false"
                  :on-success="(res) => setSkuImg(scope.row, res)"
              >
                <img v-if="scope.row.image" :src="scope.row.image" style="width:40px;height:40px;border-radius:6px" />
                <el-icon v-else size="12"><Plus /></el-icon>
              </el-upload>
            </template>
          </el-table-column>
          <el-table-column label="价格">
            <template #default="scope">
              <el-input-number v-model="scope.row.price" :min="0" :step="0.1" size="small" />
            </template>
          </el-table-column>
          <el-table-column label="折扣价">
            <template #default="scope">
              <el-input-number v-model="scope.row.discountPrice" :min="0" :step="0.1" size="small" />
            </template>
          </el-table-column>
          <el-table-column label="库存">
            <template #default="scope">
              <el-input-number v-model="scope.row.stock" :min="0" size="small" />
            </template>
          </el-table-column>
          <el-table-column label="操作" width="60" align="center">
            <template #default="scope">
              <el-button link type="danger" size="small" @click="delSku(scope.row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>

        <div style="text-align:right;margin-top:20px">
          <el-button @click="saveSkuAll">保存所有 SKU</el-button>
        </div>
      </div>
    </el-dialog>

    <!-- ================== 店铺营销设置（运费/竞价/高峰/优惠券） ================== -->
    <el-dialog v-model="settingVisible" title="店铺营销设置" width="1000px" append-to-body destroy-on-close>
      <el-tabs v-model="activeSettingTab">
        <el-tab label="运费模板" name="freight">
          <div style="padding:10px 0">
            <el-button type="primary" size="small" @click="openFreightAdd">新增运费模板</el-button>
            <el-table :data="freightList" border style="margin-top:10px" size="small">
              <el-table-column prop="name" label="模板名"/>
              <el-table-column prop="basePrice" label="起步价"/>
              <el-table-column prop="baseDistance" label="起步距离(米)"/>
              <el-table-column prop="extraPrice" label="每km加价"/>
              <el-table-column label="操作">
                <template #default="{row}">
                  <el-button link size="small" @click="openFreightEdit(row)">编辑</el-button>
                  <el-button link type="danger" size="small" @click="delFreight(row.id)">删除</el-button>
                </template>
              </el-table-column>
            </el-table>
          </div>
        </el-tab>

        <el-tab label="竞价策略" name="bid">
          <div style="padding:10px 0">
            <el-button type="primary" size="small" @click="openBidAdd">新增竞价策略</el-button>
            <el-table :data="bidList" border style="margin-top:10px" size="small">
              <el-table-column prop="name" label="策略名"/>
              <el-table-column prop="baseBid" label="基础竞价"/>
              <el-table-column prop="minBid" label="最低"/>
              <el-table-column prop="maxBid" label="最高"/>
              <el-table-column label="操作">
                <template #default="{row}">
                  <el-button link size="small" @click="openBidEdit(row)">编辑</el-button>
                  <el-button link type="danger" size="small" @click="delBid(row.id)">删除</el-button>
                </template>
              </el-table-column>
            </el-table>
          </div>
        </el-tab>

        <el-tab label="高峰溢价" name="peak">
          <div style="padding:10px 0">
            <el-button type="primary" size="small" @click="openPeakAdd">新增高峰溢价</el-button>
            <el-table :data="peakList" border style="margin-top:10px" size="small">
              <el-table-column prop="name" label="名称"/>
              <el-table-column prop="timeRange" label="时间段"/>
              <el-table-column prop="weekDays" label="周几"/>
              <el-table-column prop="rate" label="溢价倍率"/>
              <el-table-column label="操作">
                <template #default="{row}">
                  <el-button link size="small" @click="openPeakEdit(row)">编辑</el-button>
                  <el-button link type="danger" size="small" @click="delPeak(row.id)">删除</el-button>
                </template>
              </el-table-column>
            </el-table>
          </div>
        </el-tab>

        <el-tab label="优惠券" name="coupon">
          <div style="padding:10px 0">
            <el-button type="primary" size="small" @click="openCouponAdd">新增优惠券</el-button>
            <el-table :data="couponList" border style="margin-top:10px" size="small">
              <el-table-column prop="name" label="名称"/>
              <el-table-column prop="type" label="类型">
                <template #default="{row}">
                  {{ row.type===1?'满减':'折扣' }}
                </template>
              </el-table-column>
              <el-table-column prop="minAmount" label="最低消费"/>
              <el-table-column label="减多少钱">
                <template #default="{row}">
                  <span v-if="row.type===1">{{ row.reducePrice }}</span>
                </template>
              </el-table-column>
              <el-table-column label="折扣">
                <template #default="{row}">
                  <span v-if="row.type===2">{{ row.discount }}</span>
                </template>
              </el-table-column>
              <el-table-column label="操作">
                <template #default="{row}">
                  <el-button link size="small" @click="openCouponEdit(row)">编辑</el-button>
                  <el-button link type="danger" size="small" @click="delCoupon(row.id)">删除</el-button>
                </template>
              </el-table-column>
            </el-table>
          </div>
        </el-tab>
      </el-tabs>
    </el-dialog>

    <!-- 运费弹窗 -->
    <el-dialog v-model="freightDialog" title="运费模板" width="500px" append-to-body>
      <el-form :model="freightForm" label-width="100px">
        <el-form-item label="模板名称"><el-input v-model="freightForm.name"/></el-form-item>
        <el-form-item label="起步价"><el-input v-model="freightForm.basePrice"/></el-form-item>
        <el-form-item label="起步距离(米)"><el-input v-model="freightForm.baseDistance"/></el-form-item>
        <el-form-item label="每km加价"><el-input v-model="freightForm.extraPrice"/></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="freightDialog=false">取消</el-button>
        <el-button type="primary" @click="saveFreight">保存</el-button>
      </template>
    </el-dialog>

    <!-- 竞价弹窗 -->
    <el-dialog v-model="bidDialog" title="竞价策略" width="500px" append-to-body>
      <el-form :model="bidForm" label-width="100px">
        <el-form-item label="策略名称"><el-input v-model="bidForm.name"/></el-form-item>
        <el-form-item label="基础竞价"><el-input v-model="bidForm.baseBid"/></el-form-item>
        <el-form-item label="最低竞价"><el-input v-model="bidForm.minBid"/></el-form-item>
        <el-form-item label="最高竞价"><el-input v-model="bidForm.maxBid"/></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="bidDialog=false">取消</el-button>
        <el-button type="primary" @click="saveBid">保存</el-button>
      </template>
    </el-dialog>

    <!-- 高峰弹窗 -->
    <el-dialog v-model="peakDialog" title="高峰溢价" width="500px" append-to-body>
      <el-form :model="peakForm" label-width="100px">
        <el-form-item label="名称"><el-input v-model="peakForm.name"/></el-form-item>
        <el-form-item label="时间段"><el-input v-model="peakForm.timeRange" placeholder="11:00-13:00"/></el-form-item>
        <el-form-item label="周几"><el-input v-model="peakForm.weekDays" placeholder="1,2,3,4,5"/></el-form-item>
        <el-form-item label="溢价倍率"><el-input v-model="peakForm.rate"/></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="peakDialog=false">取消</el-button>
        <el-button type="primary" @click="savePeak">保存</el-button>
      </template>
    </el-dialog>

    <!-- 优惠券弹窗 -->
    <el-dialog v-model="couponDialog" title="优惠券" width="550px" append-to-body>
      <el-form :model="couponForm" label-width="100px">
        <el-form-item label="优惠券名称"><el-input v-model="couponForm.name"/></el-form-item>
        <el-form-item label="类型">
          <el-radio-group v-model="couponForm.type">
            <el-radio :label="1">满减</el-radio>
            <el-radio :label="2">折扣</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="最低消费"><el-input v-model="couponForm.minAmount"/></el-form-item>
        <el-form-item label="减金额" v-if="couponForm.type===1"><el-input v-model="couponForm.reducePrice"/></el-form-item>
        <el-form-item label="折扣率" v-if="couponForm.type===2"><el-input v-model="couponForm.discount"/></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="couponDialog=false">取消</el-button>
        <el-button type="primary" @click="saveCoupon">保存</el-button>
      </template>
    </el-dialog>

  </div>
</template>

<script setup>
import { ElMessageBox, ElMessage } from "element-plus";
import { Search, Shop, Delete, EditPen, Food, View, Plus, Setting, Location } from "@element-plus/icons-vue";
import request from "@/utils/request.js";
import { onMounted, reactive, ref } from "vue";

onMounted(() => {
  load();
  loadCategory();
});

const data = reactive({
  merchantName: null,
  categoryName: null,
  status: null,
  tabledata: [],
  pagenum: 1,
  pagesize: 10,
  total: 0,
  dialogFormVisible: false,
  form: {},
  ids: [],
  rules: {
    merchantName: [{ required: true, message: '请输入店铺名', trigger: 'blur' }],
    categoryName: [{ required: true, message: '请输入分类名称', trigger: 'blur' }]
  }
});

const categoryList = ref([]);
const formRef = ref();

const loadCategory = () => {
  request.get("/category/listAll").then(res => {
    const map = {}
    const uniqueList = res.data.filter(item => {
      if (!map[item.category_name]) {
        map[item.category_name] = true
        return true
      }
      return false
    })
    categoryList.value = uniqueList;
  });
};

const load = () => {
  request.get("/merchant/page", { params: data }).then(res => {
    data.tabledata = res.data.list;
    data.total = res.data.total;
  })
}

const reset = () => {
  data.merchantName = null;
  data.categoryName = null;
  data.status = null;
  load();
}

const handleCoverSuccess = (res) => {
  data.form.coverImg = res.data;
}
const handleGoodImgSuccess = (res) => {
  goodForm.img = res.data;
}
// 地图选点（跳转新页面，自动回填）
// 地图选点（跳转新页面 → 复制经纬度 → 粘贴即可）
const openMap = () => {
  window.open("https://lbs.amap.com/tools/picker", "_blank");
  ElMessage.success("请复制经纬度，粘贴到表单里！");
};
const HandAdd = () => {
  data.dialogFormVisible = true;
  data.form = {
    coverImg: "",
    status: 1, score: 5.0, categoryName: "", alt: "",
    address: "",
    lat: "",
    lng: ""
  }
}

const save = () => {
  formRef.value.validate(v => {
    if (v) data.form.merchantId ? update() : add()
  })
}

const add = () => {
  const categoryName = data.form.categoryName;
  request.post("/merchant/add", data.form, { params: { categoryName } }).then(() => {
    ElMessage.success("新增成功");
    data.dialogFormVisible = false;
    load();
  });
};

const update = () => {
  const categoryName = data.form.categoryName;
  request.put("/merchant/update", data.form, { params: { categoryName } }).then(() => {
    ElMessage.success("修改成功");
    data.dialogFormVisible = false;
    load();
  });
};

const HandleEdit = (row) => {
  data.form = { ...row };
  request.get("/merchant/findid", { params: { merchantId: row.merchantId } }).then(res => {
    data.form.categoryName = res.data.categoryName || "";
  });
  data.dialogFormVisible = true;
};

const deletesave = (id) => {
  ElMessageBox.confirm("确认删除？").then(() => {
    request.delete("/merchant/delBatch", { data: [id] }).then(() => {
      ElMessage.success("删除成功");
      load();
    })
  })
}

const HandDelete = () => {
  if (!data.ids.length) return ElMessage.warning("请选择");
  request.delete("/merchant/delBatch", { data: data.ids }).then(() => {
    ElMessage.success("批量删除成功");
    load();
  })
}

const handleSelectionChange = (val) => {
  data.ids = val.map(i => i.merchantId);
}

// ================== 商品相关 ==================
const goodDialogVisible = ref(false);
const goodFormRef = ref(null);
const merchantCategoryList = ref([]);

const goodForm = reactive({
  merchantId: null,
  categoryId: null,
  goodsName: "",
  price: 0,
  discountPrice: 0,
  img: "",
  stock: 999,
  status: 1,
  desc: ""
});

const editGoodDialogVisible = ref(false);
const editGoodFormRef = ref(null);
const editGoodForm = reactive({
  goodsId: null,
  goodsName: "",
  price: 0,
  discountPrice: 0,
  img: "",
  stock: 0,
  status: 1
});

const goodRules = {
  merchantId: [{ required: true, message: "请选择商家", trigger: "blur" }],
  categoryId: [{ required: true, message: "请选择分类", trigger: "blur" }],
  goodsName: [{ required: true, message: "请输入商品名称", trigger: "blur" }],
  price: [{ required: true, message: "请输入原价" }]
};

const openGoodDialog = () => {
  goodDialogVisible.value = true;
  Object.assign(goodForm, {
    merchantId: null, categoryId: null, goodsName: "", price: 0, discountPrice: 0,
    img: "", stock: 999, status: 1, desc: ""
  });
  merchantCategoryList.value = [];
};

const openEditGoodDialog = (row) => {
  editGoodDialogVisible.value = true;
  Object.assign(editGoodForm, row);
};

const handleEditGoodImgSuccess = (res) => {
  editGoodForm.img = res.data;
}

const loadMerchantCategory = () => {
  if (!goodForm.merchantId) return;
  request.get("/category/byMerchant", { params: { merchantId: goodForm.merchantId } }).then(res => {
    merchantCategoryList.value = res.data;
  });
};

const addGood = () => {
  goodFormRef.value.validate(valid => {
    if (!valid) return;
    request.post("/goods/add", goodForm).then(() => {
      ElMessage.success("商品新增成功！");
      goodDialogVisible.value = false;
    });
  });
};

const updateGood = () => {
  editGoodFormRef.value.validate(valid => {
    if (!valid) return;
    request.put("/goods/update", editGoodForm).then(() => {
      ElMessage.success("商品修改成功");
      editGoodDialogVisible.value = false;
      viewGoods(editGoodForm.merchantId);
    });
  });
};

const viewGoodDialogVisible = ref(false);
const goodList = ref([]);
const filteredGoodList = ref([]);
const selectedDesc = ref(null);
const descList = ref([]);

const viewGoods = (merchantId) => {
  request.get("/goods/list", { params: { merchantId } }).then(res => {
    goodList.value = res.data;
    filteredGoodList.value = res.data;
    const descs = [...new Set(res.data.map(item => item.desc).filter(Boolean))];
    descList.value = descs;
    viewGoodDialogVisible.value = true;
  });
};

const filterGoods = () => {
  if (!selectedDesc.value) {
    filteredGoodList.value = goodList.value;
    return;
  }
  filteredGoodList.value = goodList.value.filter(item => item.desc === selectedDesc.value);
};

// ================== SKU ==================
const skuDialogVisible = ref(false)
const currentGoodsId = ref(null)
const currentGoodsName = ref('')
const specList = ref([])
const skuList = ref([])

const openSkuDialog = (row) => {
  currentGoodsId.value = row.goodsId
  currentGoodsName.value = row.goodsName
  skuDialogVisible.value = true
  specList.value = []
  skuList.value = []
  loadSkuData()
}

const loadSkuData = () => {
  request.get('/goods/sku/' + currentGoodsId.value).then(res => {
    specList.value = res.data.specList || []
    skuList.value = res.data.skuList || []
    specList.value.forEach(s => {
      s.itemsStr = s.list?.map(i => i.name).join(',') || ''
    })
  })
}

const addSpec = () => {
  specList.value.push({name: '', itemsStr: '', list: []})
}

const delSpec = (idx) => {
  specList.value.splice(idx, 1)
}

const buildSku = () => {
  const specValues = specList.value.map(s => s.itemsStr.split(',').map(t => t.trim()).filter(Boolean))

  function cartesian(arrays) {
    return arrays.reduce((a, b) => a.flatMap(d => b.map(e => [...d, e])), [[]])
  }

  const skuItems = cartesian(specValues)
  skuList.value = skuItems.map(names => ({
    goodsId: currentGoodsId.value,
    skuNameArr: names.join(','),
    price: 0, discountPrice: 0, stock: 0, image: ''
  }))
}

const setSkuImg = (sku, res) => {
  sku.image = res.data
}

const saveSkuAll = () => {
  request.post('/goods/sku/saveAll', {
    goodsId: currentGoodsId.value, specs: specList.value, skus: skuList.value
  }).then(() => ElMessage.success('保存成功'))
}

const delSku = (row) => {
  ElMessageBox.confirm('确定删除？').then(() => {
    const i = skuList.value.indexOf(row)
    if (i > -1) skuList.value.splice(i, 1)
  })
}

// 店铺营销设置

const settingVisible = ref(false)
const activeSettingTab = ref('freight')
const currentMerchantId = ref(null)

// 核心：打开选中店铺的营销设置
const openMerchantSetting = () => {
  // 没选中店铺提示
  if (!data.ids || data.ids.length === 0) {
    ElMessage.warning('请先选择店铺！')
    return
  }

  // 多个店铺默认取第一个
  currentMerchantId.value = data.ids[0]
  console.log('打开店铺：' + currentMerchantId.value)

  // 打开弹窗
  settingVisible.value = true

  // 加载当前店铺数据
  setTimeout(() => {
    loadAllSetting()
  }, 100)
}

// 加载所有配置
const loadAllSetting = () => {
  if (!currentMerchantId.value) return
  loadFreight()
  loadBid()
  loadPeak()
  loadCoupon()
}

// 运费
const freightList = ref([])
const freightDialog = ref(false)
const freightForm = ref({})
const isFreightEdit = ref(false)

const loadFreight = () => {
  request.get('/merchant/setting/freight/list', {params: {merchantId: currentMerchantId.value}}).then(res => freightList.value = res.data)
}
const openFreightAdd = () => {
  freightForm.value = {merchantId: currentMerchantId.value}
  isFreightEdit.value = false
  freightDialog.value = true
}
const openFreightEdit = (row) => {
  freightForm.value = {...row}
  isFreightEdit.value = true
  freightDialog.value = true
}
const saveFreight = () => {
  const url = isFreightEdit.value ? '/merchant/setting/freight/update' : '/merchant/setting/freight/add'
  request.post(url, freightForm.value).then(() => {
    freightDialog.value = false
    loadFreight()
  })
}
const delFreight = (id) => {
  ElMessageBox.confirm('确定删除？').then(() => {
    request.delete('/merchant/setting/freight/del', {params: {id}}).then(loadFreight)
  })
}

// 竞价
const bidList = ref([])
const bidDialog = ref(false)
const bidForm = ref({})
const isBidEdit = ref(false)

const loadBid = () => {
  request.get('/merchant/setting/bid/list', {params: {merchantId: currentMerchantId.value}}).then(res => bidList.value = res.data)
}
const openBidAdd = () => {
  bidForm.value = {merchantId: currentMerchantId.value, status: 1}
  isBidEdit.value = false
  bidDialog.value = true
}
const openBidEdit = (row) => {
  bidForm.value = {...row}
  isBidEdit.value = true
  bidDialog.value = true
}
const saveBid = () => {
  const url = isBidEdit.value ? '/merchant/setting/bid/update' : '/merchant/setting/bid/add'
  request.post(url, bidForm.value).then(() => {
    bidDialog.value = false
    loadBid()
  })
}
const delBid = (id) => {
  ElMessageBox.confirm('确定删除？').then(() => {
    request.delete('/merchant/setting/bid/del', {params: {id}}).then(loadBid)
  })
}

//  高峰溢价
const peakList = ref([])
const peakDialog = ref(false)
const peakForm = ref({})
const isPeakEdit = ref(false)

const loadPeak = () => {
  request.get('/merchant/setting/peak/list', {params: {merchantId: currentMerchantId.value}}).then(res => peakList.value = res.data)
}
const openPeakAdd = () => {
  peakForm.value = {merchantId: currentMerchantId.value, status: 1, rate: 1.2}
  isPeakEdit.value = false
  peakDialog.value = true
}
const openPeakEdit = (row) => {
  peakForm.value = {...row}
  isPeakEdit.value = true
  peakDialog.value = true
}
const savePeak = () => {
  const url = isPeakEdit.value ? '/merchant/setting/peak/update' : '/merchant/setting/peak/add'
  request.post(url, peakForm.value).then(() => {
    peakDialog.value = false
    loadPeak()
  })
}
const delPeak = (id) => {
  ElMessageBox.confirm('确定删除？').then(() => {
    request.delete('/merchant/setting/peak/del', {params: {id}}).then(loadPeak)
  })
}

// 优惠券
const couponList = ref([])
const couponDialog = ref(false)
const couponForm = ref({})
const isCouponEdit = ref(false)

const loadCoupon = () => {
  request.get('/merchant/setting/coupon/list', {params: {merchantId: currentMerchantId.value}}).then(res => couponList.value = res.data)
}
const openCouponAdd = () => {
  couponForm.value = {merchantId: currentMerchantId.value, type: 1, status: 1}
  isCouponEdit.value = false
  couponDialog.value = true
}
const openCouponEdit = (row) => {
  couponForm.value = {...row}
  isCouponEdit.value = true
  couponDialog.value = true
}
const saveCoupon = () => {
  const url = isCouponEdit.value ? '/merchant/setting/coupon/update' : '/merchant/setting/coupon/add'
  request.post(url, couponForm.value).then(() => {
    couponDialog.value = false
    loadCoupon()
  })
}
const delCoupon = (id) => {
  ElMessageBox.confirm('确定删除？').then(() => {
    request.delete('/merchant/setting/coupon/del', {params: {id}}).then(loadCoupon)
  })
}
</script>

<style scoped>
.search-section {
  background: #fcfcfc;
  padding: 20px;
  border-radius: 12px;
  border: 1px solid #f0f0f0;
  margin-bottom: 20px
}

.btn-orange {
  background: #ff9900 !important;
  border-color: #ff9900 !important;
  color: #fff
}

.btn-orange-gradient {
  background: linear-gradient(to right, #ffb347, #ff9900) !important;
  border: none !important;
  color: #fff
}

.operation-section {
  display: flex;
  align-items: center;
  margin-bottom: 15px
}

.data-info {
  margin-left: auto;
  color: #999
}

.highlight {
  color: #ff9900;
  font-weight: bold
}

.text-orange {
  color: #ff9900 !important
}

:deep(.el-table__row) {
  height: 70px
}

.avatar-uploader .avatar {
  width: 120px;
  height: 120px;
  display: block;
}
</style>

<style>
.avatar-uploader .el-upload {
  border: 1px dashed var(--el-border-color);
  border-radius: 50%;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  transition: var(--el-transition-duration-fast);
}

.avatar-uploader .el-upload:hover {
  border-color: var(--el-color-primary);
}

.el-icon.avatar-uploader-icon {
  font-size: 28px;
  color: #8c939d;
  width: 120px;
  height: 120px;
  text-align: center;
}
</style>