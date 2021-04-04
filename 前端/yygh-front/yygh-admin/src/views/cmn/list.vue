<template>
    <div class="app-container">
        <div class="el-toolbar">
            <div class="el-toolbar-body" style="justify-content: flex-start;">
                <el-button type="text" @click="exportData"><i class="fa fa-plus"/> 导出</el-button>
                <el-button type="text" @click="importData"><i class="fa fa-plus"/> 导入</el-button>
            </div>
        </div>
        
        <el-table
            :data="list"
            style="width: 100%;margin-bottom: 20px;"
            row-key="id"
            border
            lazy
            :load="load"
            default-expand-all
            :tree-props="{children: 'children', hasChildren: 'hasChildren'}">
            <el-table-column prop="name" label="名称" sortable width="180"></el-table-column>
            <el-table-column prop="dictCode" label="编码" sortable width="180"></el-table-column>
            <el-table-column prop="value" label="值"> </el-table-column>
            <el-table-column prop="createTime" label="创建时间" align="center"> </el-table-column>
        </el-table>

        <el-dialog title="导入" :visible.sync="dialogImportVisible" width="480px">
            <el-form label-position="right" label-width="170px">
                <el-form-item label="文件">
                    <el-upload
                    :multiple="false"
                    :on-success="onUploadSuccess"
                    :action="'http://localhost:8202/admin/cmn/dict/importData'"
                    class="upload-demo">
                    <el-button size="small" type="primary" >点击上传</el-button>
                        <div slot="tip" class="el-upload__tip">只能上传xls文件，且不超过500kb</div>
                    </el-upload>
                </el-form-item>
            </el-form>
            <div slot="footer" class="dialog-footer">
                <el-button @click="dialogImportVisible = false">
                    取消
                </el-button>
            </div>
        </el-dialog>
    </div>
</template>

<script>
import dict from "@/api/dict"

export default {
    data(){
        return{
            list:[], // 字典数据集合
            dialogImportVisible: false // 模态框初始值，隐藏状态
        }
    },
    created(){
        this.getDictList(1)
    },
    methods:{
        // 显示数据字典
        getDictList(id){
            dict.dictList(id)
                .then(response => {
                    this.list = response.data
                })
        },
        // 懒加载子节点
        load(tree, treeNode, resolve){
            setTimeout(() => {
                dict.dictList(tree.id)
                    .then(response => {
                        resolve(response.data)
                    })
            }, 1000)
            
        },
        // 导出数据字典
        exportData(){
            // 直接调用dict模块的导出接口
            window.location.href = 'http://localhost:8202/admin/cmn/dict/exportData'                            
        },
        // 导入操作
        importData(){
            this.dialogImportVisible = true
        },
        // 上传成功后
        onUploadSuccess(){
            // 关闭弹窗
            this.dialogImportVisible = false
            // 刷新页面
            this.getDictList(1)
        }
    }
}
</script>
