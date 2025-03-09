<template>
  <div class="data-review">
    <!-- 搜索和筛选区域 -->
    <div class="filter-section">
      <el-form :inline="true" :model="filterForm" class="filter-form">
        <el-form-item label="测试项目">
          <el-select v-model="filterForm.sportsItemId" placeholder="选择测试项目">
            <el-option
              v-for="item in sportsItems"
              :key="item.id"
              :label="item.name"
              :value="item.id"
            ></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="录入教师">
          <el-select v-model="filterForm.teacherId" placeholder="选择教师">
            <el-option
              v-for="teacher in teachers"
              :key="teacher.id"
              :label="teacher.realName"
              :value="teacher.id"
            ></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="审核状态">
          <el-select v-model="filterForm.status" placeholder="选择状态">
            <el-option label="待审核" value="PENDING"></el-option>
            <el-option label="已通过" value="APPROVED"></el-option>
            <el-option label="已驳回" value="REJECTED"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="测试时间">
          <el-date-picker
            v-model="filterForm.dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            format="yyyy-MM-dd"
            value-format="yyyy-MM-dd"
          ></el-date-picker>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="resetFilter">重置</el-button>
        </el-form-item>
      </el-form>
      <div class="operation-buttons">
        <el-button type="primary" @click="handleAdd">
          <i class="el-icon-plus"></i> 录入成绩
        </el-button>
      </div>
    </div>

    <!-- 数据列表 -->
    <el-table
      :data="recordList"
      border
      stripe
      v-loading="loading"
      style="width: 100%"
    >
      <el-table-column prop="id" label="ID" width="80"></el-table-column>
      <el-table-column label="学生姓名">
        <template slot-scope="scope">
          {{ scope.row?.student?.realName || '未知' }}
        </template>
      </el-table-column>
      <el-table-column label="测试项目">
        <template slot-scope="scope">
          {{ scope.row?.sportsItem?.name || '未知' }}
        </template>
      </el-table-column>
      <el-table-column label="测试成绩">
        <template slot-scope="scope">
          <span :class="{ 'abnormal-score': scope.row.isAbnormal }">
            {{ scope.row?.score }}{{ scope.row?.sportsItem?.unit || '' }}
          </span>
          <el-tooltip v-if="scope.row.isAbnormal" effect="dark" placement="top">
            <div slot="content">{{ scope.row.abnormalReason }}</div>
            <i class="el-icon-warning-outline warning-icon"></i>
          </el-tooltip>
        </template>
      </el-table-column>
      <el-table-column label="录入教师">
        <template slot-scope="scope">
          {{ scope.row?.teacher?.realName || '未知' }}
        </template>
      </el-table-column>
      <el-table-column prop="testTime" label="测试时间" width="180">
        <template slot-scope="scope">
          {{ formatDate(scope.row.testTime) }}
        </template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="100">
        <template slot-scope="scope">
          <el-tag :type="getStatusType(scope.row.status)">
            {{ getStatusLabel(scope.row.status) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="250">
        <template slot-scope="scope">
          <el-button
            size="mini"
            type="primary"
            @click="handleReview(scope.row)"
            v-if="scope.row.status === 'PENDING'"
          >审核</el-button>
          <el-button
            size="mini"
            type="warning"
            @click="handleEdit(scope.row)"
            v-if="scope.row.status === 'PENDING'"
          >修改</el-button>
          <el-button
            size="mini"
            type="info"
            @click="handleDetail(scope.row)"
          >详情</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页 -->
    <div class="pagination-container">
      <el-pagination
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
        :current-page="page.current"
        :page-sizes="[10, 20, 50, 100]"
        :page-size="page.size"
        layout="total, sizes, prev, pager, next, jumper"
        :total="page.total"
      ></el-pagination>
    </div>

    <!-- 审核对话框 -->
    <el-dialog title="成绩审核" :visible.sync="reviewDialogVisible" width="500px">
      <div class="review-info">
        <p><strong>学生：</strong>{{ currentRecord?.student?.realName || '未知' }}</p>
        <p><strong>测试项目：</strong>{{ currentRecord?.sportsItem?.name || '未知' }}</p>
        <p>
          <strong>测试成绩：</strong>
          <span :class="{ 'abnormal-score': currentRecord?.isAbnormal }">
            {{ currentRecord?.score }}{{ currentRecord?.sportsItem?.unit || '' }}
          </span>
          <el-tooltip v-if="currentRecord?.isAbnormal" effect="dark" placement="top">
            <div slot="content">{{ currentRecord?.abnormalReason }}</div>
            <i class="el-icon-warning-outline warning-icon"></i>
          </el-tooltip>
        </p>
        <p><strong>测试时间：</strong>{{ formatDate(currentRecord?.testTime) }}</p>
      </div>

      <el-form :model="reviewForm" :rules="reviewRules" ref="reviewForm">
        <el-form-item label="审核结果" prop="status">
          <el-radio-group v-model="reviewForm.status">
            <el-radio label="APPROVED">通过</el-radio>
            <el-radio label="REJECTED">驳回</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item 
          label="审核意见" 
          prop="reviewComment"
          :required="reviewForm.status === 'REJECTED'"
        >
          <el-input
            type="textarea"
            v-model="reviewForm.reviewComment"
            :rows="3"
            placeholder="请输入审核意见"
          ></el-input>
        </el-form-item>
      </el-form>

      <div slot="footer">
        <el-button @click="reviewDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitReview">确定</el-button>
      </div>
    </el-dialog>

    <!-- 详情对话框 -->
    <el-dialog title="详细信息" :visible.sync="detailDialogVisible" width="500px">
      <div class="detail-info">
        <p><strong>学生：</strong>{{ currentRecord?.student?.realName || '未知' }}</p>
        <p><strong>测试项目：</strong>{{ currentRecord?.sportsItem?.name || '未知' }}</p>
        <p>
          <strong>测试成绩：</strong>
          <span :class="{ 'abnormal-score': currentRecord?.isAbnormal }">
            {{ currentRecord?.score }}{{ currentRecord?.sportsItem?.unit || '' }}
          </span>
          <el-tooltip v-if="currentRecord?.isAbnormal" effect="dark" placement="top">
            <div slot="content">{{ currentRecord?.abnormalReason }}</div>
            <i class="el-icon-warning-outline warning-icon"></i>
          </el-tooltip>
        </p>
        <p><strong>录入教师：</strong>{{ currentRecord?.teacher?.realName || '未知' }}</p>
        <p><strong>测试时间：</strong>{{ formatDate(currentRecord?.testTime) }}</p>
        <p><strong>审核状态：</strong>{{ getStatusLabel(currentRecord?.status) }}</p>
        <p><strong>审核意见：</strong>{{ currentRecord?.reviewComment || '无' }}</p>
        <p><strong>审核人：</strong>{{ currentRecord?.reviewer?.realName || '未审核' }}</p>
        <p><strong>审核时间：</strong>{{ formatDate(currentRecord?.reviewTime) || '未审核' }}</p>
      </div>
    </el-dialog>

    <!-- 添加/修改对话框 -->
    <el-dialog 
      :title="editMode ? '修改成绩' : '录入成绩'" 
      :visible.sync="formDialogVisible" 
      width="500px"
    >
      <el-form :model="form" :rules="formRules" ref="form" label-width="100px">
        <el-form-item label="学生" prop="studentId">
          <el-select 
            v-model="form.studentId" 
            placeholder="选择学生" 
            filterable
            :disabled="editMode"
          >
            <el-option
              v-for="student in students"
              :key="student.id"
              :label="student.realName"
              :value="student.id"
            ></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="测试项目" prop="sportsItemId">
          <el-select 
            v-model="form.sportsItemId" 
            placeholder="选择测试项目"
            :disabled="editMode"
          >
            <el-option
              v-for="item in sportsItems"
              :key="item.id"
              :label="item.name"
              :value="item.id"
            ></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="测试成绩" prop="score">
          <el-input-number 
            v-model="form.score" 
            :precision="2"
            :step="0.1"
            :min="0"
          ></el-input-number>
          <span style="margin-left: 10px">
            {{ getSportsItemUnit(form.sportsItemId) }}
          </span>
        </el-form-item>
        <el-form-item label="测试时间" prop="testTime">
          <el-date-picker
            v-model="form.testTime"
            type="datetime"
            placeholder="选择测试时间"
            format="yyyy-MM-dd HH:mm:ss"
            value-format="yyyy-MM-dd HH:mm:ss"
          ></el-date-picker>
        </el-form-item>
      </el-form>
      <div slot="footer">
        <el-button @click="formDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitForm">确定</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
export default {
  name: 'DataReview',
  data() {
    return {
      // ... 这里是之前的data配置
    }
  },
  methods: {
    // ... 这里是之前的methods配置
  }
}
</script>

<style scoped>
.data-review {
  padding: 20px;
}

.filter-section {
  margin-bottom: 20px;
  background-color: #f5f7fa;
  padding: 20px;
  border-radius: 4px;
}

.abnormal-score {
  color: #f56c6c;
  font-weight: bold;
}

.warning-icon {
  color: #e6a23c;
  margin-left: 5px;
}

.review-info, .detail-info {
  background-color: #f5f7fa;
  padding: 15px;
  border-radius: 4px;
  margin-bottom: 20px;
}

.pagination-container {
  margin-top: 20px;
  text-align: right;
}
</style>