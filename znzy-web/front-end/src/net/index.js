import axios from "axios";
import {ElMessage} from "element-plus";//引入用到的组件

function getAuthToken() {
  return localStorage.getItem('authToken') || '';
}

const defaultError = () => ElMessage.error('发生错误，请联系管理员。') //定义默认错误提示语
const defaultFailure = (message) => ElMessage.warning(message) //后端请求返回失败信息时将其打印
//post请求示例
function post(url, data, success, failure = defaultFailure, error = defaultError) {//导入请求路径url,请求数据data,以及失败和成功的操作
  axios.post(url, data, { //使用axios的post请求 传入路径和数据
    headers: {
      "Content-Type": "application/x-www-form-urlencoded", //设置内容类型
      "Authorization": getAuthToken()
    },
    withCredentials: true
  }).then(({data}) => {
    if (data.success)
      success(data.message, data.data,data.status) //判断数据内含的请求成功或失败并做出对应前端操作，执行的操作在组件中引用时书写
    else
      failure(data.message,data.data, data.status)
  }).catch(error)
}

function get(url, data = null, success, failure = defaultFailure, error = defaultError) {
  const config = {
    withCredentials: true,
    params: data , // 将数据作为查询参数
    headers:{
      "Authorization": getAuthToken()
    }
  };

  axios.get(url, config)
    .then(({data}) => {
      if (data.success)
        success(data.message,data.data, data.status)
      else
        failure(data.message,data.data, data.status)
    })
    .catch(error)
}

function put(url, data, success, failure = defaultFailure, error = defaultError) {
  const params = new URLSearchParams();
  for (const key in data) {
    params.append(key, data[key]);
  }

  axios.put(url, params, {
    headers: {
      "Content-Type": "application/x-www-form-urlencoded",
      "Authorization": getAuthToken(),
    },
    withCredentials: true,
  })
    .then(({ data }) => {
      if (data.success) {
        success(data.message, data.data);
      } else {
        failure(data.message);
      }
    })
    .catch(error);
}

function del(url, data, success, failure = defaultFailure, error = defaultError) {
  const config = {
    withCredentials: true,
    params: data,
    headers: {
      "Authorization": getAuthToken(),
    },
  };

  axios.delete(url, config)
    .then(({ data }) => {
      if (data.success) {
        success(data.message, data.data);
      } else {
        failure(data.message);
      }
    })
    .catch(error);
}
export { get, post, put, del }; //导出get post InternalGet方法 供所有组件使用



