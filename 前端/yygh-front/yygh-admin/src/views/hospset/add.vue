<template>
  <div class="app-container">
      <h4>医院设置添加</h4>
      <el-form label-width="120px">
         <el-form-item label="医院名称">
            <el-input v-model="hospitalSet.hosname"/>
         </el-form-item>
         <el-form-item label="医院编号">
            <el-input v-model="hospitalSet.hoscode"/>
         </el-form-item>
         <el-form-item label="api基础路径">
            <el-input v-model="hospitalSet.apiUrl"/>
         </el-form-item>
         <el-form-item label="联系人姓名">
            <el-input v-model="hospitalSet.contactsName"/>
         </el-form-item>
         <el-form-item label="联系人手机">
            <el-input v-model="hospitalSet.contactsPhone"/>
         </el-form-item>
         <el-form-item>
            <el-button type="primary" @click="saveOrUpdate()">保存</el-button>
         </el-form-item>
      </el-form>
   </div>
</template>

<script>
import hospset from "@/api/hospset"

export default {
  data() {
    return{
      hospitalSet:{}
    }    
  },
  created() {
      if(this.$route.params && this.$route.params.id){
        const id = this.$route.params.id        
        this.getHospSet(id)
      }else{
        this.hospitalSet = {}
      }
  },
  methods:{
    // 医院查询
    getHospSet(id){
      hospset.getHospSetById(id)      
        .then(response => {          
          this.hospitalSet = response.data
        })
    },
    // 医院设置保存
    saveOrUpdate(){
      if(this.hospitalSet.id){ // 如果有id，就更新
        this.update(this.hospitalSet)
      }else{ // 否则就保存添加
        this.save(this.hospitalSet)
      }
    },
    // 医院保存
    save(){
      hospset.addHospSet(this.hospitalSet)
        .then(response => {
          //提示
          this.$message({
            type: 'success',
            message: '添加成功!'
          })
          //跳转列表页面，使用路由跳转方式实现
          this.$router.push({path:'/hospSet/list'})
        })      
    },        
    // 医院修改
    update(){
      hospset.updateHospSet(this.hospitalSet)
        .then(response => {
          //提示
          this.$message({
            type: 'success',
            message: '更新成功!'
          })
          //跳转列表页面，使用路由跳转方式实现
          this.$router.push({path:'/hospSet/list'})
        })
    }
  }
}
</script>
    