# react-native-mscreen-capture

此项目基于[https://github.com/LewinJun/react-native-lewin-screen-capture](https://github.com/LewinJun/react-native-lewin-screen-capture)

在此基础上修复Android 10无法监听截屏的问题

react-native 获取系统截屏通知并获取图片/截取当前屏幕


## Install

### 1: yarn add 或者npm install,现在最新版本是1.0.0

`yarn add react-native-mscreen-capture`

### 2: yarn install 或 npm install


## Usage


```javascript
import ScreenCaptureUtil from 'react-native-mscreen-capture'

// 开始监听
ScreenCaptureUtil.startListener(res => {
    console.log(res)
    // this.setState({uri:'data:image/png;base64,' + res.base64})
    this.setState({uri: res.uri})
}, '截屏,screen')

// 停止监听
ScreenCaptureUtil.stopListener()

// 截取当前屏幕
ScreenCaptureUtil.screenCapture((res)=>{
    console.log(res)
    this.setState({uri: res.uri})
})
// 清理缓存
ScreenCaptureUtil.clearCache()
```

