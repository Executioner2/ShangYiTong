<template>
  <div class="app-container">
    <!-- 条件查询表单 -->
    <el-form :inline="true" class="demo-form-inline">
        <el-form-item>
            <el-input v-model="searchObj.hosname" placeholder="医院名称"/>
        </el-form-item>
        <el-form-item>
            <el-input v-model="searchObj.hoscode" placeholder="医院编号"/>         
        </el-form-item>
        <el-form-item>
            <el-button type="primary" icon="el-icon-search" @click="getList()">查询</el-button>
        </el-form-item>
    </el-form>
    <!-- table表格 -->
    <div>
        <el-button type="danger" size="mini" @click="removeRows()">批量删除</el-button>
    </div>
    <el-table :data="list" stripe style="width: 100%" @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="55"/>
        <el-table-column type="index"/>        
        <el-table-column prop="hosname" label="医院名称"/>
        <el-table-column prop="hoscode" label="医院编号"/>
        <el-table-column prop="apiUrl" label="api基础路径" width="200"/>
        <el-table-column prop="contactsName" label="联系人姓名"/>
        <el-table-column prop="contactsPhone" label="联系人手机"/>
        <el-table-column label="状态" width="80">
            <template slot-scope="scope">{{scope.row.status === 1 ? '可用' : '不可用'}}</template>        
        </el-table-column>
        <el-table-column label="操作" width="200" style="align: center;">
            <template slot-scope="scope">
                <el-button type="danger" size="mini" icon="el-icon-delete" @click="removeDataById(scope.row.id)" title="删除"></el-button>
                <el-button v-if="scope.row.status == 1" type="primary" size="mini" @click="lockHospSet(scope.row.id, 0)">锁定</el-button>
                <el-button v-if="scope.row.status == 0" type="success" size="mini"  @click="lockHospSet(scope.row.id, 1)">解锁</el-button>
                <!-- 路由到隐藏的edit页 -->
                <router-link :to="'/hospSet/edit/'+scope.row.id"> 
                    <el-button type="primary" size="mini" icon="el-icon-edit"></el-button>
                </router-link>
            </template>    
        </el-table-column>        
    </el-table>
     <!-- 分页 -->
    <div class="block">    
        <el-pagination            
            @current-change="getList" 
            :current-page="current"                        
            :page-size="limit"
            style="padding: 30px 0; text-align: center;"
            layout="total, prev, pager, next, jumper"
            :total="total">
        </el-pagination>
    </div>
  </div>
</template>

<script>
import hospset from '@/api/hospset'

export default{
    // 定义变量和初始值
    data(){
        return{
            current:1, // 起始记录
            limit:3, // 每页显示记录数
            searchObj:{}, // HospitalSet对象集合
            total:0, // 总记录条数
            list:[], // 对象集合
            multipleSelection:[] // 批量选择中选择的记录列表        
        }
    },
    created(){
        this.getList()
    },
    methods: { // 定义方法
        // 条件分页查询
        getList(page = 1){
            this.current = page
            // 调用hospset.js中的getPageList方法
            hospset.getPageList(this.current, this.limit, this.searchObj)
                .then(response => { // 请求成功，response是接口返回数据                    
                    this.list = response.data.records
                    this.total = response.data.total                    
                })
        },
        // 根据id删除记录
        removeDataById(id){
            this.$confirm('此操作将永久删除该文件, 是否继续?', '提示', {
                confirmButtonText: '确定',
                cancelButtonText: '取消',
                type: 'warning'
            }).then(() => {
                hospset.removeHospSetById(id)
                    .then(response => {
                        this.$message({
                            type: 'success',
                            message: '删除成功!'
                        });
                        this.getList(1)
                    })
                
            })           
        },
        // 当表格复选框选项发生变化的时候触发
        handleSelectionChange(selection) {
            this.multipleSelection = selection            
        },
        // 批量删除记录
        removeRows(){
            this.$confirm('此操作将永久删除该文件, 是否继续?', '提示', {
                confirmButtonText: '确定',
                cancelButtonText: '取消',
                type: 'warning'
            }).then(() => {
                var idList = [];        
                for(let i = 0; i < this.multipleSelection.length; i++){
                    idList.push(this.multipleSelection[i].id) 
                }                
                hospset.batchRemoveHospSet(idList)
                    .then(response => {
                        this.$message({
                            type: 'success',
                            message: '删除成功!'
                        });
                        this.getList(1)
                    })
                
            })
        },
        // 锁定和解锁状态
        lockHospSet(id, status){
            hospset.lockHospSet(id, status)
                .then(response => {
                    if(status == 0){
                        this.$message({
                            message: '锁定成功',
                            type: 'success'
                        });
                    }else if(status == 1){
                        this.$message({
                            message: '解锁成功',
                            type: 'success'
                        });
                    }
                    this.getList(this.current)                    
                })
        }       
        
    }
}
</script>
