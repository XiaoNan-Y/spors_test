(window["webpackJsonp"]=window["webpackJsonp"]||[]).push([["chunk-13199d7f"],{"209d":function(s,e,r){},"420e":function(s,e,r){"use strict";r("209d")},ecac:function(s,e,r){"use strict";r.r(e);var o=function(){var s=this,e=s._self._c;return e("div",{staticClass:"app-container"},[e("el-card",{staticClass:"box-card"},[e("div",{staticClass:"clearfix",attrs:{slot:"header"},slot:"header"},[e("span",[s._v("个人信息")])]),e("el-form",{ref:"form",attrs:{model:s.userInfo,"label-width":"120px"}},[e("el-form-item",{attrs:{label:"用户名"}},[e("el-input",{attrs:{disabled:""},model:{value:s.userInfo.username,callback:function(e){s.$set(s.userInfo,"username",e)},expression:"userInfo.username"}})],1),e("el-form-item",{attrs:{label:"真实姓名"}},[e("el-input",{model:{value:s.userInfo.realName,callback:function(e){s.$set(s.userInfo,"realName",e)},expression:"userInfo.realName"}})],1),e("el-form-item",{attrs:{label:"邮箱"}},[e("el-input",{model:{value:s.userInfo.email,callback:function(e){s.$set(s.userInfo,"email",e)},expression:"userInfo.email"}})],1),e("el-form-item",{attrs:{label:"手机号"}},[e("el-input",{model:{value:s.userInfo.phone,callback:function(e){s.$set(s.userInfo,"phone",e)},expression:"userInfo.phone"}})],1),e("el-form-item",[e("el-button",{attrs:{type:"primary"},on:{click:s.updateProfile}},[s._v("保存修改")])],1)],1),e("el-divider"),e("div",{staticClass:"password-section"},[e("h3",[s._v("修改密码")]),e("el-form",{ref:"passwordForm",attrs:{model:s.passwordForm,"label-width":"120px"}},[e("el-form-item",{attrs:{label:"原密码",prop:"oldPassword"}},[e("el-input",{attrs:{type:"password"},model:{value:s.passwordForm.oldPassword,callback:function(e){s.$set(s.passwordForm,"oldPassword",e)},expression:"passwordForm.oldPassword"}})],1),e("el-form-item",{attrs:{label:"新密码",prop:"newPassword"}},[e("el-input",{attrs:{type:"password"},model:{value:s.passwordForm.newPassword,callback:function(e){s.$set(s.passwordForm,"newPassword",e)},expression:"passwordForm.newPassword"}})],1),e("el-form-item",{attrs:{label:"确认新密码",prop:"confirmPassword"}},[e("el-input",{attrs:{type:"password"},model:{value:s.passwordForm.confirmPassword,callback:function(e){s.$set(s.passwordForm,"confirmPassword",e)},expression:"passwordForm.confirmPassword"}})],1),e("el-form-item",[e("el-button",{attrs:{type:"primary"},on:{click:s.changePassword}},[s._v("修改密码")])],1)],1)],1)],1)],1)},a=[],t=r("b775");function n(){return Object(t["a"])({url:"/api/users/profile",method:"get"})}function l(s){return Object(t["a"])({url:"/api/users/profile",method:"put",data:s})}function d(s){return Object(t["a"])({url:"/api/users/change-password",method:"put",data:{oldPassword:s.oldPassword,newPassword:s.newPassword}})}var i={name:"Profile",data(){return{userInfo:{username:"",realName:"",email:"",phone:""},passwordForm:{oldPassword:"",newPassword:"",confirmPassword:""}}},created(){this.fetchUserInfo()},methods:{async fetchUserInfo(){try{const{data:s}=await n();this.userInfo=s}catch(s){console.error("获取用户信息失败:",s)}},async updateProfile(){try{await l(this.userInfo),this.$message.success("个人信息更新成功")}catch(s){this.$message.error("更新失败："+s.message)}},async changePassword(){if(this.passwordForm.newPassword===this.passwordForm.confirmPassword)try{await d({oldPassword:this.passwordForm.oldPassword,newPassword:this.passwordForm.newPassword}),this.$message.success("密码修改成功"),this.passwordForm={oldPassword:"",newPassword:"",confirmPassword:""}}catch(s){this.$message.error("密码修改失败："+s.message)}else this.$message.error("两次输入的新密码不一致")}}},c=i,m=(r("420e"),r("2877")),u=Object(m["a"])(c,o,a,!1,null,"1b2cd82a",null);e["default"]=u.exports}}]);
//# sourceMappingURL=chunk-13199d7f.9296db52.js.map