[English](./README.md) | 简体中文

# Chat
## 产品简介
您只需集成 Chat SDK 即可轻松获得聊天、会话、群组、资料管理和直播弹幕能力，也可以通过信令消息与白板等其他产品打通。Chat 可以覆盖您的各种业务场景，支持各平台接入使用，全面满足通信需要。

<table style="text-align:center; vertical-align:middle; width:200px">
  <tr>
    <th style="text-align:center;" width="200px">Android 体验 App</th>
  </tr>
  <tr>
    <td><img style="width:200px" src="https://qcloudimg.tencent-cloud.cn/raw/078fbb462abd2253e4732487cad8a66d.png"/></td>
</table>

我们提供了一套基于 Chat SDK 的 TUIKit 组件库，组件库包含了会话、聊天、搜索、关系链、群组、音视频通话等功能。基于 UI 组件您可以像搭积木一样快速搭建起自己的业务逻辑。

<img src="https://qcloudimg.tencent-cloud.cn/raw/9c893f1a9c6368c82d44586907d5293d.png" width="800"/>

更多信息可查阅 [Chat 界面库介绍](https://trtc.io/zh/document/50062?platform=android&product=chat&menulabel=uikit)。

## 跑通 Demo
本文介绍如何快速跑通 Android 平台体验 Demo。 对于其他平台，请参考文档：

- [**chat-uikit-ios**](https://github.com/TencentCloud/chat-uikit-ios)

- [**chat-uikit-flutter**](https://github.com/TencentCloud/chat-uikit-flutter)

- [**chat-uikit-vue**](https://github.com/TencentCloud/chat-uikit-vue)

- [**chat-uikit-react**](https://github.com/TencentCloud/chat-uikit-react)

- [**chat-uikit-uniapp**](https://github.com/TencentCloud/chat-uikit-uniapp)

- [**chat-uikit-wechat**](https://github.com/TencentCloud/chat-uikit-wechat)

请注意：为尊重表情设计版权，Chat Demo/TUIKit 工程中不包含大表情元素切图，正式上线商用前请您替换为自己设计或拥有版权的其他表情包。下图所示默认的小黄脸表情包版权归腾讯云所有，可有偿授权使用，如需获得授权可 [联系我们](https://trtc.io/contact) 。

<img src="https://qcloudimg.tencent-cloud.cn/image/document/6438e8feb7bba909511e0d798dfaf91d.png" width="300px" />

### 步骤1：创建应用
1. 登录即时通信 Chat [控制台](https://console.trtc.io/)。如果您已有应用，请记录其 SDKAppID 并转到 **步骤2**。
2. 在【应用列表】页，单击【创建应用接入】。
3. 在【创建新应用】对话框中，填写新建应用的信息，单击【确认】。
 应用创建完成后，自动生成一个应用标识 SDKAppID，请记录 SDKAppID 信息。

### 步骤2：获取密钥信息

1. 单击目标应用所在行的【应用配置】，进入应用详情页面。
2. 单击【查看密钥】，拷贝并保存密钥信息。
 >请妥善保管密钥信息，谨防泄露。

### 步骤3：下载并配置 Demo 源码

1. 克隆本 Chat 工程。
2. 打开项目，找到对应的 `GenerateTestUserSig.java` 文件。
 <table>
     <tr>
         <th nowrap="nowrap">所属平台</th>  
         <th nowrap="nowrap">文件相对路径</th>  
     </tr>
  <tr>      
      <td>Android</td>   
      <td>Android/Demo/app/src/main/java/com/tencent/qcloud/tim/demo/signature/GenerateTestUserSig.java</td>   
     </tr> 
  <tr>
      <td>iOS</td>   
      <td>iOS/Demo/TUIKitDemo/Private/GenerateTestUserSig.h</td>
     </tr> 
</table>


 >本文以使用 Android Studio 打开 Android 工程为例。
  >
3. 设置 `GenerateTestUserSig` 文件中的相关参数：
 - SDKAPPID：请设置为 **步骤1** 中获取的实际应用 SDKAppID。
 - SECRETKEY：请设置为 **步骤2** 中获取的实际密钥信息。

<img src="https://cloudcache.intl.tencent-cloud.com/cms/backend-cms/e0d841971cd711ef9b9e5254002977b6.png" width="800"/>

>本文提到的获取 UserSig 的方案是在客户端代码中配置 SECRETKEY，该方法中 SECRETKEY 很容易被反编译逆向破解，一旦您的密钥泄露，攻击者就可以盗用您的腾讯云流量，因此**该方法仅适合本地跑通 Demo 和功能调试**。
>正确的 UserSig 签发方式是将 UserSig 的计算代码集成到您的服务端，并提供面向 App 的接口，在需要 UserSig 时由您的 App 向业务服务器发起请求获取动态 UserSig。更多详情请参见 [服务端生成 UserSig](https://trtc.io/zh/document/34385?product=chat&menulabel=serverapis)。

### 步骤4：编译运行（全部功能）
用 Android Studio 导入工程直接编译运行即可。

> **Demo 默认集成了音视频通话功能，由于该功能依赖的音视频 SDK 暂不支持模拟器，请使用真机调试或者运行 Demo。**

### 步骤5：编译运行（移除音视频通话）
如果您不需要音视频通话功能，只需要在 `app 模块` 的 `build.gradle` 文件中删除音视频通话模块集成代码即可：

![](https://im.sdk.qcloud.com/tools/resource/tuicalling/android/GitHubDeleteTUICallKit.jpg)

```groovy
api project(':tuicallkit')
```
操作完上述步骤后会发现，Demo 中的音频通话、视频通话入口均被隐藏。
会话界面屏蔽 TUICallKit 前后的效果：

| 修改前 | 修改后|
|--------|------|
| <img width="240" alt="GitHub_ChatIncludeCallMinimalist" src="https://user-images.githubusercontent.com/85340225/205878435-75c56857-a8c5-4262-b0cf-71a7d773b50c.png">  | <img width="240" alt="GitHub_ChatExcludeCallMinimalist" src="https://user-images.githubusercontent.com/85340225/205878234-ce5c4dd3-e6aa-4352-9c13-dababfd15c48.png"> |

联系人资料界面屏蔽 TUICallKit 前后的效果：

| 修改前 | 修改后|
|--------|------|
| <img width="240" alt="GitHub_ContactIncludeCallMinimalist" src="https://user-images.githubusercontent.com/85340225/205878892-218cb7a3-977a-4277-bda3-903360600742.png"> | <img width="240" alt="GitHub_ContactExcludeCallMinimalist" src="https://user-images.githubusercontent.com/85340225/205878978-106e0230-5111-485a-8c65-a9662e2dda9d.png"> |

> 以上演示的仅仅是 Demo 对移除音视频通话功能的处理，开发者可以按照业务要求自定义。

### 步骤6：编译运行（移除搜索模块）
如果您不需要搜索功能，那么只需要在 `app 模块` 的 `build.gradle` 文件中删除下面一行即可：

![](https://im.sdk.qcloud.com/tools/resource/tuicalling/android/GitHubDeleteTUISearch.jpg)

```groovy
api project(':tuisearch')
```
操作完上述步骤后会发现，Demo 中的消息搜索框被隐藏。

消息界面屏蔽 TUISearch 前后的效果：

| 修改前 | 修改后 |
|---------|---------|
| <img width="240" alt="GitHub_ConversationIncludeSearchMinimalist" src="https://user-images.githubusercontent.com/85340225/205879099-1577d68a-a6c2-4413-8ebe-6742f5e4aa7c.png"> | <img width="240" alt="GitHub_ConversationExcludeSearchMinimalist" src="https://user-images.githubusercontent.com/85340225/205879135-0d5753b9-029f-4c68-9dfa-9c587277106c.png"> |

> 以上演示的仅仅是 Demo 对移除搜索功能的处理，开发者可以按照业务要求自定义。
