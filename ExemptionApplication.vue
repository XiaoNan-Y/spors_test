handleDelete(id) {
  console.log(`准备删除申请ID: ${id}`);
  
  const userId = localStorage.getItem('userId');
  console.log(`当前用户ID: ${userId}`);
  
  if (!userId) {
    this.$message.error('用户未登录或ID缺失');
    return;
  }
  
  this.$confirm('确认撤销该申请?', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    // 使用简单的删除请求
    this.$axios.delete(`/api/exemptions/student/${id}?studentId=${userId}`)
    .then(response => {
      console.log('删除请求响应:', response.data);
      
      // 无论响应状态如何，都刷新列表
      if (response.data.code === 200) {
        this.$message.success('撤销成功');
      } else if (response.data.message && 
                (response.data.message.includes('不存在') || 
                 response.data.message.includes('已被删除'))) {
        this.$message({
          type: 'warning',
          message: '该申请可能已被删除，正在刷新列表...'
        });
      } else {
        this.$message.error(response.data.message || '撤销失败');
      }
      
      // 无论如何都刷新列表
      this.getExemptions();
    })
    .catch(error => {
      console.error('撤销申请失败:', error);
      
      // 如果是404错误，也可能是记录不存在
      if (error.response && error.response.status === 404) {
        this.$message({
          type: 'warning',
          message: '该申请可能已被删除，正在刷新列表...'
        });
      } else {
        this.$message.error('撤销申请失败：' + (error.response?.data?.message || error.message));
      }
      
      // 无论如何都刷新列表
      this.getExemptions();
    });
  }).catch(() => {
    this.$message.info('已取消撤销');
  });
},

// 修改获取申请列表的方法
getExemptions() {
  const userId = localStorage.getItem('userId');
  if (!userId) {
    this.$message.error('用户未登录或ID缺失');
    return;
  }

  this.$axios.get('/api/exemptions/student/list', {
    params: {
      studentId: userId,
      page: this.currentPage - 1,
      size: this.pageSize
    }
  })
  .then(response => {
    if (response.data.code === 200) {
      this.tableData = response.data.data.content;
      this.total = response.data.data.totalElements;
    } else {
      this.$message.error(response.data.message || '获取申请列表失败');
    }
  })
  .catch(error => {
    console.error('获取申请列表失败:', error);
    this.$message.error('获取申请列表失败：' + (error.response?.data?.message || error.message));
  });
} 