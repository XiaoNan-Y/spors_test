(window["webpackJsonp"]=window["webpackJsonp"]||[]).push([["chunk-4520ee9f"],{1671:function(s,e,r){"use strict";r.r(e);var a=function(){var s=this,e=s._self._c;return e("div",{staticClass:"profile"},[e("el-row",{attrs:{gutter:20}},[e("el-col",{attrs:{span:12}},[e("el-card",{staticClass:"profile-card"},[e("div",{staticClass:"card-header",attrs:{slot:"header"},slot:"header"},[e("span",[e("i",{staticClass:"el-icon-user"}),s._v(" 个人信息")])]),e("el-form",{ref:"userForm",attrs:{model:s.userForm,rules:s.rules,"label-width":"100px"}},[e("el-form-item",{attrs:{label:"学号",prop:"studentNumber"}},[e("el-input",{attrs:{disabled:""},model:{value:s.userForm.studentNumber,callback:function(e){s.$set(s.userForm,"studentNumber",e)},expression:"userForm.studentNumber"}})],1),e("el-form-item",{attrs:{label:"姓名",prop:"realName"}},[e("el-input",{attrs:{disabled:""},model:{value:s.userForm.realName,callback:function(e){s.$set(s.userForm,"realName",e)},expression:"userForm.realName"}})],1),e("el-form-item",{attrs:{label:"班级",prop:"className"}},[e("el-input",{attrs:{disabled:""},model:{value:s.userForm.className,callback:function(e){s.$set(s.userForm,"className",e)},expression:"userForm.className"}})],1),e("el-form-item",{attrs:{label:"邮箱",prop:"email"}},[e("el-input",{attrs:{placeholder:"请输入邮箱"},model:{value:s.userForm.email,callback:function(e){s.$set(s.userForm,"email",e)},expression:"userForm.email"}})],1),e("el-form-item",{attrs:{label:"手机号",prop:"phone"}},[e("el-input",{attrs:{placeholder:"请输入手机号"},model:{value:s.userForm.phone,callback:function(e){s.$set(s.userForm,"phone",e)},expression:"userForm.phone"}})],1),e("el-form-item",[e("el-button",{attrs:{type:"primary"},on:{click:s.updateProfile}},[s._v("保存修改")])],1)],1)],1)],1),e("el-col",{attrs:{span:12}},[e("el-card",{staticClass:"password-card"},[e("div",{staticClass:"card-header",attrs:{slot:"header"},slot:"header"},[e("span",[e("i",{staticClass:"el-icon-lock"}),s._v(" 修改密码")])]),e("el-form",{ref:"passwordForm",attrs:{model:s.passwordForm,rules:s.passwordRules,"label-width":"100px"}},[e("el-form-item",{attrs:{label:"原密码",prop:"oldPassword"}},[e("el-input",{attrs:{type:"password","show-password":"",placeholder:"请输入原密码"},model:{value:s.passwordForm.oldPassword,callback:function(e){s.$set(s.passwordForm,"oldPassword",e)},expression:"passwordForm.oldPassword"}})],1),e("el-form-item",{attrs:{label:"新密码",prop:"newPassword"}},[e("el-input",{attrs:{type:"password","show-password":"",placeholder:"请输入新密码"},model:{value:s.passwordForm.newPassword,callback:function(e){s.$set(s.passwordForm,"newPassword",e)},expression:"passwordForm.newPassword"}})],1),e("el-form-item",{attrs:{label:"确认密码",prop:"confirmPassword"}},[e("el-input",{attrs:{type:"password","show-password":"",placeholder:"请确认新密码"},model:{value:s.passwordForm.confirmPassword,callback:function(e){s.$set(s.passwordForm,"confirmPassword",e)},expression:"passwordForm.confirmPassword"}})],1),e("el-form-item",[e("el-button",{attrs:{type:"primary"},on:{click:s.updatePassword}},[s._v("修改密码")])],1)],1)],1)],1)],1)],1)},o=[],t=(r("d9e2"),r("14d9"),{name:"StudentProfile",data(){const s=(s,e,r)=>{e!==this.passwordForm.newPassword?r(new Error("两次输入的密码不一致")):r()};return{userForm:{studentNumber:"",realName:"",className:"",email:"",phone:""},passwordForm:{oldPassword:"",newPassword:"",confirmPassword:""},rules:{email:[{type:"email",message:"请输入正确的邮箱地址",trigger:"blur"}],phone:[{pattern:/^1[3-9]\d{9}$/,message:"请输入正确的手机号码",trigger:"blur"}]},passwordRules:{oldPassword:[{required:!0,message:"请输入原密码",trigger:"blur"},{min:6,message:"密码长度不能小于6位",trigger:"blur"}],newPassword:[{required:!0,message:"请输入新密码",trigger:"blur"},{min:6,message:"密码长度不能小于6位",trigger:"blur"}],confirmPassword:[{required:!0,message:"请确认新密码",trigger:"blur"},{validator:s,trigger:"blur"}]}}},created(){this.fetchUserInfo()},methods:{async fetchUserInfo(){try{const s=await this.$http.get("/api/student/info");if(200===s.data.code){const{real_name:e,student_number:r,class_name:a,email:o,phone:t}=s.data.data;this.userForm.realName=e,this.userForm.studentNumber=r,this.userForm.className=a,this.userForm.email=o||"",this.userForm.phone=t||""}}catch(s){this.$message.error("获取用户信息失败"),console.error(s)}},async updateProfile(){try{await this.$refs.userForm.validate();const s=await this.$http.put("/api/student/profile",{email:this.userForm.email,phone:this.userForm.phone});200===s.data.code&&this.$message.success("个人信息更新成功")}catch(s){this.$message.error(s.message||"更新失败")}},async updatePassword(){try{await this.$refs.passwordForm.validate();const s=this.$loading({lock:!0,text:"正在修改密码...",spinner:"el-icon-loading"});try{const e=await this.$http.put("/api/student/password",{oldPassword:this.passwordForm.oldPassword,newPassword:this.passwordForm.newPassword});200===e.data.code?(this.$message.success("密码修改成功，请重新登录"),this.$refs.passwordForm.resetFields(),setTimeout(()=>{localStorage.clear(),this.$router.push("/login")},1500)):this.$message.error(e.data.message||"修改密码失败")}finally{s.close()}}catch(e){var s;console.error("修改密码失败:",e),this.$message.error((null===(s=e.response)||void 0===s||null===(s=s.data)||void 0===s?void 0:s.message)||"修改密码失败，请重试")}}}}),l=t,i=(r("3674"),r("2877")),d=Object(i["a"])(l,a,o,!1,null,"61bbd8dc",null);e["default"]=d.exports},"1ecd":function(s,e,r){},3674:function(s,e,r){"use strict";r("1ecd")}}]);
//# sourceMappingURL=chunk-4520ee9f.373d339a.js.map